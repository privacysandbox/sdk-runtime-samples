package com.example.flutter_app

import android.content.Context
import io.flutter.plugin.common.MethodChannel
import androidx.lifecycle.lifecycleScope
import com.runtimeaware.sdk.ExistingSdk
import io.flutter.plugin.common.MethodCall
import io.flutter.plugin.common.MethodChannel.MethodCallHandler
import kotlinx.coroutines.launch
import android.util.AttributeSet
import android.widget.TextView
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.ui.platform.ComposeView
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.LifecycleCoroutineScope
import com.example.flutter_app.FlutterAppPlugin
import com.runtimeaware.sdk.BannerAd
import com.runtimeaware.sdk.FullscreenAd
import io.flutter.embedding.android.FlutterFragmentActivity
import io.flutter.plugin.common.StandardMessageCodec
import io.flutter.plugin.platform.PlatformView
import io.flutter.plugin.platform.PlatformViewFactory
import kotlinx.coroutines.CoroutineScope
import java.util.concurrent.atomic.AtomicInteger
import kotlinx.coroutines.*

class FlutterApMethodChannelHandler(
    val runtimeAwareSdk: ExistingSdk,
    val context: Context,
    val activity: FragmentActivity,
    val linearLayouts: MutableMap<Int, LinearLayout>,
) :
    MethodCallHandler {

    val scope = CoroutineScope(Dispatchers.Main)
    val viewsId = AtomicInteger(0)

    override fun onMethodCall(call: MethodCall, result: MethodChannel.Result) {
        Log.d("FlutterApp", "activity $activity")

        when (call.method) {
            "initializeSdk" -> {
                Log.d("FlutterApp", "before initializeSdk")
                scope.launch {
                    try {
                        if (!runtimeAwareSdk.initialize()) {
                            result.success("Failed to initialize SDK")
                        } else {
                            result.success("Initialized SDK!")
                        }
                    } catch (e: Exception) {
                        result.success("Error caught: $e")
                    }
                }
            }

            "createFile" -> {
                scope.launch {
                    val success = runtimeAwareSdk.createFile(3)

                    if (success == null) {
                        result.success("Please load the SDK first!")
                    } else {
                        result.success(success)
                    }

                }
            }

            "loadBannerAd" -> {
                scope.launch {
                    try {
                        val bannerAd = BannerAd(context)

                        var linearLayout = createLinearLayout(context)
                        linearLayouts[linearLayout.id] = linearLayout
                        linearLayout.addView(bannerAd)

                        bannerAd.loadAd(
                            activity,
                            "com.example.flutter_app",
                            shouldStartActivityPredicate(),
                            false,
                            "NONE"
                        )

                        result.success(linearLayout.id)
                    } catch (e: Exception) {
                        result.error("loadBannerAd_error", "Error caught: $e", null)
                    }
                }
            }

            "showFullscreenAd" -> {
                scope.launch {
                    try {
                        val fullscreenAd = FullscreenAd.create(context, "NONE")
                        fullscreenAd.show(activity, shouldStartActivityPredicate())
                    } catch (e: Exception) {
                        result.error("showFullscreenAd_error", "Error caught: $e", null)
                    }
                }
            }

            else -> result.notImplemented()
        }
    }

    private fun createLinearLayout(context: Context): LinearLayout {
        val linearLayout = LinearLayout(context)

        // Set layout orientation (e.g., vertical or horizontal)
        linearLayout.orientation = LinearLayout.VERTICAL // Or LinearLayout.HORIZONTAL

        // Set layout width and height
        linearLayout.layoutParams = LinearLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.MATCH_PARENT
        )

        //  Optional: Add some styling (example)
        linearLayout.setBackgroundColor(android.graphics.Color.GREEN)
        linearLayout.setPadding(16, 16, 16, 16) // Example padding
        linearLayout.id = viewsId.incrementAndGet()

        return linearLayout
    }

    private fun shouldStartActivityPredicate(): () -> Boolean {
        return { true }
    }
}