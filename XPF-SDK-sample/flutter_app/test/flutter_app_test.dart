import 'package:flutter/src/widgets/framework.dart';
import 'package:flutter_app/flutter_app.dart';
import 'package:flutter_app/flutter_app_method_channel.dart';
import 'package:flutter_app/flutter_app_platform_interface.dart';
import 'package:flutter_test/flutter_test.dart';
import 'package:plugin_platform_interface/plugin_platform_interface.dart';

class MockFlutterAppPlatform
    with MockPlatformInterfaceMixin
    implements FlutterAppPlatform {
  @override
  Future<Widget> createFile() {
    // TODO: implement createFile
    throw UnimplementedError();
  }

  @override
  Future<Widget> initializeSdk() {
    // TODO: implement initializeSdk
    throw UnimplementedError();
  }

  @override
  Future<Widget> loadBannerAd() {
    // TODO: implement loadBannerAd
    throw UnimplementedError();
  }

  @override
  Future<void> showFullscreenAd() {
    // TODO: implement showFullscreenAd
    throw UnimplementedError();
  }
}

void main() {
  final FlutterAppPlatform initialPlatform = FlutterAppPlatform.instance;

  test('$MethodChannelFlutterApp is the default instance', () {
    expect(initialPlatform, isInstanceOf<MethodChannelFlutterApp>());
  });

  test('getPlatformVersion', () async {
    FlutterApp flutterAppPlugin = FlutterApp();
    MockFlutterAppPlatform fakePlatform = MockFlutterAppPlatform();
    FlutterAppPlatform.instance = fakePlatform;
  });
}
