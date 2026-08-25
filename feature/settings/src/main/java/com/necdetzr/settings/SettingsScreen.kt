package com.necdetzr.settings

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.necdetzr.designsystem.icons.BleIcons
import com.necdetzr.model.ThemeConfig
import com.necdetzr.ui.util.getAppVersionName

@Composable
internal fun SettingsScreen(
    modifier: Modifier = Modifier,
    viewModel: SettingsViewModel = hiltViewModel()
){
    val uiState = viewModel.settingsUiState.collectAsStateWithLifecycle()
    SettingsScreen(
        modifier = modifier,
        onRssiChange = { viewModel.setRssiRange(it) },
        onThemeSelection = {viewModel.setTheme(it)},
        onPeriodClick = {viewModel.setScanPeriod(it)},
        onDeleteAllScans = {viewModel.deleteAllScans()},
        rssi = uiState.value.rssi,
        currentTheme = uiState.value.theme,
        period = uiState.value.scanPeriod,


    )
}

@Composable
internal fun SettingsScreen(
    modifier: Modifier = Modifier,
    onRssiChange: (Int) -> Unit,
    onThemeSelection: (ThemeConfig) -> Unit,
    onPeriodClick: (Long) -> Unit,
    onDeleteAllScans: () -> Unit,
    currentTheme: ThemeConfig,
    rssi: Int,
    period:Long
){
    Scaffold(
        contentWindowInsets = WindowInsets.safeDrawing,
        modifier = modifier
    ) {innerPadding->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp)
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())

        ) {
            SettingsTitle()
            Spacer(Modifier.height(24.dp))
            ScanningSection(
                onRssiChange = onRssiChange,
                onPeriodClick = onPeriodClick,
                rssi = rssi,
                period = period
            )
            Spacer(Modifier.height(24.dp))
            ThemeSection(
                currentTheme = currentTheme,
                onThemeSelection = onThemeSelection
            )
            Spacer(Modifier.height(24.dp))
            AboutSection()
            Spacer(Modifier.height(12.dp))
            DangerZoneSection(
                onDeleteAllScans = onDeleteAllScans
            )

        }
    }
}

@Composable
private fun SettingsTitle(
    modifier: Modifier = Modifier
){
    Column(
        modifier = modifier.fillMaxWidth()
    ) {
        Text(
            stringResource(R.string.feature_settings_title),
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.onBackground
        )
        Spacer(Modifier.height(4.dp))
        Text(
            text = stringResource(R.string.feature_settings_text),
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

    }
}

@Composable
private fun ScanningSection(
    onRssiChange : (Int) ->Unit,
    onPeriodClick: (Long) -> Unit,
    period: Long,
    rssi:Int
){
    var displayRssi by remember(rssi) { mutableIntStateOf(rssi) }
    SectionTitle(
        text = stringResource(R.string.feature_settings_scanning),
        modifier = Modifier.padding(horizontal = 4.dp)
    )
    Spacer(Modifier.height(8.dp))
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .border(
                width = 0.3.dp,
                color = MaterialTheme.colorScheme.outlineVariant,
                shape = RoundedCornerShape(12.dp)
            )
            .padding(16.dp)
    ) {
        Column {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = stringResource(R.string.feature_settings_rssi_title),
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(Modifier.height(2.dp))
                    Text(
                        text = stringResource(R.string.feature_settings_rssi_text),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(4.dp))
                        .background(MaterialTheme.colorScheme.primaryContainer)
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = stringResource(
                            R.string.feature_settings_rssi_value,
                            displayRssi,
                        ),
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                        style = MaterialTheme.typography.labelMedium
                    )
                }
            }
            RssiThresholdFilter(
                onRssiChange = onRssiChange,
                onDrag = {value->
                    displayRssi = value
                },
                rssi = rssi
            )

            HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp))
            ScanPeriodSection(
                onPeriodClick = onPeriodClick,
                currentPeriod = period
            )
        }
    }
}
@Composable
private fun ScanPeriodSection(
    onPeriodClick:(Long)->Unit,
    currentPeriod: Long
){
    val periods = listOf(
        5_000L,10_000L,30_000L,60_000L
    )
    Column {
        Text(
            text = stringResource(R.string.feature_settings_scan_period_title),
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurface
        )
        Spacer(Modifier.height(2.dp))
        Text(
            text = stringResource(R.string.feature_settings_scan_period_description),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(Modifier.height(8.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            periods.forEach { period->
                PeriodSubSection(
                    modifier = Modifier.weight(1f),
                    period = period,
                    onClick = onPeriodClick,
                    isSelected = period == currentPeriod
                )
            }
        }
    }
}
@Composable
private fun PeriodSubSection(
    modifier: Modifier = Modifier,
    onClick: (Long) -> Unit,
    period:Long,
    isSelected: Boolean
){
    val backColor = if(isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.background
    val color = if(isSelected) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.outline
    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .clickable(
                onClick = { onClick(period) }
            )
            .border(
                width = 0.4.dp,
                color = MaterialTheme.colorScheme.outlineVariant,
                shape = RoundedCornerShape(8.dp)
            )
            .background(
                color = backColor.copy(0.4f)
            )
            .padding(8.dp)
    ){
        Text(
            text = stringResource(
                R.string.feature_settings_period_seconds,
                period / 1_000,
            ),
            color = color,
            style = MaterialTheme.typography.bodyMedium
        )

    }
}
@Composable
private fun ThemeSection(
    currentTheme: ThemeConfig,
    onThemeSelection:(ThemeConfig) -> Unit
){
    val themes = listOf(
        Triple(ThemeConfig.DARK, stringResource(R.string.feature_settings_dark), BleIcons.Dark),
        Triple(ThemeConfig.LIGHT, stringResource(R.string.feature_settings_light), BleIcons.Light),
        Triple(ThemeConfig.FOLLOW_SYSTEM, stringResource(R.string.feature_settings_system), BleIcons.System)
    )
    SectionTitle(
        text = stringResource(R.string.feature_settings_appearance),
        modifier = Modifier.padding(horizontal = 4.dp)
    )
    Spacer(Modifier.height(8.dp))
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .border(
                width = 0.3.dp,
                color = MaterialTheme.colorScheme.outlineVariant,
                shape = RoundedCornerShape(12.dp)
            )
            .padding(16.dp)
    ){
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            themes.forEach { (theme,string, vector) ->
                ThemeSubSection(
                    modifier = Modifier.weight(1f),
                    icon = vector,
                    text = string,
                    isSelected = theme == currentTheme,
                    onClick = { onThemeSelection(theme) }
                )
            }

        }
    }

}
@Composable
private fun ThemeSubSection(
    modifier: Modifier = Modifier,
    icon: ImageVector,
    text:String,
    isSelected:Boolean,
    onClick:()-> Unit
){
    val backColor = if(isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.background
    val color = if(isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline
    Box(
        modifier = modifier
            .clip(
                RoundedCornerShape(12.dp)
            )

            .clickable(
                onClick = onClick
            )
            .border(
                width = 0.4.dp,
                color = color,
                shape = RoundedCornerShape(12.dp)
            )
            .background(
                color = backColor.copy(0.4f),
                shape = RoundedCornerShape(12.dp)

            )
            .padding(12.dp),
        contentAlignment = Alignment.Center,
    ){
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ){
            Icon(
                icon,
                contentDescription = null,
                tint = color
            )
            Spacer(Modifier.height(4.dp))
            Text(
                text = text,
                color = color,
                style = MaterialTheme.typography.bodySmall,
                textAlign = TextAlign.Center,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}
@Composable
private fun AboutSection(){
    SectionTitle(text = stringResource(R.string.feature_settings_about_system))
    Spacer(Modifier.height(8.dp))
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .border(
                width = 0.4.dp,
                color = MaterialTheme.colorScheme.outlineVariant,
                shape = RoundedCornerShape(12.dp)
            )
            .padding(vertical = 16.dp, horizontal = 24.dp)
    ){
        Column(
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = stringResource(R.string.feature_settings_version),
                    color = MaterialTheme.colorScheme.onSurface,
                    style = MaterialTheme.typography.titleMedium
                )
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(4.dp))
                        .background(MaterialTheme.colorScheme.primaryContainer)
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ){
                    Text(
                        text = stringResource(
                            R.string.feature_settings_version_value,
                            getAppVersionName(),
                        ),
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                        style = MaterialTheme.typography.titleSmall

                    )
                }
            }
        }

    }

}
@Composable
private fun DangerZoneSection(
    onDeleteAllScans: () -> Unit,
) {
    var showDeleteDialog by rememberSaveable {
        mutableStateOf(false)
    }

    Column(
        modifier = Modifier.fillMaxWidth(),
    ) {
        SectionTitle(
            text = stringResource(R.string.feature_settings_danger_zone),
            color = MaterialTheme.colorScheme.error
        )

        Spacer(Modifier.height(8.dp))

        DeleteHistoryCard(
            onDeleteClick = {
                showDeleteDialog = true
            },
        )
    }

    if (showDeleteDialog) {
        DeleteHistoryDialog(
            onConfirm = {
                showDeleteDialog = false
                onDeleteAllScans()
            },
            onDismiss = {
                showDeleteDialog = false
            },
        )
    }
}
@Composable
private fun DeleteHistoryCard(
    onDeleteClick: () -> Unit,
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.25f),
        border = BorderStroke(
            width = 1.dp,
            color = MaterialTheme.colorScheme.error.copy(alpha = 0.5f),
        ),
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
        ) {
            Row(
                verticalAlignment = Alignment.Top,
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Icon(
                    imageVector = BleIcons.Warning,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.error,
                )

                Column(
                    modifier = Modifier.weight(1f),
                ) {
                    Text(
                        text = stringResource(R.string.feature_settings_delete_scan_history),
                        style = MaterialTheme.typography.titleMedium,
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        text = stringResource(
                            R.string.feature_settings_delete_scan_history_description,
                        ),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }

            Spacer(Modifier.height(16.dp))

            OutlinedButton(
                modifier = Modifier.fillMaxWidth(),
                onClick = onDeleteClick,
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.error),
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = MaterialTheme.colorScheme.error,
                ),
            ) {
                Text(stringResource(R.string.feature_settings_delete_all_scans))
            }
        }
    }
}
@Composable
private fun DeleteHistoryDialog(
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        icon = {
            Icon(
                imageVector = BleIcons.Warning,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.error,
            )
        },
        title = {
            Text(stringResource(R.string.feature_settings_delete_dialog_title))
        },
        text = {
            Text(stringResource(R.string.feature_settings_delete_dialog_message))
        },
        confirmButton = {
            TextButton(
                onClick = onConfirm,
                colors = ButtonDefaults.textButtonColors(
                    contentColor = MaterialTheme.colorScheme.error,
                ),
            ) {
                Text(stringResource(R.string.feature_settings_delete))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.feature_settings_cancel))
            }
        },
    )
}
@Composable
private fun RssiThresholdFilter(
    onRssiChange: (Int) -> Unit,
    onDrag: (Int) -> Unit,
    rssi: Int
){
    var sliderValue by remember(rssi) { mutableFloatStateOf(rssi.toFloat()) }
    Box(
        modifier = Modifier
            .fillMaxWidth()

            .padding(6.dp)
    ){
        Column {
            Slider(
                value = sliderValue,
                onValueChange = {newValue->
                    sliderValue = newValue
                    onDrag(newValue.toInt())

                },
                onValueChangeFinished = {
                    onRssiChange(sliderValue.toInt())
                },
                valueRange = -100f..-20f,
                steps = 79,
                colors = SliderDefaults.colors(
                    thumbColor = MaterialTheme.colorScheme.primary,
                    activeTrackColor = MaterialTheme.colorScheme.primary,
                    inactiveTrackColor = MaterialTheme.colorScheme.outlineVariant,
                    activeTickColor = Color.Transparent,
                    inactiveTickColor = Color.Transparent
                ),
                modifier = Modifier.fillMaxWidth()
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = stringResource(R.string.feature_settings_weak),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = stringResource(R.string.feature_settings_strong),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun SectionTitle(
    modifier:Modifier = Modifier,
    color:Color = MaterialTheme.colorScheme.primary,
    text: String
){
    Text(
        text = text,
        style = MaterialTheme.typography.titleMedium,
        color = color,
        modifier = modifier
    )
}

