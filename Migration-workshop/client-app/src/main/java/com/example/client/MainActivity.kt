package com.example.client

import android.os.Bundle
import android.widget.Toast
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Done
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.lifecycleScope
import com.runtimeaware.sdk.RemoteUiLayout // Import RemoteUiLayout
import com.runtimeaware.sdk.RuntimeAwareSdk
import kotlinx.coroutines.launch

// SDK Loading State
enum class SdkLoadStatus {
    NOT_LOADED,
    LOADING,
    LOADED,
    FAILED
}

// State to control the visibility of the dialog containing RemoteUiLayout
var showSdkDialogState = mutableStateOf(false)
var showSdkPdfViewState = mutableStateOf(false)

// To hold a reference to RemoteUiLayout for calling its methods
var remoteUiLayoutRef: RemoteUiLayout? = null

class MainActivity : AppCompatActivity() {
    private val runtimeAwareSdk by lazy { RuntimeAwareSdk(applicationContext) }
    private var sdkLoadStatusState = mutableStateOf(SdkLoadStatus.NOT_LOADED)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val defaultFileSize = "10" // Default file size in Mb

        // Optionally, you might want to attempt initial load here or let the user do it.
        // For this example, we'll let the user initiate the first load via button.

        runtimeAwareSdk.setOnSandboxDeathCallback {
            sdkLoadStatusState.value = SdkLoadStatus.FAILED
        }

        setContent {
            MaterialTheme {
                MainScreen(
                    sdkLoadStatus = sdkLoadStatusState.value,
                    initialFileSize = defaultFileSize,
                    onLoadSdk = {
                        if (sdkLoadStatusState.value == SdkLoadStatus.LOADING
                            || sdkLoadStatusState.value == SdkLoadStatus.LOADED) {
                            makeToast("SDK is already loading or loaded.")
                            return@MainScreen
                        }
                        sdkLoadStatusState.value = SdkLoadStatus.LOADING
                        lifecycleScope.launch {
                            if (runtimeAwareSdk.initialize()) {
                                sdkLoadStatusState.value = SdkLoadStatus.LOADED
                                makeToast("SDK Initialized Successfully!")
                            } else {
                                sdkLoadStatusState.value = SdkLoadStatus.FAILED
                                makeToast("Failed to initialize SDK")
                            }
                        }
                    },
                    onShowSdkUi = {
                        if (sdkLoadStatusState.value == SdkLoadStatus.LOADED) {
                            showSdkDialogState.value = true
                        } else {
                            makeToast("SDK not loaded. Please load the SDK first.")
                        }
                    },
                    onShowSdkPdfView = {
                        if (sdkLoadStatusState.value == SdkLoadStatus.LOADED) {
                            showSdkPdfViewState.value = true
                        } else {
                            makeToast("SDK not loaded. Please load the SDK first.")
                        }
                    },
                    onCreateFileFromSdk = { fileSizeString ->
                        if (sdkLoadStatusState.value == SdkLoadStatus.LOADED) {
                            lifecycleScope.launch {
                                val fileSize = fileSizeString.toLongOrNull()
                                if (fileSize == null || fileSize <= 0) {
                                    makeToast("Invalid file size.")
                                    return@launch
                                }
                                val fileResult = runtimeAwareSdk.createFile(fileSize)
                                if (fileResult.first) {
                                    makeToast("Created file of size $fileSize Mb in the SDK's sandbox!")
                                } else {
                                    makeToast(fileResult.second!!)
                                }
                            }
                        } else {
                            makeToast("SDK not loaded. Please load the SDK first.")
                        }
                    },
                    onTriggerProcessDeath = {
                        if (sdkLoadStatusState.value == SdkLoadStatus.LOADED) {
                            lifecycleScope.launch {
                                runtimeAwareSdk.triggerProcessDeath()
                            }
                        } else {
                            makeToast("SDK not loaded. Please load the SDK first.")
                        }
                    }
                )

                if (showSdkDialogState.value) {
                    SdkUiDialog(
                        onDismiss = {
                            showSdkDialogState.value = false
                            remoteUiLayoutRef?.clearUi()
                            remoteUiLayoutRef = null
                        }
                    )
                }
                if (showSdkPdfViewState.value) {
                    SdkPdfView(
                        onDismiss = {
                            showSdkPdfViewState.value = false
                            remoteUiLayoutRef?.clearUi()
                            remoteUiLayoutRef = null
                        }
                    )
                }
            }
        }
    }

    private fun makeToast(message: String) {
        runOnUiThread { Toast.makeText(this@MainActivity, message, Toast.LENGTH_SHORT).show() }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    sdkLoadStatus: SdkLoadStatus,
    initialFileSize: String,
    onLoadSdk: () -> Unit,
    onShowSdkUi: () -> Unit,
    onShowSdkPdfView: () -> Unit,
    onCreateFileFromSdk: (String) -> Unit,
    onTriggerProcessDeath: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Client App - SDK Demo") },
                actions = { SdkStatusIndicator(sdkLoadStatus) }
            )
        }
    ) { padding ->
        var fileSizeInput by rememberSaveable { mutableStateOf(initialFileSize) }
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Button(
                onClick = onLoadSdk,
                enabled = sdkLoadStatus != SdkLoadStatus.LOADING && sdkLoadStatus != SdkLoadStatus.LOADED // Disable if loading or loaded
            ) {
                Text(
                    when (sdkLoadStatus) {
                        SdkLoadStatus.NOT_LOADED -> "Load SDK"
                        SdkLoadStatus.LOADING -> "Loading SDK..."
                        SdkLoadStatus.LOADED -> "SDK Loaded"
                        SdkLoadStatus.FAILED -> "Retry Load SDK"
                    }
                )
            }
            Spacer(modifier = Modifier.height(16.dp))
            Button(
                onClick = onShowSdkUi,
                enabled = sdkLoadStatus == SdkLoadStatus.LOADED
            ) {
                Text("Show a message in SDK-owned UI (using a RemoteUiLayout)")
            }
            Spacer(modifier = Modifier.height(16.dp))
            Button(
                onClick = onShowSdkPdfView,
                enabled = sdkLoadStatus == SdkLoadStatus.LOADED
            ) {
                Text("Show a PDF from the SDK")
            }
            Spacer(modifier = Modifier.height(16.dp))
            OutlinedTextField(
                value = fileSizeInput,
                onValueChange = { fileSizeInput = it },
                label = { Text("File Size (Mb)") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(0.8f)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Button(
                onClick = { onCreateFileFromSdk(fileSizeInput) },
                enabled = sdkLoadStatus == SdkLoadStatus.LOADED
            ) {
                Text("Create File from SDK")
            }
            Spacer(modifier = Modifier.height(8.dp))
            Button(
                onClick = { onTriggerProcessDeath() },
                enabled = sdkLoadStatus == SdkLoadStatus.LOADED
            ) {
                Text("Force SDK Runtime process crash")
            }
        }
    }
}

@Composable
fun SdkStatusIndicator(status: SdkLoadStatus) {
    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(end = 8.dp)) {
        val icon = when (status) {
            SdkLoadStatus.NOT_LOADED -> Icons.Default.Clear
            SdkLoadStatus.LOADING -> Icons.Filled.Refresh
            SdkLoadStatus.LOADED -> Icons.Filled.Done
            SdkLoadStatus.FAILED -> Icons.Filled.Clear
        }
        val text = when (status) {
            SdkLoadStatus.NOT_LOADED -> "Not Loaded"
            SdkLoadStatus.LOADING -> "Loading..."
            SdkLoadStatus.LOADED -> "Loaded"
            SdkLoadStatus.FAILED -> "Failed"
        }
        val color = when (status) {
            SdkLoadStatus.NOT_LOADED -> Color.Gray
            SdkLoadStatus.LOADING -> Color.Blue
            SdkLoadStatus.LOADED -> Color.Green
            SdkLoadStatus.FAILED -> Color.Red
        }
        Icon(imageVector = icon, contentDescription = "SDK Status", tint = color)
        Spacer(modifier = Modifier.width(4.dp))
        Text(text, color = color)
    }
}

@Composable
fun SdkUiDialog(onDismiss: () -> Unit) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    Dialog(onDismissRequest = onDismiss) {
        AndroidView(
            modifier = Modifier
                .fillMaxWidth()
                .height(300.dp),
            factory = { ctx ->
                RemoteUiLayout(ctx).also { layout ->
                    remoteUiLayoutRef = layout
                    coroutineScope.launch {
                        layout.presentMessageUiFromMyReSdk(
                            message = "Hello from Client via RemoteUiLayout!",
                            onSuccess = {
                                (ctx as? MainActivity)?.runOnUiThread {
                                    Toast.makeText(ctx, "SDK UI flow complete (RemoteUiLayout)!", Toast.LENGTH_SHORT).show()
                                }
                                onDismiss()
                            }
                        )
                    }
                }
            },
            update = { /* No update needed for this simple case */ }
        )
    }
}

@Composable
fun SdkPdfViewOld(onDismiss: () -> Unit) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    var layoutRef: RemoteUiLayout? = null

    Dialog(onDismissRequest = onDismiss) {
        AndroidView(
            modifier = Modifier
                .fillMaxWidth()
                .height(400.dp),
            factory = { ctx ->
                RemoteUiLayout(ctx).also { layout ->
                    layoutRef = layout
                    coroutineScope.launch {
                        layout.presentPdfUiFromMyReSdk(
                            url = "https://css4.pub/2017/newsletter/drylab.pdf",
                            onSuccess = {
                                (ctx as? MainActivity)?.runOnUiThread {
                                    Toast.makeText(ctx, "SDK UI flow complete (RemoteUiLayout)!", Toast.LENGTH_SHORT).show()
                                }
                            }
                        )
                    }
                }
            },
            update = { /* No update needed for this simple case */ },
            onRelease = {
                layoutRef?.clearUi()
            }
        )
    }
}

@Composable
fun SdkPdfView(onDismiss: () -> Unit) {
    val context = LocalContext.current

    // This is still correct: it ensures the RemoteUiLayout itself isn't re-created by Compose.
    val remoteUiLayout = remember {
        RemoteUiLayout(context)
    }

    Dialog(onDismissRequest = {
        onDismiss()
    }) {
        // This is still correct: it hosts our stable RemoteUiLayout.
        AndroidView(
            factory = { remoteUiLayout },
            modifier = Modifier.fillMaxWidth().height(400.dp),
            onRelease = {
                remoteUiLayout.clearUi()
            }
        )
    }

    // This is still correct: it calls the method once as a side-effect.
    LaunchedEffect(Unit) {
        remoteUiLayout.presentPdfUiFromMyReSdk(
            url = "https://css4.pub/2017/newsletter/drylab.pdf",
            onSuccess = {
                (context as? MainActivity)?.runOnUiThread {
                    Toast.makeText(context, "SDK UI flow complete (RemoteUiLayout)!", Toast.LENGTH_SHORT).show()
                }
            })
    }
}