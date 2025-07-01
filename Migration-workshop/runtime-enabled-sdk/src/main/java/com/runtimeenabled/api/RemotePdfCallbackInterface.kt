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
package com.runtimeenabled.api

import androidx.privacysandbox.tools.PrivacySandboxCallback

/**
 * Interface to be implemented by runtime-aware SDK.
 *
 * Runtime-aware SDK will create an object that implements this interface and pass it to the
 * runtime-enabled SDK.
 *
 * This interface will then be used by the runtime-enabled SDK to communicate with the client app.
 */
@PrivacySandboxCallback
interface RemotePdfCallbackInterface {
    suspend fun onPdfSuccess()
    suspend fun onPdfError()
    suspend fun onPdfSaved()
}