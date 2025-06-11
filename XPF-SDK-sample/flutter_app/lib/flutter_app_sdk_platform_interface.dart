import 'package:flutter/widgets.dart';
import 'package:plugin_platform_interface/plugin_platform_interface.dart';

import 'flutter_app_sdk_method_channel.dart';

abstract class FlutterAppSdkPlatform extends PlatformInterface {
  /// Constructs a FlutterAppSdkPlatform.
  FlutterAppSdkPlatform() : super(token: _token);

  static final Object _token = Object();

  static FlutterAppSdkPlatform _instance = MethodChannelFlutterAppSdk();

  /// The default instance of [FlutterAppSdkPlatform] to use.
  ///
  /// Defaults to [MethodChannelFlutterAppSdk].
  static FlutterAppSdkPlatform get instance => _instance;

  /// Platform-specific implementations should set this with their own
  /// platform-specific class that extends [FlutterAppSdkPlatform] when
  /// they register themselves.
  static set instance(FlutterAppSdkPlatform instance) {
    PlatformInterface.verifyToken(instance, _token);
    _instance = instance;
  }

  Future<Widget> initializeSdk() {
    throw UnimplementedError('initializeSdk() has not been implemented.');
  }

  Future<Widget> createFile() {
    throw UnimplementedError('createFile() has not been implemented.');
  }

  Future<Widget> loadBannerAd() {
    throw UnimplementedError('loadBannerAd() has not been implemented.');
  }

  Future<void> showFullscreenAd() {
    throw UnimplementedError('loadBannerAd() has not been implemented.');
  }
}
