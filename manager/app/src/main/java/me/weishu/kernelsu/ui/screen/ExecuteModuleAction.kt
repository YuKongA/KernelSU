package me.weishu.kernelsu.ui.screen

import android.os.Environment
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.add
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.foundation.layout.captionBar
import androidx.compose.foundation.layout.displayCutout
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Save
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarHost
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.key
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.dropUnlessResumed
import com.ramcosta.composedestinations.annotation.Destination
import com.ramcosta.composedestinations.annotation.RootGraph
import com.ramcosta.composedestinations.navigation.DestinationsNavigator
import dev.chrisbanes.haze.HazeState
import dev.chrisbanes.haze.HazeStyle
import dev.chrisbanes.haze.HazeTint
import dev.chrisbanes.haze.hazeEffect
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import me.weishu.kernelsu.R
import me.weishu.kernelsu.ui.component.KeyEventBlocker
import me.weishu.kernelsu.ui.util.LocalSnackbarHost
import me.weishu.kernelsu.ui.util.runModuleAction
import top.yukonga.miuix.kmp.basic.Icon
import top.yukonga.miuix.kmp.basic.IconButton
import top.yukonga.miuix.kmp.basic.Scaffold
import top.yukonga.miuix.kmp.basic.Text
import top.yukonga.miuix.kmp.basic.TopAppBar
import top.yukonga.miuix.kmp.theme.MiuixTheme.colorScheme
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
@Destination<RootGraph>
fun ExecuteModuleActionScreen(navigator: DestinationsNavigator, moduleId: String) {
    var text by rememberSaveable { mutableStateOf("") }
    var tempText: String
    val logContent = rememberSaveable { StringBuilder() }
    val snackBarHost = LocalSnackbarHost.current
    val scope = rememberCoroutineScope()
    val scrollState = rememberScrollState()
    var actionResult: Boolean

    LaunchedEffect(Unit) {
        if (text.isNotEmpty()) {
            return@LaunchedEffect
        }
        withContext(Dispatchers.IO) {
            runModuleAction(
                moduleId = moduleId,
                onStdout = {
                    tempText = "$it\n"
                    if (tempText.startsWith("[H[J")) { // clear command
                        text = tempText.substring(6)
                    } else {
                        text += tempText
                    }
                    logContent.append(it).append("\n")
                },
                onStderr = {
                    logContent.append(it).append("\n")
                }
            ).let {
                actionResult = it
            }
        }
        if (actionResult) navigator.popBackStack()
    }

    val hazeState = remember { HazeState() }
    val hazeStyle = HazeStyle(
        backgroundColor = colorScheme.background,
        tint = HazeTint(colorScheme.background.copy(0.67f))
    )

    Scaffold(
        topBar = {
            TopBar(
                onBack = dropUnlessResumed {
                    navigator.popBackStack()
                },
                onSave = {
                    scope.launch {
                        val format = SimpleDateFormat("yyyy-MM-dd-HH-mm-ss", Locale.getDefault())
                        val date = format.format(Date())
                        val file = File(
                            Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS),
                            "KernelSU_module_action_log_${date}.log"
                        )
                        file.writeText(logContent.toString())
                        snackBarHost.showSnackbar("Log saved to ${file.absolutePath}")
                    }
                },
                hazeState = hazeState,
                hazeStyle = hazeStyle
            )
        },
        snackbarHost = {
            SnackbarHost(hostState = snackBarHost) {
                Snackbar(
                    snackbarData = it,
                    containerColor = colorScheme.onBackground,
                    contentColor = colorScheme.background,
                    actionColor = colorScheme.primary
                )
            }
        },
        popupHost = { },
        contentWindowInsets = WindowInsets.systemBars.add(WindowInsets.displayCutout).only(WindowInsetsSides.Horizontal)
    ) { innerPadding ->
        val layoutDirection = LocalLayoutDirection.current
        KeyEventBlocker {
            it.key == Key.VolumeDown || it.key == Key.VolumeUp
        }
        Column(
            modifier = Modifier
                .fillMaxSize(1f)
                .padding(
                    start = innerPadding.calculateStartPadding(layoutDirection),
                    end = innerPadding.calculateStartPadding(layoutDirection),
                )
                .verticalScroll(scrollState),
        ) {
            LaunchedEffect(text) {
                scrollState.animateScrollTo(scrollState.maxValue)
            }
            Spacer(Modifier.height(innerPadding.calculateTopPadding()))
            Text(
                modifier = Modifier.padding(8.dp),
                text = text,
                fontSize = 12.sp,
                fontFamily = FontFamily.Monospace,
            )
            Spacer(
                Modifier.height(
                    12.dp + WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding() +
                            WindowInsets.captionBar.asPaddingValues().calculateBottomPadding()
                )
            )
        }
    }
}

@Composable
private fun TopBar(
    onBack: () -> Unit = {},
    onSave: () -> Unit = {},
    hazeState: HazeState,
    hazeStyle: HazeStyle
) {
    TopAppBar(
        modifier = Modifier
            .hazeEffect(state = hazeState) {
                style = hazeStyle
                blurRadius = 25.dp
                noiseFactor = 0f
            },
        color = Color.Transparent,
        title = stringResource(R.string.action),
        navigationIcon = {
            IconButton(
                modifier = Modifier.padding(start = 16.dp),
                onClick = onBack
            ) {
                Icon(
                    Icons.AutoMirrored.Rounded.ArrowBack,
                    contentDescription = null,
                    tint = colorScheme.onBackground
                )
            }
        },
        actions = {
            IconButton(
                modifier = Modifier.padding(end = 16.dp),
                onClick = onSave
            ) {
                Icon(
                    imageVector = Icons.Rounded.Save,
                    contentDescription = stringResource(id = R.string.save_log),
                    tint = colorScheme.onBackground
                )
            }
        }
    )
}