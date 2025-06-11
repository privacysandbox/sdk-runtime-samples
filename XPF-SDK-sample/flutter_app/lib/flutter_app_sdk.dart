import 'package:flutter/widgets.dart';

import 'flutter_app_sdk_platform_interface.dart';

class FlutterAppSdk {
  Future<Widget> initializeSdk() {
    return FlutterAppSdkPlatform.instance.initializeSdk();
  }

  Future<Widget> createFile() {
    return FlutterAppSdkPlatform.instance.createFile();
  }

  Future<Widget> loadBannerAd() {
    return FlutterAppSdkPlatform.instance.loadBannerAd();
  }

  Future<void> showFullscreenAd() {
    return FlutterAppSdkPlatform.instance.showFullscreenAd();
  }
}
