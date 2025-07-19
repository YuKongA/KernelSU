package me.weishu.kernelsu.ui.component


import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.displayCutout
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.union
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Alignment.Companion.CenterHorizontally
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.ramcosta.composedestinations.generated.NavGraphs
import com.ramcosta.composedestinations.utils.isRouteOnBackStackAsState
import com.ramcosta.composedestinations.utils.rememberDestinationsNavigator
import me.weishu.kernelsu.ui.screen.BottomBarDestination
import top.yukonga.miuix.kmp.basic.HorizontalDivider
import top.yukonga.miuix.kmp.basic.Text
import top.yukonga.miuix.kmp.theme.MiuixTheme
import top.yukonga.miuix.kmp.utils.Platform
import top.yukonga.miuix.kmp.utils.platform

@Composable
fun NavigationBar(
    items: List<BottomBarDestination>,
    navController: NavController,
    modifier: Modifier = Modifier,
    color: Color = MiuixTheme.colorScheme.surfaceContainer,
    showDivider: Boolean = true,
    defaultWindowInsetsPadding: Boolean = true
) {

    val navigator = navController.rememberDestinationsNavigator()

    val columnModifier = remember(modifier, color) {
        modifier
            .fillMaxWidth()
            .background(color)
    }

    Column(
        modifier = columnModifier
    ) {
        if (showDivider) {
            HorizontalDivider()
        }
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            items.forEachIndexed { index, destination ->
                val isSelected by navController.isRouteOnBackStackAsState(destination.direction)
                val label = stringResource(destination.label)

                var isPressed by remember { mutableStateOf(false) }
                val onSurfaceContainerColor = MiuixTheme.colorScheme.onSurfaceContainer
                val onSurfaceContainerVariantColor = MiuixTheme.colorScheme.onSurfaceContainerVariant

                val tint by animateColorAsState(
                    targetValue = when {
                        isPressed -> if (isSelected) {
                            onSurfaceContainerColor.copy(alpha = 0.6f)
                        } else {
                            onSurfaceContainerVariantColor.copy(alpha = 0.6f)
                        }

                        isSelected -> onSurfaceContainerColor
                        else -> onSurfaceContainerVariantColor
                    },
                    label = "tintAnimation"
                )
                val fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal

                val itemPlatform = platform()
                val itemHeight = if (itemPlatform != Platform.IOS) 64.dp else 48.dp
                val itemWeight = 1f / items.size

                val itemModifier = remember(itemHeight, itemWeight, index) {
                    Modifier
                        .height(itemHeight)
                        .weight(itemWeight)
                        .pointerInput(index) {
                            detectTapGestures(
                                onPress = {
                                    isPressed = true
                                    tryAwaitRelease()
                                    isPressed = false
                                },
                                onTap = {
                                    if (isSelected) {
                                        navigator.popBackStack(destination.direction, true)
                                    }
                                    navigator.navigate(destination.direction) {
                                        popUpTo(NavGraphs.root) {
                                            inclusive = true
                                        }
                                        launchSingleTop = true
                                    }
                                }
                            )
                        }
                }

                Column(
                    modifier = itemModifier,
                    horizontalAlignment = CenterHorizontally
                ) {
                    val imageColorFilter = remember(tint) { ColorFilter.tint(tint) }
                    Image(
                        modifier = Modifier
                            .size(32.dp)
                            .padding(top = 6.dp),
                        imageVector = if (isSelected) destination.iconSelected else destination.iconNotSelected,
                        contentDescription = label,
                        colorFilter = imageColorFilter
                    )
                    Text(
                        modifier = Modifier.padding(bottom = if (itemPlatform != Platform.IOS) 12.dp else 0.dp),
                        text = label,
                        color = tint,
                        textAlign = TextAlign.Center,
                        fontSize = 12.sp,
                        fontWeight = fontWeight
                    )
                }
            }
        }
        if (defaultWindowInsetsPadding) {
            val windowInsets = WindowInsets.systemBars.union(WindowInsets.displayCutout).only(
                WindowInsetsSides.Horizontal + WindowInsetsSides.Bottom
            )
            Spacer(
                modifier = Modifier
                    .fillMaxWidth()
                    .windowInsetsPadding(windowInsets)
                    .pointerInput(Unit) { detectTapGestures { /* Do nothing to consume the click */ } }
            )
        }
    }
}