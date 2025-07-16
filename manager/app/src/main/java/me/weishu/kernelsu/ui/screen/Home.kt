package me.weishu.kernelsu.ui.screen

import android.content.Context
import android.os.Build
import android.os.PowerManager
import android.system.Os
import androidx.annotation.StringRes
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.captionBar
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Archive
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.outlined.Block
import androidx.compose.material.icons.outlined.Warning
import androidx.compose.material.icons.rounded.CheckCircleOutline
import androidx.compose.material.icons.rounded.Link
import androidx.compose.material.icons.rounded.Warning
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.pm.PackageInfoCompat
import com.ramcosta.composedestinations.annotation.Destination
import com.ramcosta.composedestinations.annotation.RootGraph
import com.ramcosta.composedestinations.generated.destinations.InstallScreenDestination
import com.ramcosta.composedestinations.generated.destinations.ModuleScreenDestination
import com.ramcosta.composedestinations.generated.destinations.SettingScreenDestination
import com.ramcosta.composedestinations.generated.destinations.SuperUserScreenDestination
import com.ramcosta.composedestinations.navigation.DestinationsNavigator
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import me.weishu.kernelsu.KernelVersion
import me.weishu.kernelsu.Natives
import me.weishu.kernelsu.R
import me.weishu.kernelsu.getKernelVersion
import me.weishu.kernelsu.ksuApp
import me.weishu.kernelsu.ui.component.KsuIsValid
import me.weishu.kernelsu.ui.component.rememberConfirmDialog
import me.weishu.kernelsu.ui.util.checkNewVersion
import me.weishu.kernelsu.ui.util.getModuleCount
import me.weishu.kernelsu.ui.util.getSELinuxStatus
import me.weishu.kernelsu.ui.util.getSuperuserCount
import me.weishu.kernelsu.ui.util.module.LatestVersionInfo
import me.weishu.kernelsu.ui.util.reboot
import me.weishu.kernelsu.ui.util.rootAvailable
import top.yukonga.miuix.kmp.basic.BasicComponent
import top.yukonga.miuix.kmp.basic.Card
import top.yukonga.miuix.kmp.basic.Icon
import top.yukonga.miuix.kmp.basic.IconButton
import top.yukonga.miuix.kmp.basic.ListPopup
import top.yukonga.miuix.kmp.basic.ListPopupColumn
import top.yukonga.miuix.kmp.basic.ListPopupDefaults
import top.yukonga.miuix.kmp.basic.MiuixScrollBehavior
import top.yukonga.miuix.kmp.basic.PopupPositionProvider
import top.yukonga.miuix.kmp.basic.Scaffold
import top.yukonga.miuix.kmp.basic.ScrollBehavior
import top.yukonga.miuix.kmp.basic.Text
import top.yukonga.miuix.kmp.basic.TopAppBar
import top.yukonga.miuix.kmp.extra.DropdownImpl
import top.yukonga.miuix.kmp.theme.MiuixTheme
import top.yukonga.miuix.kmp.theme.MiuixTheme.colorScheme
import top.yukonga.miuix.kmp.utils.PressFeedbackType
import top.yukonga.miuix.kmp.utils.getWindowSize
import top.yukonga.miuix.kmp.utils.overScrollVertical

@Destination<RootGraph>(start = true)
@Composable
fun HomeScreen(navigator: DestinationsNavigator) {
    val kernelVersion = getKernelVersion()
    val scrollBehavior = MiuixScrollBehavior()

    Scaffold(
        topBar = {
            TopBar(
                kernelVersion,
                onSettingsClick = {
                    navigator.navigate(SettingScreenDestination)
                },
                onInstallClick = {
                    navigator.navigate(InstallScreenDestination)
                },
                scrollBehavior = scrollBehavior,
            )
        },
        popupHost = { },
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .height(getWindowSize().height.dp)
                .overScrollVertical()
                .nestedScroll(scrollBehavior.nestedScrollConnection)
                .padding(horizontal = 12.dp),
            contentPadding = PaddingValues(top = innerPadding.calculateTopPadding()),
            overscrollEffect = null,
        ) {
            item {
                val isManager = Natives.becomeManager(ksuApp.packageName)
                val ksuVersion = if (isManager) Natives.version else null
                val lkmMode = ksuVersion?.let {
                    if (it >= Natives.MINIMAL_SUPPORTED_KERNEL_LKM && kernelVersion.isGKI()) Natives.isLkmMode else null
                }

                Column(
                    modifier = Modifier.padding(top = 12.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    if (isManager && Natives.requireNewKernel()) {
                        WarningCard(
                            stringResource(id = R.string.require_kernel_version).format(
                                ksuVersion, Natives.MINIMAL_SUPPORTED_KERNEL
                            )
                        )
                    }
                    if (ksuVersion != null && !rootAvailable()) {
                        WarningCard(
                            stringResource(id = R.string.grant_root_failed)
                        )
                    }
                    StatusCard(
                        kernelVersion, ksuVersion, lkmMode,
                        onClickInstall = {
                            navigator.navigate(InstallScreenDestination)
                        },
                        onClickSuperuser = {
                            navigator.navigate(SuperUserScreenDestination)
                        },
                        onclickModule = {
                            navigator.navigate(ModuleScreenDestination)
                        }
                    )


                    val checkUpdate =
                        LocalContext.current.getSharedPreferences("settings", Context.MODE_PRIVATE)
                            .getBoolean("check_update", true)
                    if (checkUpdate) {
                        UpdateCard()
                    }
                    InfoCard()
                    DonateCard()
                    LearnMoreCard()
                    Spacer(
                        Modifier.height(
                            WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding()
                                    + WindowInsets.captionBar.asPaddingValues().calculateBottomPadding()
                                    - 12.dp // Arrangement.spacedBy(12.dp)
                        )
                    )
                }
            }
        }
    }
}

@Composable
fun UpdateCard() {
    val context = LocalContext.current
    val latestVersionInfo = LatestVersionInfo()
    val newVersion by produceState(initialValue = latestVersionInfo) {
        value = withContext(Dispatchers.IO) {
            checkNewVersion()
        }
    }

    val currentVersionCode = getManagerVersion(context).second
    val newVersionCode = newVersion.versionCode
    val newVersionUrl = newVersion.downloadUrl
    val changelog = newVersion.changelog

    val uriHandler = LocalUriHandler.current
    val title = stringResource(id = R.string.module_changelog)
    val updateText = stringResource(id = R.string.module_update)

    AnimatedVisibility(
        visible = newVersionCode > currentVersionCode,
        enter = fadeIn() + expandVertically(),
        exit = shrinkVertically() + fadeOut()
    ) {
        val updateDialog = rememberConfirmDialog(onConfirm = { uriHandler.openUri(newVersionUrl) })
        WarningCard(
            message = stringResource(id = R.string.new_version_available).format(newVersionCode),
            colorScheme.outline
        ) {
            if (changelog.isEmpty()) {
                uriHandler.openUri(newVersionUrl)
            } else {
                updateDialog.showConfirm(
                    title = title,
                    content = changelog,
                    markdown = true,
                    confirm = updateText
                )
            }
        }
    }
}

@Composable
fun RebootDropdownItem(
    @StringRes id: Int, reason: String = "",
    showTopPopup: MutableState<Boolean>,
    optionSize: Int,
    index: Int,
) {
    DropdownImpl(
        text = stringResource(id),
        optionSize = optionSize,
        isSelected = false,
        onSelectedIndexChange = {
            reboot(reason)
            showTopPopup.value = false
        },
        index = index
    )
}

@Composable
private fun TopBar(
    kernelVersion: KernelVersion,
    onInstallClick: () -> Unit,
    onSettingsClick: () -> Unit,
    scrollBehavior: ScrollBehavior? = null
) {
    TopAppBar(
        title = stringResource(R.string.app_name),
        navigationIcon = {
            IconButton(
                modifier = Modifier.padding(start = 16.dp),
                onClick = onSettingsClick
            ) {
                Icon(
                    imageVector = Icons.Filled.Settings,
                    contentDescription = stringResource(id = R.string.settings),
                    tint = colorScheme.onBackground

                )
            }
        },
        actions = {
            if (kernelVersion.isGKI()) {
                IconButton(
                    modifier = Modifier.padding(end = 16.dp),
                    onClick = onInstallClick,
                ) {
                    Icon(
                        imageVector = Icons.Filled.Archive,
                        contentDescription = stringResource(id = R.string.install),
                        tint = colorScheme.onBackground
                    )
                }
            }
            val showTopPopup = remember { mutableStateOf(false) }
            KsuIsValid {
                IconButton(
                    modifier = Modifier.padding(end = 16.dp),
                    onClick = { showTopPopup.value = true },
                    holdDownState = showTopPopup.value
                ) {
                    Icon(
                        imageVector = Icons.Filled.Refresh,
                        contentDescription = stringResource(id = R.string.reboot),
                        tint = colorScheme.onBackground
                    )
                }
                ListPopup(
                    show = showTopPopup,
                    popupPositionProvider = ListPopupDefaults.ContextMenuPositionProvider,
                    alignment = PopupPositionProvider.Align.TopRight,
                    onDismissRequest = {
                        showTopPopup.value = false
                    }
                ) {
                    val pm = LocalContext.current.getSystemService(Context.POWER_SERVICE) as PowerManager?

                    @Suppress("DEPRECATION")
                    val isRebootingUserspaceSupported =
                        Build.VERSION.SDK_INT >= Build.VERSION_CODES.R && pm?.isRebootingUserspaceSupported == true

                    ListPopupColumn {
                        RebootDropdownItem(
                            id = R.string.reboot,
                            showTopPopup = showTopPopup,
                            optionSize = if (isRebootingUserspaceSupported) 6 else 5,
                            index = 0
                        )


                        if (isRebootingUserspaceSupported) {
                            RebootDropdownItem(
                                id = R.string.reboot_userspace,
                                reason = "userspace",
                                showTopPopup = showTopPopup,
                                optionSize = 5,
                                index = 1
                            )
                        }
                        RebootDropdownItem(
                            id = R.string.reboot_recovery,
                            reason = "recovery",
                            showTopPopup = showTopPopup,
                            optionSize = if (isRebootingUserspaceSupported) 6 else 5,
                            index = if (isRebootingUserspaceSupported) 2 else 1
                        )
                        RebootDropdownItem(
                            id = R.string.reboot_bootloader,
                            reason = "bootloader",
                            showTopPopup = showTopPopup,
                            optionSize = if (isRebootingUserspaceSupported) 6 else 5,
                            index = if (isRebootingUserspaceSupported) 3 else 2
                        )
                        RebootDropdownItem(
                            id = R.string.reboot_download,
                            reason = "download",
                            showTopPopup = showTopPopup,
                            optionSize = if (isRebootingUserspaceSupported) 6 else 5,
                            index = if (isRebootingUserspaceSupported) 4 else 3
                        )
                        RebootDropdownItem(
                            id = R.string.reboot_edl,
                            reason = "edl",
                            showTopPopup = showTopPopup,
                            optionSize = if (isRebootingUserspaceSupported) 6 else 5,
                            index = if (isRebootingUserspaceSupported) 5 else 4
                        )
                    }
                }
            }
        },
        scrollBehavior = scrollBehavior
    )
}

@Composable
private fun StatusCard(
    kernelVersion: KernelVersion,
    ksuVersion: Int?,
    lkmMode: Boolean?,
    onClickInstall: () -> Unit = {},
    onClickSuperuser: () -> Unit = {},
    onclickModule: () -> Unit = {},
) {
    Column(
        modifier = Modifier
    ) {
        when {
            ksuVersion != null -> {
                val safeMode = when {
                    Natives.isSafeMode -> " [${stringResource(id = R.string.safe_mode)}]"
                    else -> ""
                }

                val workingMode = when (lkmMode) {
                    null -> ""
                    true -> " <LKM>"
                    else -> " <GKI>"
                }

                "${stringResource(id = R.string.home_working)}$workingMode$safeMode"

                Row(
                    modifier = Modifier
                        .height(200.dp)
                        .fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Card(
                        modifier = Modifier
                            .padding(end = 12.dp)
                            .weight(1f),
                        color = if (isSystemInDarkTheme()) Color(0xFF1A3825) else Color(0xFFDFFAE4),
                        onClick = {
                            if (kernelVersion.isGKI()) onClickInstall()
                        },
                        showIndication = true,
                        pressFeedbackType = PressFeedbackType.Tilt
                    ) {
                        Box(
                            modifier = Modifier.fillMaxSize()
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(all = 16.dp)
                            ) {
                                Text(
                                    modifier = Modifier.fillMaxWidth(),
                                    text = stringResource(R.string.home_working) + workingMode,
                                    fontSize = 20.sp,
                                    fontWeight = FontWeight.SemiBold,
                                )
                                Spacer(Modifier.height(2.dp))
                                Text(
                                    stringResource(R.string.home_working_version, ksuVersion),
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Medium,
                                )
                            }
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .offset(38.dp, 45.dp),
                                contentAlignment = Alignment.BottomEnd
                            ) {
                                Icon(
                                    modifier = Modifier.size(170.dp),
                                    imageVector = Icons.Rounded.CheckCircleOutline,
                                    tint = Color(0xFF36D167),
                                    contentDescription = null
                                )

                            }

                        }

                    }
                    Column(
                        modifier = Modifier
                            .wrapContentHeight()
                            .weight(1f)
                    ) {
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .weight(1f),
                            insideMargin = PaddingValues(16.dp),
                            onClick = { onClickSuperuser() },
                            showIndication = true,
                            pressFeedbackType = PressFeedbackType.Tilt
                        ) {
                            Column {
                                Text(
                                    text = stringResource(R.string.superuser),
                                    fontWeight = FontWeight.Medium,
                                    fontSize = 15.sp,
                                    color = colorScheme.onSurfaceVariantSummary
                                )
                                Text(
                                    text = getSuperuserCount().toString(),
                                    fontSize = 26.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = colorScheme.onSurface
                                )
                            }

                        }
                        Spacer(Modifier.height(12.dp))
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .weight(1f),
                            insideMargin = PaddingValues(16.dp),
                            onClick = { onclickModule() },
                            showIndication = true,
                            pressFeedbackType = PressFeedbackType.Tilt
                        ) {
                            Column {
                                Text(
                                    text = stringResource(R.string.module),
                                    fontWeight = FontWeight.Medium,
                                    fontSize = 15.sp,
                                    color = colorScheme.onSurfaceVariantSummary
                                )
                                Text(
                                    text = getModuleCount().toString(),
                                    fontSize = 26.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = colorScheme.onSurface
                                )
                            }
                        }
                    }
                }
            }

            kernelVersion.isGKI() -> {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    insideMargin = PaddingValues(16.dp),
                    onClick = {
                        if (kernelVersion.isGKI()) onClickInstall()
                    },
                    showIndication = true,
                    pressFeedbackType = PressFeedbackType.Tilt
                ) {
                    Row(
                        modifier = Modifier.padding(start = 2.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Icon(
                            Icons.Rounded.Warning,
                            stringResource(R.string.home_not_installed),
                            tint = colorScheme.onBackground,
                        )
                        Column(Modifier.padding(start = 16.dp)) {
                            Text(
                                text = stringResource(R.string.home_not_installed),
                                fontSize = 18.sp,
                                fontWeight = FontWeight(550),
                            )
                            Spacer(Modifier.height(2.dp))
                            Text(
                                text = stringResource(R.string.home_click_to_install),
                                fontSize = 14.sp
                            )
                        }
                    }
                }
            }

            else -> {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    insideMargin = PaddingValues(16.dp),
                    onClick = {
                        if (kernelVersion.isGKI()) onClickInstall()
                    },
                    showIndication = true,
                    pressFeedbackType = PressFeedbackType.Tilt
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Icon(
                            Icons.Outlined.Block,
                            stringResource(R.string.home_unsupported),
                            tint = colorScheme.onBackground
                        )
                        Column(Modifier.padding(start = 20.dp)) {
                            Text(
                                text = stringResource(R.string.home_unsupported),
                                fontSize = 20.sp,
                                fontWeight = FontWeight.SemiBold,
                            )
                            Spacer(Modifier.height(4.dp))
                            Text(
                                text = stringResource(R.string.home_unsupported_reason),
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun WarningCard(
    message: String, color: Color = if (isSystemInDarkTheme()) Color(0XFF310808) else Color(0xFFF8E2E2), onClick: (() -> Unit)? = null
) {
    Card(
        color = color,
        onClick = {
            onClick?.invoke()
        },
        showIndication = onClick != null,
        pressFeedbackType = PressFeedbackType.Tilt
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(
                text = message,
                color = Color(0xFFF72727),
                fontSize = 14.sp
            )
        }
    }
}

@Composable
fun LearnMoreCard() {
    val uriHandler = LocalUriHandler.current
    val url = stringResource(R.string.home_learn_kernelsu_url)

    Card(
        modifier = Modifier
            .fillMaxWidth(),
    ) {
        BasicComponent(
            title = stringResource(R.string.home_learn_kernelsu),
            summary = stringResource(R.string.home_click_to_learn_kernelsu),
            rightActions = {
                Icon(
                    modifier = Modifier.size(28.dp),
                    imageVector = Icons.Rounded.Link,
                    tint = colorScheme.onSurface,
                    contentDescription = null
                )
            },
            onClick = {
                uriHandler.openUri(url)
            }
        )
    }
}

@Composable
fun DonateCard() {
    val uriHandler = LocalUriHandler.current

    Card(
        modifier = Modifier
            .fillMaxWidth(),
    ) {
        BasicComponent(
            title = stringResource(R.string.home_support_title),
            summary = stringResource(R.string.home_support_content),
            rightActions = {
                Icon(
                    modifier = Modifier.size(28.dp),
                    imageVector = Icons.Rounded.Link,
                    tint = colorScheme.onSurface,
                    contentDescription = null
                )
            },
            onClick = {
                uriHandler.openUri("https://patreon.com/weishu")
            },
            insideMargin = PaddingValues(18.dp)
        )
    }
}

@Composable
private fun InfoCard() {
    @Composable
    fun InfoText(
        title: String,
        content: String,
        bottomPadding: Dp = 23.dp
    ) {
        Text(
            text = title,
            fontSize = MiuixTheme.textStyles.headline1.fontSize,
            fontWeight = FontWeight.Medium,
            color = colorScheme.onSurface
        )
        Text(
            text = content,
            fontSize = MiuixTheme.textStyles.body2.fontSize,
            color = colorScheme.onSurfaceVariantSummary,
            modifier = Modifier.padding(top = 2.dp, bottom = bottomPadding)
        )
    }
    Card {
        val context = LocalContext.current
        val uname = Os.uname()
        val managerVersion = getManagerVersion(context)
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            InfoText(
                title = stringResource(R.string.home_kernel),
                content = uname.release
            )
            InfoText(
                title = stringResource(R.string.home_manager_version),
                content = "${managerVersion.first} (${managerVersion.second})"
            )
            InfoText(
                title = stringResource(R.string.home_fingerprint),
                content = Build.FINGERPRINT
            )
            InfoText(
                title = stringResource(R.string.home_selinux_status),
                content = getSELinuxStatus(),
                bottomPadding = 0.dp
            )
        }
    }
}

fun getManagerVersion(context: Context): Pair<String, Long> {
    val packageInfo = context.packageManager.getPackageInfo(context.packageName, 0)!!
    val versionCode = PackageInfoCompat.getLongVersionCode(packageInfo)
    return Pair(packageInfo.versionName!!, versionCode)
}

@Preview
@Composable
private fun StatusCardPreview() {
    Column {
        StatusCard(KernelVersion(5, 10, 101), 1, null)
        StatusCard(KernelVersion(5, 10, 101), 20000, true)
        StatusCard(KernelVersion(5, 10, 101), null, true)
        StatusCard(KernelVersion(4, 10, 101), null, false)
    }
}

@Preview
@Composable
private fun WarningCardPreview() {
    Column {
        WarningCard(message = "Warning message")
        WarningCard(
            message = "Warning message ",
            colorScheme.outline,
            onClick = {})
    }
}