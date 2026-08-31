package com.necdetzr.radar

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import androidx.activity.compose.ManagedActivityResultLauncher
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.necdetzr.designsystem.icons.BleIcons
import com.necdetzr.model.ScannedBleDevice
import com.necdetzr.radar.components.SaveAlertDialog
import com.necdetzr.ui.DeviceDetailSheet

@SuppressLint("LocalContextGetResourceValueCall", "VisibleForTests")
@Composable
internal fun RadarRoute(
    modifier: Modifier = Modifier,
    viewModel: RadarViewModel = hiltViewModel(),
) {
    val stateHolder = rememberRadarStateHolder()
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val selectedDevice by viewModel.selectedDevice.collectAsStateWithLifecycle()

    BleHardwareEffect(
        stateHolder = stateHolder,
        onBluetoothTurnedOff = viewModel::onStopButtonClicked,
    )

    RadarMessageEffect(
        message = uiState.radarMessage,
        stateHolder = stateHolder,
        onMessageShown = viewModel::onUserMessageShown,
    )

    val launchers = rememberRadarLaunchers(
        stateHolder = stateHolder,
        onStartScanning = viewModel::onStartButtonClicked,
        onPermissionDenied = viewModel::onPermissionDenied,
        onBluetoothEnableDenied = viewModel::onBluetoothEnableDenied,
    )

    val adapter = stateHolder.bluetoothAdapter

    if (adapter == null) {
        BluetoothNotSupportedScreen()
    } else {
        RadarScreen(
            uiState = uiState,
            onDeviceClick = viewModel::onDeviceSelected,
            snackbarHostState = stateHolder.snackbarHostState,
            onButtonClick = {
                when {
                    !stateHolder.hasAllPermissions() -> {
                        launchers.permissions.launch(stateHolder.permissions)
                    }

                    adapter.isEnabled -> {
                        viewModel.onStartButtonClicked()
                    }

                    else -> {
                        launchers.enableBluetooth.launch(
                            stateHolder.createEnableBtIntent()
                        )
                    }
                }
            },
            onCancelClick = viewModel::onStopButtonClicked,
            onSaveButton = viewModel::onSaveClick,
            modifier = modifier,
        )
    }

    RadarOverlays(
        selectedDevice = selectedDevice,
        showSaveDialog = uiState.showAlertDialog,
        onDeviceDismiss = viewModel::onSheetDismissed,
        onSaveDismiss = viewModel::onSaveDialogDismissed,
        onSave = viewModel::onSaveRecordClick,
    )
}
@SuppressLint("LocalContextGetResourceValueCall")
@Composable
private fun RadarMessageEffect(
    message: RadarUserMessage?,
    stateHolder: RadarStateHolder,
    onMessageShown: () -> Unit,
) {
    LaunchedEffect(message) {
        val currentMessage = message ?: return@LaunchedEffect

        stateHolder.snackbarHostState.showSnackbar(
            message = stateHolder.context.getString(
                currentMessage.stringResource
            ),
        )
        onMessageShown()
    }
}
@Composable
private fun RadarOverlays(
    selectedDevice: ScannedBleDevice?,
    showSaveDialog: Boolean,
    onDeviceDismiss: () -> Unit,
    onSaveDismiss: () -> Unit,
    onSave: (String) -> Unit,
) {
    selectedDevice?.let { device ->
        DeviceDetailSheet(
            device = device,
            onDismissRequest = onDeviceDismiss,
        )
    }

    if (showSaveDialog) {
        SaveAlertDialog(
            onDismissRequest = onSaveDismiss,
            onSave = onSave,
        )
    }
}

@Composable
fun BluetoothNotSupportedScreen(
    modifier: Modifier = Modifier
){
    Scaffold(
        modifier = modifier
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = BleIcons.Warning,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.outline,
                modifier = Modifier.size(100.dp)
            )
            Spacer(Modifier.height(12.dp))
            Text(
                text = stringResource(R.string.feature_radar_bluetooth_not_supported),
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.onSurface,
                textAlign = TextAlign.Center

            )
            Spacer(Modifier.height(2.dp))
            Text(
                text = stringResource(R.string.feature_radar_bluetooth_not_supported_desc),
                color = MaterialTheme.colorScheme.outline,
                style = MaterialTheme.typography.bodyLarge,
                textAlign = TextAlign.Center
            )

        }
    }
}
@Composable
private fun rememberRadarLaunchers(
    stateHolder: RadarStateHolder,
    onStartScanning: () -> Unit,
    onPermissionDenied: () -> Unit,
    onBluetoothEnableDenied: () -> Unit,
): RadarLaunchers {
    val enableBluetoothLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult(),
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            onStartScanning()
        } else {
            onBluetoothEnableDenied()
        }
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions(),
    ) { permissionMap ->
        val allPermissionsGranted = permissionMap.values.all { it }
        val adapter = stateHolder.bluetoothAdapter

        when {
            !allPermissionsGranted -> {
                onPermissionDenied()
            }

            adapter?.isEnabled == true -> {
                onStartScanning()
            }

            adapter != null -> {
                enableBluetoothLauncher.launch(
                    stateHolder.createEnableBtIntent()
                )
            }
        }
    }

    return RadarLaunchers(
        enableBluetooth = enableBluetoothLauncher,
        permissions = permissionLauncher,
    )
}
private data class RadarLaunchers(
    val enableBluetooth:
    ManagedActivityResultLauncher<Intent, ActivityResult>,
    val permissions:
    ManagedActivityResultLauncher<Array<String>, Map<String, Boolean>>,
)
private val RadarUserMessage.stringResource: Int
    get() = when (this) {

        RadarUserMessage.PermissionDenied ->
            R.string.feature_radar_bluetooth_permission_denied

        RadarUserMessage.ScanFailed ->
            R.string.feature_radar_scan_failed

        RadarUserMessage.BluetoothEnableDenied ->
            R.string.feature_radar_bluetooth_enable_denied

        RadarUserMessage.SaveFailed ->
            R.string.feature_radar_save_failed
    }
