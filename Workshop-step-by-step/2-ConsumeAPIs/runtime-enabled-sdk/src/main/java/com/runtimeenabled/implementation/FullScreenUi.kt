package com.runtimeenabled.implementation

import android.annotation.SuppressLint
import android.webkit.WebSettings

class FullScreenUi {
    //    override suspend fun showFullscreenUi(activityLauncher: SdkActivityLauncher) {
//        val webView = WebView(context)
//        initializeWebViewSettings(webView.settings)
//        webView.webViewClient = object : WebViewClient() {
//            override fun shouldOverrideUrlLoading(
//                view: WebView, request: WebResourceRequest
//            ): Boolean {
//                return false
//            }
//        }
//        webView.loadUrl("https://privacysandbox.google.com/")
//
//        val handler = object : SdkSandboxActivityHandlerCompat {
//            @RequiresApi(Build.VERSION_CODES.R)
//            override fun onActivityCreated(activityHolder: ActivityHolder) {
//                val activityHandler = ActivityHandler(activityHolder, webView)
//                activityHandler.buildLayout()
//
//                ViewCompat.setOnApplyWindowInsetsListener(
//                    activityHolder.getActivity().window.decorView) { view, windowInsets ->
//                    val insets = windowInsets.getInsets(WindowInsetsCompat.Type.systemBars())
//                    view.updatePadding(top = insets.top)
//                    WindowInsetsCompat.CONSUMED
//                }
//            }
//        }
//
//        val token = controller.registerSdkSandboxActivityHandler(handler)
//        val launched = activityLauncher.launchSdkActivity(token)
//        if (!launched) controller.unregisterSdkSandboxActivityHandler(handler)
//    }

    @SuppressLint("SetJavaScriptEnabled")
    private fun initializeWebViewSettings(settings: WebSettings) {
        settings.javaScriptEnabled = true
        settings.setGeolocationEnabled(true)
        settings.setSupportZoom(true)
        settings.databaseEnabled = true
        settings.domStorageEnabled = true
        settings.allowFileAccess = true
        settings.allowContentAccess = true
        settings.useWideViewPort = true
        settings.loadWithOverviewMode = true
        settings.layoutAlgorithm = WebSettings.LayoutAlgorithm.TEXT_AUTOSIZING
    }
}