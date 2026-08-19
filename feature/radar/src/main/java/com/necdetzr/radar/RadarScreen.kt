package com.necdetzr.radar

import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.content.Intent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.VisibleForTesting
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.BasicAlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.withTransform
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.DialogProperties
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.necdetzr.designsystem.icons.BleIcons
import com.necdetzr.model.ScannedBleDevice
import com.necdetzr.ui.DeviceDetailSheet
import com.necdetzr.ui.DeviceFeedUiState
import com.necdetzr.ui.deviceFeed



@SuppressLint("LocalContextGetResourceValueCall")
@Composable
internal fun RadarScreen(
    modifier: Modifier = Modifier,
    viewModel: RadarViewModel = hiltViewModel()
) {
    val stateHolder = rememberRadarStateHolder()

    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val selectedDevice by viewModel.selectedDevice.collectAsStateWithLifecycle()
    BleHardwareEffect(
        stateHolder = stateHolder,
        onBluetoothTurnedOff = viewModel::onStopButtonClicked
    )
    LaunchedEffect(uiState.radarMessage) {
        val currentMessage = uiState.radarMessage ?: return@LaunchedEffect
        stateHolder.snackbarHostState.showSnackbar(
                message = stateHolder.context.getString(currentMessage.stringResource)
        )
        viewModel.onUserMessageShown()
    }
    val enableBluetoothLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == android.app.Activity.RESULT_OK) {
            viewModel.onStartButtonClicked()
        }else{
            viewModel.onBluetoothEnableDenied()
        }
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissionMap ->
        val areAllPermissionsGranted = permissionMap.values.all { it }
        val adapter = stateHolder.bluetoothAdapter

        when {
            !areAllPermissionsGranted -> {
                viewModel.onPermissionDenied()
            }

            adapter == null -> {
                return@rememberLauncherForActivityResult
            }

            adapter.isEnabled -> {
                viewModel.onStartButtonClicked()
            }

            else -> {
                val enableBtIntent =
                    Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)

                enableBluetoothLauncher.launch(enableBtIntent)
            }
        }
    }
    if(stateHolder.bluetoothAdapter == null){
        BluetoothNotSupportedScreen()
    }else{
        RadarScreen(
            uiState = uiState,
            onDeviceClick = {device->
                viewModel.onDeviceSelected(device)
            },
            snackbarHostState = stateHolder.snackbarHostState,
            onButtonClick = {
                if(stateHolder.hasAllPermissions()){
                    if(stateHolder.bluetoothAdapter.isEnabled){
                        viewModel.onStartButtonClicked()
                    }else{
                        enableBluetoothLauncher.launch(stateHolder.createEnableBtIntent())
                    }
                }else{
                    permissionLauncher.launch(stateHolder.permissions)
                }

            },
            onCancelClick = viewModel::onStopButtonClicked,
            onSaveButton = viewModel::onSaveClick,
            modifier = modifier
        )
    }


    selectedDevice?.let { device ->
        DeviceDetailSheet(
            device = device,
            onDismissRequest = { viewModel.onSheetDismissed() }
        )
    }
    if(uiState.showAlertDialog){
        SaveAlertDialog(
            onDismissRequest = viewModel::onSaveDialogDismissed,
            onSave = { scanName->
                viewModel.onSaveRecordClick(scanName)
            }
        )
    }
}

@VisibleForTesting(otherwise = VisibleForTesting.PRIVATE)
@Composable
internal fun RadarScreen(
    uiState: RadarScreenState,
    snackbarHostState: SnackbarHostState,
    onDeviceClick: (ScannedBleDevice) -> Unit,
    onButtonClick:()->Unit,
    onCancelClick:()->Unit,
    onSaveButton:()->Unit,
    modifier:Modifier = Modifier
){

    Scaffold(
        snackbarHost = {
            SnackbarHost(
                hostState = snackbarHostState
            )
        },
        modifier = modifier,
        contentWindowInsets = WindowInsets.safeDrawing,

        ) { paddingValues ->


        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 12.dp),
            horizontalAlignment = Alignment.CenterHorizontally

        ) {
            RadarAnimation(
                isScanning = uiState.feedState is DeviceFeedUiState.Scanning
            )

            when (val feedState = uiState.feedState) {
                is DeviceFeedUiState.Idle -> {
                    ScanReadyTitle()
                }
                is DeviceFeedUiState.Scanning -> {
                    ScanningTitle(devicesFounded = feedState.devices.size)
                }
                is DeviceFeedUiState.Success -> {
                    ScanCompletedTitle(devicesFounded = feedState.devices.size)
                }
            }

            DeviceListTitle()
            Box(modifier = Modifier.weight(1f)) {


                when (val feedState = uiState.feedState) {
                    is DeviceFeedUiState.Idle -> Box(
                        modifier = Modifier
                    ) {
                        IdleDeviceList()
                    }

                    is DeviceFeedUiState.Scanning -> LazyColumn {
                        deviceFeed(
                            feedUiState = feedState,
                            onDeviceClick = onDeviceClick
                        )
                    }

                    is DeviceFeedUiState.Success -> LazyColumn {
                        deviceFeed(
                            feedUiState = feedState,
                            onDeviceClick = onDeviceClick
                        )
                    }
                }
            }
            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp, Alignment.CenterHorizontally),
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 12.dp)
            ) {
                if (uiState.feedState is DeviceFeedUiState.Success) {
                    SaveScanButton(
                        onClick = { onSaveButton() },
                        modifier = Modifier.height(56.dp),
                        enabled = uiState.saveButtonEnabled
                    )
                }

                ScanButton(
                    onClick = {
                        if (uiState.feedState is DeviceFeedUiState.Scanning) {
                            onCancelClick()
                        } else {
                            onButtonClick()
                        }
                    },
                    modifier = Modifier.height(56.dp),
                    isScanning = uiState.feedState is DeviceFeedUiState.Scanning
                )
            }

        }
    }
}
@Composable
private fun BluetoothNotSupportedScreen(
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
private fun SaveScanButton(
    onClick: () -> Unit,
    enabled:Boolean,
    modifier: Modifier = Modifier
) {
    val text = if(enabled) stringResource(R.string.feature_radar_save_scan) else "Saved!"
    Button(
        onClick = onClick,
        enabled = enabled,
        modifier = modifier,
        colors = ButtonDefaults.buttonColors(
            containerColor = MaterialTheme.colorScheme.secondaryContainer,

            ),
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                imageVector = BleIcons.Save,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSecondaryContainer
            )
            Spacer(Modifier.width(6.dp))
            Text(
                text = text,
                color = MaterialTheme.colorScheme.onSecondaryContainer
            )
        }
    }
}
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SaveAlertDialog(
    onDismissRequest:()->Unit,
    onSave: (String) -> Unit
){
    var name by remember { mutableStateOf("") }
    BasicAlertDialog(
        onDismissRequest = {onDismissRequest()},
        modifier = Modifier.fillMaxWidth(),
        properties = DialogProperties(),
        content = {
            Surface(
                tonalElevation = 1.dp,
                shape = MaterialTheme.shapes.extraLarge,
                modifier = Modifier.wrapContentHeight(),
                color = MaterialTheme.colorScheme.surfaceContainerHigh,
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text(
                        text= "Save Scan",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "Give this scan a name so you can find it later.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    OutlinedTextField(
                        value = name,
                        onValueChange = {name = it},
                        maxLines = 1,
                        singleLine = true,
                        label = {Text("Name")},
                        shape = RoundedCornerShape(12.dp)
                    )
                    Text(
                        text = "e.g. Office Building A",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 8.dp),
                        horizontalArrangement = Arrangement.End
                    ) {
                        TextButton(
                            onClick = {onDismissRequest()},

                        ) {
                            Text("Cancel")
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Button(
                            onClick = {onSave(name)},
                            shape = RoundedCornerShape(8.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.primary,
                                contentColor = MaterialTheme.colorScheme.surface
                            )
                        ) {
                            Text("Save")
                        }

                    }
                }
            }


        })
}
@Composable
private fun ScanButton(
    modifier: Modifier = Modifier,
    onClick:()-> Unit,
    isScanning:Boolean = false,
){
    Button(
        onClick = onClick,
        modifier = modifier,
        colors = ButtonDefaults.buttonColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer,

        ),

    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
        ) {
            val icon = if(isScanning) BleIcons.Stop else BleIcons.Start
            Icon(
                icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onPrimaryContainer
            )
            Spacer(Modifier.width(6.dp))
            if (isScanning){
                Text(
                    text = stringResource(R.string.feature_radar_scanning),
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }else{
                Text(
                    text = stringResource(R.string.feature_radar_start_scan),
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }

        }
    }

}

@Composable
private fun DeviceListTitle(){
    Column {
        Text(
            text = stringResource(R.string.feature_radar_discovered_devices),
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            style = MaterialTheme.typography.titleSmall
        )
        Spacer(Modifier.height(4.dp))
        HorizontalDivider(
            thickness = 1.dp,
            color = MaterialTheme.colorScheme.outlineVariant
        )
    }


}
@Composable
private fun IdleDeviceList(){
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = BleIcons.BluetoothScanning,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(36.dp)
            )
            Spacer(Modifier.height(8.dp))
            Text(
                text = stringResource(R.string.feature_radar_no_devices),
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                style = MaterialTheme.typography.titleSmall
            )
        }

    }

}
@Composable
private fun ScanReadyTitle(){
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.padding(vertical = 16.dp)
    ) {
        Text(
            text = stringResource(R.string.feature_radar_ready_to_scan),
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.primary
        )
        Spacer(Modifier.height(4.dp))
        Text(
            text = stringResource(R.string.feature_radar_ready_title),
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onBackground,
            textAlign = TextAlign.Center
        )
    }
}
@Composable
private fun ScanningTitle(
    devicesFounded:Int
){
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.padding(vertical = 8.dp)
    ) {
        Text(
            text = stringResource(R.string.feature_radar_scanning),
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.primary
        )
        Spacer(Modifier.height(4.dp))
        Text(
            text = stringResource(
                R.string.feature_radar_devices_found,
                devicesFounded
            ),
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onBackground
        )
    }
}
@Composable
private fun ScanCompletedTitle(
    devicesFounded:Int
){
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.padding(vertical = 8.dp)
    ) {
        Text(
            text = stringResource(R.string.feature_radar_completed),
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.primary
        )
        Spacer(Modifier.height(4.dp))
        Text(
            text = stringResource(
                R.string.feature_radar_devices_found,
                devicesFounded
            ),
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onBackground
        )
    }
}


@Composable
fun RadarAnimation(isScanning: Boolean) {
    val rotation = remember { Animatable(0f) }
    LaunchedEffect(isScanning) {
        if(isScanning){
            rotation.snapTo(0f)
            while(true){
                rotation.animateTo(
                    targetValue = rotation.value + 360f,
                    animationSpec = tween(
                        durationMillis = 2500,
                        easing = LinearEasing
                    )
                )
            }
        }else{
            rotation.stop()
        }
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(200.dp)
            .background(MaterialTheme.colorScheme.background),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.size(200.dp)) {
            val canvasWidth = size.width
            val canvasHeight = size.height
            val center = Offset(canvasWidth / 2, canvasHeight / 2)
            val maxRadius = size.minDimension / 2

            drawCircle(
                color = Color(0xFF334155),
                radius = maxRadius,
                center = center,
                style = Stroke(width = 1.dp.toPx())
            )
            drawCircle(
                color = Color(0xFF334155),
                radius = maxRadius * 0.66f,
                center = center,
                style = Stroke(width = 1.dp.toPx())
            )
            drawCircle(
                color = Color(0xFF334155),
                radius = maxRadius * 0.33f,
                center = center,
                style = Stroke(width = 1.dp.toPx())
            )

            if (isScanning) {
                withTransform({
                    rotate(degrees = rotation.value, pivot = center)
                }) {
                    val sweepGradient = Brush.sweepGradient(
                        colors = listOf(
                            Color.Transparent,
                            Color.Transparent,
                            Color.Transparent,
                            Color.Transparent,
                            Color(0xFF38BDF8).copy(alpha = 0.1f),
                            Color(0xFF38BDF8).copy(alpha = 0.5f)
                        ),
                        center = center
                    )

                    drawArc(
                        brush = sweepGradient,
                        startAngle = 0f,
                        sweepAngle = 360f,
                        useCenter = true,
                        topLeft = Offset(center.x - maxRadius, center.y - maxRadius),
                        size = Size(maxRadius * 2, maxRadius * 2)
                    )
                }
            }

            drawCircle(
                color = Color(0xFF38BDF8),
                radius = 4.dp.toPx(),
                center = center
            )
        }
    }
}
private val RadarUserMessage.stringResource: Int
    get() = when (this) {

        RadarUserMessage.PermissionDenied ->
            R.string.feature_radar_bluetooth_permission_denied

        RadarUserMessage.ScanFailed ->
            R.string.feature_radar_scan_failed

        RadarUserMessage.BluetoothEnableDenied ->
            R.string.feature_radar_bluetooth_enable_denied
    }

@Preview
@Composable
private fun ScanButtonPreview(){
    ScanButton(
        onClick = {},
        isScanning = false
    )
}
@Preview
@Composable
private fun BluetoothNotSupportedPreview(){
    BluetoothNotSupportedScreen()
}
