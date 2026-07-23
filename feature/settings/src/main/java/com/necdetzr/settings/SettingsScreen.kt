package com.necdetzr.settings

import android.R.attr.text
import android.bluetooth.le.ScanSettings
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
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
        rssi = uiState.value.rssi,
        currentTheme = uiState.value.theme,
        period = uiState.value.scanPeriod

    )
}

@Composable
internal fun SettingsScreen(
    modifier: Modifier = Modifier,
    onRssiChange: (Int) -> Unit,
    onThemeSelection: (ThemeConfig) -> Unit,
    onPeriodClick: (Long) -> Unit,
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
                        text = "$displayRssi dBm",
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
            text = "Scan Period",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurface
        )
        Spacer(Modifier.height(2.dp))
        Text(
            text = "Set the scan interval duration",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(Modifier.height(8.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            periods.forEach { period->
                PeriodSubSection(
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
    onClick: (Long) -> Unit,
    period:Long,
    isSelected: Boolean
){
    val backColor = if(isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.background
    val color = if(isSelected) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.outline
    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .padding(4.dp)
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
            .width(60.dp)
    ){
        Text(
            text = "${period/1000} s",
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
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier.fillMaxWidth()
        ) {
            themes.forEach { (theme,string, vector) ->
                ThemeSubSection(
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
            .padding(4.dp)
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

            .padding(12.dp)
            .width(60.dp)
        ,
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
                style = MaterialTheme.typography.bodySmall
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
                        text = "v" + getAppVersionName(),
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                        style = MaterialTheme.typography.titleSmall

                    )
                }
            }
        }

    }

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
    text: String
){
    Text(
        text = text,
        style = MaterialTheme.typography.titleMedium,
        color = MaterialTheme.colorScheme.primary,
        modifier = modifier
    )
}

