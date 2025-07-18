package me.weishu.kernelsu.ui.screen

import android.app.Activity.RESULT_OK
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.LocalIndication
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.selection.toggleable
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.Wysiwyg
import androidx.compose.material.icons.outlined.Download
import androidx.compose.material.icons.outlined.PlayArrow
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.MoreVert
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.edit
import androidx.core.net.toUri
import androidx.lifecycle.viewmodel.compose.viewModel
import com.ramcosta.composedestinations.annotation.Destination
import com.ramcosta.composedestinations.annotation.RootGraph
import com.ramcosta.composedestinations.generated.destinations.ExecuteModuleActionScreenDestination
import com.ramcosta.composedestinations.generated.destinations.FlashScreenDestination
import com.ramcosta.composedestinations.navigation.DestinationsNavigator
import com.ramcosta.composedestinations.navigation.EmptyDestinationsNavigator
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import me.weishu.kernelsu.Natives
import me.weishu.kernelsu.R
import me.weishu.kernelsu.ksuApp
import me.weishu.kernelsu.ui.component.ConfirmResult
import me.weishu.kernelsu.ui.component.rememberConfirmDialog
import me.weishu.kernelsu.ui.component.rememberLoadingDialog
import me.weishu.kernelsu.ui.util.DownloadListener
import me.weishu.kernelsu.ui.util.LocalSnackbarHost
import me.weishu.kernelsu.ui.util.download
import me.weishu.kernelsu.ui.util.getFileName
import me.weishu.kernelsu.ui.util.hasMagisk
import me.weishu.kernelsu.ui.util.reboot
import me.weishu.kernelsu.ui.util.toggleModule
import me.weishu.kernelsu.ui.util.uninstallModule
import me.weishu.kernelsu.ui.viewmodel.ModuleViewModel
import me.weishu.kernelsu.ui.webui.WebUIActivity
import top.yukonga.miuix.kmp.basic.Card
import top.yukonga.miuix.kmp.basic.FloatingActionButton
import top.yukonga.miuix.kmp.basic.HorizontalDivider
import top.yukonga.miuix.kmp.basic.Icon
import top.yukonga.miuix.kmp.basic.IconButton
import top.yukonga.miuix.kmp.basic.ListPopup
import top.yukonga.miuix.kmp.basic.ListPopupColumn
import top.yukonga.miuix.kmp.basic.ListPopupDefaults
import top.yukonga.miuix.kmp.basic.MiuixScrollBehavior
import top.yukonga.miuix.kmp.basic.PopupPositionProvider
import top.yukonga.miuix.kmp.basic.PullToRefresh
import top.yukonga.miuix.kmp.basic.Scaffold
import top.yukonga.miuix.kmp.basic.Switch
import top.yukonga.miuix.kmp.basic.Text
import top.yukonga.miuix.kmp.basic.TopAppBar
import top.yukonga.miuix.kmp.basic.rememberPullToRefreshState
import top.yukonga.miuix.kmp.extra.DropdownImpl
import top.yukonga.miuix.kmp.theme.MiuixTheme
import top.yukonga.miuix.kmp.theme.MiuixTheme.colorScheme
import top.yukonga.miuix.kmp.utils.SmoothRoundedCornerShape
import top.yukonga.miuix.kmp.utils.getWindowSize
import top.yukonga.miuix.kmp.utils.overScrollVertical

@OptIn(ExperimentalMaterial3Api::class)
@Destination<RootGraph>
@Composable
fun ModuleScreen(navigator: DestinationsNavigator) {
    val viewModel = viewModel<ModuleViewModel>()
    val context = LocalContext.current
    val snackBarHost = LocalSnackbarHost.current
    val scope = rememberCoroutineScope()
    val prefs = context.getSharedPreferences("settings", Context.MODE_PRIVATE)

    LaunchedEffect(Unit) {
        if (viewModel.moduleList.isEmpty() || viewModel.isNeedRefresh) {
            viewModel.sortEnabledFirst = prefs.getBoolean("module_sort_enabled_first", false)
            viewModel.sortActionFirst = prefs.getBoolean("module_sort_action_first", false)
            viewModel.fetchModuleList()
        }
    }

    val isSafeMode = Natives.isSafeMode
    val hasMagisk = hasMagisk()

    val hideInstallButton = isSafeMode || hasMagisk

    val scrollBehavior = MiuixScrollBehavior()

    val webUILauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { viewModel.fetchModuleList() }

    Scaffold(
        topBar = {
            TopAppBar(
                title = stringResource(R.string.module),
                actions = {
                    val showTopPopup = remember { mutableStateOf(false) }

                    IconButton(
                        modifier = Modifier.padding(end = 16.dp),
                        onClick = { showTopPopup.value = true },
                        holdDownState = showTopPopup.value
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.MoreVert,
                            tint = colorScheme.onSurface,
                            contentDescription = stringResource(id = R.string.settings)
                        )
                        ListPopup(
                            show = showTopPopup,
                            popupPositionProvider = ListPopupDefaults.ContextMenuPositionProvider,
                            alignment = PopupPositionProvider.Align.TopRight,
                            onDismissRequest = {
                                showTopPopup.value = false
                            }
                        ) {
                            ListPopupColumn {
                                DropdownImpl(
                                    text = stringResource(R.string.module_sort_action_first),
                                    optionSize = 2,
                                    isSelected = viewModel.sortActionFirst,
                                    onSelectedIndexChange = {
                                        viewModel.sortActionFirst =
                                            !viewModel.sortActionFirst
                                        prefs.edit {
                                            putBoolean(
                                                "module_sort_action_first",
                                                viewModel.sortActionFirst
                                            )
                                        }
                                        scope.launch {
                                            viewModel.fetchModuleList()
                                        }
                                        showTopPopup.value = false
                                    },
                                    index = 0
                                )
                                DropdownImpl(
                                    text = stringResource(R.string.module_sort_enabled_first),
                                    optionSize = 2,
                                    isSelected = viewModel.sortEnabledFirst,
                                    onSelectedIndexChange = {
                                        viewModel.sortEnabledFirst =
                                            !viewModel.sortEnabledFirst
                                        prefs.edit {
                                            putBoolean(
                                                "module_sort_enabled_first",
                                                viewModel.sortEnabledFirst
                                            )
                                        }
                                        scope.launch {
                                            viewModel.fetchModuleList()
                                        }
                                        showTopPopup.value = false
                                    },
                                    index = 1
                                )
                            }
                        }
                    }
                },
                scrollBehavior = scrollBehavior
            )
        },
        floatingActionButton = {
            if (!hideInstallButton) {
                val moduleInstall = stringResource(id = R.string.module_install)
                val confirmTitle = stringResource(R.string.module)
                var zipUris by remember { mutableStateOf<List<Uri>>(emptyList()) }
                val confirmDialog = rememberConfirmDialog(onConfirm = {
                    navigator.navigate(FlashScreenDestination(FlashIt.FlashModules(zipUris)))
                    viewModel.markNeedRefresh()
                })
                val selectZipLauncher = rememberLauncherForActivityResult(
                    contract = ActivityResultContracts.StartActivityForResult()
                ) {
                    if (it.resultCode != RESULT_OK) {
                        return@rememberLauncherForActivityResult
                    }
                    val data = it.data ?: return@rememberLauncherForActivityResult
                    val clipData = data.clipData

                    val uris = mutableListOf<Uri>()
                    if (clipData != null) {
                        for (i in 0 until clipData.itemCount) {
                            clipData.getItemAt(i)?.uri?.let { it -> uris.add(it) }
                        }
                    } else {
                        data.data?.let { it -> uris.add(it) }
                    }

                    if (uris.size == 1) {
                        navigator.navigate(FlashScreenDestination(FlashIt.FlashModules(listOf(uris.first()))))
                    } else if (uris.size > 1) {
                        // multiple files selected
                        val moduleNames =
                            uris.mapIndexed { index, uri -> "\n${index + 1}. ${uri.getFileName(context)}" }.joinToString("")
                        val confirmContent = context.getString(R.string.module_install_prompt_with_name, moduleNames)
                        zipUris = uris
                        confirmDialog.showConfirm(
                            title = confirmTitle,
                            content = confirmContent
                        )
                    }
                }
                FloatingActionButton(
                    onClick = {
                        // Select the zip files to install
                        val intent = Intent(Intent.ACTION_GET_CONTENT).apply {
                            type = "application/zip"
                            putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true)
                        }
                        selectZipLauncher.launch(intent)
                    },
                    shape = SmoothRoundedCornerShape(20.dp),
                    minWidth = 100.dp,
                    content = {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                        ) {
                            Icon(
                                Icons.Rounded.Add,
                                moduleInstall,
                                Modifier.padding(start = 8.dp),
                                tint = Color.White
                            )
                            Text(
                                modifier = Modifier.padding(end = 12.dp),
                                text = moduleInstall,
                                color = Color.White,
                                fontWeight = FontWeight.Medium,
                            )
                        }
                    },
                )
            }
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
        when {
            hasMagisk -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(12.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        stringResource(R.string.module_magisk_conflict),
                        textAlign = TextAlign.Center,
                    )
                }
            }

            else -> {
                ModuleList(
                    navigator,
                    viewModel = viewModel,
                    modifier = Modifier
                        .height(getWindowSize().height.dp)
                        .overScrollVertical()
                        .nestedScroll(scrollBehavior.nestedScrollConnection)
                        .padding(horizontal = 12.dp),
                    boxModifier = Modifier.padding(innerPadding),
                    onInstallModule = {
                        navigator.navigate(FlashScreenDestination(FlashIt.FlashModules(listOf(it))))
                    },
                    onClickModule = { id, name, hasWebUi ->
                        if (hasWebUi) {
                            webUILauncher.launch(
                                Intent(context, WebUIActivity::class.java)
                                    .setData("kernelsu://webui/$id".toUri())
                                    .putExtra("id", id)
                                    .putExtra("name", name)
                            )
                        }
                    },
                    context = context,
                    snackBarHost = LocalSnackbarHost.current
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ModuleList(
    navigator: DestinationsNavigator,
    viewModel: ModuleViewModel,
    modifier: Modifier = Modifier,
    boxModifier: Modifier = Modifier,
    onInstallModule: (Uri) -> Unit,
    onClickModule: (id: String, name: String, hasWebUi: Boolean) -> Unit,
    context: Context,
    snackBarHost: SnackbarHostState
) {
    val failedEnable = stringResource(R.string.module_failed_to_enable)
    val failedDisable = stringResource(R.string.module_failed_to_disable)
    val failedUninstall = stringResource(R.string.module_uninstall_failed)
    val successUninstall = stringResource(R.string.module_uninstall_success)
    val reboot = stringResource(R.string.reboot)
    val rebootToApply = stringResource(R.string.reboot_to_apply)
    val moduleStr = stringResource(R.string.module)
    val uninstall = stringResource(R.string.uninstall)
    val cancel = stringResource(android.R.string.cancel)
    val moduleUninstallConfirm = stringResource(R.string.module_uninstall_confirm)
    val updateText = stringResource(R.string.module_update)
    val changelogText = stringResource(R.string.module_changelog)
    val downloadingText = stringResource(R.string.module_downloading)
    val startDownloadingText = stringResource(R.string.module_start_downloading)
    val fetchChangeLogFailed = stringResource(R.string.module_changelog_failed)

    val loadingDialog = rememberLoadingDialog()
    val confirmDialog = rememberConfirmDialog()

    suspend fun onModuleUpdate(
        module: ModuleViewModel.ModuleInfo,
        changelogUrl: String,
        downloadUrl: String,
        fileName: String
    ) {
        val changelogResult = loadingDialog.withLoading {
            withContext(Dispatchers.IO) {
                runCatching {
                    ksuApp.okhttpClient.newCall(
                        okhttp3.Request.Builder().url(changelogUrl).build()
                    ).execute().body!!.string()
                }
            }
        }

        val showToast: suspend (String) -> Unit = { msg ->
            withContext(Dispatchers.Main) {
                Toast.makeText(
                    context,
                    msg,
                    Toast.LENGTH_SHORT
                ).show()
            }
        }

        val changelog = changelogResult.getOrElse {
            showToast(fetchChangeLogFailed.format(it.message))
            return
        }.ifBlank {
            showToast(fetchChangeLogFailed.format(module.name))
            return
        }

        // changelog is not empty, show it and wait for confirm
        val confirmResult = confirmDialog.awaitConfirm(
            changelogText,
            content = changelog,
            markdown = true,
            confirm = updateText,
        )

        if (confirmResult != ConfirmResult.Confirmed) {
            return
        }

        showToast(startDownloadingText.format(module.name))

        val downloading = downloadingText.format(module.name)
        withContext(Dispatchers.IO) {
            download(
                context,
                downloadUrl,
                fileName,
                downloading,
                onDownloaded = onInstallModule,
                onDownloading = {
                    launch(Dispatchers.Main) {
                        Toast.makeText(context, downloading, Toast.LENGTH_SHORT).show()
                    }
                }
            )
        }
    }

    suspend fun onModuleUninstall(module: ModuleViewModel.ModuleInfo) {
        val confirmResult = confirmDialog.awaitConfirm(
            moduleStr,
            content = moduleUninstallConfirm.format(module.name),
            confirm = uninstall,
            dismiss = cancel
        )
        if (confirmResult != ConfirmResult.Confirmed) {
            return
        }

        val success = loadingDialog.withLoading {
            withContext(Dispatchers.IO) {
                uninstallModule(module.id)
            }
        }

        if (success) {
            viewModel.fetchModuleList()
        }
        val message = if (success) {
            successUninstall.format(module.name)
        } else {
            failedUninstall.format(module.name)
        }
        val actionLabel = if (success) {
            reboot
        } else {
            null
        }
        val result = snackBarHost.showSnackbar(
            message = message,
            actionLabel = actionLabel,
            duration = SnackbarDuration.Long
        )
        if (result == SnackbarResult.ActionPerformed) {
            reboot()
        }
    }

    val pullToRefreshState = rememberPullToRefreshState()
    PullToRefresh(
        pullToRefreshState = pullToRefreshState,
        onRefresh = {
            viewModel.fetchModuleList()
            pullToRefreshState.completeRefreshing { }
        },
        modifier = boxModifier
    ) {
        LocalSoftwareKeyboardController.current
        LocalFocusManager.current
        remember { FocusRequester() }

        LazyColumn(
            modifier = modifier,
            verticalArrangement = Arrangement.spacedBy(12.dp),
            contentPadding = PaddingValues(
                top = 12.dp,
                bottom = 12.dp + 60.dp + 12.dp /* Scaffold Fab Spacing + Fab container height */
            ),
            overscrollEffect = null,
        ) {
            when {
                !viewModel.isOverlayAvailable -> {
                    item {
                        Box(
                            modifier = Modifier.fillParentMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                stringResource(R.string.module_overlay_fs_not_available),
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }

                viewModel.moduleList.isEmpty() -> {
                    item {
                        Box(
                            modifier = Modifier.fillParentMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                stringResource(R.string.module_empty),
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }

                else -> {
                    items(viewModel.moduleList) { module ->
                        val scope = rememberCoroutineScope()
                        val updatedModule by produceState(initialValue = Triple("", "", "")) {
                            scope.launch(Dispatchers.IO) {
                                value = viewModel.checkUpdate(module)
                            }
                        }

                        ModuleItem(
                            navigator = navigator,
                            module = module,
                            updateUrl = updatedModule.first,
                            onUninstall = {
                                scope.launch { onModuleUninstall(module) }
                            },
                            onCheckChanged = {
                                scope.launch {
                                    val success = loadingDialog.withLoading {
                                        withContext(Dispatchers.IO) {
                                            toggleModule(module.id, !module.enabled)
                                        }
                                    }
                                    if (success) {
                                        viewModel.fetchModuleList()

                                        val result = snackBarHost.showSnackbar(
                                            message = rebootToApply,
                                            actionLabel = reboot,
                                            duration = SnackbarDuration.Long
                                        )
                                        if (result == SnackbarResult.ActionPerformed) {
                                            reboot()
                                        }
                                    } else {
                                        val message = if (module.enabled) failedDisable else failedEnable
                                        snackBarHost.showSnackbar(message.format(module.name))
                                    }
                                }
                            },
                            onUpdate = {
                                scope.launch {
                                    onModuleUpdate(
                                        module,
                                        updatedModule.third,
                                        updatedModule.first,
                                        "${module.name}-${updatedModule.second}.zip"
                                    )
                                }
                            },
                            onClick = {
                                onClickModule(it.id, it.name, it.hasWebUi)
                            }
                        )
                    }
                }
            }
        }

        DownloadListener(context, onInstallModule)

    }
}

@Composable
fun ModuleItem(
    navigator: DestinationsNavigator,
    module: ModuleViewModel.ModuleInfo,
    updateUrl: String,
    onUninstall: (ModuleViewModel.ModuleInfo) -> Unit,
    onCheckChanged: (Boolean) -> Unit,
    onUpdate: (ModuleViewModel.ModuleInfo) -> Unit,
    onClick: (ModuleViewModel.ModuleInfo) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        insideMargin = PaddingValues(16.dp)
    ) {
        val textDecoration = if (!module.remove) null else TextDecoration.LineThrough
        val interactionSource = remember { MutableInteractionSource() }
        val indication = LocalIndication.current
        val viewModel = viewModel<ModuleViewModel>()

        Row {
            Column(
                modifier = Modifier
                    .weight(1f)
                    .run {
                        if (module.hasWebUi) {
                            toggleable(
                                value = module.enabled,
                                enabled = !module.remove && module.enabled,
                                interactionSource = interactionSource,
                                role = Role.Button,
                                indication = indication,
                                onValueChange = { onClick(module) }
                            )
                        } else {
                            this
                        }
                    }
            ) {
                val moduleVersion = stringResource(id = R.string.module_version)
                val moduleAuthor = stringResource(id = R.string.module_author)

                Text(
                    text = module.name,
                    fontSize = MiuixTheme.textStyles.title3.fontSize,
                    fontWeight = FontWeight.Medium,
                    color = colorScheme.onSurface,
                    textDecoration = textDecoration,
                )

                Spacer(Modifier.height(0.5.dp))

                Text(
                    text = "$moduleVersion: ${module.version}",
                    fontSize = MiuixTheme.textStyles.body2.fontSize,
                    color = colorScheme.onSurfaceVariantSummary,
                    textDecoration = textDecoration,
                )

                Text(
                    text = "$moduleAuthor: ${module.author}",
                    fontSize = MiuixTheme.textStyles.body2.fontSize,
                    color = colorScheme.onSurfaceVariantSummary,
                    textDecoration = textDecoration,
                )
            }

            Switch(
                modifier = Modifier
                    .fillMaxHeight()
                    .align(Alignment.CenterVertically),
                checked = module.enabled,
                onCheckedChange = onCheckChanged
            )
        }

        Text(
            text = module.description,
            fontSize = MiuixTheme.textStyles.body2.fontSize,
            color = colorScheme.onSurfaceVariantSummary,
            modifier = Modifier.padding(top = 4.dp),
            overflow = TextOverflow.Ellipsis,
            maxLines = 4,
            textDecoration = textDecoration
        )

        HorizontalDivider(
            modifier = Modifier.padding(vertical = 8.dp)
        )

        Row(
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (module.hasActionScript) {
                IconButton(
                    backgroundColor = colorScheme.outline,
                    enabled = !module.remove && module.enabled,
                    onClick = {
                        navigator.navigate(ExecuteModuleActionScreenDestination(module.id))
                        viewModel.markNeedRefresh()
                    },
                ) {
                    Icon(
                        modifier = Modifier.size(20.dp),
                        imageVector = Icons.Outlined.PlayArrow,
                        tint = colorScheme.onSurface.copy(alpha = if (isSystemInDarkTheme()) 0.7f else 0.9f),
                        contentDescription = stringResource(R.string.action)
                    )
                }
            }

            if (module.hasWebUi) {
                IconButton(
                    backgroundColor = colorScheme.outline,
                    enabled = !module.remove && module.enabled,
                    onClick = { onClick(module) },
                ) {
                    Icon(
                        modifier = Modifier.size(20.dp),
                        imageVector = Icons.AutoMirrored.Outlined.Wysiwyg,
                        tint = colorScheme.onSurface.copy(alpha = if (isSystemInDarkTheme()) 0.7f else 0.9f),
                        contentDescription = stringResource(R.string.open)
                    )
                }
            }

            Spacer(modifier = Modifier.weight(1f, true))

            if (updateUrl.isNotEmpty()) {
                IconButton(
                    enabled = !module.remove,
                    onClick = { onUpdate(module) },
                ) {
                    Icon(
                        modifier = Modifier.size(20.dp),
                        imageVector = Icons.Outlined.Download,
                        tint = colorScheme.onSurface.copy(alpha = if (isSystemInDarkTheme()) 0.7f else 0.9f),
                        contentDescription = stringResource(R.string.module_update),
                    )
                }
            }

            IconButton(
                enabled = !module.remove,
                onClick = { onUninstall(module) },
                backgroundColor = Color.Red.copy(alpha = if (isSystemInDarkTheme()) 0.3f else 0.6f),
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    Icon(
                        modifier = Modifier
                            .padding(start = 10.dp)
                            .size(20.dp),
                        imageVector = Icons.Rounded.Delete,
                        tint = Color.White.copy(alpha = if (isSystemInDarkTheme()) 0.78f else 0.98f),
                        contentDescription = null
                    )
                    Text(
                        modifier = Modifier.padding(end = 12.dp),
                        text = stringResource(R.string.uninstall),
                        color = Color.White.copy(alpha = if (isSystemInDarkTheme()) 0.78f else 0.98f),
                        fontWeight = FontWeight.Medium,
                        fontSize = 15.sp
                    )
                }
            }
        }
    }
}

@Preview
@Composable
fun ModuleItemPreview() {
    val module = ModuleViewModel.ModuleInfo(
        id = "id",
        name = "name",
        version = "version",
        versionCode = 1,
        author = "author",
        description = "I am a test module and i do nothing but show a very long description",
        enabled = true,
        update = true,
        remove = false,
        updateJson = "",
        hasWebUi = false,
        hasActionScript = false
    )
    ModuleItem(EmptyDestinationsNavigator, module, "", {}, {}, {}, {})
}
