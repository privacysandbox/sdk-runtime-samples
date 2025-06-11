import 'package:plugin_platform_interface/plugin_platform_interface.dart';
import 'package:flutter/widgets.dart';
import 'flutter_app_method_channel.dart';

abstract class FlutterAppPlatform extends PlatformInterface {
  /// Constructs a FlutterAppPlatform.
  FlutterAppPlatform() : super(token: _token);

  static final Object _token = Object();

  static FlutterAppPlatform _instance = MethodChannelFlutterApp();

  /// The default instance of [FlutterAppPlatform] to use.
  ///
  /// Defaults to [MethodChannelFlutterApp].
  static FlutterAppPlatform get instance => _instance;

  /// Platform-specific implementations should set this with their own
  /// platform-specific class that extends [FlutterAppPlatform] when
  /// they register themselves.
  static set instance(FlutterAppPlatform instance) {
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
