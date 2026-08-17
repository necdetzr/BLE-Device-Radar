package com.necdetzr.designsystem.component

import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.RowScope
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.NavigationDrawerItemDefaults
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.NavigationRailItemColors
import androidx.compose.material3.NavigationRailItemDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.adaptive.WindowAdaptiveInfo
import androidx.compose.material3.adaptive.currentWindowAdaptiveInfo
import androidx.compose.material3.adaptive.navigationsuite.NavigationSuiteDefaults
import androidx.compose.material3.adaptive.navigationsuite.NavigationSuiteItemColors
import androidx.compose.material3.adaptive.navigationsuite.NavigationSuiteScaffold
import androidx.compose.material3.adaptive.navigationsuite.NavigationSuiteScaffoldDefaults
import androidx.compose.material3.adaptive.navigationsuite.NavigationSuiteScope
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.necdetzr.designsystem.icons.BleIcons


@Composable
fun RowScope.BleNavigationBarItem(
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    alwaysShowLabel: Boolean = true,
    icon: @Composable () -> Unit,
    selectedIcon: @Composable () -> Unit = icon,
    label: @Composable (() -> Unit)? = null,
){
    NavigationBarItem(
        selected = selected,
        onClick = onClick,
        modifier = modifier,
        enabled = enabled,
        alwaysShowLabel = alwaysShowLabel,
        icon = if(selected) selectedIcon else icon,
        label = label,
        colors = NavigationBarItemDefaults.colors(
            selectedIconColor = NavDefaults.navigationSelectedItemColor(),
            unselectedIconColor = NavDefaults.navigationContentColor(),
            selectedTextColor = NavDefaults.navigationSelectedItemColor(),
            unselectedTextColor = NavDefaults.navigationContentColor(),
        )
    )

}


@Composable
fun BleNavigationBar(
    modifier: Modifier = Modifier,
    content: @Composable RowScope.() -> Unit
){
    NavigationBar(
        modifier = modifier,
        content = content,
        contentColor = NavDefaults.navigationContentColor(),
        containerColor = MaterialTheme.colorScheme.background,
        tonalElevation = 0.dp
    )

}



@Composable
fun BleNavigationRailItem(
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled:Boolean = true,
    alwaysShowLabel:Boolean = true,
    icon:@Composable ()->Unit,
    selectedIcon:@Composable ()->Unit = icon,
    label: @Composable (() -> Unit)? = null,
){
    NavigationRailItem(
        selected = selected,
        onClick = onClick,
        modifier = modifier,
        enabled = enabled,
        alwaysShowLabel = alwaysShowLabel,
        icon = if(selected) selectedIcon else icon,
        label = label,
        colors = NavigationRailItemDefaults.colors(
            selectedIconColor = NavDefaults.navigationSelectedItemColor(),
            unselectedIconColor = NavDefaults.navigationContentColor(),
            selectedTextColor = NavDefaults.navigationSelectedItemColor(),
            unselectedTextColor = NavDefaults.navigationContentColor(),
        )
    )

}







@Composable
fun BleNavigationRail(
    modifier: Modifier = Modifier,
    header: @Composable (ColumnScope.() ->Unit)? = null,
    content:@Composable ColumnScope.() ->Unit
){
    NavigationRail(
        modifier = modifier,
        containerColor = Color.Transparent,
        contentColor = NavDefaults.navigationContentColor(),
        header = header,
        content = content
    )
}






@Composable
fun BleNavigationSuiteScaffold(
    navigationSuiteItems: BleNavigationSuiteScope.() -> Unit,
    modifier: Modifier = Modifier,
    windowAdaptiveInfo: WindowAdaptiveInfo = currentWindowAdaptiveInfo(),
    content: @Composable () -> Unit
){
    val layoutType = NavigationSuiteScaffoldDefaults.calculateFromAdaptiveInfo(
        windowAdaptiveInfo
    )
    val navigationSuiteItemColors = NavigationSuiteItemColors(
        navigationBarItemColors = NavigationBarItemDefaults.colors(
            selectedIconColor = NavDefaults.navigationSelectedItemColor(),
            unselectedIconColor = NavDefaults.navigationContentColor(),
            selectedTextColor = NavDefaults.navigationSelectedItemColor(),
            unselectedTextColor = NavDefaults.navigationContentColor(),
            indicatorColor = NavDefaults.navigationIndicatorColor()
        ),
        navigationRailItemColors = NavigationRailItemDefaults.colors(
            selectedIconColor = NavDefaults.navigationSelectedItemColor(),
            unselectedIconColor = NavDefaults.navigationContentColor(),
            selectedTextColor = NavDefaults.navigationSelectedItemColor(),
            unselectedTextColor = NavDefaults.navigationContentColor(),
        ),
        navigationDrawerItemColors = NavigationDrawerItemDefaults.colors(
            selectedIconColor = NavDefaults.navigationSelectedItemColor(),
            unselectedIconColor = NavDefaults.navigationContentColor(),
            selectedTextColor = NavDefaults.navigationSelectedItemColor(),
            unselectedTextColor = NavDefaults.navigationContentColor(),
        )
    )
    NavigationSuiteScaffold(
        navigationSuiteItems = {
            BleNavigationSuiteScope(
                navigationSuiteScope = this,
                navigationSuiteItemColors = navigationSuiteItemColors,
            ).run(navigationSuiteItems)
        },
        layoutType = layoutType,
        containerColor = Color.Transparent,
        navigationSuiteColors = NavigationSuiteDefaults.colors(
            navigationBarContentColor = NavDefaults.navigationContentColor(),
            navigationBarContainerColor = MaterialTheme.colorScheme.background,
            navigationRailContainerColor = MaterialTheme.colorScheme.background,

            navigationRailContentColor = Color.Transparent
        ),
        modifier = modifier
    ){
        content()
    }

}






class BleNavigationSuiteScope internal constructor(
    private val navigationSuiteScope: NavigationSuiteScope,
    private val navigationSuiteItemColors: NavigationSuiteItemColors
){
    fun item(
        selected:Boolean,
        onClick:() -> Unit,
        modifier:Modifier = Modifier,
        icon: @Composable () -> Unit,
        selectedIcon: @Composable () -> Unit = icon,
        label: @Composable (()->Unit)? = null
    ) = navigationSuiteScope.item(
        selected = selected,
        onClick = onClick,
        modifier = modifier,
        icon = {
            if(selected){
            selectedIcon()
        }else{
            icon()
        }
               },
        label = label,
        colors = navigationSuiteItemColors,


    )
}



@Preview
@Composable
fun BleNavigationBarPreview(){
    val items = listOf("Radar","History","Settings")
    val icons = listOf(
        BleIcons.Radar,
        BleIcons.History,
        BleIcons.Settings
    )

    BleNavigationBar {
        items.forEachIndexed { index,item->
            BleNavigationBarItem(
                selected = index == 0,
                onClick = {},
                icon = {
                    Icon(
                        imageVector = icons[index],
                        contentDescription = item
                    )
                },
                label = { Text(item)}
            )
        }
    }
}

@Preview
@Composable
fun BleNavigationRailPreview(){
    val items = listOf("Radar","History","Settings")
    val icons = listOf(
        BleIcons.Radar,
        BleIcons.History,
        BleIcons.Settings
    )

    BleNavigationRail{
        items.forEachIndexed { index,item ->
            BleNavigationRailItem(
                selected = index == 0,
                onClick = {},
                icon = {
                    Icon(
                        imageVector = icons[index],
                        contentDescription = item
                    )
                },
                label = { Text(item)},

            )
        }
    }
}















object NavDefaults {
    @Composable
    fun navigationContentColor() = MaterialTheme.colorScheme.onSurfaceVariant

    @Composable
    fun navigationSelectedItemColor() = MaterialTheme.colorScheme.onPrimaryContainer

    @Composable
    fun navigationIndicatorColor() = MaterialTheme.colorScheme.primaryContainer
}
