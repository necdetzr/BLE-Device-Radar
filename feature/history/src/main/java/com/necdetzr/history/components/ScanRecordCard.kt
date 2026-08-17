package com.necdetzr.history.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.necdetzr.designsystem.icons.BleIcons
import com.necdetzr.history.R
import com.necdetzr.model.ScanRecord
import com.necdetzr.ui.util.toReadableDateTime

@Composable
internal fun ScanRecordCard(
    scanRecord: ScanRecord,
    icon: ImageVector,
    isLast:Boolean,
    onScanClick: (Long) -> Unit

){
    Column {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable {
                    onScanClick(scanRecord.scanId)
                }
                .padding(18.dp),
            verticalAlignment = Alignment.CenterVertically,

            ) {
            Box(
                modifier = Modifier
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primaryContainer)
                    .padding(6.dp),
                contentAlignment = Alignment.Center
            ){
                Icon(
                    icon,
                    null
                )
            }
            Spacer(Modifier.width(8.dp))
            Column(
                modifier = Modifier.weight(1f),

                ) {
                Text(
                    text = scanRecord.scanName,
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(Modifier.height(2.dp))
                Text(
                    text = scanRecord.timestamp.toReadableDateTime(),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis

                )
            }
            Spacer(Modifier.width(8.dp))
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .background(MaterialTheme.colorScheme.primaryContainer.copy(0.4f))
                    .padding(horizontal = 12.dp, vertical = 6.dp)
            ){
                Text(
                    text = pluralStringResource(
                        id = R.plurals.feature_history_device_count,
                        count = scanRecord.deviceCount,
                        scanRecord.deviceCount,
                    ),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }
            Spacer(Modifier.width(8.dp))
            Icon(
                imageVector = BleIcons.Right,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )

        }
        if(!isLast){
            HorizontalDivider(
                thickness = 0.2.dp

            )
        }
    }
}
