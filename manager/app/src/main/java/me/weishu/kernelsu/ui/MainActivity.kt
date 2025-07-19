package me.weishu.kernelsu.ui

import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import com.ramcosta.composedestinations.DestinationsNavHost
import com.ramcosta.composedestinations.animations.NavHostAnimatedDestinationStyle
import com.ramcosta.composedestinations.generated.NavGraphs
import me.weishu.kernelsu.Natives
import me.weishu.kernelsu.ksuApp
import me.weishu.kernelsu.ui.component.NavigationBar
import me.weishu.kernelsu.ui.screen.BottomBarDestination
import me.weishu.kernelsu.ui.theme.KernelSUTheme
import me.weishu.kernelsu.ui.util.LocalSnackbarHost
import me.weishu.kernelsu.ui.util.rootAvailable
import me.weishu.kernelsu.ui.util.install
import top.yukonga.miuix.kmp.basic.Scaffold
import top.yukonga.miuix.kmp.utils.getWindowSize

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
                val bottomBarRoutes = remember {
                    BottomBarDestination.entries.map { it.direction.route }.toSet()
                }
                val windowWidth = getWindowSize().width
                val easing = CubicBezierEasing(0.12f, 0.38f, 0.2f, 1f)
                Scaffold(
                    bottomBar = { BottomBar(navController) },
                    contentWindowInsets = WindowInsets(0, 0, 0, 0)
                ) { innerPadding ->
                    CompositionLocalProvider(
                        LocalSnackbarHost provides snackBarHostState,
                    ) {
                        DestinationsNavHost(
                            modifier = Modifier.padding(innerPadding),
                            navGraph = NavGraphs.root,
                            navController = navController,
                            defaultTransitions = object : NavHostAnimatedDestinationStyle() {
                                override val enterTransition: AnimatedContentTransitionScope<NavBackStackEntry>.() -> EnterTransition = {
                                    if (targetState.destination.route !in bottomBarRoutes) {
                                        slideInHorizontally(
                                            initialOffsetX = { windowWidth },
                                            animationSpec = tween(durationMillis = 500, easing = easing)
                                        )
                                    } else {
                                        fadeIn(initialAlpha = 1f)
                                    }
                                }

                                override val exitTransition: AnimatedContentTransitionScope<NavBackStackEntry>.() -> ExitTransition = {
                                    if (initialState.destination.route in bottomBarRoutes && targetState.destination.route !in bottomBarRoutes) {
                                        slideOutHorizontally(
                                            targetOffsetX = { -windowWidth / 5 },
                                            animationSpec = tween(durationMillis = 500, easing = easing)
                                        ) + fadeOut(
                                            animationSpec = tween(durationMillis = 500),
                                            targetAlpha = 0.5f
                                        )
                                    } else {
                                        fadeOut(targetAlpha = 1f)
                                    }
                                }

                                override val popEnterTransition: AnimatedContentTransitionScope<NavBackStackEntry>.() -> EnterTransition = {
                                    if (targetState.destination.route in bottomBarRoutes) {
                                        slideInHorizontally(
                                            initialOffsetX = { -windowWidth / 5 },
                                            animationSpec = tween(durationMillis = 500, easing = easing)
                                        ) + fadeIn(
                                            animationSpec = tween(durationMillis = 500),
                                            initialAlpha = 0.5f
                                        )
                                    } else {
                                        fadeIn(initialAlpha = 1f)
                                    }
                                }

                                override val popExitTransition: AnimatedContentTransitionScope<NavBackStackEntry>.() -> ExitTransition = {
                                    if (initialState.destination.route !in bottomBarRoutes) {
                                        slideOutHorizontally(
                                            targetOffsetX = { windowWidth },
                                            animationSpec = tween(durationMillis = 500, easing = easing)
                                        )
                                    } else {
                                        fadeOut(targetAlpha = 1f)
                                    }
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
private fun BottomBar(navController: NavHostController) {
    val isManager = Natives.becomeManager(ksuApp.packageName)
    val fullFeatured = isManager && !Natives.requireNewKernel() && rootAvailable()

    val item = BottomBarDestination.entries.filter { destination ->
        fullFeatured || !destination.rootRequired

    }
    if (item.size == 1) return

    NavigationBar(
        item,
        navController = navController
    )

}
