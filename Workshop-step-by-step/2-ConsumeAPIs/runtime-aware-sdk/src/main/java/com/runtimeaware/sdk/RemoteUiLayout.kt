package com.runtimeaware.sdk

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.widget.LinearLayout
import android.widget.Toast
import androidx.privacysandbox.ui.client.view.SandboxedSdkView
import com.runtimeaware.sdk.ReSdkLoader.isSdkLoaded
import com.runtimeaware.sdk.ReSdkLoader.getSdkService
import com.runtimeenabled.api.RemoteUiCallbackInterface
import com.runtimeenabled.api.RemoteUiRequest

class RemoteUiLayout(context: Context, attrs: AttributeSet?) : LinearLayout(context, attrs) {

    constructor(context: Context) : this(context, null)

    suspend fun presentUiFromMyReSdk(message: String, onSuccess: () -> Unit) {
       TODO("Create an SSV, obtain the adapter, and set the adapter")
    }

    fun clearUi() {
        removeAllViews()
    }

    private fun addViewToLayout(view: View) {
        removeAllViews()
        view.layoutParams = LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT)
        super.addView(view)
    }
}