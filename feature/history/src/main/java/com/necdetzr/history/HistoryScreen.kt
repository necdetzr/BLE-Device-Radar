package com.necdetzr.history

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.necdetzr.designsystem.icons.BleIcons
import com.necdetzr.history.components.HistorySearchPlaceholder
import com.necdetzr.history.components.ScanRecordCard
import com.necdetzr.history.components.ScanRecordSheet
import com.necdetzr.model.ScanRecord
import com.necdetzr.ui.util.toReadableDateTime

@Composable
internal fun HistoryScreen(
    modifier: Modifier = Modifier,
    onSearchClick: () -> Unit,
    viewModel: HistoryViewModel = hiltViewModel()
){
    val totalScans by viewModel.totalScans.collectAsStateWithLifecycle()
    val recentScans by viewModel.recentScans.collectAsStateWithLifecycle()
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    HistoryScreen(
        modifier = modifier,
        totalScans = totalScans,
        recentScans = recentScans,
        onSearchClick = onSearchClick,
        onScanClick = viewModel::onScanClick
    )
    uiState.selectedScan?.let { scan->
        ScanRecordSheet(
            onDismissRequest = viewModel::onSheetDismissed,
            onDeviceClick = viewModel::onDeviceClick,
            scanRecordDetail = scan,
            selectedDevice = uiState.selectedDevice,
            onBackClick =  viewModel::onDeviceDetailBack
        )
    }

}

@Composable
internal fun HistoryScreen(
    modifier: Modifier = Modifier,
    totalScans:Int,
    onSearchClick:()->Unit,
    onScanClick: (Long) -> Unit,
    recentScans: List<ScanRecord>
){
    val lastScan = recentScans.firstOrNull()

    val lastSeenValue =
        lastScan?.timestamp?.toReadableDateTime()
            ?: stringResource(R.string.feature_history_no_value)

    val lastSeenDescription = if (lastScan != null) {
        pluralStringResource(
            id = R.plurals.feature_history_device_count,
            count = lastScan.deviceCount,
            lastScan.deviceCount,
        )
    } else {
        stringResource(R.string.feature_history_no_scans_saved)
    }
    Scaffold(
        contentWindowInsets = WindowInsets.safeDrawing,
        modifier = modifier

    ) { innerPadding->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp)
                .padding(innerPadding)
        ) {
            HistoryTitle()
            Spacer(Modifier.height(12.dp))
            HistorySearchPlaceholder (
                onClick = onSearchClick
            )
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                StatisticsCard(
                    modifier = Modifier.weight(1f),
                    icon = BleIcons.Statistic,
                    title = stringResource(
                        R.string.feature_history_total_scans
                    ),
                    value = totalScans.toString(),
                    desc = stringResource(
                        R.string.feature_history_saved_all_time
                    ),
                )

                StatisticsCard(
                    modifier = Modifier.weight(1f),
                    icon = BleIcons.Time,
                    title = stringResource(
                        R.string.feature_history_last_seen_title
                    ),
                    value = lastSeenValue,
                    desc = lastSeenDescription,
                )
            }
            RecentSection(
                recentScans = recentScans,
                onScanClick = onScanClick
            )


        }
    }
}
@Composable
private fun HistoryTitle(){
    Column(
        modifier = Modifier.fillMaxWidth()
    ) {
        Text(
            text = stringResource(R.string.feature_history_title),
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.onSurface
        )
        Spacer(Modifier.height(2.dp))
        Text(
            text = stringResource(R.string.feature_history_subtitle),
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}
@Composable
private fun StatisticsCard(
    modifier:Modifier = Modifier,
    icon: ImageVector,
    title:String,
    value:String,
    desc:String
){
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        tonalElevation = 1.dp

    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
                Spacer(Modifier.width(8.dp))
                Text(
                    text = title.uppercase(),
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,

                )
            }
            Spacer(Modifier.height(4.dp))
            Text(
                text = value,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Spacer(Modifier.height(4.dp))

            Text(
                text = desc,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }

    }
}
@Composable
private fun RecentSection(
    recentScans:List<ScanRecord>,
    onScanClick:(Long)->Unit
){
    Column(
        modifier = Modifier.padding(bottom = 12.dp)
    ) {
        RecentTitle()
        Spacer(Modifier.height(8.dp))
        Surface(
            modifier = Modifier
                .fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 1.dp
        ) {
            LazyColumn(

            ) {
                items(
                    items = recentScans,
                    key = {it.scanId},

                ){
                    ScanRecordCard(
                        icon = BleIcons.Bluetooth,
                        scanRecord = it,
                        isLast = it == recentScans.last(),
                        onScanClick = onScanClick
                    )

                }
            }




        }
    }
}

@Composable
private fun RecentTitle(){
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = stringResource(R.string.feature_history_recent_activity),
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

