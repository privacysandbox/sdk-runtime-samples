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
package com.example.client

import android.os.Bundle
import android.widget.Toast
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.client.data.sampleMenuItems
import com.example.client.ui.screens.RestaurantMenuScreen
import com.example.client.ui.theme.ClientTheme
import com.runtimeaware.sdk.ExistingSdk
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {
    private val runtimeAwareSdk = ExistingSdk(this)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Initialize the Payment SDK
        lifecycleScope.launch {
            if (!runtimeAwareSdk.initialize()) {
                makeToast("Failed to initialize SDK")
            } else {
                makeToast("Initialized SDK!")
            }
        }

        // Use an interface to provide abstraction between the SDK and the client
        val paymentProvider = PaymentProvider(runtimeAwareSdk, APP_DISPLAY_NAME, this)

        // Set up the UI
        setContent {
            ClientTheme {
                RestaurantMenuScreen(
                    APP_DISPLAY_NAME,
                    sampleMenuItems,
                    paymentProvider,
                    lifecycleScope
                )
            }
        }
    }

    private fun makeToast(message: String) {
        runOnUiThread { Toast.makeText(this@MainActivity, message, Toast.LENGTH_SHORT).show() }
    }

    companion object {
        private const val APP_DISPLAY_NAME = "Ron's Cafe"
    }
}
