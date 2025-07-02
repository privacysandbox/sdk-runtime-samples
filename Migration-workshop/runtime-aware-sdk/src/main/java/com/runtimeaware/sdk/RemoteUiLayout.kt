package com.runtimeaware.sdk

import android.content.Context
import android.graphics.Color
import android.util.AttributeSet
import android.util.Log
import android.view.Gravity
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.core.graphics.toColorInt
import androidx.privacysandbox.ui.client.view.SandboxedSdkView
import androidx.privacysandbox.ui.client.view.SandboxedSdkViewEventListener
import com.runtimeaware.sdk.RuntimeAwareSdk.Loader.isSdkLoaded
import com.runtimeaware.sdk.RuntimeAwareSdk.Loader.loadSdkIfNeeded
import com.runtimeenabled.api.RemotePdfCallbackInterface
import com.runtimeenabled.api.RemotePdfRequest
import com.runtimeenabled.api.RemoteUiCallbackInterface
import com.runtimeenabled.api.RemoteUiRequest
import com.runtimeenabled.api.SdkSandboxedUiAdapter
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/**
 * A layout that MANAGES a single, stable SandboxedSdkView instance to prevent flicker.
 * It uses a composition pattern where it creates its child SSV lazily and only once
 * for its lifetime, reusing it for subsequent adapter changes.
 */
class RemoteUiLayout(context: Context, attrs: AttributeSet?) : LinearLayout(context, attrs) {
    companion object { const val TAG = "FlickerDebug" }
    //Allows programmatic creation
    constructor(context: Context) : this(context, null)

    private val viewScope = CoroutineScope(Dispatchers.Main.immediate)

    // The single, stable SandboxedSdkView instance. It's nullable because it's created lazily.
    private var sandboxedSdkView: SandboxedSdkView? = null

    init {
        Log.d(TAG, "RemoteUiLayout constructor CALLED. HashCode: ${this.hashCode()}")
    }

    // Helper function to create the SSV on-demand and add it to this layout.
    private fun getOrCreateSandboxedSdkView(): SandboxedSdkView {
        sandboxedSdkView?.let {
            Log.d(TAG, "getOrCreateSandboxedSdkView: RE-USING existing SSV. HashCode: ${it.hashCode()}")
            return it
        }
        Log.d(TAG, "getOrCreateSandboxedSdkView: CREATING NEW SSV instance.")
        val newSsv = SandboxedSdkView(context).apply {
            layoutParams = LayoutParams(
                LayoutParams.MATCH_PARENT,
                LayoutParams.MATCH_PARENT
            )
        }

        val listener = object : SandboxedSdkViewEventListener {
            override fun onUiDisplayed() { Log.d(TAG, "SSV Event: onUiDisplayed") }
            override fun onUiError(error: Throwable) { Log.e(TAG, "SSV Event: onUiError", error) }
            override fun onUiClosed() { Log.d(TAG, "SSV Event: onUiClosed") }
        }
        newSsv.setEventListener(listener)
        Log.d(TAG, "getOrCreateSandboxedSdkView: NEW SSV CREATED. HashCode: ${newSsv.hashCode()}")
        this.sandboxedSdkView = newSsv
        addView(newSsv)
        return newSsv
    }

    fun presentMessageUiFromMyReSdk(message: String, onSuccess: () -> Unit) {
        if (!isSdkLoaded()) return

        viewScope.launch {
            val sdk = loadSdkIfNeeded(context) ?: return@launch
            val request = RemoteUiRequest(message)
            val callback = object : RemoteUiCallbackInterface {
                override suspend fun onDoSomething() {
                    Toast.makeText(context, "Something was done", Toast.LENGTH_SHORT).show()
                    onSuccess()
                }
            }
            val adapter = sdk.getRemoteUiAdapter(request, callback)
            getOrCreateSandboxedSdkView().setAdapter(adapter)
        }
    }

    fun presentPdfUiFromMyReSdk(url: String, onSuccess: () -> Unit) {
        Log.d(TAG, "presentPdfUiFromMyReSdk CALLED.")
        if (!isSdkLoaded()) return

        val progressText =
            TextView(context).apply {
                text = "Loading PDF..."
                setTextColor(Color.WHITE)
            }

        val progressBar =
            ProgressBar(context, null, android.R.attr.progressBarStyleHorizontal).apply {
                isIndeterminate = false
                max = 100
            }

        val percentageText =
            TextView(context).apply {
                text = "0%"
                setTextColor(Color.WHITE)
                gravity = Gravity.CENTER
            }

        val loadingUiContainer =
            LinearLayout(context).apply {
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

        viewScope.launch {
            val sdk = loadSdkIfNeeded(context) ?: return@launch
            var adapter: SdkSandboxedUiAdapter? = null
            val request = RemotePdfRequest(url, null, null, true, null)
            val callback = object : RemotePdfCallbackInterface {
                override fun onPdfLoadStart() {
                    Log.d(TAG, "onPdfLoadStart")
                    //Should start loading UI
                }

                override fun onPdfLoadProgress(
                    progress: Int,
                    downloadedBytes: Long,
                    totalBytes: Long?
                ) {
                    Log.d(TAG, "onPdfLoadProgress: $progress%, $downloadedBytes/$totalBytes")
                    //Should update loading UI
                }

                override fun onPdfLoadSuccess(absolutePath: String) {
                    Log.d(TAG, "onPdfLoadSuccess: $absolutePath")
                }

                override fun onError(error: String) {
                    Log.e(TAG, "onError: $error")
                }

                override fun onPageChanged(currentPage: Int, totalPage: Int) {
                    Log.d(TAG, "onPageChanged: $currentPage/$totalPage")
                }

                override fun onPdfRenderStart() {
                    Log.d(TAG, "onPdfRenderStart")
                }

                override fun onPdfRenderSuccess() {
                    Log.d(TAG, "onPdfRenderSuccess")
                }
            }
            adapter = sdk.getPdfUiAdapter(request, callback)
            getOrCreateSandboxedSdkView().setAdapter(adapter)
        }
    }


    fun clearUi() {
        Log.d(TAG, "clearUi CALLED.")
        // Close the session on the existing SSV...
        sandboxedSdkView?.setAdapter(null)
        // ...then remove all views to reset this layout for the next time it's used.
        removeAllViews()
        sandboxedSdkView = null
    }
}