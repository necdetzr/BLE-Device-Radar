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
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.necdetzr.designsystem.icons.BleIcons
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
            SearchBar(
                onClick = onSearchClick
            )
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                StatisticsCard(
                    modifier = Modifier.weight(1f),
                    icon = BleIcons.Statistic,
                    title = "Total Scans",
                    value = totalScans.toString(),
                    desc = "All Time"
                )
                val lastScan = recentScans.firstOrNull()

                StatisticsCard(
                    modifier = Modifier.weight(1f),
                    icon = BleIcons.Time,
                    title = "Last Seen",
                    value = lastScan?.timestamp?.toReadableDateTime() ?: "-",
                    desc = lastScan?.deviceCount?.toString() + " devices"
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
            text = "History",
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.onSurface
        )
        Spacer(Modifier.height(2.dp))
        Text(
            text = "Search your saved scans.",
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
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Spacer(Modifier.height(4.dp))
            Text(
                text = value,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(Modifier.height(4.dp))

            Text(
                text = desc,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
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
private fun ScanRecordCard(
    scanRecord: ScanRecord,
    icon: ImageVector,
    isLast:Boolean,
    onScanClick: (Long) -> Unit

){
    Column {
        Row(
            modifier = Modifier
                .clickable(
                    onClick = { onScanClick(scanRecord.scanId) }
                )
                .fillMaxWidth()
                .padding(20.dp),
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
            Column {
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
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Spacer(Modifier.weight(1f))
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .background(MaterialTheme.colorScheme.primaryContainer.copy(0.4f))
                    .padding(horizontal = 12.dp, vertical = 6.dp)
            ){
                Text(
                    text = "${scanRecord.deviceCount} devices",
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
                modifier = Modifier.padding(vertical = 4.dp),
                thickness = 0.4.dp

            )
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
            text = "Recent Activity",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}
@Composable
private fun SearchBar(
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
    ) {
        Row(
            modifier = Modifier.padding(
                horizontal = 16.dp,
                vertical = 14.dp
            ),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = BleIcons.Search,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(Modifier.width(12.dp))

            Text(
                text = "Search scans or devices...",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
