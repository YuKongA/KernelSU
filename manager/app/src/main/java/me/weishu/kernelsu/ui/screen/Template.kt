package me.weishu.kernelsu.ui.screen

import android.widget.Toast
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.ImportExport
import androidx.compose.material.icons.rounded.Sync
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.dropUnlessResumed
import androidx.lifecycle.viewmodel.compose.viewModel
import com.ramcosta.composedestinations.annotation.Destination
import com.ramcosta.composedestinations.annotation.RootGraph
import com.ramcosta.composedestinations.generated.destinations.TemplateEditorScreenDestination
import com.ramcosta.composedestinations.navigation.DestinationsNavigator
import com.ramcosta.composedestinations.result.ResultRecipient
import com.ramcosta.composedestinations.result.getOr
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import me.weishu.kernelsu.R
import me.weishu.kernelsu.ui.viewmodel.TemplateViewModel
import top.yukonga.miuix.kmp.basic.Card
import top.yukonga.miuix.kmp.basic.FloatingActionButton
import top.yukonga.miuix.kmp.basic.Icon
import top.yukonga.miuix.kmp.basic.IconButton
import top.yukonga.miuix.kmp.basic.MiuixScrollBehavior
import top.yukonga.miuix.kmp.basic.PullToRefresh
import top.yukonga.miuix.kmp.basic.Scaffold
import top.yukonga.miuix.kmp.basic.ScrollBehavior
import top.yukonga.miuix.kmp.basic.Text
import top.yukonga.miuix.kmp.basic.TopAppBar
import top.yukonga.miuix.kmp.basic.rememberPullToRefreshState
import top.yukonga.miuix.kmp.theme.MiuixTheme.colorScheme
import top.yukonga.miuix.kmp.utils.SmoothRoundedCornerShape
import top.yukonga.miuix.kmp.utils.overScrollVertical

/**
 * @author weishu
 * @date 2023/10/20.
 */

@OptIn(ExperimentalMaterialApi::class, ExperimentalMaterial3Api::class)
@Destination<RootGraph>
@Composable
fun AppProfileTemplateScreen(
    navigator: DestinationsNavigator,
    resultRecipient: ResultRecipient<TemplateEditorScreenDestination, Boolean>
) {
    val viewModel = viewModel<TemplateViewModel>()
    val scope = rememberCoroutineScope()
    val scrollBehavior = MiuixScrollBehavior()

    LaunchedEffect(Unit) {
        if (viewModel.templateList.isEmpty()) {
            viewModel.fetchTemplates()
        }
    }

    // handle result from TemplateEditorScreen, refresh if needed
    resultRecipient.onNavResult { result ->
        if (result.getOr { false }) {
            scope.launch { viewModel.fetchTemplates() }
        }
    }

    Scaffold(
        topBar = {
            val clipboardManager = LocalClipboardManager.current
            val context = LocalContext.current
            val showToast = fun(msg: String) {
                scope.launch(Dispatchers.Main) {
                    Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                }
            }
            TopBar(
                onBack = dropUnlessResumed { navigator.popBackStack() },
                onSync = {
                    scope.launch { viewModel.fetchTemplates(true) }
                },
                onImport = {
                    clipboardManager.getText()?.text?.let {
                        if (it.isEmpty()) {
                            showToast(context.getString(R.string.app_profile_template_import_empty))
                            return@let
                        }
                        scope.launch {
                            viewModel.importTemplates(
                                it, {
                                    showToast(context.getString(R.string.app_profile_template_import_success))
                                    viewModel.fetchTemplates(false)
                                },
                                showToast
                            )
                        }
                    }
                },
                onExport = {
                    scope.launch {
                        viewModel.exportTemplates(
                            {
                                showToast(context.getString(R.string.app_profile_template_export_empty))
                            }
                        ) {
                            clipboardManager.setText(AnnotatedString(it))
                        }
                    }
                },
                scrollBehavior = scrollBehavior
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    navigator.navigate(
                        TemplateEditorScreenDestination(
                            TemplateViewModel.TemplateInfo(),
                            false
                        )
                    )
                },
                shape = SmoothRoundedCornerShape(22.dp),
                content = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                    ) {
                        Icon(
                            Icons.Rounded.Add,
                            null,
                            Modifier.padding(start = 8.dp),
                            tint = Color.White
                        )
                        Text(
                            modifier = Modifier.padding(end = 12.dp),
                            text = stringResource(id = R.string.app_profile_template_create),
                            color = Color.White,
                            fontWeight = FontWeight.Medium,
                        )
                    }
                },
            )
        },
        contentWindowInsets = WindowInsets.safeDrawing.only(WindowInsetsSides.Top + WindowInsetsSides.Horizontal)
    ) { innerPadding ->
        val pullToRefreshState = rememberPullToRefreshState()
        PullToRefresh(
            pullToRefreshState = pullToRefreshState,
            onRefresh = {
                scope.launch { viewModel.fetchTemplates() }
                pullToRefreshState.completeRefreshing { }
            },
            contentPadding = innerPadding
        ) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .overScrollVertical()
                    .nestedScroll(scrollBehavior.nestedScrollConnection)
                    .padding(horizontal = 16.dp),
                contentPadding = PaddingValues(
                    top = innerPadding.calculateTopPadding(),
                    bottom = 56.dp + 16.dp /* Scaffold Fab Spacing + Fab container height */
                )
            ) {
                item {
                    Spacer(Modifier.height(12.dp))
                }
                items(viewModel.templateList, key = { it.id }) { app ->
                    TemplateItem(navigator, app)
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun TemplateItem(
    navigator: DestinationsNavigator,
    template: TemplateViewModel.TemplateInfo
) {
    Card(
        modifier = Modifier.padding(bottom = 12.dp)
    ) {
        ListItem(
            modifier = Modifier
                .clickable {
                    navigator.navigate(TemplateEditorScreenDestination(template, !template.local))
                },
            colors = ListItemDefaults.colors(
                containerColor = colorScheme.surface,
                headlineColor = colorScheme.onSurface,
                supportingColor = colorScheme.onSurfaceVariantSummary,
            ),
            headlineContent = { Text(template.name) },
            supportingContent = {
                Column {
                    Text(
                        text = "${template.id}${if (template.author.isEmpty()) "" else "@${template.author}"}",
                        style = MaterialTheme.typography.bodySmall,
                        color = colorScheme.onSurface,
                        fontSize = 12.sp,
                    )
                    Text(template.description)
                    FlowRow {
                        LabelText(label = "UID: ${template.uid}")
                        LabelText(label = "GID: ${template.gid}")
                        LabelText(label = template.context)
                        if (template.local) {
                            LabelText(label = "local")
                        } else {
                            LabelText(label = "remote")
                        }
                    }
                }
            },
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TopBar(
    onBack: () -> Unit,
    onSync: () -> Unit = {},
    onImport: () -> Unit = {},
    onExport: () -> Unit = {},
    scrollBehavior: ScrollBehavior
) {
    TopAppBar(
        title = stringResource(R.string.settings_profile_template),
        navigationIcon = {
            IconButton(
                modifier = Modifier.padding(start = 16.dp),
                onClick = onBack
            ) { Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = null) }
        },
        actions = {
            IconButton(
                modifier = Modifier.padding(end = 16.dp),
                onClick = onSync
            ) {
                Icon(
                    Icons.Rounded.Sync,
                    contentDescription = stringResource(id = R.string.app_profile_template_sync),
                    tint = colorScheme.onBackground
                )
            }

            var showDropdown by remember { mutableStateOf(false) }
            IconButton(
                modifier = Modifier.padding(end = 16.dp),
                onClick = { showDropdown = true }
            ) {
                Icon(
                    imageVector = Icons.Rounded.ImportExport,
                    contentDescription = stringResource(id = R.string.app_profile_import_export),
                    tint = colorScheme.onBackground
                )

                DropdownMenu(expanded = showDropdown, onDismissRequest = {
                    showDropdown = false
                }) {
                    DropdownMenuItem(text = {
                        Text(
                            stringResource(id = R.string.app_profile_import_from_clipboard),
                            color = colorScheme.onBackground
                        )
                    }, onClick = {
                        onImport()
                        showDropdown = false
                    })
                    DropdownMenuItem(text = {
                        Text(
                            stringResource(id = R.string.app_profile_export_to_clipboard),
                            color = colorScheme.onBackground
                        )
                    }, onClick = {
                        onExport()
                        showDropdown = false
                    })
                }
            }
        },
        scrollBehavior = scrollBehavior
    )
}