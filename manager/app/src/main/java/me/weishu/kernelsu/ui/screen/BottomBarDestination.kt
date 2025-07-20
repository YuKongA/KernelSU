package me.weishu.kernelsu.ui.screen

import androidx.annotation.StringRes
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Apps
import androidx.compose.material.icons.rounded.Home
import androidx.compose.material.icons.rounded.Security
import androidx.compose.ui.graphics.vector.ImageVector
import me.weishu.kernelsu.R

enum class BottomBarDestination(
    @StringRes val label: Int,
    val iconSelected: ImageVector,
    val iconNotSelected: ImageVector,
) {
    Home( R.string.home, Icons.Rounded.Home, Icons.Rounded.Home),
    SuperUser( R.string.superuser, Icons.Rounded.Security, Icons.Rounded.Security),
    Module(R.string.module, Icons.Rounded.Apps, Icons.Rounded.Apps)
}
