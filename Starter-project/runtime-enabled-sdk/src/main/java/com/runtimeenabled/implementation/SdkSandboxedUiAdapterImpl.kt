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
import android.view.View
import android.widget.Button
import android.widget.TextView
import androidx.privacysandbox.ui.core.SandboxedUiAdapter
import androidx.privacysandbox.ui.core.SessionData
import androidx.privacysandbox.ui.provider.AbstractSandboxedUiAdapter
import com.runtimeenabled.R
import com.runtimeenabled.api.RemoteUiCallbackInterface
import com.runtimeenabled.api.RemoteUiRequest
import com.runtimeenabled.api.SdkSandboxedUiAdapter
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.asCoroutineDispatcher
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import java.util.concurrent.Executor

/**
 * Implementation of [SdkSandboxedUiAdapter] that handles requests for the payment UI.
 *
 * This class extends [AbstractSandboxedUiAdapter] and provides the functionality to open
 * UI sessions. The usage of [AbstractSandboxedUiAdapter] simplifies the implementation.
 *
 * @param sdkContext The context of the SDK.
 * @param request The payment UI request.
 */
class SdkSandboxedUiAdapterImpl(
    private val sdkContext: Context,
    private val request: RemoteUiRequest,
    private val callback: RemoteUiCallbackInterface,
) : AbstractSandboxedUiAdapter(), SdkSandboxedUiAdapter {
    /**
     * Opens a new session to display remote UI.
     * The session will handle notifications from and to the client.
     * We consider the client the owner of the SandboxedSdkView.
     *
    @param context The client's context.
     * @param sessionData Constants related to the session, such as the presentation id.
     * @param initialWidth The initial width of the adapter's view.
     * @param initialHeight The initial height of the adapter's view.
     * @param isZOrderOnTop Whether the session's view should be drawn on top of other views.
     * @param clientExecutor The executor to use for client callbacks.
     * @param client A UI adapter representing the client of this single session.
     */
    override fun openSession(
        context: Context,
        sessionData: SessionData,
        initialWidth: Int,
        initialHeight: Int,
        isZOrderOnTop: Boolean,
        clientExecutor: Executor,
        client: SandboxedUiAdapter.SessionClient
    ) {
        val session = SdkUiSession(clientExecutor, sdkContext, request, callback)
        clientExecutor.execute {
            client.onSessionOpened(session)
        }
    }
}

/**
 * Implementation of [SandboxedUiAdapter.Session], used for payment UI requests.
 * This class extends [AbstractSandboxedUiAdapter.AbstractSession] to provide the functionality in
 * cohesion with [AbstractSandboxedUiAdapter]
 *
 * @param clientExecutor The executor to use for client callbacks.
 * @param sdkContext The context of the SDK.
 * @param request The payment UI request.
 */
private class SdkUiSession(
    clientExecutor: Executor,
    private val sdkContext: Context,
    private val request: RemoteUiRequest,
    private val callback: RemoteUiCallbackInterface,
) : AbstractSandboxedUiAdapter.AbstractSession() {

    /** A scope for launching coroutines in the client executor. */
    private val scope = CoroutineScope(clientExecutor.asCoroutineDispatcher() + Job())

    override val view: View = getRemoteView()

    private fun getRemoteView(): View {
        val view = View.inflate(sdkContext, R.layout.payment, null).apply {
            findViewById<TextView>(R.id.merchant_header_view).text =
                context.getString(R.string.merchant_label, request.appName)
            findViewById<TextView>(R.id.subtotal_value).text =
                String.format("$%.2f", request.amount)
            findViewById<TextView>(R.id.tax_value).text =
                String.format("$%.2f", request.amount * 0.08)
            findViewById<TextView>(R.id.total_value).text =
                String.format("$%.2f", request.amount * 1.08)

            findViewById<Button>(R.id.pay_button).setOnClickListener {
                scope.launch {
                    callback.onPaymentComplete()
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
