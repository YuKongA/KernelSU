package me.weishu.kernelsu.ui.screen

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.BugReport
import androidx.compose.material.icons.rounded.Compress
import androidx.compose.material.icons.rounded.ContactPage
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.DeveloperMode
import androidx.compose.material.icons.rounded.Fence
import androidx.compose.material.icons.rounded.FolderDelete
import androidx.compose.material.icons.rounded.RemoveModerator
import androidx.compose.material.icons.rounded.Save
import androidx.compose.material.icons.rounded.Share
import androidx.compose.material.icons.rounded.Update
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarHost
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.LineHeightStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.content.FileProvider
import androidx.core.content.edit
import androidx.lifecycle.compose.dropUnlessResumed
import com.ramcosta.composedestinations.annotation.Destination
import com.ramcosta.composedestinations.annotation.RootGraph
import com.ramcosta.composedestinations.generated.destinations.AppProfileTemplateScreenDestination
import com.ramcosta.composedestinations.navigation.DestinationsNavigator
import com.ramcosta.composedestinations.navigation.EmptyDestinationsNavigator
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import me.weishu.kernelsu.BuildConfig
import me.weishu.kernelsu.Natives
import me.weishu.kernelsu.R
import me.weishu.kernelsu.ui.component.AboutDialog
import me.weishu.kernelsu.ui.component.ConfirmResult
import me.weishu.kernelsu.ui.component.KsuIsValid
import me.weishu.kernelsu.ui.component.UninstallDialog
import me.weishu.kernelsu.ui.component.rememberConfirmDialog
import me.weishu.kernelsu.ui.component.rememberLoadingDialog
import me.weishu.kernelsu.ui.util.LocalSnackbarHost
import me.weishu.kernelsu.ui.util.getBugreportFile
import me.weishu.kernelsu.ui.util.shrinkModules
import top.yukonga.miuix.kmp.basic.Card
import top.yukonga.miuix.kmp.basic.Icon
import top.yukonga.miuix.kmp.basic.IconButton
import top.yukonga.miuix.kmp.basic.MiuixScrollBehavior
import top.yukonga.miuix.kmp.basic.Scaffold
import top.yukonga.miuix.kmp.basic.ScrollBehavior
import top.yukonga.miuix.kmp.basic.Text
import top.yukonga.miuix.kmp.basic.TopAppBar
import top.yukonga.miuix.kmp.extra.SuperArrow
import top.yukonga.miuix.kmp.extra.SuperSwitch
import top.yukonga.miuix.kmp.theme.MiuixTheme.colorScheme
import top.yukonga.miuix.kmp.utils.getWindowSize
import top.yukonga.miuix.kmp.utils.overScrollVertical
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

/**
 * @author weishu
 * @date 2023/1/1.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Destination<RootGraph>
@Composable
fun SettingScreen(navigator: DestinationsNavigator) {
    val scrollBehavior = MiuixScrollBehavior()
    val snackBarHost = LocalSnackbarHost.current

    Scaffold(
        topBar = {
            TopBar(
                onBack = dropUnlessResumed {
                    navigator.popBackStack()
                },
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
    ) { innerPadding ->
        val loadingDialog = rememberLoadingDialog()
        val shrinkDialog = rememberConfirmDialog()

        val showAboutDialog = rememberSaveable { mutableStateOf(false) }
        val aboutDialog = AboutDialog(showAboutDialog)
        val showUninstallDialog = rememberSaveable { mutableStateOf(false) }
        val uninstallDialog = UninstallDialog(showUninstallDialog, navigator)

        LazyColumn(
            modifier = Modifier
                .height(getWindowSize().height.dp)
                .overScrollVertical()
                .nestedScroll(scrollBehavior.nestedScrollConnection)
                .padding(horizontal = 12.dp),
            contentPadding = innerPadding,
            overscrollEffect = null,
        ) {
            item {
                Card(
                    modifier = Modifier
                        .padding(vertical = 12.dp)
                        .fillMaxWidth(),
                ) {
                    val context = LocalContext.current
                    val scope = rememberCoroutineScope()

                    val exportBugreportLauncher = rememberLauncherForActivityResult(
                        ActivityResultContracts.CreateDocument("application/gzip")
                    ) { uri: Uri? ->
                        if (uri == null) return@rememberLauncherForActivityResult
                        scope.launch(Dispatchers.IO) {
                            loadingDialog.show()
                            context.contentResolver.openOutputStream(uri)?.use { output ->
                                getBugreportFile(context).inputStream().use {
                                    it.copyTo(output)
                                }
                            }
                            loadingDialog.hide()
                            snackBarHost.showSnackbar(context.getString(R.string.log_saved))
                        }
                    }

                    val profileTemplate = stringResource(id = R.string.settings_profile_template)
                    KsuIsValid {
                        SuperArrow(
                            title = profileTemplate,
                            summary = stringResource(id = R.string.settings_profile_template_summary),
                            leftAction = {
                                Icon(
                                    Icons.Rounded.Fence,
                                    modifier = Modifier.padding(end = 16.dp),
                                    contentDescription = profileTemplate,
                                    tint = colorScheme.onBackground
                                )
                            },
                            onClick = {
                                navigator.navigate(AppProfileTemplateScreenDestination)
                            }
                        )
                    }

                    var umountChecked by rememberSaveable {
                        mutableStateOf(Natives.isDefaultUmountModules())
                    }

                    KsuIsValid {
                        SuperSwitch(
                            title = stringResource(id = R.string.settings_umount_modules_default),
                            summary = stringResource(id = R.string.settings_umount_modules_default_summary),
                            leftAction = {
                                Icon(
                                    Icons.Rounded.FolderDelete,
                                    modifier = Modifier.padding(end = 16.dp),
                                    contentDescription = stringResource(id = R.string.settings_umount_modules_default),
                                    tint = colorScheme.onBackground
                                )
                            },
                            checked = umountChecked,
                            onCheckedChange = { it ->
                                if (Natives.setDefaultUmountModules(it)) {
                                    umountChecked = it
                                }
                            }
                        )
                    }

                    KsuIsValid {
                        if (Natives.version >= Natives.MINIMAL_SUPPORTED_SU_COMPAT) {
                            var isSuDisabled by rememberSaveable {
                                mutableStateOf(!Natives.isSuEnabled())
                            }
                            SuperSwitch(
                                title = stringResource(id = R.string.settings_disable_su),
                                summary = stringResource(id = R.string.settings_disable_su_summary),
                                leftAction = {
                                    Icon(
                                        Icons.Rounded.RemoveModerator,
                                        modifier = Modifier.padding(end = 16.dp),
                                        contentDescription = stringResource(id = R.string.settings_disable_su),
                                        tint = colorScheme.onBackground
                                    )
                                },
                                checked = isSuDisabled,
                                onCheckedChange = { checked: Boolean ->
                                    val shouldEnable = !checked
                                    if (Natives.setSuEnabled(shouldEnable)) {
                                        isSuDisabled = !shouldEnable
                                    }
                                }
                            )
                        }
                    }

                    val prefs = context.getSharedPreferences("settings", Context.MODE_PRIVATE)
                    var checkUpdate by rememberSaveable {
                        mutableStateOf(
                            prefs.getBoolean("check_update", true)
                        )
                    }
                    SuperSwitch(
                        title = stringResource(id = R.string.settings_check_update),
                        summary = stringResource(id = R.string.settings_check_update_summary),
                        leftAction = {
                            Icon(
                                Icons.Rounded.Update,
                                modifier = Modifier.padding(end = 16.dp),
                                contentDescription = stringResource(id = R.string.settings_check_update),
                                tint = colorScheme.onBackground
                            )
                        },
                        checked = checkUpdate,
                        onCheckedChange = { it ->
                            prefs.edit { putBoolean("check_update", it) }
                            checkUpdate = it
                        }
                    )

                    var enableWebDebugging by rememberSaveable {
                        mutableStateOf(
                            prefs.getBoolean("enable_web_debugging", false)
                        )
                    }

                    KsuIsValid {
                        SuperSwitch(
                            title = stringResource(id = R.string.enable_web_debugging),
                            summary = stringResource(id = R.string.enable_web_debugging_summary),
                            leftAction = {
                                Icon(
                                    Icons.Rounded.DeveloperMode,
                                    modifier = Modifier.padding(end = 16.dp),
                                    contentDescription = stringResource(id = R.string.enable_web_debugging),
                                    tint = colorScheme.onBackground
                                )
                            },
                            checked = enableWebDebugging,
                            onCheckedChange = { it ->
                                prefs.edit { putBoolean("enable_web_debugging", it) }
                                enableWebDebugging = it
                            }
                        )
                    }

                    var showBottomsheet by remember { mutableStateOf(false) }

                    SuperArrow(
                        title = stringResource(id = R.string.send_log),
                        leftAction = {
                            Icon(
                                Icons.Rounded.BugReport,
                                modifier = Modifier.padding(end = 16.dp),
                                contentDescription = stringResource(id = R.string.send_log),
                                tint = colorScheme.onBackground
                            )
                        },
                        onClick = {
                            showBottomsheet = true
                        },
                    )
                    if (showBottomsheet) {
                        ModalBottomSheet(
                            onDismissRequest = { showBottomsheet = false },
                            content = {
                                Row(
                                    modifier = Modifier
                                        .padding(10.dp)
                                        .align(Alignment.CenterHorizontally)

                                ) {
                                    Box {
                                        Column(
                                            modifier = Modifier
                                                .padding(16.dp)
                                                .clickable {
                                                    val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd_HH_mm")
                                                    val current = LocalDateTime.now().format(formatter)
                                                    exportBugreportLauncher.launch("KernelSU_bugreport_${current}.tar.gz")
                                                    showBottomsheet = false
                                                }
                                        ) {
                                            Icon(
                                                Icons.Rounded.Save,
                                                contentDescription = null,
                                                modifier = Modifier.align(Alignment.CenterHorizontally),
                                                tint = colorScheme.onBackground
                                            )
                                            Text(
                                                text = stringResource(id = R.string.save_log),
                                                modifier = Modifier.padding(top = 16.dp),
                                                textAlign = TextAlign.Center.also {
                                                    LineHeightStyle(
                                                        alignment = LineHeightStyle.Alignment.Center,
                                                        trim = LineHeightStyle.Trim.None
                                                    )
                                                }

                                            )
                                        }
                                    }
                                    Box {
                                        Column(
                                            modifier = Modifier
                                                .padding(16.dp)
                                                .clickable {
                                                    scope.launch {
                                                        val bugreport = loadingDialog.withLoading {
                                                            withContext(Dispatchers.IO) {
                                                                getBugreportFile(context)
                                                            }
                                                        }

                                                        val uri: Uri =
                                                            FileProvider.getUriForFile(
                                                                context,
                                                                "${BuildConfig.APPLICATION_ID}.fileprovider",
                                                                bugreport
                                                            )

                                                        val shareIntent = Intent(Intent.ACTION_SEND).apply {
                                                            putExtra(Intent.EXTRA_STREAM, uri)
                                                            setDataAndType(uri, "application/gzip")
                                                            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                                                        }

                                                        context.startActivity(
                                                            Intent.createChooser(
                                                                shareIntent,
                                                                context.getString(R.string.send_log)
                                                            )
                                                        )
                                                    }
                                                }
                                        ) {
                                            Icon(
                                                Icons.Rounded.Share,
                                                contentDescription = null,
                                                modifier = Modifier.align(Alignment.CenterHorizontally),
                                                tint = colorScheme.onBackground
                                            )
                                            Text(
                                                text = stringResource(id = R.string.send_log),
                                                modifier = Modifier.padding(top = 16.dp),
                                                textAlign = TextAlign.Center.also {
                                                    LineHeightStyle(
                                                        alignment = LineHeightStyle.Alignment.Center,
                                                        trim = LineHeightStyle.Trim.None
                                                    )
                                                }
                                            )
                                        }
                                    }
                                }
                            }
                        )
                    }

                    val shrink = stringResource(id = R.string.shrink_sparse_image)
                    KsuIsValid {
                        SuperArrow(
                            title = shrink,
                            leftAction = {
                                Icon(
                                    Icons.Rounded.Compress,
                                    modifier = Modifier.padding(end = 16.dp),
                                    contentDescription = shrink,
                                    tint = colorScheme.onBackground
                                )
                            },
                            onClick = {
                                scope.launch {
                                    val result = shrinkDialog.awaitConfirm(title = shrink)
                                    if (result == ConfirmResult.Confirmed) {
                                        loadingDialog.withLoading {
                                            shrinkModules()
                                        }
                                    }
                                }
                            },
                        )
                    }

                    val lkmMode = Natives.version >= Natives.MINIMAL_SUPPORTED_KERNEL_LKM && Natives.isLkmMode
                    if (lkmMode) {
                        val uninstall = stringResource(id = R.string.settings_uninstall)
                        SuperArrow(
                            title = uninstall,
                            leftAction = {
                                Icon(
                                    Icons.Rounded.Delete,
                                    modifier = Modifier.padding(end = 16.dp),
                                    contentDescription = uninstall,
                                    tint = colorScheme.onBackground,
                                )
                            },
                            onClick = {
                                showUninstallDialog.value = true
                                uninstallDialog
                            }
                        )
                    }
                    val about = stringResource(id = R.string.about)
                    SuperArrow(
                        title = about,
                        leftAction = {
                            Icon(
                                Icons.Rounded.ContactPage,
                                modifier = Modifier.padding(end = 16.dp),
                                contentDescription = about,
                                tint = colorScheme.onBackground
                            )
                        },
                        onClick = {
                            showAboutDialog.value = true
                            aboutDialog
                        }
                    )
                }
            }
        }
    }
}

enum class UninstallType(val title: Int, val message: Int) {
    TEMPORARY(
        R.string.settings_uninstall_temporary,
        R.string.settings_uninstall_temporary_message
    ),
    PERMANENT(
        R.string.settings_uninstall_permanent,
        R.string.settings_uninstall_permanent_message
    ),
    RESTORE_STOCK_IMAGE(
        R.string.settings_restore_stock_image,
        R.string.settings_restore_stock_image_message
    ),
    NONE(0, 0)
}

@Composable
private fun TopBar(
    onBack: () -> Unit = {},
    scrollBehavior: ScrollBehavior? = null
) {
    TopAppBar(
        title = stringResource(R.string.settings),
        navigationIcon = {
            IconButton(
                modifier = Modifier.padding(start = 16.dp),
                onClick = onBack
            ) {
                Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = null)
            }
        },
        scrollBehavior = scrollBehavior
    )
}

@Preview
@Composable
private fun SettingsPreview() {
    SettingScreen(EmptyDestinationsNavigator)
}
