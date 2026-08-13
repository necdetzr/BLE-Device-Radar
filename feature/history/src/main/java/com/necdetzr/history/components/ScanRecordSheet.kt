package com.necdetzr.history.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.necdetzr.designsystem.icons.BleIcons
import com.necdetzr.model.ScanRecord
import com.necdetzr.model.ScanRecordDetail
import com.necdetzr.model.ScannedBleDevice
import com.necdetzr.ui.BleDeviceCard
import com.necdetzr.ui.DeviceDetailSheet
import com.necdetzr.ui.DeviceDetailSheetContent
import com.necdetzr.ui.DeviceFeedUiState
import com.necdetzr.ui.deviceFeed
import com.necdetzr.ui.util.toReadableDateTime

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScanRecordSheet(
    scanRecordDetail: ScanRecordDetail,
    onDismissRequest: () -> Unit,
    onDeviceClick: (ScannedBleDevice) -> Unit,
    selectedDevice: ScannedBleDevice?,
    onBackClick:()->Unit

) {
    ModalBottomSheet(
        onDismissRequest = onDismissRequest
    ) {
        if(selectedDevice != null){
            IconButton(
                onClick = onBackClick
            ) {
                Icon(
                    imageVector = BleIcons.Back,
                    contentDescription = null
                )
            }
            DeviceDetailSheetContent(
                bleDevice = selectedDevice
            )
        }else{
            ScanRecordSheetContent(
                scan = scanRecordDetail.scan,
                devices = scanRecordDetail.devices,
                onDeviceClick = onDeviceClick
            )
        }

    }
}

@Composable
fun ScanRecordSheetContent(
    scan: ScanRecord,
    devices: List<ScannedBleDevice>,
    onDeviceClick: (ScannedBleDevice) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 8.dp)
    ) {
        item {
            Column{
                Text(
                    text = scan.scanName,
                    style = MaterialTheme.typography.headlineMedium
                )
                Spacer(Modifier.height(4.dp))

                Text(
                    text = scan.timestamp.toReadableDateTime(),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

        }

        item {
            Row(
                modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                ScanRecordDataCard(
                    modifier = Modifier.weight(1f),
                    title = "Devices",
                    value = scan.deviceCount.toString()
                )

                ScanRecordDataCard(
                    modifier = Modifier.weight(1f),
                    title = "Packets",
                    value = devices.sumOf { it.packetCount }.toString()
                )

                ScanRecordDataCard(
                    modifier = Modifier.weight(1f),
                    title = "Connectable",
                    value = devices.count {
                        it.advertisement.isConnectable == true
                    }.toString()
                )
            }
        }

        item {
            Text(
                text = "Devices",
                style = MaterialTheme.typography.titleMedium
            )
        }

        items(
            items = devices,
            key = { it.macAddress }
        ) { device ->
            BleDeviceCard(
                bleDevice = device,
                onClick = {
                    onDeviceClick(device)
                },
                modifier = Modifier.padding(8.dp)
            )
        }
    }
}
@Composable
fun ScanRecordDataCard(
    modifier: Modifier = Modifier,
    title: String,
    value: String
) {
    Surface(
        modifier = modifier,
        tonalElevation = 1.dp,
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier.padding(12.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.labelMedium
            )
            Spacer(Modifier.height(4.dp))

            Text(
                text = value,
                style = MaterialTheme.typography.titleMedium
            )
        }
    }
}
