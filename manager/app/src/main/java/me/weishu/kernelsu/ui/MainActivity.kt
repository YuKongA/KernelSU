package me.weishu.kernelsu.ui

import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavBackStackEntry
import androidx.navigation.compose.rememberNavController
import com.ramcosta.composedestinations.DestinationsNavHost
import com.ramcosta.composedestinations.animations.NavHostAnimatedDestinationStyle
import com.ramcosta.composedestinations.annotation.Destination
import com.ramcosta.composedestinations.annotation.RootGraph
import com.ramcosta.composedestinations.generated.NavGraphs
import com.ramcosta.composedestinations.navigation.DestinationsNavigator
import dev.chrisbanes.haze.HazeState
import dev.chrisbanes.haze.HazeStyle
import dev.chrisbanes.haze.HazeTint
import dev.chrisbanes.haze.hazeEffect
import dev.chrisbanes.haze.hazeSource
import kotlinx.coroutines.launch
import me.weishu.kernelsu.Natives
import me.weishu.kernelsu.ksuApp
import me.weishu.kernelsu.ui.screen.BottomBarDestination
import me.weishu.kernelsu.ui.screen.HomePager
import me.weishu.kernelsu.ui.screen.ModulePager
import me.weishu.kernelsu.ui.screen.SuperUserPager
import me.weishu.kernelsu.ui.theme.KernelSUTheme
import me.weishu.kernelsu.ui.util.LocalSnackbarHost
import me.weishu.kernelsu.ui.util.install
import me.weishu.kernelsu.ui.util.rootAvailable
import top.yukonga.miuix.kmp.basic.NavigationBar
import top.yukonga.miuix.kmp.basic.NavigationItem
import top.yukonga.miuix.kmp.basic.Scaffold
import top.yukonga.miuix.kmp.theme.MiuixTheme

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {

        // Enable edge to edge
        enableEdgeToEdge()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            window.isNavigationBarContrastEnforced = false
        }

        super.onCreate(savedInstanceState)

        val isManager = Natives.becomeManager(ksuApp.packageName)
        if (isManager) install()

        setContent {
            KernelSUTheme {
                val navController = rememberNavController()
                val snackBarHostState = remember { SnackbarHostState() }

                Scaffold { innerPadding ->
                    CompositionLocalProvider(
                        LocalSnackbarHost provides snackBarHostState,
                    ) {
                        DestinationsNavHost(
                            modifier = Modifier,
                            navGraph = NavGraphs.root,
                            navController = navController,
                            defaultTransitions = object : NavHostAnimatedDestinationStyle() {
                                override val enterTransition: AnimatedContentTransitionScope<NavBackStackEntry>.() -> EnterTransition =
                                    {
                                        slideInHorizontally(
                                            initialOffsetX = { it },
                                            animationSpec = tween(durationMillis = 500, easing = FastOutSlowInEasing)
                                        )
                                    }

                                override val exitTransition: AnimatedContentTransitionScope<NavBackStackEntry>.() -> ExitTransition =
                                    {
                                        slideOutHorizontally(
                                            targetOffsetX = { -it / 5 },
                                            animationSpec = tween(durationMillis = 500, easing = FastOutSlowInEasing)
                                        )
                                    }

                                override val popEnterTransition: AnimatedContentTransitionScope<NavBackStackEntry>.() -> EnterTransition =
                                    {
                                        slideInHorizontally(
                                            initialOffsetX = { -it / 5 },
                                            animationSpec = tween(durationMillis = 500, easing = FastOutSlowInEasing)
                                        )
                                    }

                                override val popExitTransition: AnimatedContentTransitionScope<NavBackStackEntry>.() -> ExitTransition =
                                    {
                                        slideOutHorizontally(
                                            targetOffsetX = { it },
                                            animationSpec = tween(durationMillis = 500, easing = FastOutSlowInEasing)
                                        )
                                    }
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
@Destination<RootGraph>(start = true)
fun MainScreen(navController: DestinationsNavigator) {
    val pagerState = rememberPagerState(initialPage = 0, pageCount = { 3 })
    val hazeState = remember { HazeState() }
    val hazeStyle = HazeStyle(
        backgroundColor = MiuixTheme.colorScheme.background,
        tint = HazeTint(MiuixTheme.colorScheme.background.copy(0.67f))
    )

    Scaffold(
        bottomBar = { BottomBar(pagerState, hazeState, hazeStyle) },
    ) { innerPadding ->
        HorizontalPager(
            modifier = Modifier
                .hazeSource(state = hazeState),
            state = pagerState,
            userScrollEnabled = false,
            beyondViewportPageCount = 1
        ) {
            when (it) {
                0 -> HomePager(pagerState, navController, innerPadding.calculateBottomPadding())
                1 -> SuperUserPager(navController, innerPadding.calculateBottomPadding())
                2 -> ModulePager(navController, innerPadding.calculateBottomPadding())
            }
        }
    }
}


@Composable
private fun BottomBar(
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

        val isSelected = currentPager == index

        NavigationItem(
            label = stringResource(destination.label),
            icon = if (isSelected) destination.iconSelected else destination.iconNotSelected,
        )
    }

    NavigationBar(
        modifier = Modifier
            .hazeEffect(hazeState) {
                style = hazeStyle
                blurRadius = 25.dp
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
