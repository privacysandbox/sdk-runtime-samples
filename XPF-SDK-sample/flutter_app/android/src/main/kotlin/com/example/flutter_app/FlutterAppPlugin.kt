package com.example.flutter_app


import android.app.Activity
import android.content.Context
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.material.Text
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.unit.dp
import com.example.flutter_app.ComposeViewFactory
import com.runtimeaware.sdk.BannerAd
import com.runtimeaware.sdk.FullscreenAd
import com.runtimeaware.sdk.ExistingSdk
import io.flutter.embedding.android.FlutterFragmentActivity
import io.flutter.embedding.engine.FlutterEngine
import io.flutter.plugin.common.MethodChannel
import io.flutter.plugin.common.StandardMessageCodec
import io.flutter.plugin.platform.PlatformView
import io.flutter.plugin.platform.PlatformViewFactory
import io.flutter.embedding.engine.plugins.FlutterPlugin
import io.flutter.embedding.engine.plugins.activity.ActivityAware
import io.flutter.embedding.engine.plugins.activity.ActivityPluginBinding
import java.util.concurrent.atomic.AtomicInteger

/** FlutterAppPlugin */
class FlutterAppPlugin: FlutterPlugin, ActivityAware {
  val composeViews = mutableMapOf<Int, ComposeView>()
  val linearLayouts = mutableMapOf<Int, LinearLayout>()
  private lateinit var methodChannel: MethodChannel
  private lateinit var runtimeAwareSdk: ExistingSdk
  private var activity: FlutterFragmentActivity? = null
  private lateinit var context: Context

  override fun onAttachedToEngine(binding: FlutterPlugin.FlutterPluginBinding) {
    Log.d("FlutterAppPlugin", "onAttachedToEngine")

    runtimeAwareSdk = ExistingSdk(binding.applicationContext)

    binding.platformViewRegistry.registerViewFactory(
      "compose-view",
      ComposeViewFactory(composeViews)
    )
    binding.platformViewRegistry.registerViewFactory(
      "linear-layout-view", LinearLayoutViewFactory(linearLayouts)
    )

    methodChannel = MethodChannel(
      binding.binaryMessenger, CHANNEL,
    )
    context = binding.applicationContext
  }

  override fun onDetachedFromEngine(binding: FlutterPlugin.FlutterPluginBinding) {
    Log.d("FlutterAppPlugin", "onDetachedFromEngine")
    methodChannel.setMethodCallHandler(null)
    activity = null
  }

  override fun onAttachedToActivity(binding: ActivityPluginBinding) {
    Log.d("FlutterAppPlugin", "onAttachedToActivity, Activity Class: ${binding.activity.javaClass.name}")
    activity = binding.activity as? FlutterFragmentActivity
    if (activity == null) {
      Log.e("FlutterAppPlugin", "Error: Could not cast activity to AppCompatActivity")
      // Optionally, you could try to defer handler creation or signal an error
      return
    } else {
      Log.d("FlutterAppPlugin", "Activity is AppCompatActivity")
    }

    val methodChannelHandler = MyFancySdkMethodChannelHandler(
      runtimeAwareSdk,
      context,
      activity!!,
      linearLayouts,
    )
    methodChannel.setMethodCallHandler(methodChannelHandler)
  }

  override fun onDetachedFromActivityForConfigChanges() {
    Log.d("FlutterAppPlugin", "onDetachedFromActivityForConfigChanges")
    activity = null
  }

  override fun onReattachedToActivityForConfigChanges(binding: ActivityPluginBinding) {
    activity = binding.activity as? FlutterFragmentActivity
    Log.d("FlutterAppPlugin", "Warning: Re-attached activity is not an AppCompatActivity")
  }

  override fun onDetachedFromActivity() {
    Log.d("FlutterAppPlugin", "onDetachedFromActivity")
    activity = null
  }

  companion object {
    private const val CHANNEL = "flutter_app"
  }
}

class ComposeViewFactory(private val composeViews: Map<Int, ComposeView>) :
  PlatformViewFactory(StandardMessageCodec.INSTANCE) {
  override fun create(context: Context, viewId: Int, args: Any?): PlatformView {
    val id = (args as Map<String, Any?>)["id"] as Int
    val composeView = composeViews[id]
      ?: throw IllegalArgumentException("No ComposeView found with id: $id")

    return ComposeViewAndroid(composeView)
  }
}

class ComposeViewAndroid(private val composeView: ComposeView) : PlatformView {
  override fun getView(): View = composeView

  override fun dispose() {}

}


class LinearLayoutViewFactory(private val linearLayouts: Map<Int, LinearLayout>) :
  PlatformViewFactory(StandardMessageCodec.INSTANCE) {
  override fun create(context: Context, viewId: Int, args: Any?): PlatformView {
    val id = (args as Map<String, Any?>)["id"] as Int
    val linearLayout = linearLayouts[id]
      ?: throw IllegalArgumentException("No LinearLayout found with id: $id")
    return LinearLayoutViewAndroid(linearLayout)
  }
}

class LinearLayoutViewAndroid(private val linearLayout: LinearLayout) : PlatformView {
  override fun getView(): View = linearLayout

  override fun dispose() {}
}