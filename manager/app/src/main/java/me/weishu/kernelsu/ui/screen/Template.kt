package me.weishu.kernelsu.ui.screen

import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.outlined.Fingerprint
import androidx.compose.material.icons.outlined.Group
import androidx.compose.material.icons.outlined.Shield
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.ImportExport
import androidx.compose.material.icons.rounded.Sync
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
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
import me.weishu.kernelsu.ui.component.DropdownItem
import me.weishu.kernelsu.ui.viewmodel.TemplateViewModel
import top.yukonga.miuix.kmp.basic.Card
import top.yukonga.miuix.kmp.basic.FloatingActionButton
import top.yukonga.miuix.kmp.basic.HorizontalDivider
import top.yukonga.miuix.kmp.basic.Icon
import top.yukonga.miuix.kmp.basic.IconButton
import top.yukonga.miuix.kmp.basic.ListPopup
import top.yukonga.miuix.kmp.basic.ListPopupColumn
import top.yukonga.miuix.kmp.basic.MiuixScrollBehavior
import top.yukonga.miuix.kmp.basic.PullToRefresh
import top.yukonga.miuix.kmp.basic.Scaffold
import top.yukonga.miuix.kmp.basic.ScrollBehavior
import top.yukonga.miuix.kmp.basic.Text
import top.yukonga.miuix.kmp.basic.TopAppBar
import top.yukonga.miuix.kmp.basic.rememberPullToRefreshState
import top.yukonga.miuix.kmp.theme.MiuixTheme.colorScheme
import top.yukonga.miuix.kmp.utils.PressFeedbackType
import top.yukonga.miuix.kmp.utils.SmoothRoundedCornerShape
import top.yukonga.miuix.kmp.utils.getWindowSize
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
                shape = SmoothRoundedCornerShape(20.dp),
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
        popupHost = { }
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
                    .height(getWindowSize().height.dp)
                    .overScrollVertical()
                    .nestedScroll(scrollBehavior.nestedScrollConnection)
                    .padding(horizontal = 12.dp),
                contentPadding = innerPadding,
                overscrollEffect = null
            ) {
                item {
                    Spacer(Modifier.height(12.dp))
                }
                items(viewModel.templateList, key = { it.id }) { app ->
                    TemplateItem(navigator, app)
                }
                item {
                    Spacer(Modifier.height(60.dp + 12.dp /* Scaffold Fab Spacing + Fab container height */))
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
        modifier = Modifier.padding(bottom = 12.dp),
        onClick = {
            navigator.navigate(TemplateEditorScreenDestination(template, !template.local))
        },
        showIndication = true,
        pressFeedbackType = PressFeedbackType.Sink
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = template.name,
                    fontWeight = FontWeight.SemiBold,
                    style = MaterialTheme.typography.titleMedium
                )
                Spacer(modifier = Modifier.weight(1f))
                if (template.local) {
                    Text(
                        text = "LOCAL",
                        style = MaterialTheme.typography.labelSmall,
                        color = colorScheme.onTertiaryContainer,
                        fontWeight = FontWeight.Bold
                    )
                } else {
                    Text(
                        text = "REMOTE",
                        style = MaterialTheme.typography.labelSmall,
                        color = colorScheme.onSurfaceSecondary,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Text(
                text = "${template.id}${if (template.author.isEmpty()) "" else " by @${template.author}"}",
                style = MaterialTheme.typography.bodySmall,
                color = colorScheme.onSurfaceSecondary
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = template.description,
                style = MaterialTheme.typography.bodyMedium,
                color = colorScheme.onSurface
            )

            HorizontalDivider(
                modifier = Modifier.padding(vertical = 16.dp),
                thickness = 0.5.dp,
                color = colorScheme.outline.copy(alpha = 0.5f)
            )

            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                InfoChip(
                    icon = Icons.Outlined.Fingerprint,
                    text = "UID: ${template.uid}"
                )
                InfoChip(
                    icon = Icons.Outlined.Group,
                    text = "GID: ${template.gid}"
                )
                InfoChip(
                    icon = Icons.Outlined.Shield,
                    text = template.context
                )
            }
        }
    }
}

@Composable
private fun InfoChip(icon: ImageVector, text: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier.size(14.dp),
            tint = colorScheme.onSurfaceSecondary.copy(alpha = 0.8f)
        )
        Spacer(modifier = Modifier.width(6.dp))
        Text(
            text = text,
            style = MaterialTheme.typography.bodySmall,
            color = colorScheme.onSurfaceSecondary
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
                onClick = onSync
            ) {
                Icon(
                    Icons.Rounded.Sync,
                    contentDescription = stringResource(id = R.string.app_profile_template_sync),
                    tint = colorScheme.onBackground
                )
            }

            val showDropdown = remember { mutableStateOf(false) }
            IconButton(
                modifier = Modifier.padding(end = 16.dp),
                onClick = { showDropdown.value = true }
            ) {
                Icon(
                    imageVector = Icons.Rounded.ImportExport,
                    contentDescription = stringResource(id = R.string.app_profile_import_export),
                    tint = colorScheme.onBackground
                )

                ListPopup(
                    show = showDropdown,
                    onDismissRequest = { showDropdown.value = false }
                ) {
                    ListPopupColumn {
                        val items = listOf(
                            stringResource(id = R.string.app_profile_import_from_clipboard),
                            stringResource(id = R.string.app_profile_export_to_clipboard)
                        )

                        items.forEachIndexed { index, text ->
                            DropdownItem(
                                text = text,
                                optionSize = items.size,
                                index = index,
                                onSelectedIndexChange = { selectedIndex ->
                                    if (selectedIndex == 0) {
                                        onImport()
                                    } else {
                                        onExport()
                                    }
                                    showDropdown.value = false
                                }
                            )
                        }
                    }
                }
            }
        },
        scrollBehavior = scrollBehavior
    )
}
