package com.example.todolist.ui.todo

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.todolist.domain.model.TodoTask
import com.example.todolist.presentation.list.TodoListViewModel

/**
 * 待办列表页 Composable
 *
 * 通过参数传入：
 * - viewModel: 对应列表的 ViewModel
 * - onItemClick: 点击某一条任务，外部决定怎么导航到详情页
 * - onAddClick: 点击新增按钮，外部决定怎么导航到“新建详情页”
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TodoListScreen(
    viewModel: TodoListViewModel,
    onItemClick: (Long) -> Unit,
    onAddClick: () -> Unit
) {
    // 从 StateFlow 收集 UI 状态
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("待办任务") }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = onAddClick) {
                Text("+") // 简单写个 "+"
            }
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            when {
                uiState.isLoading -> {
                    // 加载中
                    CircularProgressIndicator(
                        modifier = Modifier.align(Alignment.Center)
                    )
                }
                uiState.todos.isEmpty() -> {
                    // 空列表提示
                    Text(
                        text = "暂无待办任务，点击右下角 + 新建",
                        modifier = Modifier.align(Alignment.Center)
                    )
                }
                else -> {
                    // 列表
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(uiState.todos) { todo ->
                            TodoListItem(
                                task = todo,
                                onClick = { onItemClick(todo.id) },
                                onToggleCompleted = { viewModel.onToggleCompleted(todo.id) },
                                onDeleteClick = { viewModel.onDeleteTodo(todo.id) }
                            )
                        }
                    }
                }
            }

            // 错误提示（这里用一个简单的 Snackbar 示例，你也可以用 AlertDialog）
            uiState.errorMessage?.let { message ->
                SnackbarHost(
                    hostState = SnackbarHostState(), // 示例：真实项目里应 hoist 出来
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

/**
 * 单条待办在列表中的展示。
 */
@Composable
private fun TodoListItem(
    task: TodoTask,
    onClick: () -> Unit,
    onToggleCompleted: () -> Unit,
    onDeleteClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .padding(12.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Checkbox(
                checked = task.isCompleted,
                onCheckedChange = { onToggleCompleted() }
            )

            Spacer(modifier = Modifier.width(8.dp))

            Column(
                modifier = Modifier
                    .weight(1f) // 占据剩余宽度
            ) {
                Text(
                    text = task.title,
                    style = MaterialTheme.typography.titleMedium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                if (task.description.isNotBlank()) {
                    Text(
                        text = task.description,
                        style = MaterialTheme.typography.bodyMedium,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                task.deadline?.let { deadline ->
                    Text(
                        text = "截止：$deadline",
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }

            IconButton(onClick = onDeleteClick) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = "删除"
                )
            }
        }
    }
}
