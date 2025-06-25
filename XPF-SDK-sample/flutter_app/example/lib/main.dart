import 'package:flutter/material.dart';
import 'package:flutter_app/flutter_app_sdk.dart';

void main() {
  runApp(const MyApp());
}

class MyApp extends StatefulWidget {
  const MyApp({super.key});

  @override
  State<MyApp> createState() => _MyAppState();
}

class _MyAppState extends State<MyApp> {
  Widget _initializedSdkWidget = Text("");
  Widget _sdkWidget = SizedBox.shrink();

  final _flutterAppSdkPlugin = FlutterAppSdk();

  @override
  Widget build(BuildContext context) {
    return MaterialApp(
      home: Scaffold(
        appBar: AppBar(title: const Text('Plugin example app')),
        body: Center(
          child: Column(
            mainAxisSize: MainAxisSize.min,
            children: [
              _initializedSdkWidget,
              ElevatedButton(
                onPressed: () async {
                  final newSdkWidget =
                      await _flutterAppSdkPlugin.initializeSdk();

                  setState(() {
                    _initializedSdkWidget = newSdkWidget;
                  });
                },
                child: Text('Initialize SDK'),
              ),
              SizedBox(height: 20),
              ElevatedButton(
                onPressed: () async {
                  final newSdkWidget = await _flutterAppSdkPlugin.createFile();
                  setState(() {
                    _sdkWidget = newSdkWidget;
                  });
                },
                child: Text('Create file'),
              ),
              SizedBox(height: 20),
              ElevatedButton(
                onPressed: () async {
                  final newSdkWidget =
                      await _flutterAppSdkPlugin.loadBannerAd();
                  setState(() {
                    _sdkWidget = newSdkWidget;
                  });
                },
                child: Text('Load banner ad'),
              ),
              SizedBox(height: 20),
              ElevatedButton(
                onPressed: () async {
                  await _flutterAppSdkPlugin.showFullscreenAd();
                },
                child: Text('Show fullscreen ad'),
              ),
              SizedBox(height: 20),
              _sdkWidget,
            ],
          ),
        ),
      ),
    );
  }
}
