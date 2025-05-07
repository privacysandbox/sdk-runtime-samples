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
package com.inappmediatee.sdk

import android.content.Context
import android.content.Intent
import android.view.View
import android.webkit.WebView
import com.inappmediatee.R

class InAppMediateeSdk(private val context: Context) {

    private val webViewUrl = "https://www.google.com"

    fun loadBannerAd(isWebViewBannerAd: Boolean) : View {
        if (isWebViewBannerAd) {
            val webview = WebView(context)
            webview.loadUrl(webViewUrl)
            return webview
        }
        return View.inflate(context, R.layout.banner, null)
    }

    fun loadFullscreenAd() {
      // All the heavy logic to load fullscreen Ad that Mediatee needs to perform goes here.
    }

    fun showFullscreenAd() {
        val intent = Intent(context, LocalActivity::class.java)
        context.startActivity(intent)
    }
}