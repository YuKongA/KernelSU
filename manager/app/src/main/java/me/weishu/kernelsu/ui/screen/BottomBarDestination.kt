package me.weishu.kernelsu.ui.screen

import androidx.annotation.StringRes
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Cottage
import androidx.compose.material.icons.rounded.Extension
import androidx.compose.material.icons.rounded.Security
import androidx.compose.ui.graphics.vector.ImageVector
import me.weishu.kernelsu.R

enum class BottomBarDestination(
    @get:StringRes val label: Int,
    val iconSelected: ImageVector,
    val iconNotSelected: ImageVector,
) {
    Home(R.string.home, Icons.Rounded.Cottage, Icons.Rounded.Cottage),
    SuperUser(R.string.superuser, Icons.Rounded.Security, Icons.Rounded.Security),
    Module(R.string.module, Icons.Rounded.Extension, Icons.Rounded.Extension)
}
