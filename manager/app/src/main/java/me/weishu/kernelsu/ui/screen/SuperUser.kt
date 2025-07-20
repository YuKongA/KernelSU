package me.weishu.kernelsu.ui.screen

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.FlowColumn
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.add
import androidx.compose.foundation.layout.displayCutout
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.MoreVert
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.ramcosta.composedestinations.annotation.Destination
import com.ramcosta.composedestinations.annotation.RootGraph
import com.ramcosta.composedestinations.generated.destinations.AppProfileScreenDestination
import com.ramcosta.composedestinations.navigation.DestinationsNavigator
import kotlinx.coroutines.launch
import me.weishu.kernelsu.Natives
import me.weishu.kernelsu.R
import me.weishu.kernelsu.ui.component.DropdownItem
import me.weishu.kernelsu.ui.viewmodel.SuperUserViewModel
import top.yukonga.miuix.kmp.basic.BasicComponent
import top.yukonga.miuix.kmp.basic.Card
import top.yukonga.miuix.kmp.basic.Icon
import top.yukonga.miuix.kmp.basic.IconButton
import top.yukonga.miuix.kmp.basic.InputField
import top.yukonga.miuix.kmp.basic.ListPopup
import top.yukonga.miuix.kmp.basic.ListPopupColumn
import top.yukonga.miuix.kmp.basic.ListPopupDefaults
import top.yukonga.miuix.kmp.basic.MiuixScrollBehavior
import top.yukonga.miuix.kmp.basic.PopupPositionProvider
import top.yukonga.miuix.kmp.basic.PullToRefresh
import top.yukonga.miuix.kmp.basic.Scaffold
import top.yukonga.miuix.kmp.basic.SearchBar
import top.yukonga.miuix.kmp.basic.Text
import top.yukonga.miuix.kmp.basic.TopAppBar
import top.yukonga.miuix.kmp.basic.rememberPullToRefreshState
import top.yukonga.miuix.kmp.icon.MiuixIcons
import top.yukonga.miuix.kmp.icon.icons.basic.ArrowRight
import top.yukonga.miuix.kmp.icon.icons.basic.SearchCleanup
import top.yukonga.miuix.kmp.theme.MiuixTheme.colorScheme
import top.yukonga.miuix.kmp.utils.PressFeedbackType
import top.yukonga.miuix.kmp.utils.getWindowSize
import top.yukonga.miuix.kmp.utils.overScrollVertical

@Composable
@Destination<RootGraph>
fun SuperUserScreen(navigator: DestinationsNavigator) {
    val viewModel = viewModel<SuperUserViewModel>()
    val scope = rememberCoroutineScope()
    val scrollBehavior = MiuixScrollBehavior()
    val listState = rememberLazyListState()

    LaunchedEffect(key1 = navigator) {
        viewModel.search = ""
        if (viewModel.appList.isEmpty()) {
            viewModel.fetchAppList()
        }
    }

    LaunchedEffect(viewModel.search) {
        if (viewModel.search.isEmpty()) {
            listState.scrollToItem(0)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = stringResource(R.string.superuser),
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
                                DropdownItem(
                                    text = stringResource(R.string.refresh),
                                    optionSize = 2,
                                    onSelectedIndexChange = {
                                        scope.launch {
                                            viewModel.fetchAppList()
                                        }
                                        showTopPopup.value = false
                                    },
                                    index = 0
                                )
                                DropdownItem(
                                    text = if (viewModel.showSystemApps) {
                                        stringResource(R.string.hide_system_apps)
                                    } else {
                                        stringResource(R.string.show_system_apps)
                                    },
                                    optionSize = 2,
                                    onSelectedIndexChange = {
                                        viewModel.showSystemApps = !viewModel.showSystemApps
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
        popupHost = { },
        contentWindowInsets = WindowInsets.systemBars.add(WindowInsets.displayCutout).only(WindowInsetsSides.Horizontal)
    ) { innerPadding ->
        val pullToRefreshState = rememberPullToRefreshState()
        PullToRefresh(
            pullToRefreshState = pullToRefreshState,
            onRefresh = {
                scope.launch {
                    viewModel.fetchAppList()
                    pullToRefreshState.completeRefreshing { }
                }
            },
            contentPadding = innerPadding
        ) {
            val keyboardController = LocalSoftwareKeyboardController.current
            val focusManager = LocalFocusManager.current
            val focusRequester = remember { FocusRequester() }
            var expanded by remember { mutableStateOf(false) }

            LazyColumn(
                state = listState,
                modifier = Modifier
                    .height(getWindowSize().height.dp)
                    .overScrollVertical()
                    .nestedScroll(scrollBehavior.nestedScrollConnection),
                contentPadding = innerPadding,
                overscrollEffect = null,
            ) {
                item {
                    SearchBar(
                        modifier = Modifier
                            .padding(vertical = 12.dp)
                            .focusRequester(focusRequester)
                            .onFocusChanged { focusState ->
                                expanded = focusState.isFocused
                            },
                        insideMargin = DpSize(12.dp, 0.dp),
                        inputField = {
                            InputField(
                                query = viewModel.search,
                                onQueryChange = { viewModel.search = it },
                                onSearch = { },
                                expanded = false,
                                onExpandedChange = { },
                                trailingIcon = {
                                    AnimatedVisibility(
                                        visible = viewModel.search.isNotEmpty(),
                                        enter = fadeIn(),
                                        exit = fadeOut(),
                                    ) {
                                        Box(
                                            modifier = Modifier.padding(start = 8.dp, end = 16.dp),
                                            contentAlignment = Alignment.CenterStart
                                        ) {
                                            Icon(
                                                modifier = Modifier
                                                    .clip(CircleShape)
                                                    .clickable { viewModel.search = "" },
                                                imageVector = MiuixIcons.Basic.SearchCleanup,
                                                tint = colorScheme.onSurfaceContainerHighest,
                                                contentDescription = "Search Cleanup"
                                            )
                                        }
                                    }
                                }
                            )
                        },
                        onExpandedChange = { },
                        outsideRightAction = {
                            Text(
                                modifier = Modifier
                                    .padding(end = 12.dp)
                                    .clickable(
                                        interactionSource = null,
                                        indication = null
                                    ) {
                                        viewModel.search = ""
                                        focusManager.clearFocus()
                                        keyboardController?.hide()
                                    },
                                text = stringResource(android.R.string.cancel),
                                style = TextStyle(fontSize = 17.sp, fontWeight = FontWeight.Bold),
                                color = colorScheme.primary
                            )
                        },
                        expanded = expanded,
                    ) {}
                }
                item {
                    if (viewModel.appList.isEmpty()) {
                        Text(
                            modifier = Modifier.fillMaxSize(),
                            text = "Refresh...",
                            textAlign = TextAlign.Center,
                            color = colorScheme.onSecondaryContainer,
                        )
                    }
                }
                items(viewModel.appList, key = { it.packageName + it.uid }) { app ->
                    AppItem(app) {
                        navigator.navigate(AppProfileScreenDestination(app)) {
                            launchSingleTop = true
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun AppItem(
    app: SuperUserViewModel.AppInfo,
    onClickListener: () -> Unit,
) {
    Card(
        modifier = Modifier
            .padding(horizontal = 12.dp)
            .padding(bottom = 12.dp),
        onClick = {
            onClickListener()
        },
        pressFeedbackType = PressFeedbackType.Sink,
        showIndication = true,
    ) {
        BasicComponent(
            title = app.label,
            summary = app.packageName,
            leftAction = {
                AsyncImage(
                    model = ImageRequest.Builder(LocalContext.current)
                        .data(app.packageInfo)
                        .crossfade(true)
                        .build(),
                    contentDescription = app.label,
                    modifier = Modifier
                        .padding(end = 12.dp)
                        .size(48.dp)
                )
            },
            rightActions = {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    FlowColumn(
                        verticalArrangement = Arrangement.spacedBy(4.dp),
                        itemHorizontalAlignment = Alignment.End
                    ) {
                        if (app.allowSu) {
                            StatusTag(
                                label = "ROOT",
                                backgroundColor = colorScheme.tertiaryContainer,
                                contentColor = colorScheme.onTertiaryContainer
                            )
                        } else {
                            if (Natives.uidShouldUmount(app.uid)) {
                                StatusTag(
                                    label = "UMOUNT",
                                    backgroundColor = colorScheme.secondaryContainer,
                                    contentColor = colorScheme.onSecondaryContainer
                                )
                            }
                        }
                        if (app.hasCustomProfile) {
                            StatusTag(
                                label = "CUSTOM",
                                backgroundColor = colorScheme.primaryContainer,
                                contentColor = colorScheme.onPrimaryContainer
                            )
                        }
                    }
                    Image(
                        modifier = Modifier
                            .padding(start = 8.dp)
                            .size(10.dp, 16.dp),
                        imageVector = MiuixIcons.Basic.ArrowRight,
                        contentDescription = null,
                        colorFilter = ColorFilter.tint(colorScheme.onSurfaceVariantActions),
                    )
                }
            }
        )
    }
}

@Composable
private fun StatusTag(label: String, backgroundColor: Color, contentColor: Color) {
    Box(
        modifier = Modifier
            .background(
                color = backgroundColor.copy(alpha = 0.8f),
                shape = RoundedCornerShape(6.dp)
            )
    ) {
        Text(
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp),
            text = label,
            color = contentColor,
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold
        )
    }
}
