package com.example.todolist

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.todolist.presentation.detail.TodoDetailViewModel
import com.example.todolist.presentation.list.TodoListViewModel
import com.example.todolist.ui.todo.TodoDetailScreen
import com.example.todolist.ui.todo.TodoListScreen

/**
 * 整个待办模块的入口：
 * - 起始页面是 Todo 列表
 * - 支持跳转到“新建”和“编辑”详情页
 */
@Composable
fun TodoApp() {
    val navController = rememberNavController()

    // 记住一个 AppContainer，保证整个 app 生命周期内共享同一套 UseCase/Repository
    val container = remember { TodoAppContainer() }

    NavHost(
        navController = navController,
        startDestination = "todo_list"
    ) {
        // 1. 列表页
        composable("todo_list") {
            val listViewModel: TodoListViewModel = viewModel(
                factory = TodoListViewModelFactory(container)
            )

            TodoListScreen(
                viewModel = listViewModel,
                onItemClick = { id ->
                    // 跳转到编辑页面
                    navController.navigate("todo_detail/$id")
                },
                onAddClick = {
                    // 跳转到新建页面
                    navController.navigate("todo_create")
                }
            )
        }

        // 2. 新建任务详情页（没有 id）
        composable("todo_create") {
            val detailViewModel: TodoDetailViewModel = viewModel(
                factory = TodoDetailViewModelFactory(container)
            )

            TodoDetailScreen(
                viewModel = detailViewModel,
                isEdit = false,
                todoId = null,
                onBack = { navController.popBackStack() }
            )
        }

        // 3. 编辑任务详情页（带 id 参数）
        composable(
            route = "todo_detail/{id}",
            arguments = listOf(
                navArgument("id") { type = NavType.LongType }
            )
        ) { backStackEntry ->
            val detailViewModel: TodoDetailViewModel = viewModel(
                factory = TodoDetailViewModelFactory(container)
            )

            val id = backStackEntry.arguments?.getLong("id")

            TodoDetailScreen(
                viewModel = detailViewModel,
                isEdit = true,
                todoId = id,
                onBack = { navController.popBackStack() }
            )
        }
    }
}
