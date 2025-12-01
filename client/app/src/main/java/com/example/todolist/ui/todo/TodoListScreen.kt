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
import androidx.compose.ui.tooling.preview.Preview
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

//    // 把真正的 UI 渲染委托给一个“无状态”的版本，方便 Preview 复用
//    TodoListScreenStateless(
//        uiState = uiState,
//        onItemClick = onItemClick,
//        onAddClick = onAddClick,
//        onToggleCompleted = { id -> viewModel.onToggleCompleted(id) },
//        onDeleteClick = { id -> viewModel.onDeleteTodo(id) },
//        onErrorShown = { viewModel.onErrorShown() }
//    )

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



///**
// * 无状态版本：只依赖传入的 UiState 和回调，不关心 ViewModel
// * Preview 就是直接调这个
// */
//@OptIn(ExperimentalMaterial3Api::class)
//@Composable
//private fun TodoListScreenStateless(
//    uiState: TodoListViewModel.UiState,
//    onItemClick: (Long) -> Unit,
//    onAddClick: () -> Unit,
//    onToggleCompleted: (Long) -> Unit,
//    onDeleteClick: (Long) -> Unit,
//    onErrorShown: () -> Unit
//) {
//    Scaffold(
//        topBar = {
//            TopAppBar(
//                title = { Text("待办任务") }
//            )
//        },
//        floatingActionButton = {
//            FloatingActionButton(onClick = onAddClick) {
//                Text("+")
//            }
//        }
//    ) { padding ->
//        Box(
//            modifier = Modifier
//                .fillMaxSize()
//                .padding(padding)
//        ) {
//            when {
//                uiState.isLoading -> {
//                    CircularProgressIndicator(
//                        modifier = Modifier.align(Alignment.Center)
//                    )
//                }
//
//                uiState.todos.isEmpty() -> {
//                    Text(
//                        text = "暂无待办任务，点击右下角 + 新建",
//                        modifier = Modifier.align(Alignment.Center)
//                    )
//                }
//
//                else -> {
//                    LazyColumn(
//                        modifier = Modifier.fillMaxSize(),
//                        contentPadding = PaddingValues(16.dp),
//                        verticalArrangement = Arrangement.spacedBy(8.dp)
//                    ) {
//                        items(uiState.todos) { todo ->
//                            TodoListItem(
//                                task = todo,
//                                onClick = { onItemClick(todo.id) },
//                                onToggleCompleted = { onToggleCompleted(todo.id) },
//                                onDeleteClick = { onDeleteClick(todo.id) }
//                            )
//                        }
//                    }
//                }
//            }
//
//            // 这里为了简单预览，不用真正的 SnackbarHostState，
//            // 只要能看到界面的基本样子就行
//            uiState.errorMessage?.let { message ->
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
// * 预览：这里不用 ViewModel，只造一份假的 UiState
// */
//@Preview(showBackground = true)
//@Composable
//fun TodoListScreenPreview() {
//    // 几条假数据
//    val sampleTodos = listOf(
//        TodoTask(
//            id = 1L,
//            title = "完成课程作业",
//            description = "把待办任务模块的客户端部分写完并提交",
//            deadline = "2025-12-01",
//            isCompleted = false
//        ),
//        TodoTask(
//            id = 2L,
//            title = "组会讨论",
//            description = "和同组同学讨论后端接口和数据格式",
//            deadline = "2025-12-02",
//            isCompleted = true
//        )
//    )
//
//    val fakeState = TodoListViewModel.UiState(
//        isLoading = false,
//        todos = sampleTodos,
//        errorMessage = null
//    )
//
//    MaterialTheme {
//        TodoListScreenStateless(
//            uiState = fakeState,
//            onItemClick = {},
//            onAddClick = {},
//            onToggleCompleted = {},
//            onDeleteClick = {},
//            onErrorShown = {}
//        )
//    }
//}
