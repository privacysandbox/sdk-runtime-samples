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
import android.util.Log
import androidx.privacysandbox.sdkruntime.client.SdkSandboxManagerCompat
import androidx.privacysandbox.sdkruntime.client.SdkSandboxProcessDeathCallbackCompat
import androidx.privacysandbox.sdkruntime.core.LoadSdkCompatException
import com.runtimeaware.sdk.ReSdkLoader.getSdkService
import com.runtimeaware.sdk.ReSdkLoader.isSdkLoaded
import com.runtimeaware.sdk.ReSdkLoader.unloadSdk
import java.util.concurrent.Executor
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

/**
 * The Runtime-Aware SDK is a statically linked library that works as a translation interface
 * between the app and the Runtime-Enabled SDK.
 */
class RuntimeAwareSdk(private val context: Context) {



    // Handling process death
    // Executor for the death callback.
    private val deathCallbackExecutor: ExecutorService = Executors.newSingleThreadExecutor()

    private val deathCallback = SandboxDeathCallback()

    private var onSandboxDeathClientCallback: (() -> Unit)? = null

    /**
     * Initializes the SDK.
     *
     * This function attempts to load the RE (Runtime-Enabled) SDK into the SDK Sandbox.
     * It also registers a callback to handle the death of the SDK sandbox process.
     *
     * @return `true` if the RE SDK is successfully loaded and the service is available,
     *         `false` otherwise.
     * @throws LoadSdkCompatException if there's an issue loading the SDK into the sandbox.
     *                               This can happen for various reasons, such as the RE SDK
     *                               not being installed or version incompatibility.
     * @see SdkSandboxManagerCompat.loadSdk
     * @see SdkSandboxProcessDeathCallbackCompat
     */
    suspend fun initialize(): Boolean {
        val sandboxManager = SdkSandboxManagerCompat.from(context)
        try {
            sandboxManager.addSdkSandboxProcessDeathCallback(
                deathCallbackExecutor,
                deathCallback
            )
        } catch (e: Exception) {
            Log.e(TAG, "Failed to register SDK sandbox death callback.", e)
        }
        return getSdkService(context) != null
    }

    /**
     * Unregisters the sandbox death callback and shuts down the executor.
     * This should be called when the SDK is no longer needed to prevent resource leaks.
     */
    fun close() {
        val sandboxManager = SdkSandboxManagerCompat.from(context)
        try {
            sandboxManager.removeSdkSandboxProcessDeathCallback(deathCallback)
        } catch(e: Exception) {
            Log.w(TAG, "Failed to unregister death callback.", e)
        }
        deathCallbackExecutor.shutdown()
    }

    /**
     * Creates a file in the RE SDK's sandbox.
     *
     * This function is a wrapper around the RE SDK's `createFile` method. It demonstrates
     * a more idiomatic Kotlin approach to handling operations that can fail by returning
     * a `Result` object.
     *
     * @param size The size of the file to create in megabytes.
     * @return A `Result` object that encapsulates the outcome:
     *         - On success, it holds a `Result.success` with the file path (`String?`).
     *         - On failure, it holds a `Result.failure` with the `Exception` that occurred.
     * @throws IllegalStateException if the SDK has not been initialized before calling this method.
     */
    suspend fun createFile(size: Long): Result<String?> {
        val sdkService = getSdkService(context)
            ?: throw IllegalStateException("RE SDK not loaded. Please call initialize() first.")

        return runCatching {
            sdkService.createFile(size)
        }.onFailure { e ->
            Log.e(TAG, "Failed to create file", e)
        }
    }

    /**
     * Triggers the death of the SDK runtime process for testing purposes.
     * This method is intended for testing how the app and SDK handle sandbox process death.
     * It will attempt to call the `triggerProcessDeath` method on the RE SDK service.
     *
     * @throws IllegalStateException if the SDK has not been initialized.
     */
    suspend fun triggerProcessDeath() {
        if (!isSdkLoaded()) {
            throw IllegalStateException("SDK not loaded. Please call initialize() first.")
        }
        try {
            getSdkService(context)?.triggerProcessDeath()
        } catch (e: Exception) {
            Log.e(TAG, "Failed to kill SDK runtime process", e)
        }
    }

    /**
     * Registers a callback to be invoked if the SDK sandbox process dies.
     * The callback will be executed on the thread provided by [deathCallbackExecutor].
     *
     * @param callback The lambda to execute when the sandbox process dies.
     */
    fun setOnSandboxDeathCallback(callback: () -> Unit) {
        this.onSandboxDeathClientCallback = callback
    }

    // Inner class for the death callback
    private inner class SandboxDeathCallback : SdkSandboxProcessDeathCallbackCompat {
        init {
            Log.d(TAG, "SandboxDeathCallback initialized.")
        }
        override fun onSdkSandboxDied() {
            Log.e(TAG, "SDK Sandbox process died! State is lost. " +
                    "SDKs need to be reloaded by the client.")

            // The sandbox process has died. Reset our internal state FIRST.
            unloadSdk()

            // Now, notify the client (UI) that it can reload.
            onSandboxDeathClientCallback?.invoke()

            // It's generally safer to unregister the callback after we are done.
            close()
        }
    }

    companion object {
        private const val TAG = "RuntimeAwareSdk" // More descriptive and conventional
    }
}
