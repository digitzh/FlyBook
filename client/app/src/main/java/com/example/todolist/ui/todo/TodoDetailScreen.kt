package com.example.todolist.ui.todo

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.clickable
import com.example.todolist.presentation.detail.TodoDetailViewModel
import com.example.todolist.ui.theme.TodoType

/**
 * 待办详情页（新建 & 编辑）
 *
 * 参数：
 * - viewModel：详情 ViewModel
 * - isEdit：是否是编辑模式（true: 编辑已有任务；false: 新建任务）
 * - todoId：编辑模式下需要传入的任务 id
 * - onBack：保存成功或点击返回时，通知外部返回上一页
 *
 * 说明：
 * - 这里没有处理 Nav 的 SavedStateHandle，假定调用者在创建 VM 时负责把 id 传进去并调用 loadExistingTask
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TodoDetailScreen(
    viewModel: TodoDetailViewModel,
    isEdit: Boolean,
    todoId: Long?,
    onBack: () -> Unit
) {
    // 如果是编辑模式并且有 id，则让 VM 加载数据
    LaunchedEffect(key1 = isEdit, key2 = todoId) {
        if (isEdit && todoId != null) {
            viewModel.loadExistingTask(todoId)
        }
    }

    val uiState by viewModel.uiState.collectAsState()

    // 如果保存成功，通知外部并重置标志
    LaunchedEffect(key1 = uiState.saveSuccess) {
        if (uiState.saveSuccess) {
            onBack()
            viewModel.onSaveConsumed()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (isEdit) "编辑待办" else "新建待办") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "返回"
                        )
                    }
                },
                actions = {
                    TextButton(onClick = { viewModel.onSaveClicked() }) {
                        if (uiState.isSaving) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(18.dp),
                                strokeWidth = 2.dp
                            )
                        } else {
                            Text("保存")
                        }
                    }
                }
            )
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            if (uiState.isLoading) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            } else {
                TodoDetailContent(
                    uiState = uiState,
                    onTitleChange = viewModel::onTitleChange,
                    onTypeChange = viewModel::onTypeChange,
                    onDescriptionChange = viewModel::onDescriptionChange,
                    onDeadlineChange = viewModel::onDeadlineChange,
                    onCompletedChange = viewModel::onCompletedChange
                )
            }

            uiState.errorMessage?.let { message ->
                SnackbarHost(
                    hostState = SnackbarHostState(),
                    modifier = Modifier.align(Alignment.BottomCenter)
                ) {
                    Snackbar(
                        action = {
                            TextButton(onClick = { viewModel.onErrorShown() }) {
                                Text("知道了")
                            }
                        }
                    ) {
                        Text(message)
                    }
                }
            }
        }
    }

//    // 把布局交给无状态版本，方便 Preview 复用
//    TodoDetailScreenStateless(
//        uiState = uiState,
//        isEdit = isEdit,
//        onBack = onBack,
//        onTitleChange = viewModel::onTitleChange,
//        onDescriptionChange = viewModel::onDescriptionChange,
//        onDeadlineChange = viewModel::onDeadlineChange,
//        onCompletedChange = viewModel::onCompletedChange,
//        onSaveClicked = { viewModel.onSaveClicked() },
//        onErrorShown = { viewModel.onErrorShown() }
//    )


}

@Composable
private fun TodoDetailContent(
    uiState: TodoDetailViewModel.UiState,
    onTitleChange: (String) -> Unit,
    onTypeChange: (TodoType) -> Unit,
    onDescriptionChange: (String) -> Unit,
    onDeadlineChange: (String) -> Unit,
    onCompletedChange: (Boolean) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        OutlinedTextField(
            value = uiState.title,
            onValueChange = onTitleChange,
            label = { Text("标题") },
            modifier = Modifier.fillMaxWidth()
        )

        //类型选择控件（标题和描述之间）
        TodoTypeSelector(
            selectedType = uiState.type,
            onTypeChange = onTypeChange
        )

        OutlinedTextField(
            value = uiState.description,
            onValueChange = onDescriptionChange,
            label = { Text("描述") },
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 120.dp),
            maxLines = 6
        )

        OutlinedTextField(
            value = uiState.deadline,
            onValueChange = onDeadlineChange,
            label = { Text("截止日期 (如 2025-11-24)") },
            modifier = Modifier.fillMaxWidth()
        )

        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Checkbox(
                checked = uiState.isCompleted,
                onCheckedChange = onCompletedChange
            )
            Text(text = "已完成")
        }
    }
}

@Composable
private fun TodoTypeSelector(
    selectedType: TodoType,
    onTypeChange: (TodoType) -> Unit
) {
    Column {
        Text(text = "类型", style = MaterialTheme.typography.labelLarge)

        Spacer(modifier = Modifier.height(4.dp))

        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            // 对每个枚举值画一个 RadioButton + 文本
            TodoType.entries.forEach { type ->
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .padding(end = 12.dp)
                        .clickable { onTypeChange(type) }
                ) {
                    RadioButton(
                        selected = (type == selectedType),
                        onClick = { onTypeChange(type) }
                    )
                    Text(
                        text = when (type) {
                            TodoType.FILE -> "文件"
                            TodoType.CONF -> "会议"
                            TodoType.MSG  -> "消息"
                            TodoType.OTHER -> "其他"
                        }
                    )
                }
            }
        }
    }
}




///**
// * 无状态版本：只依赖 UiState + 回调。
// * 真实运行和 Preview 都可以共用这个。
// */
//@OptIn(ExperimentalMaterial3Api::class)
//@Composable
//private fun TodoDetailScreenStateless(
//    uiState: TodoDetailViewModel.UiState,
//    isEdit: Boolean,
//    onBack: () -> Unit,
//    onTitleChange: (String) -> Unit,
//    onDescriptionChange: (String) -> Unit,
//    onDeadlineChange: (String) -> Unit,
//    onCompletedChange: (Boolean) -> Unit,
//    onSaveClicked: () -> Unit,
//    onErrorShown: () -> Unit
//) {
//    Scaffold(
//        topBar = {
//            TopAppBar(
//                title = { Text(if (isEdit) "编辑待办" else "新建待办") },
//                navigationIcon = {
//                    IconButton(onClick = onBack) {
//                        Icon(
//                            imageVector = Icons.Filled.ArrowBack,
//                            contentDescription = "返回"
//                        )
//                    }
//                },
//                actions = {
//                    TextButton(onClick = onSaveClicked) {
//                        if (uiState.isSaving) {
//                            CircularProgressIndicator(
//                                modifier = Modifier.size(18.dp),
//                                strokeWidth = 2.dp
//                            )
//                        } else {
//                            Text("保存")
//                        }
//                    }
//                }
//            )
//        }
//    ) { padding ->
//        Box(
//            modifier = Modifier
//                .fillMaxSize()
//                .padding(padding)
//        ) {
//            if (uiState.isLoading) {
//                CircularProgressIndicator(
//                    modifier = Modifier.align(Alignment.Center)
//                )
//            } else {
//                TodoDetailContent(
//                    uiState = uiState,
//                    onTitleChange = onTitleChange,
//                    onDescriptionChange = onDescriptionChange,
//                    onDeadlineChange = onDeadlineChange,
//                    onCompletedChange = onCompletedChange
//                )
//            }
//
//            uiState.errorMessage?.let { message ->
//                // 为了 Preview 简单一点，不用 SnackbarHostState，直接用一个 Surface 弹条消息
//                Surface(
//                    tonalElevation = 4.dp,
//                    modifier = Modifier
//                        .align(Alignment.BottomCenter)
//                        .padding(16.dp)
//                ) {
//                    Row(
//                        modifier = Modifier
//                            .padding(horizontal = 16.dp, vertical = 8.dp),
//                        verticalAlignment = Alignment.CenterVertically
//                    ) {
//                        Text(
//                            text = message,
//                            style = MaterialTheme.typography.bodyMedium,
//                            modifier = Modifier.weight(1f)
//                        )
//                        TextButton(onClick = onErrorShown) {
//                            Text("知道了")
//                        }
//                    }
//                }
//            }
//        }
//    }
//}
//
///**
// * Preview：造一个假的 UiState，看看整体界面效果
// */
//@Preview(showBackground = true)
//@Composable
//fun TodoDetailScreenPreview() {
//    val fakeState = TodoDetailViewModel.UiState(
//        id = 1L,
//        title = "完成 Android 办公应用作业",
//        description = "实现待办模块客户端：列表 + 详情页 UI、ViewModel、UseCase，并和协作者项目结构对齐。",
//        deadline = "2025-12-05",
//        isCompleted = false,
//        isNewTask = false,
//        isLoading = false,
//        isSaving = false,
//        errorMessage = null,
//        saveSuccess = false
//    )
//
//    MaterialTheme {
//        TodoDetailScreenStateless(
//            uiState = fakeState,
//            isEdit = true,
//            onBack = {},
//            onTitleChange = {},
//            onDescriptionChange = {},
//            onDeadlineChange = {},
//            onCompletedChange = {},
//            onSaveClicked = {},
//            onErrorShown = {}
//        )
//    }
//}