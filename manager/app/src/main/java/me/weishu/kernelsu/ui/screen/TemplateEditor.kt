package me.weishu.kernelsu.ui.screen

import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.DeleteForever
import androidx.compose.material.icons.rounded.Save
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.input.pointer.pointerInteropFilter
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.dropUnlessResumed
import com.ramcosta.composedestinations.annotation.Destination
import com.ramcosta.composedestinations.annotation.RootGraph
import com.ramcosta.composedestinations.result.ResultBackNavigator
import me.weishu.kernelsu.Natives
import me.weishu.kernelsu.R
import me.weishu.kernelsu.ui.component.profile.RootProfileConfig
import me.weishu.kernelsu.ui.util.deleteAppProfileTemplate
import me.weishu.kernelsu.ui.util.getAppProfileTemplate
import me.weishu.kernelsu.ui.util.setAppProfileTemplate
import me.weishu.kernelsu.ui.viewmodel.TemplateViewModel
import me.weishu.kernelsu.ui.viewmodel.toJSON
import top.yukonga.miuix.kmp.basic.Card
import top.yukonga.miuix.kmp.basic.Icon
import top.yukonga.miuix.kmp.basic.IconButton
import top.yukonga.miuix.kmp.basic.MiuixScrollBehavior
import top.yukonga.miuix.kmp.basic.Scaffold
import top.yukonga.miuix.kmp.basic.ScrollBehavior
import top.yukonga.miuix.kmp.basic.Text
import top.yukonga.miuix.kmp.basic.TextField
import top.yukonga.miuix.kmp.basic.TopAppBar
import top.yukonga.miuix.kmp.theme.MiuixTheme.colorScheme
import top.yukonga.miuix.kmp.utils.getWindowSize
import top.yukonga.miuix.kmp.utils.overScrollVertical

/**
 * @author weishu
 * @date 2023/10/20.
 */
@Composable
@Destination<RootGraph>
fun TemplateEditorScreen(
    navigator: ResultBackNavigator<Boolean>,
    initialTemplate: TemplateViewModel.TemplateInfo,
    readOnly: Boolean = true,
) {

    val isCreation = initialTemplate.id.isBlank()
    val autoSave = !isCreation

    var template by rememberSaveable {
        mutableStateOf(initialTemplate)
    }

    val scrollBehavior = MiuixScrollBehavior()

    BackHandler {
        navigator.navigateBack(result = !readOnly)
    }

    Scaffold(
        topBar = {
            val saveTemplateFailed = stringResource(id = R.string.app_profile_template_save_failed)
            val context = LocalContext.current

            TopBar(
                title = if (isCreation) {
                    stringResource(R.string.app_profile_template_create)
                } else if (readOnly) {
                    stringResource(R.string.app_profile_template_view)
                } else {
                    stringResource(R.string.app_profile_template_edit)
                },
                readOnly = readOnly,
                onBack = dropUnlessResumed { navigator.navigateBack(result = !readOnly) },
                onDelete = {
                    if (deleteAppProfileTemplate(template.id)) {
                        navigator.navigateBack(result = true)
                    }
                },
                onSave = {
                    if (saveTemplate(template, isCreation)) {
                        navigator.navigateBack(result = true)
                    } else {
                        Toast.makeText(context, saveTemplateFailed, Toast.LENGTH_SHORT).show()
                    }
                },
                scrollBehavior = scrollBehavior
            )
        },
        popupHost = { }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .height(getWindowSize().height.dp)
                .overScrollVertical()
                .nestedScroll(scrollBehavior.nestedScrollConnection)
                .pointerInteropFilter {
                    // disable click and ripple if readOnly
                    readOnly
                },
            contentPadding = innerPadding,
            overscrollEffect = null
        ) {
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                ) {
                    var errorHint by remember {
                        mutableStateOf("")
                    }

                    TextEdit(
                        label = stringResource(id = R.string.app_profile_template_name),
                        text = template.name
                    ) { value ->
                        template.copy(name = value).run {
                            if (autoSave) {
                                if (!saveTemplate(this)) {
                                    // failed
                                    return@run
                                }
                            }
                            template = this
                        }
                    }

                    val idConflictError = stringResource(id = R.string.app_profile_template_id_exist)
                    val idInvalidError = stringResource(id = R.string.app_profile_template_id_invalid)
                    TextEdit(
                        label = stringResource(id = R.string.app_profile_template_id),
                        text = template.id,
                        errorHint = errorHint,
                        isError = errorHint.isNotEmpty()
                    ) { value ->
                        errorHint = if (isTemplateExist(value)) {
                            idConflictError
                        } else if (!isValidTemplateId(value)) {
                            idInvalidError
                        } else {
                            ""
                        }
                        template = template.copy(id = value)
                    }

                    TextEdit(
                        label = stringResource(R.string.module_author),
                        text = template.author
                    ) { value ->
                        template.copy(author = value).run {
                            if (autoSave) {
                                if (!saveTemplate(this)) {
                                    // failed
                                    return@run
                                }
                            }
                            template = this
                        }
                    }

                    TextEdit(
                        label = stringResource(id = R.string.app_profile_template_description),
                        text = template.description
                    ) { value ->
                        template.copy(description = value).run {
                            if (autoSave) {
                                if (!saveTemplate(this)) {
                                    // failed
                                    return@run
                                }
                            }
                            template = this
                        }
                    }

                    RootProfileConfig(
                        fixedName = true,
                        profile = toNativeProfile(template),
                        onProfileChange = {
                            template.copy(
                                uid = it.uid,
                                gid = it.gid,
                                groups = it.groups,
                                capabilities = it.capabilities,
                                context = it.context,
                                namespace = it.namespace,
                                rules = it.rules.split("\n")
                            ).run {
                                if (autoSave) {
                                    if (!saveTemplate(this)) {
                                        // failed
                                        return@run
                                    }
                                }
                                template = this
                            }
                        }
                    )
                }
            }
        }
    }
}

fun toNativeProfile(templateInfo: TemplateViewModel.TemplateInfo): Natives.Profile {
    return Natives.Profile().copy(
        rootTemplate = templateInfo.id,
        uid = templateInfo.uid,
        gid = templateInfo.gid,
        groups = templateInfo.groups,
        capabilities = templateInfo.capabilities,
        context = templateInfo.context,
        namespace = templateInfo.namespace,
        rules = templateInfo.rules.joinToString("\n").ifBlank { "" })
}

fun isTemplateValid(template: TemplateViewModel.TemplateInfo): Boolean {
    if (template.id.isBlank()) {
        return false
    }

    if (!isValidTemplateId(template.id)) {
        return false
    }

    return true
}

fun saveTemplate(template: TemplateViewModel.TemplateInfo, isCreation: Boolean = false): Boolean {
    if (!isTemplateValid(template)) {
        return false
    }

    if (isCreation && isTemplateExist(template.id)) {
        return false
    }

    val json = template.toJSON()
    json.put("local", true)
    return setAppProfileTemplate(template.id, json.toString())
}

@Composable
private fun TopBar(
    title: String,
    readOnly: Boolean,
    onBack: () -> Unit,
    onDelete: () -> Unit = {},
    onSave: () -> Unit = {},
    scrollBehavior: ScrollBehavior
) {
    TopAppBar(
        title = title,
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
            if (readOnly) {
                return@TopAppBar
            }
            IconButton(
                modifier = Modifier.padding(end = 16.dp),
                onClick = onDelete
            ) {
                Icon(
                    Icons.Rounded.DeleteForever,
                    contentDescription = stringResource(id = R.string.app_profile_template_delete),
                    tint = colorScheme.onBackground
                )
            }
            IconButton(
                modifier = Modifier.padding(end = 16.dp),
                onClick = onSave
            ) {
                Icon(
                    imageVector = Icons.Rounded.Save,
                    contentDescription = stringResource(id = R.string.app_profile_template_save),
                    tint = colorScheme.onBackground
                )
            }
        },
        scrollBehavior = scrollBehavior
    )
}

@Composable
private fun TextEdit(
    label: String,
    text: String,
    errorHint: String = "",
    isError: Boolean = false,
    onValueChange: (String) -> Unit = {}
) {
    val keyboardController = LocalSoftwareKeyboardController.current

    TextField(
        value = text,
        onValueChange = onValueChange,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .padding(top = 16.dp),
        backgroundColor = colorScheme.surfaceContainer,
        borderColor = if (isError) colorScheme.tertiaryContainer else colorScheme.primary,
        keyboardOptions = KeyboardOptions(
            keyboardType = KeyboardType.Number,
            imeAction = ImeAction.Done
        ),
        keyboardActions = KeyboardActions(onDone = {
            keyboardController?.hide()
        }),
        trailingIcon = {
            Text(
                text = if (isError) errorHint else label.uppercase(),
                fontSize = 13.sp,
                modifier = Modifier.padding(horizontal = 16.dp)
            )
        }
    )
}

private fun isValidTemplateId(id: String): Boolean {
    return Regex("""^([A-Za-z][A-Za-z\d_]*\.)*[A-Za-z][A-Za-z\d_]*$""").matches(id)
}

private fun isTemplateExist(id: String): Boolean {
    return getAppProfileTemplate(id).isNotBlank()
}