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

import com.runtimeenabled.api.PaymentCallbackInterface

/**
 * Class that implements the interface declared by the runtime-enabled SDK.
 */
class PaymentCallback(private val callback: () -> Unit) : PaymentCallbackInterface {

    override suspend fun onPaymentComplete() {
        callback()
    }
}