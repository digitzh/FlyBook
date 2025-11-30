package com.example.todolist.ui.todo

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import com.example.todolist.presentation.detail.TodoDetailViewModel

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
}

@Composable
private fun TodoDetailContent(
    uiState: TodoDetailViewModel.UiState,
    onTitleChange: (String) -> Unit,
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
