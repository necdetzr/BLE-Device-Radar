package com.necdetzr.radar

import android.Manifest
import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.VisibleForTesting
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.withTransform
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.necdetzr.designsystem.icons.BleIcons
import com.necdetzr.model.BleDevice
import com.necdetzr.ui.DeviceFeedUiState
import com.necdetzr.ui.deviceFeed



@SuppressLint("LocalContextGetResourceValueCall")
@Composable
internal fun RadarScreen(
    onDeviceClick: (BleDevice) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: RadarViewModel = hiltViewModel()
) {

    val uiState by viewModel.feedUiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val userMessage by viewModel.radarMessage.collectAsStateWithLifecycle()
    val snackBarHostState = remember {
        SnackbarHostState()
    }
    val bluetoothManager = context.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
    val bluetoothAdapter = bluetoothManager.adapter

    val permissions = remember {
        arrayOf(
            Manifest.permission.BLUETOOTH_SCAN,
            Manifest.permission.BLUETOOTH_CONNECT
        )
    }
    LaunchedEffect(userMessage) {
        val currentMessage = userMessage ?: return@LaunchedEffect
        snackBarHostState.showSnackbar(
            message = context.getString(
                currentMessage.stringResource
            ),
        )
        viewModel.onUserMessageShown()
    }
    DisposableEffect(context) {
        val receiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context?, intent: Intent?) {
                if(intent?.action == BluetoothAdapter.ACTION_STATE_CHANGED){
                    val state = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, BluetoothAdapter.ERROR)
                    if(state == BluetoothAdapter.STATE_OFF || state == BluetoothAdapter.STATE_TURNING_OFF){
                        viewModel.onStopButtonClicked()
                    }
                }
            }
        }
        val filter = IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED)
        ContextCompat.registerReceiver(
            context,
            receiver,
            filter,
            ContextCompat.RECEIVER_NOT_EXPORTED
        )
        onDispose {
            context.unregisterReceiver(receiver)
        }

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
    LaunchedEffect(bluetoothAdapter) {
        if (bluetoothAdapter == null) {
            viewModel.onBluetoothNotSupported()
        }
    }

    if (bluetoothAdapter == null) return
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissionMap ->
        val areAllPermissionsGranted = permissionMap.values.all { it }
        if (areAllPermissionsGranted) {
            if (bluetoothAdapter?.isEnabled == true) {
                viewModel.onStartButtonClicked()
            } else {
                val enableBtIntent = android.content.Intent(android.bluetooth.BluetoothAdapter.ACTION_REQUEST_ENABLE)
                enableBluetoothLauncher.launch(enableBtIntent)
            }
        }else{
            viewModel.onPermissionDenied()
        }
    }

    RadarScreen(
        uiState = uiState,
        onDeviceClick = onDeviceClick,
        snackbarHostState = snackBarHostState,
        onButtonClick = {
            val hasAllPermissions = permissions.all { permission ->
                ContextCompat.checkSelfPermission(context, permission) == PackageManager.PERMISSION_GRANTED
            }
            if (hasAllPermissions) {
                if (bluetoothAdapter.isEnabled) {
                    viewModel.onStartButtonClicked()
                } else {
                    val enableBtIntent = android.content.Intent(android.bluetooth.BluetoothAdapter.ACTION_REQUEST_ENABLE)
                    enableBluetoothLauncher.launch(enableBtIntent)
                }
            } else {
                permissionLauncher.launch(permissions)
            }
        },
        onCancelClick = viewModel::onStopButtonClicked,
        modifier = modifier
    )
}

@VisibleForTesting(otherwise = VisibleForTesting.PRIVATE)
@Composable
internal fun RadarScreen(
    uiState: DeviceFeedUiState,
    snackbarHostState: SnackbarHostState,
    onDeviceClick: (BleDevice) -> Unit,
    onButtonClick:()->Unit,
    onCancelClick:()->Unit,
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
                isScanning = uiState is DeviceFeedUiState.Scanning
            )

            when (uiState) {
                is DeviceFeedUiState.Idle -> {
                    ScanReadyTitle()
                }
                is DeviceFeedUiState.Scanning -> {
                    ScanningTitle(devicesFounded = uiState.devices.size)
                }
                is DeviceFeedUiState.Success -> {
                    ScanCompletedTitle(devicesFounded = uiState.devices.size)
                }
            }

            DeviceListTitle()
            Box(modifier = Modifier.weight(1f)) {


                when (uiState) {
                    is DeviceFeedUiState.Idle -> Box(
                        modifier = Modifier
                    ) {
                        IdleDeviceList()
                    }

                    is DeviceFeedUiState.Scanning -> LazyColumn {
                        deviceFeed(
                            feedUiState = uiState,
                            onDeviceClick = onDeviceClick
                        )
                    }

                    is DeviceFeedUiState.Success -> LazyColumn {
                        deviceFeed(
                            feedUiState = uiState,
                            onDeviceClick = onDeviceClick
                        )
                    }
                }
            }
            ScanButton(
                onClick ={
                    if(uiState is DeviceFeedUiState.Scanning){
                        onCancelClick()

                    }else{
                        onButtonClick()
                    }
                },
                modifier = Modifier.padding(vertical = 12.dp).height(56.dp),
                isScanning = uiState is DeviceFeedUiState.Scanning
            )
        }
    }



}
@Composable
private fun ScanButton(
    onClick:()-> Unit,
    isScanning:Boolean,
    modifier: Modifier = Modifier
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
        RadarUserMessage.BluetoothNotSupported ->
            R.string.feature_radar_bluetooth_not_supported

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
