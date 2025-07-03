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
import androidx.privacysandbox.sdkruntime.client.SdkSandboxProcessDeathCallbackCompat
import androidx.privacysandbox.sdkruntime.core.LoadSdkCompatException
import com.runtimeenabled.api.MyReSdkService
import com.runtimeenabled.api.MyReSdkServiceFactory
import java.util.concurrent.Executor // Required for the callback

/**
 * This class represents an SDK that was created before the SDK runtime was available. It is in the
 * process of migrating to use the SDK runtime. At this point, all of the functionality has been
 * migrated to the "runtime enabled SDK" and this SDK merely serves as a wrapper.
 */
class RuntimeAwareSdk(private val context: Context) { // Use context for broader lifecycle

    private var sandboxManager: SdkSandboxManagerCompat? = null


    /**
     * Initialize the SDK. If the SDK failed to initialize, return false, else true.
     */
    suspend fun initialize(): Boolean {
        // Initialize the sandbox manager if not already done
        if (sandboxManager == null) {
            sandboxManager = SdkSandboxManagerCompat.from(context)
        }

        // Register the death callback
        try {
            val deathCallback = SandboxDeathCallback()
            sandboxManager?.addSdkSandboxProcessDeathCallback(
                deathCallbackExecutor,
                deathCallback
            )
            Log.d(TAG, "SDK sandbox death callback registered.")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to register SDK sandbox death callback.", e)
        }

        val isRuntimeEnabledSdkLoaded = loadSdkIfNeeded() != null
        return isRuntimeEnabledSdkLoaded
    }

    suspend fun createFile(size: Long): Pair<Boolean, String?> {
        if (!isSdkLoaded()) {
            throw IllegalStateException("SDK not loaded. Please call initialize() first.")
        }
        try {
            return Pair(true, loadSdkIfNeeded()?.createFile(size))
        } catch (e: Exception) {
            Log.e(TAG, "Failed to create file", e)
            return Pair(false, "Failed to create file: ${e.message}")
        }
    }

    //--- Handling process death

    // Executor for the death callback.
    private val deathCallbackExecutor: Executor = Executor { command -> command.run() } // Or Executors.newSingleThreadExecutor()

    private var onSandboxDeathClientCallback: (() -> Unit)? = null

    /**
     * Registers a callback to be invoked if the SDK sandbox process dies.
     * The callback will be executed on the thread provided by [deathCallbackExecutor].
     *
     * @param callback The lambda to execute when the sandbox process dies.
     */
    fun setOnSandboxDeathCallback(callback: () -> Unit) {
        this.onSandboxDeathClientCallback = callback
    }
    suspend fun triggerProcessDeath() {
        if (!isSdkLoaded()) {
            throw IllegalStateException("SDK not loaded. Please call initialize() first.")
        }
        try {
            loadSdkIfNeeded()?.triggerProcessDeath()
        } catch (e: Exception) {
            Log.e(TAG, "Failed to kill SDK runtime process", e)
        }
    }

    // Inner class for the death callback
    private inner class SandboxDeathCallback : SdkSandboxProcessDeathCallbackCompat {
        init {
            Log.d(TAG, "SandboxDeathCallback initialized.")
        }
        override fun onSdkSandboxDied() {
            Log.e(TAG, "SDK Sandbox process died! State is lost. SDKs need to be reloaded by the client.")

            // The sandbox process has died. The SDK is no longer usable.
            // Reset the loaded SDK instance.
            remoteInstance = null

            // Invoke the client-provided callback if it's set
            onSandboxDeathClientCallback?.invoke()

        }
    }

    private suspend fun loadSdkIfNeeded(): MyReSdkService? {
        val manager = this.sandboxManager ?: SdkSandboxManagerCompat.from(context).also { this.sandboxManager = it }
        return loadSdkIfNeeded(context, manager)
    }

    /** Keeps a reference to a sandboxed SDK and makes sure it's only loaded once. */
    companion object Loader {
        private const val TAG = "RuntimeAwareSdk"

        // Package name of the SDK to load
        private const val SDK_NAME = "com.runtimeenabled.sdk"

        @Volatile // Ensure visibility across threads
        private var remoteInstance: MyReSdkService? = null

        suspend fun loadSdkIfNeeded(context: Context, manager: SdkSandboxManagerCompat? = null): MyReSdkService? {

            val currentManager = manager ?: SdkSandboxManagerCompat.from(context)

            try {
                if (remoteInstance != null) return remoteInstance

                Log.d(TAG, "Loading SDK: $SDK_NAME")
                val sandboxedSdk = currentManager.loadSdk(SDK_NAME, Bundle.EMPTY)
                val service = MyReSdkServiceFactory.wrapToMyReSdkService(sandboxedSdk.getInterface()!!)
                service.initialize()
                remoteInstance = service
                Log.d(TAG, "SDK loaded successfully: $SDK_NAME")
                return remoteInstance
            } catch (e: LoadSdkCompatException) {
                Log.e(TAG, "Failed to load SDK ($SDK_NAME), error code: ${e.loadSdkErrorCode}", e)
                remoteInstance = null
                return null
            } catch (e: Exception) {
                Log.e(TAG, "An unexpected error occurred while loading SDK ($SDK_NAME)", e)
                remoteInstance = null
                return null
            }
        }

        fun isSdkLoaded(): Boolean {
            return remoteInstance != null
        }
    }
}
