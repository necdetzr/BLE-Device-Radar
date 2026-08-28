package com.necdetzr.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.necdetzr.designsystem.icons.BleIcons
import com.necdetzr.model.ScannedBleDevice

@Composable
fun BleDeviceCard(
    bleDevice: ScannedBleDevice,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
){
    Surface(
        onClick = onClick,
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 1.dp
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
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.SpaceAround
            ) {
                Text(
                    bleDevice.name ?: stringResource(R.string.core_ui_unknown_device),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    color = MaterialTheme.colorScheme.primary,
                    style = MaterialTheme.typography.titleMedium
                    )
                Text(
                    bleDevice.macAddress,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Spacer(Modifier.width(12.dp))
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ){
                RssiIcon(rssi = bleDevice.rssi)
                Spacer(Modifier.height(4.dp))
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(
                        MaterialTheme.colorScheme.primary.copy(0.1f)
                    ).padding(horizontal = 8.dp, vertical = 2.dp)
                ){
                    Text(
                        text = stringResource(R.string.core_ui_dbm_value, bleDevice.rssi,),
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        }
    }
}
@Composable
internal fun RssiIcon(rssi:Int){
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
        bleDevice = ScannedBleDevice(
            name = "Test Device",
            macAddress = "00:00:00:00:00:00",
            rssi = -35,
            firstSeenAt = 0
        ),
        onClick = {}
    )
}

