package com.necdetzr.history.search

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.necdetzr.designsystem.icons.BleIcons
import com.necdetzr.history.R
import com.necdetzr.history.components.DeviceSearchItem
import com.necdetzr.history.components.HistorySearchField
import com.necdetzr.history.components.HistorySearchMessage
import com.necdetzr.history.components.ScanRecordCard
import com.necdetzr.history.components.ScanRecordSheet
import com.necdetzr.model.DeviceSearchResult
import com.necdetzr.model.ScanRecord

@Composable
fun HistorySearchScreen(
    modifier: Modifier = Modifier,
    onBackButton:()->Unit,
    viewModel: HistorySearchViewModel = hiltViewModel(),
){
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    HistorySearchScreen(
        modifier = modifier,
        onBackButton = onBackButton,
        query = uiState.query,
        onQueryChange = viewModel::onQueryChange,
        onCategoryClick = viewModel::onCategoryClick,
        selectedCategory = uiState.selectedCategory,
        onScanClick = viewModel::onScanClick,
        onDeviceExpandClick = viewModel::onDeviceExpandClick,
        expandedDeviceMac = uiState.expandedDeviceMac,
        expandedDeviceScans = uiState.expandedDeviceScans,
        contentState = uiState.contentState
    )
    uiState.selectedScan?.let {scan->
        ScanRecordSheet(
            scanRecordDetail = scan,
            onDismissRequest = viewModel::onSheetDismissed,
            onDeviceClick = viewModel::onDeviceClick,
            onBackClick = viewModel::onDeviceDetailBack,
            selectedDevice = uiState.selectedDevice
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun HistorySearchScreen(
    modifier: Modifier = Modifier,
    query:String,
    selectedCategory: SearchCategory,
    onBackButton: () -> Unit,
    onCategoryClick: (SearchCategory) -> Unit,
    onQueryChange:(String)->Unit,
    onScanClick: (Long) -> Unit,
    onDeviceExpandClick: (String) -> Unit,
    expandedDeviceMac:String?,
    expandedDeviceScans: List<ScanRecord>,
    contentState: HistorySearchContentState
){
    Scaffold(
        modifier = modifier,
        topBar = {
            SearchTopBar(
                onBackButton
            )
        }
    ) { innerPadding->
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(innerPadding)
                .padding(horizontal = 12.dp)

        ) {
            HistorySearchField(
                query = query,
                onQueryChange =onQueryChange
            )
            CategorySection(
                onCategoryClick = onCategoryClick,
                selectedCategory = selectedCategory
            )
            SearchResult(
                modifier = Modifier.weight(1f),
                selectedCategory = selectedCategory,
                contentState = contentState,
                onScanClick = onScanClick,
                onDeviceExpandClick = onDeviceExpandClick,
                expandedDeviceMac = expandedDeviceMac,
                expandedDeviceScans = expandedDeviceScans
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SearchTopBar(
    onBackButton: () -> Unit
){
    TopAppBar(
        title = {
            Text(
                text = stringResource(R.string.feature_history_search_title),
                modifier = Modifier.padding(horizontal = 4.dp)
            )
        },
        navigationIcon = {
            IconButton(
                onClick = onBackButton
            ){
                Icon(
                    imageVector = BleIcons.Back,
                    contentDescription = null
                )
            }
        }

    )
}
@Composable
private fun CategorySection(
    onCategoryClick:(SearchCategory)->Unit,
    selectedCategory: SearchCategory
){
    val categories = SearchCategory.entries
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        categories.forEach { category->
            val selected = category == selectedCategory

            Surface(
                modifier = Modifier.weight(1f)
                    .padding(vertical = 8.dp),
                shape = RoundedCornerShape(6.dp),
                color = if(selected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface,
                tonalElevation = 1.dp,
                onClick = { onCategoryClick(category) },
            ) {
                Text(
                    modifier = Modifier.padding(8.dp),
                    text = stringResource(category.labelRes),
                    style = MaterialTheme.typography.titleSmall,
                    color =  if (selected) {
                        MaterialTheme.colorScheme.onPrimaryContainer
                    } else {
                        MaterialTheme.colorScheme.onSurface
                    },
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}
@Composable
private fun SearchResult(
    modifier: Modifier = Modifier,
    selectedCategory: SearchCategory,
    onScanClick: (Long) -> Unit,
    onDeviceExpandClick: (String) -> Unit,
    expandedDeviceMac: String?,
    expandedDeviceScans: List<ScanRecord>,
    contentState: HistorySearchContentState
) {
    when (contentState) {
        HistorySearchContentState.Loading -> {
            HistorySearchLoading()
        }

        HistorySearchContentState.Empty -> {
            HistorySearchEmpty(
                title = stringResource(
                    R.string.feature_history_search_empty_title
                ),
                description = stringResource(
                    R.string.feature_history_search_empty_description
                )
            )
        }

        HistorySearchContentState.Error -> {
            HistorySearchError()
        }

        is HistorySearchContentState.Success -> {
            LazyColumn(
                modifier = modifier.fillMaxWidth()
            ) {
                if (
                    contentState.scans.isNotEmpty()
                    &&
                    (selectedCategory == SearchCategory.ALL || selectedCategory == SearchCategory.SCAN)
                ) {
                    scanSection(
                        scans = contentState.scans,
                        onScanClick = onScanClick
                    )
                }

                if (
                    contentState.devices.isNotEmpty()
                    &&
                    (selectedCategory == SearchCategory.ALL || selectedCategory == SearchCategory.DEVICE)
                ) {
                    deviceSection(
                        devices = contentState.devices,
                        expandedDeviceMac = expandedDeviceMac,
                        onDeviceExpandClick = onDeviceExpandClick,
                        onScanClick = onScanClick,
                        expandedDeviceScans = expandedDeviceScans
                    )
                }
            }
        }
    }
}
private fun LazyListScope.scanSection(
    scans:List<ScanRecord>,
    onScanClick:(Long)->Unit
){
    item {
        Text(
            text = stringResource(R.string.feature_history_scans),
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
    itemsIndexed(
        items = scans,
        key = {_,scan -> scan.scanId}
    ){index, scan->
        ScanRecordCard(
            scanRecord = scan,
            icon = BleIcons.Scan,
            isLast = index == scans.lastIndex,
            onScanClick = onScanClick
        )
    }
}
@Composable
private fun HistorySearchEmpty(
    title: String,
    description: String
) {
    HistorySearchMessage(
        icon = BleIcons.Warning,
        title = title,
        description = description
    )
}

@Composable
private fun HistorySearchLoading() {
    LinearProgressIndicator(
        modifier = Modifier.fillMaxWidth()
    )
}

@Composable
private fun HistorySearchError() {
    HistorySearchMessage(
        icon = BleIcons.Error,
        title = stringResource(
            R.string.feature_history_search_error_title
        ),
        description = stringResource(
            R.string.feature_history_search_error_description
        )
    )
}

private fun LazyListScope.deviceSection(
    devices:List<DeviceSearchResult>,
    onDeviceExpandClick: (String) -> Unit,
    expandedDeviceMac: String?,
    onScanClick: (Long) -> Unit,
    expandedDeviceScans: List<ScanRecord>
){

    item {
        Text(
            text = stringResource(R.string.feature_history_devices),
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurface
        )
    }

    items(
        items = devices,
        key = {it.device.macAddress}
    ){device->
        val expanded =
            expandedDeviceMac == device.device.macAddress
        DeviceSearchItem(
            device = device.device,
            scanCount = device.scanCount,
            onExpandClick = onDeviceExpandClick,
            expanded = expandedDeviceMac == device.device.macAddress,
            onScanClick = onScanClick,
            seenInScans = if (expanded) {
                expandedDeviceScans
            } else {
                emptyList()
            },
        )
    }

}
