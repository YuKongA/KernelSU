package me.weishu.kernelsu.ui.component

import android.R
import androidx.activity.compose.LocalActivity
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandHorizontally
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.shrinkHorizontally
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInWindow
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.onClick
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import top.yukonga.miuix.kmp.basic.Icon
import top.yukonga.miuix.kmp.basic.InfiniteProgressIndicator
import top.yukonga.miuix.kmp.basic.InputField
import top.yukonga.miuix.kmp.basic.Text
import top.yukonga.miuix.kmp.icon.MiuixIcons
import top.yukonga.miuix.kmp.icon.icons.basic.Search
import top.yukonga.miuix.kmp.icon.icons.basic.SearchCleanup
import top.yukonga.miuix.kmp.theme.MiuixTheme.colorScheme
import top.yukonga.miuix.kmp.utils.BackHandler
import top.yukonga.miuix.kmp.utils.overScrollVertical

// Remember Search Status
@Composable
fun rememberSearchStatus(
    label: String = "",
): SearchStatus {
    val searchStatus = remember { SearchStatus(label) }
    var isInitialized by remember { mutableStateOf(false) }
    LocalActivity.current

    LaunchedEffect(searchStatus.current) {
        if (searchStatus.current == SearchStatus.Status.COLLAPSED) searchStatus.searchText = ""
        if (!isInitialized) {
            isInitialized = true
            return@LaunchedEffect
        }
    }

    return searchStatus
}

// Search Status Class
@Stable
class SearchStatus(val label: String) {
    private var isInitialized = false
    var searchText by mutableStateOf("")
    var current by mutableStateOf(Status.COLLAPSED)

    var offsetY by mutableStateOf(0.dp)
    var resultStatus by mutableStateOf(ResultStatus.DEFAULT)

    fun isExpand() = current == Status.EXPANDED
    fun isCollapsed() = current == Status.COLLAPSED
    fun shouldExpand() = current == Status.EXPANDED || current == Status.EXPANDING
    fun shouldCollapsed() = current == Status.COLLAPSED || current == Status.COLLAPSING
    fun isAnimatingExpand() = current == Status.EXPANDING
    fun isAnimatingCollapse() = current == Status.COLLAPSING

    // 动画完成回调
    fun onAnimationComplete() {
        current = when (current) {
            Status.EXPANDING -> Status.EXPANDED
            Status.COLLAPSING -> {
                searchText = ""
                Status.COLLAPSED
            }

            else -> current
        }
    }

    @Composable
    fun TopAppBarAnim(
        modifier: Modifier = Modifier,
        visible: Boolean = shouldCollapsed(),
        content: @Composable() () -> Unit
    ) {
        val topAppBarAlpha = animateFloatAsState(
            if (visible) 1f else 0f,
            animationSpec = tween(if (visible) 550 else 0, easing = FastOutSlowInEasing),

            )
        Box(
            modifier = modifier.alpha(topAppBarAlpha.value),
        ) {
            content()

        }
    }


    enum class Status { EXPANDED, EXPANDING, COLLAPSED, COLLAPSING }
    enum class ResultStatus { DEFAULT, EMPTY, LOAD, SHOW }
}

// Search Box Composable
@Composable
fun SearchStatus.SearchBox(
    modifier: Modifier = Modifier,
    collapseBar: @Composable (SearchStatus) -> Unit = { SearchBarFake(label) },
    content: @Composable ColumnScope.() -> Unit
) {
    val searchStatus = this
    val density = LocalDensity.current

    animateFloatAsState(if (searchStatus.shouldCollapsed()) 1f else 0f)

    val offsetY = remember { mutableIntStateOf(0) }

    Column(
        modifier = modifier
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 8.dp)
                .alpha(if (searchStatus.isCollapsed()) 1f else 0f)
                //.scale(scaleX = 1f, scaleY = collapseBarScale.value)
                .onGloballyPositioned {
                    it.positionInWindow().y.apply {
                        offsetY.intValue = (this@apply * 0.9).toInt()
                        with(density) {
                            searchStatus.offsetY = this@apply.toDp()
                        }
                    }
                }
                .pointerInput(Unit) {
                    detectTapGestures { searchStatus.current = SearchStatus.Status.EXPANDING }
                }
//                .then(
//                    if (searchStatus.isCollapsed()){
//                        Modifier
//                    }else{
//                        Modifier.offset(x = 0.dp, y = -searchStatus.offsetY)
//                    }
//                )
        ) {
            collapseBar(searchStatus)

        }
        AnimatedVisibility(
            modifier = Modifier,
            visible = searchStatus.shouldCollapsed(),
            enter = fadeIn(tween(300, easing = LinearOutSlowInEasing))
                    + slideInVertically(
                tween(300, easing = LinearOutSlowInEasing)
            ) {
                -offsetY.intValue
            },
            exit = fadeOut(tween(300, easing = LinearOutSlowInEasing))
                    + slideOutVertically(
                tween(300, easing = LinearOutSlowInEasing)
            ) {
                -offsetY.intValue
            }
        ) {
            content()
        }
    }
}

// Search Pager Composable
@Composable
fun SearchStatus.SearchPager(
    defaultResult: @Composable () -> Unit,
    expandBar: @Composable (SearchStatus) -> Unit = { searchStatus ->
        SearchBar(searchStatus = searchStatus)
    },
    result: LazyListScope.() -> Unit
) {
    val searchStatus = this
    val systemBarsPadding = WindowInsets.systemBars.asPaddingValues().calculateTopPadding()
    val topPadding by animateDpAsState(
        if (searchStatus.shouldExpand()) systemBarsPadding + 5.dp else searchStatus.offsetY,
        animationSpec = tween(300, easing = LinearOutSlowInEasing)
    ) {
        searchStatus.onAnimationComplete()
    }
    val backgroundAlpha by animateFloatAsState(
        if (searchStatus.shouldExpand()) 1f else 0f,
        animationSpec = tween(200, easing = FastOutSlowInEasing)
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .zIndex(1f)
            .background(colorScheme.background.copy(alpha = backgroundAlpha))
            .semantics { onClick { false } }
            .then(
                if (!searchStatus.isCollapsed()) Modifier.pointerInput(Unit) {} else Modifier
            )
    ) {
        Row(
            Modifier
                .fillMaxWidth()
                .padding(top = topPadding)
                .alpha(if (searchStatus.isCollapsed()) 0f else 1f),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            AnimatedVisibility(
                visible = !searchStatus.isCollapsed(),
                modifier = Modifier.weight(1f),
                enter = fadeIn(),
                exit = fadeOut()
            ) {
                expandBar(searchStatus)
            }
            AnimatedVisibility(
                visible = searchStatus.isExpand() || searchStatus.isAnimatingExpand(),
                enter = expandHorizontally() + slideInHorizontally(initialOffsetX = { it }),
                exit = shrinkHorizontally() + slideOutHorizontally(targetOffsetX = { it })
            ) {
                BackHandler(enabled = true) {
                    searchStatus.current = SearchStatus.Status.COLLAPSING
                }
                Text(
                    text = stringResource(R.string.cancel),
                    fontWeight = FontWeight.Bold,
                    color = colorScheme.primary,
                    modifier = Modifier
                        .padding(start = 4.dp, end = 24.dp)
                        .clickable(
                            interactionSource = null,
                            enabled = searchStatus.isExpand(),
                            indication = null
                        ) { searchStatus.current = SearchStatus.Status.COLLAPSING }
                )
            }
        }
        Spacer(modifier = Modifier.height(10.dp))
        AnimatedVisibility(
            visible = searchStatus.isExpand(),
            modifier = Modifier
                .fillMaxSize()
                .zIndex(1f),
            enter = fadeIn(),
            exit = fadeOut()
        ) {
            when (searchStatus.resultStatus) {
                SearchStatus.ResultStatus.DEFAULT -> defaultResult()
                SearchStatus.ResultStatus.EMPTY -> {}
                SearchStatus.ResultStatus.LOAD -> Box(
                    Modifier
                        .padding(vertical = 20.dp)
                        .fillMaxWidth(),
                    contentAlignment = Alignment.TopCenter
                ) {

                    Loading()
                }

                SearchStatus.ResultStatus.SHOW -> LazyColumn(
                    Modifier
                        .fillMaxSize()
                        .overScrollVertical(),
                    contentPadding = PaddingValues(top = 6.dp)
                ) {
                    result()
                }
            }
        }
    }
}

@Composable
fun SearchBar(
    searchStatus: SearchStatus
) {
    val focusRequester = remember { FocusRequester() }
    var expanded by rememberSaveable { mutableStateOf(false) }



    InputField(
        query = searchStatus.searchText,
        onQueryChange = { searchStatus.searchText = it },
        label = "",
        leadingIcon = {
            Icon(
                imageVector = MiuixIcons.Basic.Search,
                contentDescription = "back",
                modifier = Modifier
                    .size(44.dp)
                    .padding(start = 16.dp, end = 8.dp),
                tint = colorScheme.onSurfaceContainerHigh,
            )
        },
        trailingIcon = {
            AnimatedVisibility(
                searchStatus.searchText.isNotEmpty(),
                enter = fadeIn() + scaleIn(),
                exit = fadeOut() + scaleOut(),
            ) {
                Icon(
                    imageVector = MiuixIcons.Basic.SearchCleanup,
                    tint = colorScheme.onSurface,
                    contentDescription = "back",
                    modifier = Modifier
                        .size(44.dp)
                        .padding(start = 8.dp, end = 16.dp)
                        .clickable(
                            interactionSource = null,
                            indication = null
                        ) {
                            searchStatus.searchText = ""
                        },
                )

            }

        },
        modifier = Modifier
            .padding(horizontal = 12.dp)
            .focusRequester(focusRequester),
        onSearch = { it },
        expanded = searchStatus.shouldExpand(),
        onExpandedChange = {
            searchStatus.current = if (it) SearchStatus.Status.EXPANDED else SearchStatus.Status.COLLAPSED
        }
    )
    LaunchedEffect(Unit) {
        if (!expanded && searchStatus.shouldExpand()) {
            focusRequester.requestFocus()
            expanded = true
        }
    }


}

@Composable
fun SearchBarFake(
    label: String
) {

    InputField(
        query = "",
        onQueryChange = { },
        label = label,
        leadingIcon = {
            Icon(
                imageVector = MiuixIcons.Basic.Search,
                contentDescription = "back",
                modifier = Modifier
                    .size(44.dp)
                    .padding(start = 16.dp, end = 8.dp),
                tint = colorScheme.onSurfaceContainerHigh,
            )
        },
        modifier = Modifier.padding(horizontal = 12.dp),
        onSearch = { it },
        enabled = false,
        expanded = false,
        onExpandedChange = { }
    )

}

@Composable
fun Loading() {

    InfiniteProgressIndicator(
        modifier = Modifier
            .fillMaxWidth()
    )


}