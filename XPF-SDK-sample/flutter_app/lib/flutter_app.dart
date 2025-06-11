import 'package:flutter/widgets.dart';

import 'flutter_app_platform_interface.dart';

class FlutterApp {
  Future<Widget> initializeSdk() {
    return FlutterAppPlatform.instance.initializeSdk();
  }

  Future<Widget> createFile() {
    return FlutterAppPlatform.instance.createFile();
  }

  Future<Widget> loadBannerAd() {
    return FlutterAppPlatform.instance.loadBannerAd();
  }

  Future<void> showFullscreenAd() {
    return FlutterAppPlatform.instance.showFullscreenAd();
  }
}
