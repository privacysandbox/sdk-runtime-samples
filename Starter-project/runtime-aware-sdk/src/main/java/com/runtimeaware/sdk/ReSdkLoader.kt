package com.runtimeaware.sdk

import android.content.Context
import android.os.Bundle
import android.util.Log
import androidx.privacysandbox.sdkruntime.client.SdkSandboxManagerCompat
import androidx.privacysandbox.sdkruntime.core.LoadSdkCompatException
import com.runtimeenabled.api.MyReSdkService
import com.runtimeenabled.api.MyReSdkServiceFactory


/**
 * Manages the loading and unloading of the Runtime Enabled SDK (RE-SDK).
 *
 * This object is responsible for ensuring that the RE-SDK is loaded only once
 * and provides a cached instance of the SDK service for other parts of the application
 * to use. It handles potential errors during the SDK loading process.
 */
internal object ReSdkLoader {
    private const val TAG = "Loader"

    private const val SDK_NAME = "com.runtimeenabled.sdk"

    private var cachedSdkService: MyReSdkService? = null

    /**
     * Retrieves the instance of the RE-SDK service.
     *
     * If the SDK has already been loaded, it returns the cached instance.
     * Otherwise, it attempts to load the SDK and then returns the instance.
     *
     * @param context The application context.
     * @return The [MyReSdkService] instance if loaded successfully, or null if an error occurs.
     */
    suspend fun getSdkService(context: Context): MyReSdkService? {
        if (cachedSdkService != null) return cachedSdkService
        return loadSdk(context)
    }

    /**
     * Loads the RE-SDK using the [SdkSandboxManagerCompat].
     * @param context The application context.
     * @return The [MyReSdkService] instance if loaded successfully, or null if an error occurs.
     */
    private suspend fun loadSdk(context: Context): MyReSdkService? {
        try {
            val sandboxManager = SdkSandboxManagerCompat.from(context)
            val sandboxedSdk = sandboxManager.loadSdk(SDK_NAME, Bundle.EMPTY)
            val myReSdkService = MyReSdkServiceFactory.wrapToMyReSdkService(sandboxedSdk.getInterface()!!)
            myReSdkService.initialize()
            cachedSdkService = myReSdkService
            Log.d(TAG, "SDK loaded successfully: $SDK_NAME")
            return cachedSdkService
        } catch (e: LoadSdkCompatException) {
            Log.e(TAG, "Failed to load SDK ($SDK_NAME), error code: ${e.loadSdkErrorCode}", e)
            cachedSdkService = null
            return null
        } catch (e: Exception) {
            Log.e(TAG, "An unexpected error occurred while loading SDK ($SDK_NAME)", e)
            cachedSdkService = null
            return null
        }
    }

    /**
     * Checks if the RE-SDK has been loaded successfully.
     *
     * @return True if the SDK is loaded, false otherwise.
     */
    fun isSdkLoaded(): Boolean {
        return cachedSdkService != null
    }

    /**
     * Unloads the RE-SDK by clearing the cached service instance.
     * @return True if the SDK was unloaded (or was already unloaded).
     * @throws IllegalStateException if the SDK is not loaded when this method is called.
     */
    @Synchronized // Ensures only one thread can execute this at a time.
    fun unloadSdk() {
        if (cachedSdkService == null) {
            // SDK is already unloaded, do nothing.
            return
        }
        cachedSdkService = null
        Log.d(TAG, "SDK unloaded.")
    }
}