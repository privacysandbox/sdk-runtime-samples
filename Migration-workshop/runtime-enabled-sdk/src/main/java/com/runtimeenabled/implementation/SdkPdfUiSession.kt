/*
 * Copyright (C) 2025 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.runtimeenabled.implementation

import android.content.Context
import android.content.res.Configuration
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LifecycleRegistry
import androidx.lifecycle.lifecycleScope
import androidx.privacysandbox.ui.provider.AbstractSandboxedUiAdapter.AbstractSession
import com.rajat.pdfviewer.PdfRendererView
import com.runtimeenabled.R
import com.runtimeenabled.api.RemotePdfCallbackInterface
import com.runtimeenabled.api.RemotePdfRequest
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.asCoroutineDispatcher
import kotlinx.coroutines.cancel
import java.util.concurrent.Executor

class SdkPdfUiSession(
    private val sdkContext: Context, // Use SDK's context for view inflation
    private val request: RemotePdfRequest,
    private val remoteCallback: RemotePdfCallbackInterface, // Renamed for clarity
    private val clientExecutor: Executor // Store for potential future use if needed for callbacks
) : AbstractSession(), LifecycleOwner {

    private val lifecycleRegistry = LifecycleRegistry(this)
    override val lifecycle: Lifecycle
        get() = lifecycleRegistry

    // CoroutineScope tied to this session's lifecycle for managing PDF viewer tasks
    // or other session-specific coroutines.
    private val sessionCoroutineScope = CoroutineScope(clientExecutor.asCoroutineDispatcher() + Job())

    private var pdfViewer: PdfRendererView? = null

    override val view: View = getPdfView()

    private fun getPdfView(): View {
        val rootView = View.inflate(sdkContext, R.layout.pdf_viewer_layout, null)
        pdfViewer = rootView.findViewById(R.id.pdfView)

        // Attach a View.OnAttachStateChangeListener to manage lifecycle transitions
        pdfViewer?.addOnAttachStateChangeListener(object : View.OnAttachStateChangeListener {
            override fun onViewAttachedToWindow(v: View) {
                Log.d("PdfUiSession", "onViewAttachedToWindow. Current Lifecycle state: ${lifecycleRegistry.currentState}")
                Log.d("PdfUiSession", "PDF View attached, lifecycle RESUMED")
//                lifecycleRegistry.currentState = Lifecycle.State.STARTED
//                lifecycleRegistry.currentState = Lifecycle.State.RESUMED
            }

            override fun onViewDetachedFromWindow(v: View) {
                Log.d("PdfUiSession", "onViewDetachedFromWindow. Current Lifecycle state: ${lifecycleRegistry.currentState}")
                //Log.d("PdfUiSession", "PDF View detached, lifecycle DESTROYED")
                // View is detached. Move lifecycle back to CREATED.
                // The session is not necessarily closed yet.
                // lifecycleRegistry.currentState = Lifecycle.State.STARTED // Go through STARTED first
                // lifecycleRegistry.currentState = Lifecycle.State.CREATED
                //Log.d("PdfUiSession", "PDF View detached, lifecycle CREATED")
            }
        })

        pdfViewer?.initWithUrl(
            url = request.url,
            lifecycleCoroutineScope = this.lifecycleScope,
            lifecycle = this.lifecycle
        )

        return rootView
    }

    init {
    }

    override fun close() {
        Log.d("PdfUiSession", "Session close requested. Current Lifecycle state: ${lifecycleRegistry.currentState}")
        pdfViewer?.closePdfRender()
        sessionCoroutineScope.cancel("PdfUiSession closed")
        pdfViewer = null
    }

    // --- Other AbstractSession methods ---
    // These are called by the system when the corresponding events occur on the client side.

    override fun notifyConfigurationChanged(configuration: Configuration) {
        // The hosting Activity's configuration changed.
        // You might need to inform your PDF viewer or re-layout if it doesn't handle this automatically.
        Log.d("PdfUiSession", "notifyConfigurationChanged: $configuration")
        // pdfViewer?.onConfigurationChanged(configuration) // If such a method exists
    }

    override fun notifyResized(width: Int, height: Int) {
        // The SandboxedSdkView in the client app has been resized.
        // The PDF view within your SDK should adapt. Often, standard Android layout mechanisms handle this.
        Log.d("PdfUiSession", "notifyResized: width=$width, height=$height")
        // pdfViewer?.layoutParams = pdfViewer?.layoutParams?.apply {
        //     this.width = width
        //     this.height = height
        // }
        // pdfViewer?.requestLayout()
    }

    override fun notifyUiChanged(uiContainerInfo: Bundle) {
        // Provides information about the UI container in the client app,
        // like on-screen geometry, opacity.
        //val sandboxedSdkViewUiInfo = SandboxedSdkViewUiInfo.fromBundle(uiContainerInfo)
        //Log.d("PdfUiSession", "notifyUiChanged: $sandboxedSdkViewUiInfo")
    }

    override fun notifyZOrderChanged(isZOrderOnTop: Boolean) {
        // Informs if the SDK's UI is now on top (e.g., for SurfaceView Z-ordering).
        // Usually less relevant for View-based UIs unless you're doing something specific with layering.
        Log.d("PdfUiSession", "notifyZOrderChanged: isZOrderOnTop=$isZOrderOnTop")
    }
}