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
import android.widget.Button
import android.widget.TextView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LifecycleRegistry
import androidx.lifecycle.lifecycleScope
import androidx.privacysandbox.ui.core.SandboxedUiAdapter
import androidx.privacysandbox.ui.core.SandboxedUiAdapter.Session
import androidx.privacysandbox.ui.provider.AbstractSandboxedUiAdapter
import androidx.privacysandbox.ui.provider.AbstractSandboxedUiAdapter.AbstractSession
import com.rajat.pdfviewer.PdfRendererView
import com.runtimeenabled.R
import com.runtimeenabled.api.RemotePdfCallbackInterface
import com.runtimeenabled.api.RemotePdfRequest
import com.runtimeenabled.api.RemoteUiCallbackInterface
import com.runtimeenabled.api.RemoteUiRequest
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.asCoroutineDispatcher
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import java.util.concurrent.Executor

/**
 * Implementation of [SandboxedUiAdapter.Session], used for UI requests.
 * This class extends [AbstractSandboxedUiAdapter.AbstractSession] to provide the functionality in
 * cohesion with [AbstractSandboxedUiAdapter]
 *
 * @param clientExecutor The executor to use for client callbacks.
 * @param sdkContext The context of the SDK.
 * @param request The payment UI request.
 */
class SdkMessageUiSession(
    clientExecutor: Executor,
    private val sdkContext: Context,
    private val request: RemoteUiRequest,
    private val callback: RemoteUiCallbackInterface,
) : AbstractSandboxedUiAdapter.AbstractSession() {

    /** A scope for launching coroutines in the client executor. */
    private val scope = CoroutineScope(clientExecutor.asCoroutineDispatcher() + Job())

    override val view: View = getRemoteView()

    private fun getRemoteView(): View {
        val view = View.inflate(sdkContext, R.layout.message, null).apply {
            findViewById<TextView>(R.id.message).text = request.message
            findViewById<Button>(R.id.button).setOnClickListener {
                scope.launch {
                    callback.onDoSomething()
                }
            }
        }
        return view
    }

    override fun close() {
        // Notifies that the client has closed the session. It's a good opportunity to dispose
        // any resources that were acquired to maintain the session.
        scope.cancel()
    }

    override fun notifyConfigurationChanged(configuration: Configuration) {
        // Notifies that the device configuration has changed and affected the app.
    }

    override fun notifyResized(width: Int, height: Int) {
        // Notifies that the size of the presentation area in the app has changed.
    }

    override fun notifyUiChanged(uiContainerInfo: Bundle) {
        // Notify the session when the presentation state of its UI container has changed.
    }

    override fun notifyZOrderChanged(isZOrderOnTop: Boolean) {
        // Notifies that the Z order has changed for the UI associated by this session.
    }
}