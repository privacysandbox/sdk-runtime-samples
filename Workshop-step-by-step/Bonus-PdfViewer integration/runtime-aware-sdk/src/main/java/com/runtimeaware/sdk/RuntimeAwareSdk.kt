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
import android.view.View
import androidx.privacysandbox.sdkruntime.client.SdkSandboxManagerCompat
import androidx.privacysandbox.sdkruntime.client.SdkSandboxProcessDeathCallbackCompat
import androidx.privacysandbox.sdkruntime.core.LoadSdkCompatException
import com.runtimeenabled.api.MyReSdkService
import com.runtimeenabled.api.MyReSdkServiceFactory
import com.runtimeenabled.api.RemotePdfCallbackInterface
import com.runtimeenabled.api.RemotePdfRequest
import java.util.concurrent.Executor // Required for the callback

/**
 * This class represents an SDK that was created before the SDK runtime was available. It is in the
 * process of migrating to use the SDK runtime. At this point, all of the functionality has been
 * migrated to the "runtime enabled SDK" and this SDK merely serves as a wrapper.
 */
class RuntimeAwareSdk(private val context: Context) { // Use context for broader lifecycle

    // Executor for the death callback. You can use a cached thread pool or a single thread executor.
    // For simplicity, using a direct executor for this example, but consider your threading needs.
    // For UI updates from the callback, ensure you switch to the main thread.
    private val deathCallbackExecutor: Executor = Executor { command -> command.run() } // Or Executors.newSingleThreadExecutor()
    private var sandboxManager: SdkSandboxManagerCompat? = null

    // Optional: A client-provided callback to be invoked on sandbox death
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


    /**
     * Initialize the SDK. If the SDK failed to initialize, return false, else true.
     */
    suspend fun initialize(): Boolean {
        // Initialize the sandbox manager if not already done
        if (sandboxManager == null) {
            sandboxManager = SdkSandboxManagerCompat.from(context)
        }

        // Register the death callback once the sandbox manager is available.
        // This should ideally be done before any SDK loading attempts.
        try {
            val deathCallback = SandboxDeathCallback()
            sandboxManager?.addSdkSandboxProcessDeathCallback(
                deathCallbackExecutor,
                deathCallback
            )
            Log.d(TAG, "SDK sandbox death callback registered.")
        } catch (e: Exception) {
            // This can happen if the sandbox is not available or other issues.
            Log.e(TAG, "Failed to register SDK sandbox death callback.", e)
            // Depending on your app's requirements, you might want to consider this a fatal error
            // or attempt to proceed without the death callback.
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
            //TODO: Update UI to reflect this.

            // The sandbox process has died. The SDK is no longer usable.
            // Reset the loaded SDK instance.
            remoteInstance = null

            // Here you can implement logic to:
            // 1. Notify the client app (e.g., via a callback or event bus)
            // 2. Attempt to reload the SDK (perhaps with a backoff strategy)
            // 3. Update UI to reflect that the SDK is unavailable

            // Invoke the client-provided callback if it's set
            onSandboxDeathClientCallback?.invoke()

            // Example: You might want to automatically try to re-initialize the SDK.
            // Be cautious with automatic re-initialization to avoid loops if the sandbox keeps crashing.
            // GlobalScope.launch { // Or a specific coroutine scope tied to your SDK's lifecycle
            //     Log.d(TAG, "Attempting to re-initialize SDK after sandbox death...")
            //     initialize() // This will also re-register the death callback
            // }
        }
    }

//    // In your RuntimeAwareSdk (simplified concept)
//// This method would internally call MyReSdkService.getPdfUiAdapter
//    suspend fun createPdfView(
//        // For now, no specific parameters from client, as they are hardcoded in SDK
//        // In the future, you'd pass RemotePdfRequest here
//        onSuccess: () -> Unit,
//        onError: () -> Unit,
//        onSaved: () -> Unit // Assuming these are the callbacks from RemotePdfCallbackInterface
//    ): View? {
//
//
//        // In a real scenario, you'd construct a RemotePdfRequest here
//        val dummyRequest = RemotePdfRequest(
//            url = "hardcoded_or_default_url_in_sdk_for_now",
//            title = "PDF Title",
//            saveTo = null,
//            enableDownload = false,
//            password = null
//        )
//
//        val callback = RemotePdfCallbackInterface() {
//            override fun onPdfSuccess() {
//                onSuccess()
//            }
//            override fun onPdfError() {
//                onError()
//            }
//            override fun onPdfSaved() {
//                onSaved()
//            }
//        }
//        // This is the key call to get the SandboxedUiAdapter
//        val sdkSandboxedUiAdapter = loadSdkIfNeeded()?.getPdfUiAdapter(dummyRequest, callback)
//
//        // The client then uses this adapter to create a view
//        // The PrivacySandboxU solche I Jetpack library provides helpers for this.
//        // For simplicity here, we'll assume a method that directly returns a View
//        // after the client library has processed the adapter.
//        // In a real app, you'd use SandboxedSdkProvider.createViewFromAdapter(sdkSandboxedUiAdapter, activity, ...)
//        // For now, let's represent this with a placeholder:
//        return viewProvider.createViewFromAdapter(sdkSandboxedUiAdapter, /* activity or context */, /* token */)
//    }

    // Expose these methods from the instance, calling the companion object's methods
    // This allows the instance to manage context and sandboxManager properly.
    private suspend fun loadSdkIfNeeded(): MyReSdkService? {
        val manager = this.sandboxManager ?: SdkSandboxManagerCompat.from(context).also { this.sandboxManager = it }
        return loadSdkIfNeeded(context, manager)
    }

    /** Keeps a reference to a sandboxed SDK and makes sure it's only loaded once. */
    companion object Loader {
        private const val TAG = "RuntimeAwareSdk"

        /**
         * Name of the SDK to be loaded.
         */
        private const val SDK_NAME = "com.runtimeenabled.sdk"

        @Volatile // Ensure visibility across threads
        private var remoteInstance: MyReSdkService? = null

        // This method should be internal or private to RuntimeAwareSdk
        // and called by the instance methods.
        suspend fun loadSdkIfNeeded(context: Context, manager: SdkSandboxManagerCompat? = null): MyReSdkService? {
            //If no manager is being passed, it means that it's being called from outside this class
            val currentManager = manager ?: SdkSandboxManagerCompat.from(context)
            
            try {
                if (remoteInstance != null) return remoteInstance

                Log.d(TAG, "Loading SDK: $SDK_NAME")
                val sandboxedSdk = currentManager.loadSdk(SDK_NAME, Bundle.EMPTY)
                val service = MyReSdkServiceFactory.wrapToMyReSdkService(sandboxedSdk.getInterface()!!)
                service.initialize() // Initialize the SDK service from the runtime-enabled SDK
                remoteInstance = service
                Log.d(TAG, "SDK loaded successfully: $SDK_NAME")
                return remoteInstance
            } catch (e: LoadSdkCompatException) {
                Log.e(TAG, "Failed to load SDK ($SDK_NAME), error code: ${e.loadSdkErrorCode}", e)
                remoteInstance = null // Ensure it's null on failure
                return null
            } catch (e: Exception) { // Catch other potential exceptions during loading
                Log.e(TAG, "An unexpected error occurred while loading SDK ($SDK_NAME)", e)
                remoteInstance = null
                return null
            }
        }

        fun isSdkLoaded(): Boolean {
            return remoteInstance != null
        }

        // Method to explicitly unload/reset the SDK, e.g., after sandbox death
        fun unloadSdk(context: Context, manager: SdkSandboxManagerCompat? = null) {
            val currentManager = manager ?: SdkSandboxManagerCompat.from(context)
            Log.d(TAG, "Unloading SDK instance.")
            remoteInstance = null
            currentManager.unloadSdk(SDK_NAME)
        }
    }
}
