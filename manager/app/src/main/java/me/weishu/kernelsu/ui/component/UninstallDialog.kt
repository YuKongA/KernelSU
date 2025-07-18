package me.weishu.kernelsu.ui.component

import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
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
import top.yukonga.miuix.kmp.basic.Card
import top.yukonga.miuix.kmp.basic.Text
import top.yukonga.miuix.kmp.basic.TextButton
import top.yukonga.miuix.kmp.extra.SuperCheckbox
import top.yukonga.miuix.kmp.extra.SuperDialog
import top.yukonga.miuix.kmp.theme.MiuixTheme

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

            Card(
                modifier = Modifier.padding(horizontal = 24.dp),
                color = MiuixTheme.colorScheme.secondaryContainer,
            ) {
                options.forEachIndexed { index, type ->
                    SuperCheckbox(
                        checked = selectedType.value == type,
                        onCheckedChange = {
                            selectedType.value = if (it) type else NONE
                        },
                        title = stringResource(type.title),
                        summary = if (type.message != 0) stringResource(type.message) else null,
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
