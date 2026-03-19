package com.faheemlabs.pocketapp.ui.theme

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

data class ResponsiveMetrics(
    val screenHorizontalPadding: Dp,
    val screenVerticalPadding: Dp,
    val sectionSpacing: Dp,
    val cardPadding: Dp,
    val chipSpacing: Dp,
    val topBadgeSize: Dp,
    val navItemSize: Dp,
    val navIconSize: Dp,
    val settingsHeaderSize: TextUnit
)

@Composable
fun rememberResponsiveMetrics(): ResponsiveMetrics {
    val configuration = LocalConfiguration.current
    val screenWidth = configuration.screenWidthDp

    return remember(screenWidth) {
        when {
            screenWidth < 360 -> ResponsiveMetrics(
                screenHorizontalPadding = 12.dp,
                screenVerticalPadding = 10.dp,
                sectionSpacing = 8.dp,
                cardPadding = 12.dp,
                chipSpacing = 6.dp,
                topBadgeSize = 34.dp,
                navItemSize = 42.dp,
                navIconSize = 20.dp,
                settingsHeaderSize = 26.sp
            )

            screenWidth < 412 -> ResponsiveMetrics(
                screenHorizontalPadding = 14.dp,
                screenVerticalPadding = 12.dp,
                sectionSpacing = 10.dp,
                cardPadding = 14.dp,
                chipSpacing = 8.dp,
                topBadgeSize = 38.dp,
                navItemSize = 46.dp,
                navIconSize = 22.dp,
                settingsHeaderSize = 30.sp
            )

            else -> ResponsiveMetrics(
                screenHorizontalPadding = 16.dp,
                screenVerticalPadding = 14.dp,
                sectionSpacing = 12.dp,
                cardPadding = 16.dp,
                chipSpacing = 10.dp,
                topBadgeSize = 40.dp,
                navItemSize = 48.dp,
                navIconSize = 24.dp,
                settingsHeaderSize = 32.sp
            )
        }
    }
}

