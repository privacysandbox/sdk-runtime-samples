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

import android.content.Context
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.privacysandbox.ui.client.view.SandboxedSdkUi
import androidx.privacysandbox.ui.core.SandboxedUiAdapter
import com.runtimeaware.sdk.ExistingSdk

class PaymentProvider(
    private val runtimeAwareSdk: ExistingSdk,
    private val appDisplayName: String,
    private val context: Context
) : PaymentProviderInterface {

    private lateinit var paymentSdkAdapter: SandboxedUiAdapter

    override suspend fun initialize(
        totalAmount: Double,
        onPaymentSuccess: () -> Unit
    ) {
        paymentSdkAdapter = runtimeAwareSdk.getSandboxedUiAdapter(
            appDisplayName,
            totalAmount,
            onPaymentSuccess,
            context
        )
    }

    @Composable
    override fun PaymentUi() {
        SandboxedSdkUi(
            sandboxedUiAdapter = paymentSdkAdapter,
            providerUiOnTop = true,
            modifier = Modifier.height(520.dp)
        )
    }
}