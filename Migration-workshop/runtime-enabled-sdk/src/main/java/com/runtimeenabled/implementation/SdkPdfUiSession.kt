package com.runtimeenabled.implementation

import android.content.Context
import android.content.res.Configuration
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.Gravity
import android.view.View
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView
import androidx.core.graphics.toColorInt
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LifecycleRegistry
import androidx.lifecycle.lifecycleScope
import androidx.privacysandbox.ui.provider.AbstractSandboxedUiAdapter.AbstractSession
import com.rajat.pdfviewer.PdfRendererView
import com.rajat.pdfviewer.util.CacheStrategy
import com.runtimeenabled.api.RemotePdfCallbackInterface
import com.runtimeenabled.api.RemotePdfRequest
import java.util.concurrent.Executor
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.asCoroutineDispatcher
import kotlinx.coroutines.delay
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch

class SdkPdfUiSession(
    private val sdkContext: Context,
    private val request: RemotePdfRequest,
    private val remoteCallback: RemotePdfCallbackInterface,
    private val clientExecutor: Executor
) : AbstractSession(), LifecycleOwner {

    companion object {
        const val TAG = "PdfSessionLifecycle"
    }

    private val instanceId = this.hashCode() // Unique ID for this session instance

    private val lifecycleRegistry = LifecycleRegistry(this)
    override val lifecycle: Lifecycle
        get() = lifecycleRegistry

    private var pdfViewer: PdfRendererView? = null
    private var isFirstPageRendered = false

    init {
        // The session starts in the CREATED state.
        lifecycleRegistry.currentState = Lifecycle.State.CREATED
        Log.i(TAG, "[$instanceId] ++ SESSION CREATED ++. Lifecycle is CREATED.")
    }

    /**
     * The root view is the PdfRendererView itself. Since it's a FrameLayout, we can add a
     * ProgressBar directly to it as a child.
     */
    override val view: View by lazy {
        Log.d(
            TAG,
            "[$instanceId] LAZY VIEW INIT: Creating overlay layout with determinate progress."
        )
        val container = FrameLayout(sdkContext)

        // 1. Create the PdfRendererView and add it as the bottom layer.
        pdfViewer =
            PdfRendererView(sdkContext).apply {
                layoutParams =
                    FrameLayout.LayoutParams(
                        FrameLayout.LayoutParams.MATCH_PARENT,
                        FrameLayout.LayoutParams.MATCH_PARENT
                    )
            }
        container.addView(pdfViewer)

        // 2. Create the loading UI components.
        val progressText =
            TextView(sdkContext).apply {
                text = "Loading PDF..."
                setTextColor(Color.WHITE)
            }

        val progressBar =
            ProgressBar(sdkContext, null, android.R.attr.progressBarStyleHorizontal).apply {
                isIndeterminate = false
                max = 100
            }

        val percentageText =
            TextView(sdkContext).apply {
                text = "0%"
                setTextColor(Color.WHITE)
                gravity = Gravity.CENTER
            }

        // 3. Create a container for the loading UI to keep it together.
        val loadingUiContainer =
            LinearLayout(sdkContext).apply {
                orientation = LinearLayout.VERTICAL
                gravity = Gravity.CENTER
                setBackgroundColor("#80000000".toColorInt()) // Semi-transparent black
                setPadding(80, 80, 80, 80)
                layoutParams =
                    FrameLayout.LayoutParams(
                        FrameLayout.LayoutParams.MATCH_PARENT,
                        FrameLayout.LayoutParams.MATCH_PARENT,
                        Gravity.CENTER
                    )
                // Add the loading components to this inner container
                addView(progressText)
                addView(
                    progressBar,
                    FrameLayout.LayoutParams.MATCH_PARENT,
                    FrameLayout.LayoutParams.WRAP_CONTENT
                )
                addView(percentageText)
            }
        container.addView(loadingUiContainer)

        // 4. Configure the PdfRendererView to update the progress bar.
        configureAndLoadPdf(pdfViewer!!, loadingUiContainer, progressBar, percentageText)

        // 5. Return the master container.
        container
    }

    private fun configureAndLoadPdf(
        pdfView: PdfRendererView,
        loadingUi: View,
        progressBar: ProgressBar,
        percentageTextView: TextView
    ) {
        pdfView.statusListener =
            object : PdfRendererView.StatusCallBack {
                override fun onPdfLoadProgress(
                    progress: Int,
                    downloadedBytes: Long,
                    totalBytes: Long?
                ) {
                    loadingUi.post {
                        progressBar.progress = progress
                        percentageTextView.text = "$progress%"
                    }
                }

                override fun onPdfRenderSuccess() {
                    Log.d(TAG, "[$instanceId] PDF Render SUCCESS.")
                    clientExecutor.execute { remoteCallback.onPdfRenderSuccess() }
                    if (isFirstPageRendered) {
                            loadingUi.postDelayed( {
                                loadingUi.alpha = 0f
                                loadingUi.isClickable = false
                            }, 30)
                    }
                }

                override fun onError(error: Throwable) {
                    Log.e(TAG, "[$instanceId] PDF Render ERROR.", error)
                    loadingUi.post {
                        loadingUi.alpha = 0f
                        loadingUi.isClickable = false
                    }
                    clientExecutor.execute {
                        remoteCallback.onError(error.message ?: "Unknown Error")
                    }
                }

                // Forward other callbacks as needed
                override fun onPdfLoadStart() {
                    clientExecutor.execute { remoteCallback.onPdfLoadStart() }
                }

                override fun onPageChanged(currentPage: Int, totalPage: Int) {
                    if (!isFirstPageRendered) {
                        Log.d(TAG, "[$instanceId] First page rendered. Hiding loading UI.")
                        isFirstPageRendered = true
                    }
                    clientExecutor.execute { remoteCallback.onPageChanged(currentPage, totalPage) }
                }
            }

        pdfView.initWithUrl(
            url = request.url,
            lifecycleCoroutineScope = this.lifecycleScope,
            lifecycle = this.lifecycle,
            cacheStrategy = CacheStrategy.MINIMIZE_CACHE
        )
    }

    override fun close() {
        Log.e(TAG, "[$instanceId] -- SESSION CLOSE REQUESTED --", Exception("Stack trace"))
        if (lifecycleRegistry.currentState != Lifecycle.State.DESTROYED) {
            lifecycleRegistry.currentState = Lifecycle.State.DESTROYED
            Log.e(TAG, "[$instanceId] Session lifecycle is now DESTROYED.")
        }
        pdfViewer?.closePdfRender()
        pdfViewer = null
    }

    // --- Other methods for logging ---
    override fun notifyResized(width: Int, height: Int) {
        Log.d(TAG, "[$instanceId] notifyResized: w=$width, h=$height")
    }

    // Other non-logging methods
    override fun notifyConfigurationChanged(configuration: Configuration) {}

    override fun notifyUiChanged(uiContainerInfo: Bundle) {}

    override fun notifyZOrderChanged(isZOrderOnTop: Boolean) {}
}