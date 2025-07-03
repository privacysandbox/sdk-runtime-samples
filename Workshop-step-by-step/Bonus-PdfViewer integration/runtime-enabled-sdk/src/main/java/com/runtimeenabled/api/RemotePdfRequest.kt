package com.runtimeenabled.api

import androidx.privacysandbox.tools.PrivacySandboxValue

@PrivacySandboxValue
data class RemotePdfRequest(
    val url: String,
    val title: String?,
    val saveTo: String?,
    val enableDownload: Boolean?,
    val password: String?
)