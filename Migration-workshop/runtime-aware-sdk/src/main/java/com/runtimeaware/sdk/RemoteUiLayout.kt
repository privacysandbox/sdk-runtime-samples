package com.runtimeaware.sdk

import android.R
import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.widget.LinearLayout
import android.widget.Toast
import androidx.privacysandbox.ui.client.view.SandboxedSdkView
import com.runtimeaware.sdk.RuntimeAwareSdk.Loader.isSdkLoaded
import com.runtimeaware.sdk.RuntimeAwareSdk.Loader.loadSdkIfNeeded
import com.runtimeenabled.api.RemoteUiCallbackInterface
import com.runtimeenabled.api.RemoteUiRequest

class RemoteUiLayout(context: Context, attrs: AttributeSet?) : LinearLayout(context, attrs) {

    constructor(context: Context) : this(context, null) // Allow programmatic creation

    suspend fun presentUiFromMyReSdk(message: String, onSuccess: () -> Unit) {
        if (!isSdkLoaded()) return

        val sandboxedSdkView = SandboxedSdkView(context)

        //Create the request
        val request = RemoteUiRequest(message)
        val callback = object : RemoteUiCallbackInterface {
            override suspend fun onDoSomething() {
                Toast.makeText(context, "Something was done", Toast.LENGTH_SHORT).show()
                onSuccess
            }
        }
        //Fetch the UI adapter from the SDK.
        val sdk = loadSdkIfNeeded(context)
            ?: throw IllegalStateException("SDK could not be loaded")
        val adapter = sdk.getRemoteUiAdapter(
            request,
            callback
        )

        //Create the SandboxedSdkView to encapsulate the UI
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