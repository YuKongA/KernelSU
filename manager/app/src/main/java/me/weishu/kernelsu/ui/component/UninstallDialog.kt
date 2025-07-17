package me.weishu.kernelsu.ui.component

import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.BlendModeColorFilter
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.SubcomposeLayout
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import com.ramcosta.composedestinations.generated.destinations.FlashScreenDestination
import com.ramcosta.composedestinations.navigation.DestinationsNavigator
import me.weishu.kernelsu.R
import me.weishu.kernelsu.ui.screen.FlashIt
import me.weishu.kernelsu.ui.screen.UninstallType
import me.weishu.kernelsu.ui.screen.UninstallType.NONE
import me.weishu.kernelsu.ui.screen.UninstallType.PERMANENT
import me.weishu.kernelsu.ui.screen.UninstallType.RESTORE_STOCK_IMAGE
import me.weishu.kernelsu.ui.screen.UninstallType.TEMPORARY
import top.yukonga.miuix.kmp.basic.ButtonDefaults
import top.yukonga.miuix.kmp.basic.Text
import top.yukonga.miuix.kmp.basic.TextButton
import top.yukonga.miuix.kmp.extra.DropdownColors
import top.yukonga.miuix.kmp.extra.DropdownDefaults
import top.yukonga.miuix.kmp.extra.SuperDialog
import top.yukonga.miuix.kmp.icon.MiuixIcons
import top.yukonga.miuix.kmp.icon.icons.basic.Check
import top.yukonga.miuix.kmp.theme.MiuixTheme
import kotlin.math.min

@Composable
fun UninstallDialog(
    showDialog: MutableState<Boolean>,
    navigator: DestinationsNavigator,
) {
    val context = LocalContext.current
    val selectedType = remember { mutableStateOf(NONE) }
    val options = listOf(
        // TEMPORARY,
        PERMANENT,
        RESTORE_STOCK_IMAGE
    )
    val showTodo = {
        Toast.makeText(context, "TODO", Toast.LENGTH_SHORT).show()
    }
    val run = { type: UninstallType ->
        when (type) {
            PERMANENT -> navigator.navigate(
                FlashScreenDestination(FlashIt.FlashUninstall)
            )

            RESTORE_STOCK_IMAGE -> navigator.navigate(
                FlashScreenDestination(FlashIt.FlashRestore)
            )

            TEMPORARY -> showTodo()
            NONE -> Unit
        }
    }

    SuperDialog(
        show = showDialog,
        insideMargin = DpSize(0.dp, 0.dp),
        onDismissRequest = {
            showDialog.value = false
        },
        content = {
            Text(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 24.dp, bottom = 12.dp),
                text = stringResource(R.string.uninstall),
                fontSize = MiuixTheme.textStyles.title4.fontSize,
                fontWeight = FontWeight.Medium,
                textAlign = TextAlign.Center,
                color = MiuixTheme.colorScheme.onSurface
            )

            options.forEachIndexed { index, type ->
                ListColumn {
                    DropdownWithSummaryImpl(
                        title = stringResource(type.title),
                        summary = if (type.message != 0) stringResource(type.message) else null,
                        isSelected = selectedType.value == type,
                        onSelectedIndexChange = { selectedIndex ->
                            if (selectedIndex == index) {
                                selectedType.value = type
                            }
                        },
                        index = index
                    )
                }
            }
            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.padding(top = 12.dp, bottom = 24.dp, start = 24.dp, end = 24.dp)
            ) {
                TextButton(
                    text = stringResource(id = android.R.string.cancel),
                    onClick = {
                        showDialog.value = false
                    },
                    modifier = Modifier.weight(1f)
                )
                Spacer(Modifier.width(20.dp))
                TextButton(
                    text = stringResource(id = android.R.string.ok),
                    onClick = {
                        run(selectedType.value)
                        showDialog.value = false
                    },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.textButtonColorsPrimary()
                )
            }
        }
    )
}

@Composable
fun DropdownWithSummaryImpl(
    title: String,
    summary: String?,
    isSelected: Boolean,
    index: Int,
    dropdownColors: DropdownColors = DropdownDefaults.dropdownColors(),
    onSelectedIndexChange: (Int) -> Unit
) {
    val currentOnSelectedIndexChange = rememberUpdatedState(onSelectedIndexChange)

    val itemColors = remember(isSelected, dropdownColors) {
        if (isSelected) {
            Pair(dropdownColors.selectedContentColor, dropdownColors.selectedContainerColor)
        } else {
            Pair(dropdownColors.contentColor, dropdownColors.containerColor)
        }
    }
    val textColor = itemColors.first
    val backgroundColor = itemColors.second
    val checkColor = remember(isSelected, dropdownColors) {
        if (isSelected) dropdownColors.selectedContentColor else Color.Transparent
    }

    val itemModifier = remember(
        index,
        backgroundColor,
        currentOnSelectedIndexChange,
    ) {
        Modifier
            .clickable {
                currentOnSelectedIndexChange.value(index)
            }
            .background(backgroundColor)
            .padding(horizontal = 24.dp, vertical = 12.dp)
    }

    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
        modifier = itemModifier
    ) {
        Column(
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = title,
                fontSize = MiuixTheme.textStyles.body1.fontSize,
                fontWeight = FontWeight.Medium,
                color = textColor,
            )
            summary?.let { summary ->
                if (summary.isNotEmpty()) {
                    Text(
                        text = summary,
                        fontSize = MiuixTheme.textStyles.body2.fontSize,
                        color = if (isSelected) itemColors.first else MiuixTheme.colorScheme.onSurfaceVariantSummary,
                    )
                }
            }
        }
        val checkColorFilter = remember(checkColor) { BlendModeColorFilter(checkColor, BlendMode.SrcIn) }
        Image(
            modifier = Modifier
                .padding(start = 12.dp)
                .size(20.dp),
            imageVector = MiuixIcons.Basic.Check,
            colorFilter = checkColorFilter,
            contentDescription = null,
        )
    }
}

@Composable
fun ListColumn(
    content: @Composable () -> Unit
) {
    val scrollState = rememberScrollState()
    val currentContent by rememberUpdatedState(content)

    SubcomposeLayout(
        modifier = Modifier.verticalScroll(scrollState)
    ) { constraints ->
        var listHeight = 0
        val tempConstraints = constraints.copy(minWidth = 0, minHeight = 0)

        // Measure pass to find the widest item
        val listWidth = subcompose("miuixPopupListFake", currentContent).map {
            it.measure(tempConstraints)
        }.maxOfOrNull { it.width } ?: 200.dp.roundToPx()

        val childConstraints = constraints.copy(minWidth = listWidth, maxWidth = listWidth, minHeight = 0)

        // Actual measure and layout pass
        val placeables = subcompose("miuixPopupListReal", currentContent).map {
            val placeable = it.measure(childConstraints)
            listHeight += placeable.height
            placeable
        }
        layout(listWidth, min(constraints.maxHeight, listHeight)) {
            var currentY = 0
            placeables.forEach {
                it.place(0, currentY)
                currentY += it.height
            }
        }
    }
}