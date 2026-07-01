package com.necdetzr.model

data class UserPreferences(
    val themeConfig: ThemeConfig,
    val sortType: SortType,
    val scanPeriod: Long
)
