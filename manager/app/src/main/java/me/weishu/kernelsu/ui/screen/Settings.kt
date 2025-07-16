package me.weishu.kernelsu.ui.screen

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.captionBar
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Undo
import androidx.compose.material.icons.filled.BugReport
import androidx.compose.material.icons.filled.Compress
import androidx.compose.material.icons.filled.ContactPage
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DeleteForever
import androidx.compose.material.icons.filled.DeveloperMode
import androidx.compose.material.icons.filled.Fence
import androidx.compose.material.icons.filled.FolderDelete
import androidx.compose.material.icons.filled.RemoveModerator
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Update
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
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
import androidx.compose.ui.graphics.vector.ImageVector
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
import com.maxkeppeker.sheets.core.models.base.Header
import com.maxkeppeker.sheets.core.models.base.IconSource
import com.maxkeppeker.sheets.core.models.base.rememberUseCaseState
import com.maxkeppeler.sheets.list.ListDialog
import com.maxkeppeler.sheets.list.models.ListOption
import com.maxkeppeler.sheets.list.models.ListSelection
import com.ramcosta.composedestinations.annotation.Destination
import com.ramcosta.composedestinations.annotation.RootGraph
import com.ramcosta.composedestinations.generated.destinations.AppProfileTemplateScreenDestination
import com.ramcosta.composedestinations.generated.destinations.FlashScreenDestination
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
import me.weishu.kernelsu.ui.component.DialogHandle
import me.weishu.kernelsu.ui.component.KsuIsValid
import me.weishu.kernelsu.ui.component.rememberConfirmDialog
import me.weishu.kernelsu.ui.component.rememberCustomDialog
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
        snackbarHost = { SnackbarHost(snackBarHost) },
        popupHost = { },
    ) { innerPadding ->
        val aboutDialog = rememberCustomDialog {
            AboutDialog(it)
        }
        val loadingDialog = rememberLoadingDialog()
        val shrinkDialog = rememberConfirmDialog()

        LazyColumn(
            modifier = Modifier
                .height(getWindowSize().height.dp)
                .overScrollVertical()
                .nestedScroll(scrollBehavior.nestedScrollConnection)
                .padding(top = 12.dp)
                .padding(horizontal = 16.dp),
            contentPadding = PaddingValues(top = innerPadding.calculateTopPadding()),
            overscrollEffect = null,
        ) {

            item {
                Card(
                    modifier = Modifier
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
                            leftAction = {
                                Icon(
                                    Icons.Filled.Fence,
                                    modifier = Modifier.padding(end = 16.dp),
                                    contentDescription = profileTemplate
                                )
                            },
                            title = profileTemplate,
                            summary = stringResource(id = R.string.settings_profile_template_summary),
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
                            leftAction = {
                                Icon(
                                    Icons.Filled.FolderDelete,
                                    modifier = Modifier.padding(end = 16.dp),
                                    contentDescription = stringResource(id = R.string.settings_umount_modules_default)
                                )
                            },
                            title = stringResource(id = R.string.settings_umount_modules_default),
                            summary = stringResource(id = R.string.settings_umount_modules_default_summary),
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
                                leftAction = {
                                    Icon(
                                        Icons.Filled.RemoveModerator,
                                        modifier = Modifier.padding(end = 16.dp),
                                        contentDescription = stringResource(id = R.string.settings_disable_su)
                                    )
                                },
                                title = stringResource(id = R.string.settings_disable_su),
                                summary = stringResource(id = R.string.settings_disable_su_summary),
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
                        leftAction = {
                            Icon(
                                Icons.Filled.Update,
                                modifier = Modifier.padding(end = 16.dp),
                                contentDescription = stringResource(id = R.string.settings_check_update)
                            )
                        },
                        title = stringResource(id = R.string.settings_check_update),
                        summary = stringResource(id = R.string.settings_check_update_summary),
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
                            leftAction = {
                                Icon(
                                    Icons.Filled.DeveloperMode,
                                    modifier = Modifier.padding(end = 16.dp),
                                    contentDescription = stringResource(id = R.string.enable_web_debugging)
                                )
                            },
                            title = stringResource(id = R.string.enable_web_debugging),
                            summary = stringResource(id = R.string.enable_web_debugging_summary),
                            checked = enableWebDebugging,
                            onCheckedChange = { it ->
                                prefs.edit { putBoolean("enable_web_debugging", it) }
                                enableWebDebugging = it
                            }
                        )
                    }

                    var showBottomsheet by remember { mutableStateOf(false) }

                    SuperArrow(
                        leftAction = {
                            Icon(
                                Icons.Filled.BugReport,
                                modifier = Modifier.padding(end = 16.dp),
                                contentDescription = stringResource(id = R.string.send_log)
                            )
                        },
                        title = stringResource(id = R.string.send_log),
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
                                                Icons.Filled.Save,
                                                contentDescription = null,
                                                modifier = Modifier.align(Alignment.CenterHorizontally)
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
                                                Icons.Filled.Share,
                                                contentDescription = null,
                                                modifier = Modifier.align(Alignment.CenterHorizontally)
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
                    val shrinkMessage = shrink
                    KsuIsValid {
                        SuperArrow(
                            leftAction = {
                                Icon(
                                    Icons.Filled.Compress,
                                    modifier = Modifier.padding(end = 16.dp),
                                    contentDescription = shrink
                                )
                            },
                            title = shrink,
                            onClick = {
                                scope.launch {
                                    val result = shrinkDialog.awaitConfirm(title = shrink, content = shrinkMessage)
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
                        UninstallItem(navigator) {
                            loadingDialog.withLoading(it)
                        }
                    }

                    val about = stringResource(id = R.string.about)
                    SuperArrow(
                        leftAction = {
                            Icon(
                                Icons.Filled.ContactPage,
                                modifier = Modifier.padding(end = 16.dp),
                                contentDescription = about
                            )
                        },
                        title = about,
                        onClick = {
                            aboutDialog.show()
                        }
                    )
                }
                Spacer(
                    Modifier.height(
                        WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding()
                                + WindowInsets.captionBar.asPaddingValues().calculateBottomPadding()
                    )
                )
            }
        }
    }
}

@Composable
fun UninstallItem(
    navigator: DestinationsNavigator,
    withLoading: suspend (suspend () -> Unit) -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val uninstallConfirmDialog = rememberConfirmDialog()
    val showTodo = {
        Toast.makeText(context, "TODO", Toast.LENGTH_SHORT).show()
    }
    val uninstallDialog = rememberUninstallDialog { uninstallType ->
        scope.launch {
            val result = uninstallConfirmDialog.awaitConfirm(
                title = context.getString(uninstallType.title),
                content = context.getString(uninstallType.message)
            )
            if (result == ConfirmResult.Confirmed) {
                withLoading {
                    when (uninstallType) {
                        UninstallType.TEMPORARY -> showTodo()
                        UninstallType.PERMANENT -> navigator.navigate(
                            FlashScreenDestination(FlashIt.FlashUninstall)
                        )

                        UninstallType.RESTORE_STOCK_IMAGE -> navigator.navigate(
                            FlashScreenDestination(FlashIt.FlashRestore)
                        )

                        UninstallType.NONE -> Unit
                    }
                }
            }
        }
    }
    val uninstall = stringResource(id = R.string.settings_uninstall)
    SuperArrow(
        leftAction = {
            Icon(
                Icons.Filled.Delete,
                modifier = Modifier.padding(end = 16.dp),
                contentDescription = uninstall
            )
        },
        title = uninstall,
        onClick = {
            uninstallDialog.show()
        }
    )
}

enum class UninstallType(val title: Int, val message: Int, val icon: ImageVector) {
    TEMPORARY(
        R.string.settings_uninstall_temporary,
        R.string.settings_uninstall_temporary_message,
        Icons.Filled.Delete
    ),
    PERMANENT(
        R.string.settings_uninstall_permanent,
        R.string.settings_uninstall_permanent_message,
        Icons.Filled.DeleteForever
    ),
    RESTORE_STOCK_IMAGE(
        R.string.settings_restore_stock_image,
        R.string.settings_restore_stock_image_message,
        Icons.AutoMirrored.Filled.Undo
    ),
    NONE(0, 0, Icons.Filled.Delete)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun rememberUninstallDialog(onSelected: (UninstallType) -> Unit): DialogHandle {
    return rememberCustomDialog { dismiss ->
        val options = listOf(
            // UninstallType.TEMPORARY,
            UninstallType.PERMANENT,
            UninstallType.RESTORE_STOCK_IMAGE
        )
        val listOptions = options.map {
            ListOption(
                titleText = stringResource(it.title),
                subtitleText = if (it.message != 0) stringResource(it.message) else null,
                icon = IconSource(it.icon)
            )
        }

        var selection = UninstallType.NONE
        ListDialog(
            state = rememberUseCaseState(visible = true, onFinishedRequest = {
                if (selection != UninstallType.NONE) {
                    onSelected(selection)
                }
            }, onCloseRequest = {
                dismiss()
            }), header = Header.Default(
                title = stringResource(R.string.settings_uninstall),
            ), selection = ListSelection.Single(
                showRadioButtons = false,
                options = listOptions,
            ) { index, _ ->
                selection = options[index]
            })
    }
}

@OptIn(ExperimentalMaterial3Api::class)
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
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null)
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
