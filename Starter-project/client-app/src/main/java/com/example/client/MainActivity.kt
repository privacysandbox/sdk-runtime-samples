/*
 * Copyright (C) 2025 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
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
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.lifecycle.lifecycleScope
import com.runtimeaware.sdk.RuntimeAwareSdk
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {
    private val runtimeAwareSdk = RuntimeAwareSdk(this)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        lifecycleScope.launch {
            if (!runtimeAwareSdk.initialize()) {
                makeToast("Failed to initialize SDK")
            } else {
                makeToast("Initialized SDK!")
            }
        }

        setContent {
            MaterialTheme {
                MainScreen(
                    onShowSdkUi = {
                        lifecycleScope.launch {
                            val sdkUiAdapter = runtimeAwareSdk.getSandboxedUiAdapter(
                                "Hello from the Client App!",
                                onSdkSuccess = { makeToast("SDK UI flow complete!") },
                                context = this@MainActivity
                            )

                            // Show the SDK UI in a dialog
                            showDialogState.value = sdkUiAdapter
                        }
                    }
                )
            }
        }
    }

    private fun makeToast(message: String) {
        runOnUiThread { Toast.makeText(this@MainActivity, message, Toast.LENGTH_SHORT).show() }
    }
}

@Composable
fun MainScreen(onShowSdkUi: () -> Unit) {
    Scaffold(
        topBar = {
            TopAppBar(title = { Text("Client App") })
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Button(onClick = onShowSdkUi) {
                Text("Show SDK UI")
            }
        }
    }
}