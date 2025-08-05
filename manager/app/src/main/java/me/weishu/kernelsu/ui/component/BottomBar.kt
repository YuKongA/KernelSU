package me.weishu.kernelsu.ui.component

import androidx.annotation.StringRes
import androidx.compose.foundation.pager.PagerState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Cottage
import androidx.compose.material.icons.rounded.Extension
import androidx.compose.material.icons.rounded.Security
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import dev.chrisbanes.haze.HazeState
import dev.chrisbanes.haze.HazeStyle
import dev.chrisbanes.haze.hazeEffect
import kotlinx.coroutines.launch
import me.weishu.kernelsu.Natives
import me.weishu.kernelsu.R
import me.weishu.kernelsu.ksuApp
import me.weishu.kernelsu.ui.util.rootAvailable
import top.yukonga.miuix.kmp.basic.NavigationBar
import top.yukonga.miuix.kmp.basic.NavigationItem


@Composable
fun BottomBar(
    pagerState: PagerState,
    hazeState: HazeState,
    hazeStyle: HazeStyle
) {
    val isManager = Natives.becomeManager(ksuApp.packageName)
    val fullFeatured = isManager && !Natives.requireNewKernel() && rootAvailable()

    if (!fullFeatured) return

    val currentPager = pagerState.currentPage
    val coroutineScope = rememberCoroutineScope()

    val item = BottomBarDestination.entries.mapIndexed { index, destination ->
        NavigationItem(
            label = stringResource(destination.label),
            icon = destination.icon,
        )
    }

    NavigationBar(
        modifier = Modifier
            .hazeEffect(hazeState) {
                style = hazeStyle
                blurRadius = 30.dp
                noiseFactor = 0f
            },
        color = Color.Transparent,
        items = item,
        selected = currentPager,
        onClick = { index ->
            coroutineScope.launch {
                pagerState.animateScrollToPage(index)
            }
        }
    )
}

enum class BottomBarDestination(
    @get:StringRes val label: Int,
    val icon: ImageVector,
) {
    Home(R.string.home, Icons.Rounded.Cottage),
    SuperUser(R.string.superuser, Icons.Rounded.Security),
    Module(R.string.module, Icons.Rounded.Extension)
}
