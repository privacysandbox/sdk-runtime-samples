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
        //This should never happen, because the button to call this function is disabled until the SDK is loaded
        if (!isSdkLoaded()) return

        //Create the SandboxedSdkView to encapsulate the UI
        val sandboxedSdkView = SandboxedSdkView(context)

        //Create the request
        val request = RemoteUiRequest(message)
        val callback = object : RemoteUiCallbackInterface {
            override suspend fun onDoSomething() {
                Toast.makeText(context, "Something was done", Toast.LENGTH_SHORT).show()
                onSuccess()
            }
        }

        val adapter = getSdkService(context)?.getRemoteUiAdapter(
            request,
            callback
        )

        addViewToLayout(sandboxedSdkView)
        //Set the SSV's adapter to the obtained one
        sandboxedSdkView.setAdapter(adapter)
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