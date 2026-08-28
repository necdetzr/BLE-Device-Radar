@file:Suppress("TooManyFunctions")
package com.necdetzr.radar
import androidx.annotation.VisibleForTesting
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.DialogProperties
import com.necdetzr.designsystem.icons.BleIcons
import com.necdetzr.model.ScannedBleDevice
import com.necdetzr.radar.components.RadarAnimation
import com.necdetzr.ui.DeviceFeedUiState
import com.necdetzr.ui.deviceFeed

@VisibleForTesting(otherwise = VisibleForTesting.PRIVATE)
@Composable
internal fun RadarScreen(
    uiState: RadarScreenState,
    snackbarHostState: SnackbarHostState,
    onDeviceClick: (ScannedBleDevice) -> Unit,
    onButtonClick: () -> Unit,
    onCancelClick: () -> Unit,
    onSaveButton: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Scaffold(
        snackbarHost = {
            SnackbarHost(hostState = snackbarHostState)
        },
        modifier = modifier,
        contentWindowInsets = WindowInsets.safeDrawing,
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            RadarAnimation(
                isScanning =
                    uiState.feedState is DeviceFeedUiState.Scanning,
            )

            RadarStatusTitle(
                feedState = uiState.feedState,
            )

            DeviceListTitle()

            RadarDeviceList(
                feedState = uiState.feedState,
                onDeviceClick = onDeviceClick,
                modifier = Modifier.weight(1f),
            )

            RadarActionButtons(
                feedState = uiState.feedState,
                saveButtonEnabled = uiState.saveButtonEnabled,
                onStartClick = onButtonClick,
                onCancelClick = onCancelClick,
                onSaveClick = onSaveButton,
            )
        }
    }
}
@Composable
private fun RadarStatusTitle(
    feedState: DeviceFeedUiState,
) {
    when (feedState) {
        DeviceFeedUiState.Idle -> {
            ScanReadyTitle()
        }

        is DeviceFeedUiState.Scanning -> {
            ScanningTitle(
                devicesFounded = feedState.devices.size,
            )
        }

        is DeviceFeedUiState.Success -> {
            ScanCompletedTitle(
                devicesFounded = feedState.devices.size,
            )
        }
    }
}
@Composable
private fun RadarDeviceList(
    feedState: DeviceFeedUiState,
    onDeviceClick: (ScannedBleDevice) -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier,
    ) {
        when (feedState) {
            DeviceFeedUiState.Idle -> {
                IdleDeviceList()
            }

            is DeviceFeedUiState.Scanning,
            is DeviceFeedUiState.Success,
                -> {
                LazyColumn {
                    deviceFeed(
                        feedUiState = feedState,
                        onDeviceClick = onDeviceClick,
                    )
                }
            }
        }
    }
}
@Composable
private fun RadarActionButtons(
    feedState: DeviceFeedUiState,
    saveButtonEnabled: Boolean,
    onStartClick: () -> Unit,
    onCancelClick: () -> Unit,
    onSaveClick: () -> Unit,
) {
    val isScanning = feedState is DeviceFeedUiState.Scanning
    val isCompleted = feedState is DeviceFeedUiState.Success

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp),
        horizontalArrangement = Arrangement.spacedBy(
            space = 12.dp,
            alignment = Alignment.CenterHorizontally,
        ),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (isCompleted) {
            SaveScanButton(
                onClick = onSaveClick,
                modifier = Modifier
                    .weight(1f)
                    .heightIn(min = 56.dp),
                enabled = saveButtonEnabled,
            )
        }

        ScanButton(
            onClick = {
                if (isScanning) {
                    onCancelClick()
                } else {
                    onStartClick()
                }
            },
            modifier = Modifier
                .weight(1f)
                .heightIn(min = 56.dp),
            isScanning = isScanning,
        )
    }
}

@Composable
private fun SaveScanButton(
    onClick: () -> Unit,
    enabled:Boolean,
    modifier: Modifier = Modifier
) {
    val text = if(enabled) stringResource(R.string.feature_radar_save_scan)
    else stringResource(R.string.feature_radar_saved)
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
                color = MaterialTheme.colorScheme.onSecondaryContainer,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
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
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }else{
                Text(
                    text = stringResource(R.string.feature_radar_start_scan),
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
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
