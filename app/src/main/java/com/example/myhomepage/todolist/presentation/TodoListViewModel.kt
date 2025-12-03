package com.example.myhomepage.todolist.presentation


import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myhomepage.todolist.data.TodoTask
import com.example.myhomepage.todolist.domain.usecase.DeleteTodoUseCase
import com.example.myhomepage.todolist.domain.usecase.ObserveTodoListUseCase
import com.example.myhomepage.todolist.domain.usecase.ToggleTodoCompletedUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * 待办列表页的 ViewModel
 *
 * 负责：
 * - 从 UseCase 拿到列表数据
 * - 保持当前 UI 状态（加载中 / 正常 / 错误）
 * - 响应用户操作（切换完成、删除）
 */
class TodoListViewModel(
    private val observeTodoListUseCase: ObserveTodoListUseCase,
    private val toggleTodoCompletedUseCase: ToggleTodoCompletedUseCase,
    private val deleteTodoUseCase: DeleteTodoUseCase
) : ViewModel() {

    // UI 状态：列表 + 是否加载中 + 错误信息。

    data class UiState(
        val isLoading: Boolean = false,
        val todos: List<TodoTask> = emptyList(),
        val errorMessage: String? = null
    )

    private val _uiState = MutableStateFlow(UiState(isLoading = true))
    val uiState: StateFlow<UiState> = _uiState.asStateFlow()

    init {
        observeTodos()
    }

    private fun observeTodos() {
        viewModelScope.launch {
            // 收集 Flow 并更新列表
            observeTodoListUseCase()
                .collect { list ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            todos = list,
                            errorMessage = null
                        )
                    }
                }
        }
    }

    // 点击复选框时调用，切换完成状态

    fun onToggleCompleted(id: Long) {
        viewModelScope.launch {
            try {
                toggleTodoCompletedUseCase(id)
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(errorMessage = "更新任务状态失败: ${e.message}")
                }
            }
        }
    }

    // 删除按钮点击

    fun onDeleteTodo(id: Long) {
        viewModelScope.launch {
            try {
                deleteTodoUseCase(id)
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(errorMessage = "删除任务失败: ${e.message}")
                }
            }
        }
    }

    // 错误展示完毕后清除（例如 Snackbar 点击“知道了”）

    fun onErrorShown() {
        _uiState.update { it.copy(errorMessage = null) }
    }
}
