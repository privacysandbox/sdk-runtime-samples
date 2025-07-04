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

import android.annotation.SuppressLint
import android.content.Context
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.os.Process
import android.webkit.WebResourceRequest
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.annotation.RequiresApi
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updatePadding
import androidx.privacysandbox.activity.core.SdkActivityLauncher
import androidx.privacysandbox.sdkruntime.core.activity.ActivityHolder
import androidx.privacysandbox.sdkruntime.core.activity.SdkSandboxActivityHandlerCompat
import androidx.privacysandbox.sdkruntime.core.controller.SdkSandboxControllerCompat
import androidx.privacysandbox.ui.core.SandboxedSdkViewUiInfo
import androidx.privacysandbox.ui.core.SandboxedUiAdapterSignalOptions
import androidx.privacysandbox.ui.core.SessionObserver
import androidx.privacysandbox.ui.core.SessionObserverContext
import androidx.privacysandbox.ui.core.SessionObserverFactory
import com.runtimeenabled.api.MyReSdkService
import com.runtimeenabled.api.RemotePdfCallbackInterface
import com.runtimeenabled.api.RemotePdfRequest
import com.runtimeenabled.api.RemoteUiCallbackInterface
import com.runtimeenabled.api.RemoteUiRequest
import com.runtimeenabled.api.SdkSandboxedUiAdapter
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.nio.file.Files
import java.nio.file.Paths



class MyReSdkServiceImpl(private val context: Context) : MyReSdkService {

    private val controller = SdkSandboxControllerCompat.from(context)

    override suspend fun initialize() {
    }

    override suspend fun showFullscreenUi(activityLauncher: SdkActivityLauncher) {
        TODO("Not yet implemented")
    }

    override suspend fun getRemoteUiAdapter(
        request: RemoteUiRequest,
        callback: RemoteUiCallbackInterface
    ): SdkSandboxedUiAdapter {
        val remoteUiAdapter = SdkSandboxedUiAdapterImpl(
            context,
            request,
            callback
        ) { sdkCtx, req, cb, clientEx ->
            SdkMessageUiSession(clientExecutor = clientEx, sdkContext = sdkCtx, request = req, callback = cb)
        }
        remoteUiAdapter.addObserverFactory(SessionObserverFactoryImpl())
        return remoteUiAdapter
    }

    override suspend fun getPdfUiAdapter(
        request: RemotePdfRequest,
        callback: RemotePdfCallbackInterface
    ): SdkSandboxedUiAdapter {
        val remoteUiAdapter = SdkSandboxedUiAdapterImpl(
            context,
            request,
            callback
        ) { sdkCtx, req, cb, clientEx ->
            SdkPdfUiSession(clientExecutor = clientEx, sdkContext = sdkCtx, request = req, remoteCallback = cb)
        }
        remoteUiAdapter.addObserverFactory(SessionObserverFactoryImpl())
        return remoteUiAdapter
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override suspend fun createFile(sizeInMb: Long): String {
        val path = Paths.get(
            context.applicationContext.dataDir.path, "file.txt"
        )
        withContext(Dispatchers.IO) {
            Files.deleteIfExists(path)
            Files.createFile(path)
            val buffer = ByteArray(sizeInMb.toInt() * 1024 * 1024)
            Files.write(path, buffer)
        }

        val file = File(path.toString())
        val actualFileSize: Long = file.length() / (1024 * 1024)
        return "Created $actualFileSize MB file successfully"
    }

    override suspend fun triggerProcessDeath() {
        Process.killProcess(Process.myPid())
    }
}

/**
 * A factory for creating [SessionObserver] instances.
 *
 * This class provides a way to create observers that can monitor the lifecycle of UI sessions
 * and receive updates about UI container changes.
 */
private class SessionObserverFactoryImpl : SessionObserverFactory {
    override val signalOptions: Set<String> =
        setOf(
            SandboxedUiAdapterSignalOptions.GEOMETRY
        )

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
