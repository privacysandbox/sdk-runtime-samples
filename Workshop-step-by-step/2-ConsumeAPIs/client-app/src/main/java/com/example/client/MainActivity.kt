package com.example.client

import android.os.Bundle
import android.widget.Toast
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Done
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.getValue
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.lifecycleScope
import com.runtimeaware.sdk.RemoteUiLayout
import com.runtimeaware.sdk.RuntimeAwareSdk
import kotlinx.coroutines.launch

// SDK Loading State
enum class SdkLoadStatus {
    NOT_LOADED,
    LOADING,
    LOADED,
    FAILED
}

class MainActivity : AppCompatActivity() {
    private val runtimeAwareSdk by lazy { RuntimeAwareSdk(applicationContext) }
    private var sdkLoadStatusState = mutableStateOf(SdkLoadStatus.NOT_LOADED)

    // State to trigger snackbar display. Pair holds the message and a trigger ID.
    private var snackbarState by mutableStateOf<Pair<String, Int>?>(null)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val defaultFileSize = "10" // Default file size in Mb

        runtimeAwareSdk.setOnSandboxDeathCallback {
            sdkLoadStatusState.value = SdkLoadStatus.FAILED
            showSnackbar("SDK Runtime process died unexpectedly.")
        }

        setContent {
            MaterialTheme {
                var dialogMessage by remember { mutableStateOf<String?>(null) }

                MainScreen(
                    sdkLoadStatus = sdkLoadStatusState.value,
                    initialFileSize = defaultFileSize,
                    snackbarState = snackbarState,
                    onSnackbarShown = { snackbarState = null }, // Clear state after showing
                    onLoadSdk = {
                        if (sdkLoadStatusState.value == SdkLoadStatus.LOADING || sdkLoadStatusState.value == SdkLoadStatus.LOADED) {
                            showSnackbar("SDK is already loading or loaded.")
                            return@MainScreen
                        }
                        sdkLoadStatusState.value = SdkLoadStatus.LOADING
                        lifecycleScope.launch {
                            if (runtimeAwareSdk.initialize()) {
                                sdkLoadStatusState.value = SdkLoadStatus.LOADED
                                showSnackbar("SDK Initialized Successfully!")
                            } else {
                                sdkLoadStatusState.value = SdkLoadStatus.FAILED
                                showSnackbar("Failed to initialize SDK")
                            }
                        }
                    },
                    onShowSdkUi = { message ->
                        if (sdkLoadStatusState.value == SdkLoadStatus.LOADED) {
                            dialogMessage = message
                        } else {
                            showSnackbar("SDK not loaded. Please load the SDK first.")
                        }
                    },
                    onCreateFileFromSdk = { fileSizeString ->
                        if (sdkLoadStatusState.value == SdkLoadStatus.LOADED) {
                            lifecycleScope.launch {
                                val fileSize = fileSizeString.toLongOrNull()
                                if (fileSize == null || fileSize <= 0) {
                                    showSnackbar("Invalid file size.")
                                    return@launch
                                }
                                runtimeAwareSdk.createFile(fileSize)
                                    .onSuccess {
                                        showSnackbar("Created file of size $fileSize Mb in the SDK's sandbox!")
                                    }
                                    .onFailure { exception ->
                                        showSnackbar("Failed to create file: ${exception.message}")
                                    }
                            }
                        } else {
                            showSnackbar("SDK not loaded. Please load the SDK first.")
                        }
                    },
                    onTriggerProcessDeath = {
                        if (sdkLoadStatusState.value == SdkLoadStatus.LOADED) {
                            lifecycleScope.launch {
                                runtimeAwareSdk.triggerProcessDeath()
                            }
                        } else {
                            showSnackbar("SDK not loaded. Please load the SDK first.")
                        }
                    }
                )

                dialogMessage?.let { message ->
                    SdkUiDialog(
                        message = message,
                        onDismiss = { dialogMessage = null }
                    )
                }
            }
        }
    }

    private fun showSnackbar(message: String) {
        // Update state with the message and a new trigger value to ensure LaunchedEffect runs
        snackbarState = message to (snackbarState?.second?.plus(1) ?: 0)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    sdkLoadStatus: SdkLoadStatus,
    initialFileSize: String,
    snackbarState: Pair<String, Int>?,
    onSnackbarShown: () -> Unit,
    onLoadSdk: () -> Unit,
    onShowSdkUi: (message: String) -> Unit,
    onCreateFileFromSdk: (String) -> Unit,
    onTriggerProcessDeath: () -> Unit
) {
    val snackbarHostState = remember { SnackbarHostState() }
    snackbarState?.let { (message, _) ->
        LaunchedEffect(snackbarState) {
            snackbarHostState.showSnackbar(message)
            onSnackbarShown()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("SDK Runtime Demo") },
                actions = { SdkStatusIndicator(sdkLoadStatus) },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->
        var fileSizeInput by rememberSaveable { mutableStateOf(initialFileSize) }
        var sdkUiMessageInput by rememberSaveable { mutableStateOf("Hello from Client App!") }

        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Button(
                    onClick = onLoadSdk,
                    enabled = sdkLoadStatus != SdkLoadStatus.LOADING && sdkLoadStatus != SdkLoadStatus.LOADED,
                    modifier = Modifier.fillMaxWidth()
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

                FunctionalityCard(
                    title = "Display SDK-owned UI",
                    enabled = sdkLoadStatus == SdkLoadStatus.LOADED
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedTextField(
                            value = sdkUiMessageInput,
                            onValueChange = { sdkUiMessageInput = it },
                            label = { Text("Message") },
                            singleLine = true,
                            modifier = Modifier.weight(1f),
                            enabled = sdkLoadStatus == SdkLoadStatus.LOADED,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedContainerColor = MaterialTheme.colorScheme.surface,
                                unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                                disabledContainerColor = MaterialTheme.colorScheme.surface,
                            )
                        )
                        Button(
                            onClick = { onShowSdkUi(sdkUiMessageInput) },
                            enabled = sdkLoadStatus == SdkLoadStatus.LOADED
                        ) {
                            Text("Show")
                        }
                    }
                }

                FunctionalityCard(
                    title = "Manage SDK Files",
                    enabled = sdkLoadStatus == SdkLoadStatus.LOADED
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedTextField(
                            value = fileSizeInput,
                            onValueChange = { fileSizeInput = it },
                            label = { Text("File Size (Mb)") },
                            singleLine = true,
                            modifier = Modifier.weight(1f),
                            enabled = sdkLoadStatus == SdkLoadStatus.LOADED,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedContainerColor = MaterialTheme.colorScheme.surface,
                                unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                                disabledContainerColor = MaterialTheme.colorScheme.surface,
                            )
                        )
                        Button(
                            onClick = { onCreateFileFromSdk(fileSizeInput) },
                            enabled = sdkLoadStatus == SdkLoadStatus.LOADED
                        ) {
                            Text("Create")
                        }
                    }
                }

                FunctionalityCard(
                    title = "Debugging",
                    enabled = sdkLoadStatus == SdkLoadStatus.LOADED
                ) {
                    Button(
                        onClick = onTriggerProcessDeath,
                        enabled = sdkLoadStatus == SdkLoadStatus.LOADED,
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Force SDK Runtime Process Crash")
                    }
                }
            }
        }
    }
}

/**
 * A reusable Card composable to group related functionality,
 * following Material 3 design patterns.
 */
@Composable
fun FunctionalityCard(
    title: String,
    enabled: Boolean,
    content: @Composable ColumnScope.() -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(
            modifier = Modifier
                .padding(16.dp)
                .let { if (enabled) it else it.then(Modifier.graphicsLayer(alpha = 0.5f)) },
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            content()
        }
    }
}

@Composable
fun SdkStatusIndicator(status: SdkLoadStatus) {
    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(end = 16.dp)) {
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
            SdkLoadStatus.LOADING -> MaterialTheme.colorScheme.primary
            SdkLoadStatus.LOADED -> Color(0xFF008000)
            SdkLoadStatus.FAILED -> MaterialTheme.colorScheme.error
        }
        Icon(imageVector = icon, contentDescription = "SDK Status", tint = color)
        Spacer(modifier = Modifier.width(4.dp))
        Text(text, color = color, style = MaterialTheme.typography.bodyMedium)
    }
}

/**
 * A dialog that hosts the RemoteUiLayout from the SDK.
 * Its visibility is controlled by state, and it receives the message to display.
 */
@Composable
fun SdkUiDialog(message: String, onDismiss: () -> Unit) {
    val coroutineScope = rememberCoroutineScope()

    Dialog(onDismissRequest = onDismiss) {
        AndroidView(
            modifier = Modifier
            .fillMaxWidth()
            .height(300.dp),
            factory = { ctx ->
                RemoteUiLayout(ctx).also { layout ->
                    coroutineScope.launch {
                        layout.presentUiFromMyReSdk(
                            message = message,
                            onSuccess = {
                                (ctx as? MainActivity)?.runOnUiThread {
                                    Toast.makeText(
                                        ctx,
                                        "SDK UI flow complete!",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }
                            }
                        )
                    }
                }
            },
            update = {}
        )
    }
}