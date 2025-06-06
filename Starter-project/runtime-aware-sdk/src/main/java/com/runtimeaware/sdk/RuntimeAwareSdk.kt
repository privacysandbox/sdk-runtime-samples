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
package com.runtimeaware.sdk

import android.content.Context
import android.os.Bundle
import android.util.Log
import androidx.privacysandbox.sdkruntime.client.SdkSandboxManagerCompat
import androidx.privacysandbox.sdkruntime.core.LoadSdkCompatException
import androidx.privacysandbox.ui.core.SandboxedUiAdapter
import com.runtimeenabled.api.PaymentUiRequest
import com.runtimeenabled.api.SdkService
import com.runtimeenabled.api.SdkServiceFactory

/**
 * This class represents an SDK that was created before the SDK runtime was available. It is in the
 * process of migrating to use the SDK runtime. At this point, all of the functionality has been
 * migrated to the "runtime enabled SDK" and this SDK merely serves as a wrapper.
 */

class RuntimeAwareSdk(private val context: Context) {

    /**
     * Initialize the SDK. If the SDK failed to initialize, return false, else true.
     */
    suspend fun initialize(): Boolean {
        // You can also have a fallback mechanism here, where if the SDK cannot be loaded in the SDK
        // runtime, initialize as you usually would.
        val isRuntimeEnabledSdkLoaded = loadSdkIfNeeded(context) != null
        return isRuntimeEnabledSdkLoaded
    }

    suspend fun getSandboxedUiAdapter(
        message: String,
        amount: Double,
        onPaymentSuccess: () -> Unit,
        context: Context
    ): SandboxedUiAdapter {
        val request = PaymentUiRequest(message, amount)
        return checkNotNull(
            loadSdkIfNeeded(
                context
            )?.getPaymentUiAdapter(request, PaymentCallback(onPaymentSuccess))
        ) { "Could not launch payment SDK!" }
    }

    /** Keeps a reference to a sandboxed SDK and makes sure it's only loaded once. */
    internal companion object Loader {

        private const val TAG = "ExistingSdk"

        /**
         * Name of the SDK to be loaded.
         *
         * (needs to be the one defined in runtime-enabled-sdk-bundle/build.gradle)
         */
        private const val SDK_NAME = "com.runtimeenabled.sdk"

        private var remoteInstance: SdkService? = null

        suspend fun loadSdkIfNeeded(context: Context): SdkService? {
            try {
                // First we need to check if the SDK is already loaded. If it is we just return it.
                // The sandbox manager will throw an exception if we try to load an SDK that is
                // already loaded.
                if (remoteInstance != null) return remoteInstance

                // An [SdkSandboxManagerCompat], used to communicate with the sandbox and load SDKs.
                val sandboxManagerCompat = SdkSandboxManagerCompat.from(context)

                val sandboxedSdk = sandboxManagerCompat.loadSdk(SDK_NAME, Bundle.EMPTY)
                remoteInstance = SdkServiceFactory.wrapToSdkService(sandboxedSdk.getInterface()!!)
                // Initialize SDK.
                remoteInstance?.initialize()
                return remoteInstance
            } catch (e: LoadSdkCompatException) {
                Log.e(TAG, "Failed to load SDK, error code: ${e.loadSdkErrorCode}", e)
                return null
            }
        }

        fun isSdkLoaded(): Boolean {
            return remoteInstance != null
        }
    }
}
