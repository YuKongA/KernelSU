package me.weishu.kernelsu.ui.screen

import androidx.annotation.StringRes
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Apps
import androidx.compose.material.icons.rounded.Home
import androidx.compose.material.icons.rounded.Security
import androidx.compose.ui.graphics.vector.ImageVector
import com.ramcosta.composedestinations.generated.destinations.HomeScreenDestination
import com.ramcosta.composedestinations.generated.destinations.ModuleScreenDestination
import com.ramcosta.composedestinations.generated.destinations.SuperUserScreenDestination
import com.ramcosta.composedestinations.spec.DirectionDestinationSpec
import me.weishu.kernelsu.R

enum class BottomBarDestination(
    val direction: DirectionDestinationSpec,
    @get:StringRes val label: Int,
    val iconSelected: ImageVector,
    val iconNotSelected: ImageVector,
    val rootRequired: Boolean,
) {
    Home(HomeScreenDestination, R.string.home, Icons.Rounded.Home, Icons.Rounded.Home, false),
    SuperUser(SuperUserScreenDestination, R.string.superuser, Icons.Rounded.Security, Icons.Rounded.Security, true),
    Module(ModuleScreenDestination, R.string.module, Icons.Rounded.Apps, Icons.Rounded.Apps, true)
}
