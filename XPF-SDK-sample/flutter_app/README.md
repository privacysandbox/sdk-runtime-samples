# Introducing the SDK: A Flutter Integration Example

This project provides a clear and concise example of how to integrate the SDK into your Flutter applications using a Flutter plugin.

## Getting Started: Preparing Your Development Environment
To have this project configured properly in Android Studio you have to have the Flutter installed on the machine you're opening the project in (either virtual or stationary).
Once you've cloned the repository, follow these initial steps to configure your Android Studio environment:
1.  **Navigate to the Example Directory:** Open your terminal and change the current directory to the example project within `flutter_app`:

    ```bash
    cd flutter_app/example
    ```

2.  **Configure Project Dependencies:** Run the following Flutter command to ensure all necessary configurations are set up for Android Studio:

    ```bash
    flutter build apk --config-only
    ```

    This command prepares the Android project structure. Please allow some time for the configuration to complete.

3.  **Open in Android Studio:** For the best development experience, opening the `example/android/build.gradle.kts` file in Android Studio is recommended. This will allow Android Studio to properly recognize and configure the Android-specific parts of the project.

## Running the example app
Currently, directly building an App Bundle (`flutter build aab`) for this example with the SDK isn't fully configured with bundletool.
To run the example app and ensure the SDK is correctly initialized, please follow these steps:

1.  **Build the Flutter App in Debug Mode:** Create a debuggable App Bundle using the following command:

    ```bash
    flutter build aab --debug
    ```

2.  **Build SDK-Specific APKs:** Utilize `bundletool` to build APKs specifically for each SDK bundle. Make sure you have `bundletool` installed and accessible in your environment. Execute the following commands:

    ```bash
    bundletool build-sdk-apks --sdk-bundle=../android/runtime-enabled-sdk-bundle/build/outputs/asb/single/runtime-enabled-sdk-bundle.asb --output=reSdkBundle.apks
    bundletool build-sdk-apks --sdk-bundle=../android/mediatee-sdk-adapter-bundle/build/outputs/asb/single/mediatee-sdk-adapter-bundle.asb --output=mediateeSdkAdapterBundle.apks
    bundletool build-sdk-apks --sdk-bundle=../android/mediatee-sdk-bundle/build/outputs/asb/single/mediatee-sdk-bundle.asb --output=mediateeSdkBundle.apks
    ```

3.  **Build the App APKs Including the SDK Bundles:** Now, build the main application APKs, referencing the SDK bundles you just created:

    ```bash
    bundletool build-apks --bundle=build/app/outputs/bundle/debug/app-debug.aab --sdk-bundles=../android/runtime-enabled-sdk-bundle/build/outputs/asb/single/runtime-enabled-sdk-bundle.asb,../android/mediatee-sdk-bundle/build/outputs/asb/single/mediatee-sdk-bundle.asb,../android/mediatee-sdk-adapter-bundle/build/outputs/asb/single/mediatee-sdk-adapter-bundle.asb --output=RuntimeAppDebug.apks
    ```

4.  **Install the SDK APKs on Your Device/Emulator:** Install the SDK-specific APKs onto your connected device or emulator:

    ```bash
    bundletool install-apks --apks=reSdkBundle.apks
    bundletool install-apks --apks=mediateeSdkBundle.apks
    bundletool install-apks --apks=mediateeSdkAdapterBundle.apks
    ```

5.  **Install the Main App APK on Your Device/Emulator:** Finally, install the main application APK that now includes the SDK:

    ```bash
    bundletool install-apks --apks=RuntimeAppDebug.apks
    ```
After completing these steps, the example app should be running on your device or emulator with the SDK successfully initialized.

## Common Problems and Solutions
You might encounter some issues during the setup. Here's a quick guide to troubleshoot them:

* **Missing `.asb` Files for SDK Bundles:** If you find that the `.asb` (Android SDK Bundle) files are missing in the specified output directories (e.g., `../android/runtime-enabled-sdk-bundle/build/outputs/asb/single/`), you'll need to instruct Android Studio to build these modules. To do this:
    1.  Open your project in Android Studio.
    2.  Navigate to the **Build** menu in the top toolbar.
    3.  Select **Make Module** followed by the name of the missing module (e.g., `runtime-enabled-sdk-bundle`). Repeat this for each missing SDK module (`mediatee-sdk-adapter-bundle` and `mediatee-sdk-bundle`).
    4.  After the build process completes successfully, the `.asb` files should be generated in their respective output directories. You can then proceed with step 2 under "Running the Example Application with the SDK".


