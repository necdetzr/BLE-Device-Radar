package com.necdetzr.ui

import android.content.ClipData
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.ClipEntry
import androidx.compose.ui.platform.LocalClipboard
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.necdetzr.designsystem.icons.BleIcons
import com.necdetzr.model.ScannedBleDevice
import com.necdetzr.ui.mapper.BleUuidMapper
import kotlinx.coroutines.launch
import kotlin.time.Duration.Companion.milliseconds

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DeviceDetailSheet(
    device: ScannedBleDevice,
    onDismissRequest: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    ModalBottomSheet(
        onDismissRequest = onDismissRequest,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.background
    ) {
        DeviceDetailSheetContent(
            bleDevice = device
        )
    }
}

@Composable
private fun DeviceDetailSheetContent(
    bleDevice: ScannedBleDevice
) {
    val clipBoardManager = LocalClipboard.current
    val coroutineScope = rememberCoroutineScope()
    var copied by remember{ mutableStateOf(false)}
    val scrollState = rememberScrollState()
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .verticalScroll(scrollState)
            .padding(horizontal = 20.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(24.dp),


    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .background(
                        color = MaterialTheme.colorScheme.surfaceVariant,
                        shape = RoundedCornerShape(16.dp),
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = BleIcons.Bluetooth,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }
            SheetTitle(
                device = bleDevice,
                onCopyClick = {
                    coroutineScope.launch {
                        val clipData = ClipData.newPlainText("BLE Mac address", bleDevice.macAddress )
                        val clipEntry = ClipEntry(clipData)
                        clipBoardManager.setClipEntry(clipEntry)
                        copied = true
                        kotlinx.coroutines.delay(2000.milliseconds)
                        copied = false

                    }
                },
                copied = copied
            )
        }

        RssiCard(
            rssi = bleDevice.rssi,
            lastSeen = stringResource(R.string.core_ui_just_now)
        )

        DeviceInfoGrid(
            device = bleDevice
        )
        DetailedInfoSection(
            bleDevice = bleDevice
        )

        Spacer(modifier = Modifier.height(32.dp))
    }
}
@Composable
private fun DetailedInfoSection(
    bleDevice: ScannedBleDevice
){
    DeviceOtherInfoCard {
        DeviceOtherInfoRow(
            title = stringResource(R.string.core_ui_advertised_services),
            value = stringResource(R.string.core_ui_services,bleDevice.advertisement.serviceUuids.size),
            icon = BleIcons.Services,

            ) {

            if (bleDevice.advertisement.serviceUuids.isEmpty()) {
                Text(
                    text = stringResource(R.string.core_ui_no_advertised),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            } else {
                bleDevice.advertisement.serviceUuids.forEach { uuid ->
                    Text(
                        text = BleUuidMapper.getReadableName(uuid),
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = uuid,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

            }
        }

        DeviceOtherInfoRow(
            title = stringResource(R.string.core_ui_manufacturer_data),
            value = bleDevice.advertisement.manufacturerData.size.toString(),
            icon = BleIcons.Manufacturer,

            ) {
            if (bleDevice.advertisement.manufacturerData.isEmpty()) {
                Text(
                    text = stringResource(R.string.core_ui_no_manufacturer),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            } else {
                bleDevice.advertisement.manufacturerData.forEach { data ->
                    Column(modifier = Modifier.padding(vertical = 4.dp)) {
                        Text(
                            text = BleUuidMapper.getCompanyName(data.companyId),
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "Payload: ${data.payload.contentToString()}",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
        DeviceOtherInfoRow (
            title = stringResource(R.string.core_ui_service_data),
            value = stringResource(R.string.core_ui_records,bleDevice.advertisement.serviceData.size),
            icon = BleIcons.Sensors,
        ) {
            if (bleDevice.advertisement.serviceData.isEmpty()) {
                Text(
                    text = stringResource(R.string.core_ui_no_service_data),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            } else {
                bleDevice.advertisement.serviceData.forEach { service ->
                    Column(modifier = Modifier.padding(vertical = 4.dp)) {
                        SelectionContainer {
                            Column{
                                Text(
                                    text = BleUuidMapper.getReadableName(service.serviceUuid),
                                    style = MaterialTheme.typography.bodyLarge,
                                    fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = "Data: ${service.payload.joinToString(" ") { String.format("%02X", it) }}",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }

                        }

                    }
                }
            }
        }
        DeviceOtherInfoRow(
            title = stringResource(R.string.core_ui_raw_advertising_packet),
            value = stringResource(R.string.core_ui_bytes,bleDevice.advertisement.rawData.size),
            icon = BleIcons.Code,
        ) {
            SelectionContainer {
                Text(
                    text = bleDevice.advertisement.rawData.joinToString(" ") { String.format("%02X", it) },
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

        }
        DeviceOtherInfoRow(
            title = stringResource(R.string.core_ui_extended_advertising),
            value = "BT 5.0+",
            icon = BleIcons.Info,
            showDivider = false
        ) {
            Text(
                text = stringResource(R.string.core_ui_sid,bleDevice.advertisement.advertisingSid ?: "N/A"),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = stringResource(R.string.core_ui_secondary_phy,bleDevice.advertisement.secondaryPhy.name),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = stringResource(R.string.core_ui_periodic_interval,bleDevice.advertisement.periodicAdvertisingInterval ?: "N/A"),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

    }
}
@Composable
private fun SheetTitle(
    device: ScannedBleDevice,
    onCopyClick: () -> Unit,
    copied: Boolean
) {
    Column(
        modifier = Modifier.fillMaxWidth()
    ) {
        Text(
            text = device.name ?: stringResource(R.string.core_ui_unknown_device),
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.onSurface
        )
        Spacer(Modifier.height(8.dp))
        Row(
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                text = device.macAddress,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(Modifier.width(12.dp))
            IconButton(
                onClick = onCopyClick,
                modifier = Modifier.size(16.dp),

            ) {
                AnimatedContent(
                    targetState = copied,
                    transitionSpec = {
                        (scaleIn(animationSpec = tween(220)) + fadeIn(animationSpec = tween(220))) togetherWith
                                (scaleOut(animationSpec = tween(220)) + fadeOut(animationSpec = tween(220)))
                    },
                    label = "iconAnimation"
                ) { isCopied ->
                    Icon(
                        imageVector = if (isCopied) BleIcons.Done else BleIcons.Copy,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onBackground,
                        modifier = Modifier.size(14.dp)
                    )
                }
            }
        }
    }
}
@Composable
private fun DeviceInfoGrid(device: ScannedBleDevice) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            InfoCard(
                modifier = Modifier.weight(1f),
                title = stringResource(R.string.core_ui_tx_power),
                value = "${device.advertisement.txPower ?: "N/A"} dBm",
                icon = BleIcons.Energy
            )
            InfoCard(
                modifier = Modifier.weight(1f),
                title = stringResource(R.string.core_ui_connectable),
                value = if (device.advertisement.isConnectable == true) stringResource(R.string.core_ui_yes) else stringResource(R.string.core_ui_no),
                icon = BleIcons.Connect
            )
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            InfoCard(
                modifier = Modifier.weight(1f),
                title = stringResource(R.string.core_ui_packets),
                value = device.packetCount.toString(),
                icon = BleIcons.Packet
            )
            InfoCard(
                modifier = Modifier.weight(1f),
                title =stringResource(R.string.core_ui_phy),
                value = device.advertisement.primaryPhy.name,
                icon = BleIcons.System
            )
        }

    }
}
@Composable
private fun DeviceOtherInfoCard(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
){
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 1.dp
    ) {
        Column(
            modifier = Modifier.fillMaxWidth()
        ) {
            content()
        }
    }
}
@Composable
private fun DeviceOtherInfoRow(
    title: String,
    value: String,
    icon: ImageVector,
    showDivider: Boolean = true,
    expandedContent: @Composable () -> Unit
){
    var expanded by remember{mutableStateOf(false)}
    val rotation by animateFloatAsState(
        targetValue = if (expanded) 180f else 0f,
        animationSpec = tween(durationMillis = 300),
        label = "arrowRotation"
    )
    Column(
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { expanded = !expanded }
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(24.dp)
            )
            Text(
                text = title,
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.padding(start = 16.dp)
            )
            Spacer(modifier = Modifier.weight(1f))
            Text(
                text = value,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(end = 8.dp)
            )

            Icon(
                imageVector = BleIcons.DownArrow,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.rotate(rotation)
            )
        }
        AnimatedVisibility(
            visible = expanded,
            enter = expandVertically(animationSpec = tween(300)),
            exit = shrinkVertically(animationSpec = tween(300))
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 56.dp, end = 16.dp, bottom = 16.dp)
            ) {
                expandedContent()
            }
        }
        if (showDivider) {
            HorizontalDivider(
                modifier = Modifier.padding(horizontal = 16.dp),
                thickness = 1.dp,
                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
            )
        }
    }

}
@Composable
private fun InfoCard(
    modifier: Modifier = Modifier,
    title: String,
    value: String,
    icon: ImageVector
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 1.dp,
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(18.dp)
                )
                Text(
                    text = title,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    style = MaterialTheme.typography.labelMedium
                )
            }
            Text(
                text = value,
                color = MaterialTheme.colorScheme.onSurface,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
private fun RssiCard(
    rssi: Int,
    lastSeen: String
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 1.dp,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            Text(
                text = stringResource(R.string.core_ui_signal_strength),
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontWeight = FontWeight.Medium
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.Bottom
                ) {
                    Text(
                        text = rssi.toString(),
                        color = MaterialTheme.colorScheme.primary,
                        style = MaterialTheme.typography.displayMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "dBm",
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        style = MaterialTheme.typography.titleMedium,
                        modifier = Modifier.padding(bottom = 6.dp)
                    )
                }

                RssiIcon(rssi)
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = BleIcons.Distance,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(24.dp)
                    )
                    Column {
                        Text(
                            text = stringResource(R.string.core_ui_estimated_distance),
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = stringResource(R.string.core_ui_undetermined),
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
                Row(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = BleIcons.Time,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(24.dp)
                    )
                    Column {
                        Text(
                            text = stringResource(R.string.core_ui_last_seen),
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = lastSeen,
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }
        }
    }
}
