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
import android.os.Bundle
import android.util.Log
import androidx.privacysandbox.ui.core.ExperimentalFeatures
import androidx.privacysandbox.ui.core.SandboxedSdkViewUiInfo
import androidx.privacysandbox.ui.core.SessionObserver
import androidx.privacysandbox.ui.core.SessionObserverContext
import androidx.privacysandbox.ui.core.SessionObserverFactory
import com.runtimeenabled.api.PaymentCallbackInterface
import com.runtimeenabled.api.PaymentSdkService
import com.runtimeenabled.api.PaymentUiRequest
import com.runtimeenabled.api.SdkSandboxedUiAdapter

class SdkServiceImpl(private val context: Context) : PaymentSdkService {

    override suspend fun initialize() {
    }

    @OptIn(ExperimentalFeatures.DelegatingAdapterApi::class)
    override suspend fun getPaymentUiAdapter(
        request: PaymentUiRequest,
        callback: PaymentCallbackInterface
    ): SdkSandboxedUiAdapter {
        val paymentUiAdapter = SdkSandboxedUiAdapterImpl(context, request, callback)
        paymentUiAdapter.addObserverFactory(SessionObserverFactoryImpl())
        return paymentUiAdapter
    }
}

/**
 * A factory for creating [SessionObserver] instances.
 *
 * This class provides a way to create observers that can monitor the lifecycle of UI sessions
 * and receive updates about UI container changes.
 */
private class SessionObserverFactoryImpl : SessionObserverFactory {
    override fun create(): SessionObserver {
        return SessionObserverImpl()
    }

    /**
     * An implementation of [SessionObserver] that logs session lifecycle events and UI container
     * information.
     */
    private inner class SessionObserverImpl : SessionObserver {
        override fun onSessionOpened(sessionObserverContext: SessionObserverContext) {
            Log.i("SessionObserver", "onSessionOpened $sessionObserverContext")
        }

        /**
         * Called when the UI container associated with a session changes.
         *
         * @param uiContainerInfo A Bundle containing information about the UI container,
         * including on-screen geometry, width, height, and opacity.
         */
        override fun onUiContainerChanged(uiContainerInfo: Bundle) {
            val sandboxedSdkViewUiInfo = SandboxedSdkViewUiInfo.fromBundle(uiContainerInfo)
            val onScreen = sandboxedSdkViewUiInfo.onScreenGeometry
            val width = sandboxedSdkViewUiInfo.uiContainerWidth
            val height = sandboxedSdkViewUiInfo.uiContainerHeight
            val opacity = sandboxedSdkViewUiInfo.uiContainerOpacityHint
            Log.i("SessionObserver", "UI info: " +
                    "On-screen geometry: $onScreen, width: $width, height: $height," +
                    " opacity: $opacity")
        }

        override fun onSessionClosed() {
            Log.i("SessionObserver", "onSessionClosed")
        }
    }
}
