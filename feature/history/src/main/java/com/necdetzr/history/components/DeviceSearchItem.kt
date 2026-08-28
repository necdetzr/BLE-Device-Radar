package com.necdetzr.history.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.necdetzr.designsystem.icons.BleIcons
import com.necdetzr.history.R
import com.necdetzr.model.ScanRecord
import com.necdetzr.model.ScannedBleDevice
import com.necdetzr.ui.util.toReadableDateTime

@Composable
fun DeviceSearchItem(
    device: ScannedBleDevice,
    scanCount: Int,
    seenInScans: List<ScanRecord>,
    expanded: Boolean,
    onExpandClick: (String) -> Unit,
    onScanClick: (Long) -> Unit,
    modifier: Modifier = Modifier,
) {
    val arrowRotation by animateFloatAsState(
        targetValue = if (expanded) 180f else 0f,
        label = "deviceArrowRotation",
    )
    val seenInScansText = pluralStringResource(
        id = R.plurals.feature_history_seen_in_scan_count,
        count = scanCount,
        scanCount,
    )
    val lastSeenText = stringResource(
        id = R.string.feature_history_last_seen,
        device.lastSeenAt.toReadableDateTime(),
    )
    val historySummary = stringResource(
        id = R.string.feature_history_device_history_summary,
        seenInScansText,
        lastSeenText,
    )

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 4.dp)
            .clip(RoundedCornerShape(8.dp))
            .clickable { onExpandClick(device.macAddress) },
        shape = RoundedCornerShape(8.dp),
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 1.dp,
    ) {
        Column {
            DeviceSearchHeader(
                device = device,
                historySummary = historySummary,
                arrowRotation = arrowRotation,
                onExpandClick = onExpandClick,
            )

            AnimatedVisibility(
                visible = expanded,
                enter = expandVertically() + fadeIn(),
                exit = shrinkVertically() + fadeOut(),
            ) {
                DeviceScanHistory(
                    scans = seenInScans,
                    onScanClick = onScanClick,
                )
            }
        }
    }
}
@Composable
private fun DeviceSearchHeader(
    device: ScannedBleDevice,
    historySummary: String,
    arrowRotation: Float,
    onExpandClick: (String) -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier
                .size(44.dp)
                .background(
                    color = MaterialTheme.colorScheme.primaryContainer,
                    shape = CircleShape,
                ),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                modifier = Modifier.size(23.dp),
                imageVector = BleIcons.Bluetooth,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onPrimaryContainer,
            )
        }

        DeviceSearchSummary(
            modifier = Modifier
                .weight(1f)
                .padding(horizontal = 12.dp),
            device = device,
            historySummary = historySummary,
        )

        IconButton(
            onClick = { onExpandClick(device.macAddress) },
        ) {
            Icon(
                modifier = Modifier.graphicsLayer {
                    rotationZ = arrowRotation
                },
                imageVector = BleIcons.DownArrow,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}
@Composable
private fun DeviceSearchSummary(
    device: ScannedBleDevice,
    historySummary: String,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(2.dp),
    ) {
        Text(
            text = device.name
                ?: stringResource(R.string.feature_history_unknown_device),
            style = MaterialTheme.typography.titleSmall,
            color = MaterialTheme.colorScheme.onSurface,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )

        Text(
            text = device.macAddress,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )

        Text(
            text = historySummary,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.outline,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}
@Composable
private fun DeviceScanHistory(
    scans: List<ScanRecord>,
    onScanClick: (Long) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.fillMaxWidth(),
    ) {
        HorizontalDivider(
            modifier = Modifier.padding(horizontal = 12.dp),
            color = MaterialTheme.colorScheme.outlineVariant,
        )

        Text(
            modifier = Modifier.padding(
                start = 16.dp,
                end = 16.dp,
                top = 14.dp,
                bottom = 8.dp,
            ),
            text = stringResource(R.string.feature_history_seen_in_scans_title,),
            style = MaterialTheme.typography.titleSmall,
            color = MaterialTheme.colorScheme.onSurface,
        )

        if (scans.isEmpty()) {
            Text(
                modifier = Modifier.padding(
                    horizontal = 16.dp,
                    vertical = 12.dp,
                ),
                text = stringResource(R.string.feature_history_no_scan_history,),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        } else {
            scans.forEach{  scan ->
                DeviceScanHistoryItem(
                    scan = scan,
                    onClick = {
                        onScanClick(scan.scanId)
                    },
                )
            }
        }
    }
}

@Suppress("LongMethod")
@Composable
private fun DeviceScanHistoryItem(
    scan: ScanRecord,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        onClick = onClick,
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 1.dp
    ) {
        Row(
            modifier = Modifier.padding(
                horizontal = 16.dp,
                vertical = 10.dp,
            ),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .background(
                        color = MaterialTheme.colorScheme.secondaryContainer,
                        shape = CircleShape,
                    ),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    modifier = Modifier.size(17.dp),
                    imageVector = BleIcons.Scan,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSecondaryContainer,
                )
            }
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(2.dp),
            ) {
                Text(
                    text = scan.scanName,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Text(
                    text = scan.timestamp.toReadableDateTime(),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
            Text(
                text = pluralStringResource(
                    id = R.plurals.feature_history_device_count,
                    count = scan.deviceCount,
                    scan.deviceCount),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.primary,
                maxLines = 1,
            )
        }
    }
}
