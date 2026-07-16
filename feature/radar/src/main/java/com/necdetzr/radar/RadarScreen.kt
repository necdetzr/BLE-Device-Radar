package com.necdetzr.radar

import android.Manifest
import android.R.attr.onClick
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.VisibleForTesting
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.layout.windowInsetsBottomHeight
import androidx.compose.foundation.layout.windowInsetsTopHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PaintingStyle.Companion.Stroke
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.withTransform
import androidx.compose.ui.platform.LocalContext
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
import java.util.Collections.rotate

@Composable
internal fun RadarScreen(
    onDeviceClick: (BleDevice) -> Unit,
    modifier: Modifier = Modifier,

    viewModel: RadarViewModel = hiltViewModel()
){

    val uiState by viewModel.feedUiState.collectAsStateWithLifecycle()
    RadarScreen (
        uiState = uiState,
        onDeviceClick = {
        },
        onButtonClick = { viewModel.onStartButtonClicked() },
        onCancelClick = {viewModel.onStopButtonClicked()}
    )
}

@VisibleForTesting(otherwise = VisibleForTesting.PRIVATE)
@Composable
internal fun RadarScreen(
    uiState: DeviceFeedUiState,
    onDeviceClick: (BleDevice) -> Unit,
    onButtonClick:()->Unit,
    onCancelClick:()->Unit,
){
    val permissions = remember {
        arrayOf(
            Manifest.permission.BLUETOOTH_SCAN,
            Manifest.permission.BLUETOOTH_CONNECT
        )
    }
    val context = LocalContext.current
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissionMap->
        val areAllPermissionsGranted = permissionMap.values.all { it }
        if(areAllPermissionsGranted) onButtonClick()
    }
    Spacer(Modifier.windowInsetsTopHeight(WindowInsets.safeDrawing))

    Column(
        modifier = Modifier.fillMaxWidth()
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
                ScanningTitle(deviceFounded = uiState.devices.size)
            }
            is DeviceFeedUiState.Success -> {
                ScanReadyTitle()
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
                    val hasAllPermissions = permissions.all { permission ->
                        ContextCompat.checkSelfPermission(context, permission) == PackageManager.PERMISSION_GRANTED
                    }
                    if (hasAllPermissions) {
                        onButtonClick()
                    } else {
                        permissionLauncher.launch(permissions)
                    }
                }
            },
            modifier = Modifier.padding(vertical = 12.dp).height(56.dp),
            isScanning = uiState is DeviceFeedUiState.Scanning
        )
        Spacer(Modifier.windowInsetsBottomHeight(WindowInsets.safeDrawing))


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
                    text = "Scanning",
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }else{
                Text(
                    text = "Start Scan",
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
            text = "Discovered Devices",
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
                text = "No Devices detected yet.",
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
            text = "Ready to Scan",
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.primary
        )
        Spacer(Modifier.height(4.dp))
        Text(
            text = "Start a sweep to find nearby BLE devices",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onBackground,
            textAlign = TextAlign.Center
        )
    }

}
@Composable
private fun ScanningTitle(
    deviceFounded:Int
){
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.padding(vertical = 8.dp)
    ) {
        Text(
            text = "Scanning",
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.primary
        )
        Spacer(Modifier.height(4.dp))
        Text(
            text = "$deviceFounded devices found.",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onBackground
        )
    }
}


@Composable
fun RadarAnimation(isScanning: Boolean) {
    val infiniteTransition = rememberInfiniteTransition(label = "")
    val rotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(2500, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = ""
    )

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
                    rotate(degrees = rotation, pivot = center)
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
@Preview
@Composable
private fun ScanButtonPreview(){
    ScanButton(
        onClick = {},
        isScanning = false
    )
}
