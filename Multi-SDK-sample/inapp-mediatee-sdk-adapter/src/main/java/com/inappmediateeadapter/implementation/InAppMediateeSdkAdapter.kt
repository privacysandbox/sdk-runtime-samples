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
package com.inappmediateeadapter.implementation

import android.content.Context
import android.os.Bundle
import androidx.privacysandbox.ui.provider.toCoreLibInfo
import androidx.privacysandbox.activity.core.SdkActivityLauncher
import com.inappmediatee.sdk.InAppMediateeSdk
import com.runtimeenabled.api.MediateeAdapterInterface

/**
 * Adapter class that implements the interface declared by the Mediator.
 *
 * This is loaded by and registered with Mediator from runtime-aware-sdk (RA_SDK).
 */
class InAppMediateeSdkAdapter(private val context: Context): MediateeAdapterInterface {

    private val inAppMediateeSdk = InAppMediateeSdk(context)

    override suspend fun getBannerAd(
        appPackageName: String,
        activityLauncher: SdkActivityLauncher,
        isWebViewBannerAd: Boolean
    ): Bundle {
        // We return a Bundle containing a SandboxedUiAdapter binder.
        // The SandboxedUiAdapter contains the ad view returned from in app mediatee.
        // We return a Bundle here, not an interface that extends SandboxedUiAdapter, since a
        // PrivacySandboxInterface declared in one SDK cannot be implemented by another and
        // returned back.
        // A PrivacySandboxInterface is expected to be implemented by the declaring SDK (runtime-enabled-sdk
        // in this case) and a PrivacySandboxCallback is expected to be implemented by the
        // consuming SDK (adapter sdks).
        return InAppAdViewSandboxedUiAdapter(inAppMediateeSdk.loadBannerAd(isWebViewBannerAd))
            .toCoreLibInfo(context)
    }

    override suspend fun loadFullscreenAd() {
        inAppMediateeSdk.loadFullscreenAd()
    }

    override suspend fun showFullscreenAd(activityLauncher: SdkActivityLauncher) {
        // SdkActivityLauncher is ignored since In-App mediatee does not need it in its
        // showFullscreenAd() call.
        // In-app mediatee declares an Activity in its manifest that is used to show the ad,
        // SdkActivityLauncher is not used.
        inAppMediateeSdk.showFullscreenAd()
    }
}