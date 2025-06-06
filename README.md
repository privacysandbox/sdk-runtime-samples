# SDK Runtime Sample App

This app provides an example of how to use
the [SDK Runtime](https://privacysandbox.google.com/private-advertising/sdk-runtime), an Android
feature that allows third-party SDKs to run in isolation from the app process, providing
stronger safeguards for user data.

![Diagram of client app and SDK integration](/assets/screens.svg)

> This image demonstrates the client app ("Ron's Cafe") invoking a runtime-enabled payment SDK
> ("ZenithPay"). The "Pay Now" action transitions to the payment SDK UI, which is loaded and
> executed within the SDK Runtime. All information input into the payment SDK UI is sandboxed,
> enhancing user data privacy.

The demo simulates an SDK that provides a payment processing UI. It's structured to specifically
illustrate how an existing SDK can
be [migrated in phases](https://privacysandbox.google.com/private-advertising/sdk-runtime/developer-guide/key-concepts#migrate_existing_sdks):

* Core SDK functionality has been moved to a **runtime-enabled** component.
* A **runtime-aware** component still exists, primarily acting as a wrapper for the new
  runtime-enabled functionality.

![Diagram of module interaction](/assets/diagram.svg)

> This diagram illustrates module interaction during a phased SDK Runtime code migration. Arrows
> show the `client-app` communicating with the `runtime-aware-sdk` (both in the app's runtime). The
> `runtime-aware-sdk` then bridges to the `runtime-enabled-sdk`, which operates within the separate
> SDK Runtime sandbox, isolating its process and data.

This setup allows client app developers to continue using the SDK with minimal changes while the SDK
provider gradually transitions to the full SDK Runtime environment. The next logical step in this
migration would be to eventually remove the runtime-aware wrapper, having client applications
directly interface with the runtime-enabled SDK.

## Purpose

This demo aims to illustrate:

* The structure of a project utilizing the SDK Runtime.
* How an SDK can be split into runtime-aware and runtime-enabled parts during a phased migration.
* The interaction between a client application and an SDK undergoing this transition.
* The typical module setup for such a scenario.

## Modules

The project is divided into the following key modules:

### 1. `client-app`

* **Description:** This is the main Android application module that acts as the client consuming the
  SDK.
* **Responsibilities:**
    * Demonstrates how an app integrates with an SDK that is partially migrated to the SDK Runtime.
    * Initializes and interacts with the `runtime-aware-sdk` module.
    * Displays a simple UI (e.g., a list of items to "purchase") and triggers the payment SDK when
      an action is performed.
* **Key Learnings:** Shows how the app-to-SDK interaction might look during the transitional phase.

### 2. `runtime-aware-sdk` (Legacy/Wrapper SDK)

* **Description:** This module represents the traditional part of the SDK that the `client-app`
  directly depends on. In this demo, it primarily acts as a wrapper.
* **Responsibilities:**
    * Provides the public API surface that the `client-app` interacts with.
    * Internally, it handles the logic to load and communicate with the `runtime-enabled-sdk` if the
      SDK Runtime environment is available.
    * It might contain some not yet migrated SDK logic, but in this specific demo, its main role is
      to delegate calls to the `runtime-enabled-sdk`.
* **Key Learnings:** Illustrates how an existing SDK can start adopting the SDK Runtime without
  breaking changes for its consumers. It acts as a bridge to the newer, sandboxed functionality.

### 3. `runtime-enabled-sdk` (Migrated SDK)

* **Description:** This module contains the core functionality of the SDK that is designed to run in
  the sandboxed SDK Runtime environment.
* **Responsibilities:**
    * Implements the actual payment processing UI and logic.
    * Defines the interface for how it communicates with the `runtime-aware-sdk` (or directly with
      the client app in a future migration phase).
    * This module's code is packaged into the `runtime-enabled-sdk-bundle`.
* **Key Learnings:** Shows where the sandboxed code of your SDK resides. This is the part that
  benefits from the enhanced privacy and security of the SDK Runtime.

### 4. `runtime-enabled-sdk-bundle`

* **Description:** This is
  an [Android SDK Bundle (ASB)](https://developer.android.com/studio/command-line/bundletool#asb-format)
  module. Its primary purpose is to package the `runtime-enabled-sdk` into the format required for
  publishing to an app store and getting loaded by the SDK Runtime.
* **Responsibilities:**
    * Builds the `runtime-enabled-sdk` into an ASB.
    * This ASB is what would be distributed to app developers via an app store for inclusion in
      their apps. The `client-app` would then declare a dependency on this bundle.
* **Key Learnings:** Demonstrates the packaging mechanism for the sandboxed part of your SDK.

## Workflow Demonstrated

This is the order of events after a user adds items to their cart and presses the "Pay Now" button:

1. The `client-app` makes a call to a function in the `runtime-aware-sdk`.
2. The `runtime-aware-sdk`, acting as a wrapper, checks for the SDK Runtime environment.
3. It then loads the `runtime-enabled-sdk` (via the `runtime-enabled-sdk-bundle`) into the SDK
   Runtime.
4. The `runtime-aware-sdk` delegates the call to the loaded `runtime-enabled-sdk`.
5. The `runtime-enabled-sdk` executes its core logic (e.g., displaying the payment UI and handling
   payment processing) within the sandboxed environment.
6. Results or callbacks are passed back from the `runtime-enabled-sdk` to the `runtime-aware-sdk`,
   and subsequently to the `client-app`.

## Future Steps (Beyond this Demo)

* **Complete Migration:** Migrate any remaining SDK functionality from the `runtime-aware-sdk` to
  the `runtime-enabled-sdk`. Remove the `runtime-aware-sdk` module.
* **Direct Interaction:** The `client-app` would then directly interact with an SDK that is fully
  runtime-enabled, typically by depending directly on the `runtime-enabled-sdk-bundle` and using
  APIs provided by the SDK Runtime framework to load and communicate with it.

## Build and Run

The following section explains how to prepare your environment to launch the sample app, and debug code that executes in the SDK Runtime.

### Set up your dev environment

Make sure you have upgraded to the latest version of Android Studio.

- Help menu > Find action > Type "Check for updates"

Depending on the Android version of your device or emulator, the sample Runtime-Enabled SDKs will either run on the SDK Runtime process, or statically linked to the app in backward compatible mode, transparently.

To get SDKs to run in the SDK Runtime, you'll need to be on Android 14 or higher.

#### Enable the SDK Runtime on a physical device

While the SDK Runtime is available in all GMS devices with Android 14 or higher, it's behind a configuration flag.

To enable it, you can run the following commands:

```shell
adb shell device_config put adservices global_kill_switch false
adb shell device_config put adservices disable_sdk_sandbox false
```

This isn't necessary on emulators.

### Launch and use the client app

1. Clone this repository.
2. Open the project in Android Studio.
3. Ensure your emulator or physical device are set up and running.
4. Select the `client-app` configuration and run it.

## Debug the sample app

### Debug the non runtime-enabled code

The non runtime-enabled code is code that does not run in the SDK Runtime sandbox. This code
includes the `client-app` and the `runtime-aware-sdk`. To debug this code:

- In Android Studio, set breakpoints in the non runtime-enabled code.
- Press the Debug button. This will launch the client app and attach a debugger to it.

### Debug the runtime-enabled code

The runtime-enabled code is code that does run in the SDK Runtime sandbox. This code includes the
`runtime-enabled-sdk`. To debug this code:

- In Android Studio, set breakpoints in the runtime-enabled code.
- Launch the client app.
- In the client app, add items to your cart and click the Pay Now button. This will start the SDK
  Runtime process.
- Click the Run menu > Attach debugger to Android Process.
- Choose com.example.privacysandbox.client_sdk_sandbox and click OK.
- Once again, add items to your cart and click the Pay Now button to trigger the runtime-enabled
  code.

### Debug initialization of the runtime-enable code

In order to debug the initialization methods in the runtime-enabled code, you will have to start the
SDK Runtime process manually. To do this:

- In Android Studio, set breakpoints in the runtime-enabled code initialization method.
- Launch the client app.
- Enter these commands in the terminal to start the SDK Runtime process:
    - `adb shell cmd deviceidle tempwhitelist com.example.privacysandbox.client`
    - `adb shell cmd sdk_sandbox start com.example.privacysandbox.client`
- Click the Run menu > Attach debugger to Android Process.
- Choose com.example.privacysandbox.client_sdk_sandbox and click OK.
- In the client app, add items to your cart and click the Pay Now button to start the SDK Runtime
  process and trigger the initialization methods.