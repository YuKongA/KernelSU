package me.weishu.kernelsu.ui.screen

import android.annotation.SuppressLint
import androidx.annotation.StringRes
import androidx.compose.animation.Crossfade
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.AccountCircle
import androidx.compose.material.icons.rounded.Android
import androidx.compose.material.icons.rounded.Security
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarHost
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.dropUnlessResumed
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.ramcosta.composedestinations.annotation.Destination
import com.ramcosta.composedestinations.annotation.RootGraph
import com.ramcosta.composedestinations.generated.destinations.AppProfileTemplateScreenDestination
import com.ramcosta.composedestinations.generated.destinations.TemplateEditorScreenDestination
import com.ramcosta.composedestinations.navigation.DestinationsNavigator
import kotlinx.coroutines.launch
import me.weishu.kernelsu.Natives
import me.weishu.kernelsu.R
import me.weishu.kernelsu.ui.component.profile.AppProfileConfig
import me.weishu.kernelsu.ui.component.profile.RootProfileConfig
import me.weishu.kernelsu.ui.component.profile.TemplateConfig
import me.weishu.kernelsu.ui.util.LocalSnackbarHost
import me.weishu.kernelsu.ui.util.forceStopApp
import me.weishu.kernelsu.ui.util.getSepolicy
import me.weishu.kernelsu.ui.util.launchApp
import me.weishu.kernelsu.ui.util.restartApp
import me.weishu.kernelsu.ui.util.setSepolicy
import me.weishu.kernelsu.ui.viewmodel.SuperUserViewModel
import me.weishu.kernelsu.ui.viewmodel.getTemplateInfoById
import top.yukonga.miuix.kmp.basic.BasicComponent
import top.yukonga.miuix.kmp.basic.Card
import top.yukonga.miuix.kmp.basic.HorizontalDivider
import top.yukonga.miuix.kmp.basic.Icon
import top.yukonga.miuix.kmp.basic.IconButton
import top.yukonga.miuix.kmp.basic.ListPopup
import top.yukonga.miuix.kmp.basic.ListPopupColumn
import top.yukonga.miuix.kmp.basic.MiuixScrollBehavior
import top.yukonga.miuix.kmp.basic.Scaffold
import top.yukonga.miuix.kmp.basic.ScrollBehavior
import top.yukonga.miuix.kmp.basic.TabRowWithContour
import top.yukonga.miuix.kmp.basic.Text
import top.yukonga.miuix.kmp.basic.TopAppBar
import top.yukonga.miuix.kmp.extra.DropdownColors
import top.yukonga.miuix.kmp.extra.DropdownDefaults
import top.yukonga.miuix.kmp.extra.SuperSwitch
import top.yukonga.miuix.kmp.theme.MiuixTheme
import top.yukonga.miuix.kmp.theme.MiuixTheme.colorScheme

/**
 * @author weishu
 * @date 2023/5/16.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Destination<RootGraph>
@Composable
fun AppProfileScreen(
    navigator: DestinationsNavigator,
    appInfo: SuperUserViewModel.AppInfo,
) {
    val context = LocalContext.current
    val snackBarHost = LocalSnackbarHost.current
    val scrollBehavior = MiuixScrollBehavior()
    val scope = rememberCoroutineScope()
    val failToUpdateAppProfile = stringResource(R.string.failed_to_update_app_profile).format(appInfo.label)
    val failToUpdateSepolicy = stringResource(R.string.failed_to_update_sepolicy).format(appInfo.label)
    val suNotAllowed = stringResource(R.string.su_not_allowed).format(appInfo.label)

    val packageName = appInfo.packageName
    val initialProfile = Natives.getAppProfile(packageName, appInfo.uid)
    if (initialProfile.allowSu) {
        initialProfile.rules = getSepolicy(packageName)
    }
    var profile by rememberSaveable {
        mutableStateOf(initialProfile)
    }

    Scaffold(
        topBar = {
            TopBar(
                onBack = dropUnlessResumed { navigator.popBackStack() },
                scrollBehavior = scrollBehavior
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
    ) { paddingValues ->
        AppProfileInner(
            modifier = Modifier
                .padding(paddingValues)
                .nestedScroll(scrollBehavior.nestedScrollConnection)
                .verticalScroll(rememberScrollState()),
            packageName = appInfo.packageName,
            appLabel = appInfo.label,
            appIcon = {
                AsyncImage(
                    model = ImageRequest.Builder(context).data(appInfo.packageInfo).crossfade(true).build(),
                    contentDescription = appInfo.label,
                    modifier = Modifier
                        .padding(4.dp)
                        .width(48.dp)
                        .height(48.dp)
                )
            },
            profile = profile,
            onViewTemplate = {
                getTemplateInfoById(it)?.let { info ->
                    navigator.navigate(TemplateEditorScreenDestination(info))
                }
            },
            onManageTemplate = {
                navigator.navigate(AppProfileTemplateScreenDestination())
            },
            onProfileChange = {
                scope.launch {
                    if (it.allowSu) {
                        // sync with allowlist.c - forbid_system_uid
                        if (appInfo.uid < 2000 && appInfo.uid != 1000) {
                            snackBarHost.showSnackbar(suNotAllowed)
                            return@launch
                        }
                        if (!it.rootUseDefault && it.rules.isNotEmpty() && !setSepolicy(profile.name, it.rules)) {
                            snackBarHost.showSnackbar(failToUpdateSepolicy)
                            return@launch
                        }
                    }
                    if (!Natives.setAppProfile(it)) {
                        snackBarHost.showSnackbar(failToUpdateAppProfile.format(appInfo.uid))
                    } else {
                        profile = it
                    }
                }
            },
        )
    }
}

@Composable
private fun AppProfileInner(
    modifier: Modifier = Modifier,
    packageName: String,
    appLabel: String,
    appIcon: @Composable () -> Unit,
    profile: Natives.Profile,
    onViewTemplate: (id: String) -> Unit = {},
    onManageTemplate: () -> Unit = {},
    onProfileChange: (Natives.Profile) -> Unit,
) {
    val isRootGranted = profile.allowSu

    Column(
        modifier = modifier
    ) {
        AppMenuBox(packageName) {
            BasicComponent(
                title = appLabel,
                summary = packageName,
                leftAction = appIcon,
            )
        }

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .padding(bottom = 12.dp),
        ) {
            SuperSwitch(
                leftAction = {
                    Icon(
                        imageVector = Icons.Rounded.Security,
                        contentDescription = null,
                        modifier = Modifier.padding(end = 16.dp)
                    )
                },
                title = stringResource(id = R.string.superuser),
                checked = isRootGranted,
                onCheckedChange = { onProfileChange(profile.copy(allowSu = it)) },
            )
        }

        Crossfade(targetState = isRootGranted, label = "") { current ->
            Column(
                modifier = Modifier.padding(bottom = 12.dp)
            ) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                        .padding(bottom = 12.dp),
                ) {
                    if (current) {
                        val initialMode = if (profile.rootUseDefault) {
                            Mode.Default
                        } else if (profile.rootTemplate != null) {
                            Mode.Template
                        } else {
                            Mode.Custom
                        }
                        var mode by rememberSaveable {
                            mutableStateOf(initialMode)
                        }
                        ProfileBox(mode, true) {
                            // template mode shouldn't change profile here!
                            if (it == Mode.Default || it == Mode.Custom) {
                                onProfileChange(profile.copy(rootUseDefault = it == Mode.Default))
                            }
                            mode = it
                        }
                        Crossfade(targetState = mode, label = "") { currentMode ->
                            if (currentMode == Mode.Default) {
                                Spacer(Modifier.height(16.dp))
                            }
                            if (currentMode == Mode.Template) {
                                TemplateConfig(
                                    profile = profile,
                                    onViewTemplate = onViewTemplate,
                                    onManageTemplate = onManageTemplate,
                                    onProfileChange = onProfileChange
                                )
                            } else if (mode == Mode.Custom) {
                                RootProfileConfig(
                                    fixedName = true,
                                    profile = profile,
                                    onProfileChange = onProfileChange
                                )
                            }
                        }
                    } else {
                        val mode = if (profile.nonRootUseDefault) Mode.Default else Mode.Custom
                        ProfileBox(mode, false) {
                            onProfileChange(profile.copy(nonRootUseDefault = (it == Mode.Default)))
                        }
                        Crossfade(targetState = mode, label = "") { currentMode ->
                            val modifyEnabled = currentMode == Mode.Custom
                            AppProfileConfig(
                                fixedName = true,
                                profile = profile,
                                enabled = modifyEnabled,
                                onProfileChange = onProfileChange
                            )
                        }
                    }
                }
            }
        }
    }
}

private enum class Mode(@StringRes private val res: Int) {
    Default(R.string.profile_default), Template(R.string.profile_template), Custom(R.string.profile_custom);

    val text: String
        @Composable get() = stringResource(res)
}

@Composable
private fun TopBar(
    onBack: () -> Unit,
    scrollBehavior: ScrollBehavior? = null
) {
    TopAppBar(
        title = stringResource(R.string.profile),
        navigationIcon = {
            IconButton(
                modifier = Modifier.padding(start = 16.dp),
                onClick = onBack
            ) {
                Icon(
                    Icons.AutoMirrored.Rounded.ArrowBack,
                    contentDescription = null
                )
            }
        },
        scrollBehavior = scrollBehavior
    )
}

@Composable
private fun ProfileBox(
    mode: Mode,
    hasTemplate: Boolean,
    onModeChange: (Mode) -> Unit,
) {
    BasicComponent(
        title = stringResource(R.string.profile),
        summary = mode.text,
        leftAction = {
            Icon(
                Icons.Rounded.AccountCircle,
                modifier = Modifier.padding(end = 16.dp),
                contentDescription = null
            )
        },
    )
    HorizontalDivider(thickness = Dp.Hairline)
    val defaultText = stringResource(R.string.profile_default)
    val templateText = stringResource(R.string.profile_template)
    val customText = stringResource(R.string.profile_custom)

    val modesAndTitles = remember(hasTemplate, defaultText, templateText, customText) {
        buildList {
            add(Mode.Default to defaultText)
            if (hasTemplate) {
                add(Mode.Template to templateText)
            }
            add(Mode.Custom to customText)
        }
    }

    val tabTitles = modesAndTitles.map { it.second }
    val selectedIndex = modesAndTitles.indexOfFirst { it.first == mode }

    TabRowWithContour(
        tabs = tabTitles,
        selectedTabIndex = if (selectedIndex == -1) 0 else selectedIndex,
        onTabSelected = { index ->
            onModeChange(modesAndTitles[index].first)
        },
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
    )
}

@SuppressLint("UnusedBoxWithConstraintsScope")
@Composable
private fun AppMenuBox(packageName: String, content: @Composable () -> Unit) {
    val expanded = remember { mutableStateOf(false) }
    BoxWithConstraints(
        Modifier
            .fillMaxSize()
            .pointerInput(Unit) {
                detectTapGestures {
                    expanded.value = true
                }
            }
    ) {
        content()

        ListPopup(
            show = expanded,
            onDismissRequest = { expanded.value = false }
        ) {
            ListPopupColumn {
                val items = listOf(
                    stringResource(id = R.string.launch_app),
                    stringResource(id = R.string.force_stop_app),
                    stringResource(id = R.string.restart_app)
                )

                items.forEachIndexed { index, text ->
                    DropdownItem(
                        text = text,
                        optionSize = items.size,
                        index = index,
                        onSelectedIndexChange = { selectedIndex ->
                            when (selectedIndex) {
                                0 -> launchApp(packageName)
                                1 -> forceStopApp(packageName)
                                2 -> restartApp(packageName)
                            }
                            expanded.value = false
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun DropdownItem(
    text: String,
    optionSize: Int,
    index: Int,
    dropdownColors: DropdownColors = DropdownDefaults.dropdownColors(),
    onSelectedIndexChange: (Int) -> Unit
) {
    val currentOnSelectedIndexChange = rememberUpdatedState(onSelectedIndexChange)
    val additionalTopPadding = if (index == 0) 20f.dp else 12f.dp
    val additionalBottomPadding = if (index == optionSize - 1) 20f.dp else 12f.dp

    Row(
        modifier = Modifier
            .clickable { currentOnSelectedIndexChange.value(index) }
            .background(dropdownColors.containerColor)
            .padding(horizontal = 20.dp)
            .padding(
                top = additionalTopPadding,
                bottom = additionalBottomPadding
            ),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = text,
            fontSize = MiuixTheme.textStyles.body1.fontSize,
            fontWeight = FontWeight.Medium,
            color = dropdownColors.contentColor,
        )
    }
}

@Preview
@Composable
private fun AppProfilePreview() {
    var profile by remember { mutableStateOf(Natives.Profile("")) }
    AppProfileInner(
        packageName = "icu.nullptr.test",
        appLabel = "Test",
        appIcon = { Icon(Icons.Rounded.Android, null) },
        profile = profile,
        onProfileChange = {
            profile = it
        },
    )
}

