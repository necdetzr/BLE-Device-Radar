package com.necdetzr.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.necdetzr.designsystem.icons.BleIcons
import com.necdetzr.model.BleDevice

@Composable
fun BleDeviceCard(
    bleDevice: BleDevice,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
){

    Card(
        onClick = onClick,
        modifier = modifier
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically

        ) {
            Icon(
                BleIcons.Bluetooth,
                contentDescription = "bluetooth",
                tint = MaterialTheme.colorScheme.onSurface
            )
            Spacer(Modifier.width(16.dp))
            Column(
                verticalArrangement = Arrangement.SpaceAround
            ) {
                Text(
                    bleDevice.name ?: "Unknown Device",
                    color = MaterialTheme.colorScheme.primary,
                    style = MaterialTheme.typography.titleMedium

                    )
                Text(
                    bleDevice.macAddress,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Spacer(Modifier.weight(1f))
            RssiIcon(rssi = bleDevice.rssi)

        }


    }

}
@Composable
fun RssiIcon(rssi:Int){
    val rssiIcon = when {
        rssi >= -60 -> BleIcons.RssiHigh
        rssi >= -80 -> BleIcons.RssiMedium
        else -> BleIcons.RssiLow
    }
    Icon(
        imageVector = rssiIcon,
        contentDescription = "rssi",
        tint = MaterialTheme.colorScheme.onSurface
    )
}

@Preview("Card")
@Composable
fun BleDeviceCardPreview(){
    BleDeviceCard(
        bleDevice = BleDevice(
            name = "Test Device",
            macAddress = "00:00:00:00:00:00",
            rssi = -35
        ),
        onClick = {}
    )
}

