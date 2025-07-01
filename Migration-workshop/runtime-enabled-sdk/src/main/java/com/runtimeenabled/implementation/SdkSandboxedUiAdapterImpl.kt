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
class SdkSandboxedUiAdapterImpl<Req, CallbackIF, SessionType : AbstractSandboxedUiAdapter.AbstractSession>(
    private val sdkContext: Context,
    private val request: Req,
    private val callback: CallbackIF,
    private val sessionFactory: (
        sdkContext: Context,
        request: Req,
        callback: CallbackIF,
        clientExecutor: Executor
    ) -> SessionType) :
    AbstractSandboxedUiAdapter(), SdkSandboxedUiAdapter {

    override fun openSession(
        context: Context,
        sessionData: SessionData,
        initialWidth: Int,
        initialHeight: Int,
        isZOrderOnTop: Boolean,
        clientExecutor: Executor,
        client: SandboxedUiAdapter.SessionClient
    ) {
        val session = sessionFactory(sdkContext, request, callback, clientExecutor)
        clientExecutor.execute {
            client.onSessionOpened(session)
        }
    }
}

//(
//    private val sdkContext: Context,
//    private val request: RemoteUiRequest,
//    private val callback: RemoteUiCallbackInterface,
//) : AbstractSandboxedUiAdapter(), SdkSandboxedUiAdapter {
//    /**
//     * Opens a new session to display remote UI.
//     * The session will handle notifications from and to the client.
//     * We consider the client the owner of the SandboxedSdkView.
//     *
//    @param context The client's context.
//     * @param sessionData Constants related to the session, such as the presentation id.
//     * @param initialWidth The initial width of the adapter's view.
//     * @param initialHeight The initial height of the adapter's view.
//     * @param isZOrderOnTop Whether the session's view should be drawn on top of other views.
//     * @param clientExecutor The executor to use for client callbacks.
//     * @param client A UI adapter representing the client of this single session.
//     */
//    override fun openSession(
//        context: Context,
//        sessionData: SessionData,
//        initialWidth: Int,
//        initialHeight: Int,
//        isZOrderOnTop: Boolean,
//        clientExecutor: Executor,
//        client: SandboxedUiAdapter.SessionClient
//    ) {
//        val session = SdkUiSession(clientExecutor, sdkContext, request, callback)
//        clientExecutor.execute {
//            client.onSessionOpened(session)
//        }
//    }
//}
//*/