package com.example.myhomepage.todolist.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myhomepage.todolist.domain.TodoTask
import com.example.myhomepage.todolist.domain.usecase.ObserveTodoDetailUseCase
import com.example.myhomepage.todolist.domain.usecase.SaveTodoUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import com.example.myhomepage.ui.theme.TodoType

/**
 * 待办详情页（新建 & 编辑共用）的 ViewModel。
 *
 * 约定：
 * - newTask = true 表示新建，id 在点击保存时生成
 * - newTask = false 表示编辑，外部传入已有 id，并调用 loadExistingTask
 */
class TodoDetailViewModel(
    private val observeTodoDetailUseCase: ObserveTodoDetailUseCase,
    private val saveTodoUseCase: SaveTodoUseCase
) : ViewModel() {

    data class UiState(
        val id: Long? = null,          // null 表示新建
        val title: String = "",
        val description: String = "",
        val deadline: String = "",     // 简化：用字符串存 yyyy-MM-dd
        val isCompleted: Boolean = false,
        val type: TodoType = TodoType.OTHER,  //当前选择的任务类型，默认 OTHER
        val isNewTask: Boolean = true,
        val isLoading: Boolean = false,
        val isSaving: Boolean = false,
        val errorMessage: String? = null,
        val saveSuccess: Boolean = false // 保存成功用于通知 UI 返回上一页
    )

    private val _uiState = MutableStateFlow(UiState())
    val uiState: StateFlow<UiState> = _uiState.asStateFlow()

    fun startNewTask() {
        _uiState.value = UiState()   // 把所有字段重置为默认
    }

    /**
     * 编辑已有任务时调用，加载 id 对应的数据
     * 一般在 Composable 初始化时或 NavBackStackEntry 里调用
     */
    fun loadExistingTask(id: Long) {
        // 避免重复加载
        if (_uiState.value.id == id && !_uiState.value.isNewTask) return

        _uiState.update { it.copy(isLoading = true, isNewTask = false) }

        viewModelScope.launch {
            observeTodoDetailUseCase(id).collect { task ->
                if (task != null) {
                    _uiState.update {
                        it.copy(
                            id = task.id,
                            title = task.title,
                            description = task.description,
                            deadline = task.deadline.orEmpty(),
                            isCompleted = task.isCompleted,
                            type = task.type,
                            isLoading = false,
                            errorMessage = null
                        )
                    }
                } else {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = "未找到该任务（可能已被删除）"
                        )
                    }
                }
            }
        }
    }

    // 以下是对各输入控件的事件响应

    fun onTitleChange(newTitle: String) {
        _uiState.update { it.copy(title = newTitle) }
    }

    fun onTypeChange(newType: TodoType) {
        _uiState.update { it.copy(type = newType) }
    }

    fun onDescriptionChange(newDescription: String) {
        _uiState.update { it.copy(description = newDescription) }
    }

    fun onDeadlineChange(newDeadline: String) {
        _uiState.update { it.copy(deadline = newDeadline) }
    }

    fun onCompletedChange(completed: Boolean) {
        _uiState.update { it.copy(isCompleted = completed) }
    }

    fun onErrorShown() {
        _uiState.update { it.copy(errorMessage = null) }
    }

    fun onSaveConsumed() {
        _uiState.update { it.copy(saveSuccess = false) }
    }

    /**
     * 点击“保存”按钮时调用
     */
    fun onSaveClicked() {
        val current = _uiState.value

//        if (current.title.isBlank()) {
//            _uiState.update { it.copy(errorMessage = "标题不能为空") }
//            return
//        }

        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true) }

            try {
                val todo = TodoTask(
                    id = current.id ?: System.currentTimeMillis(), // 新建时简单用时间戳当 id
                    title = current.title.trim(),
                    description = current.description.trim(),
                    deadline = current.deadline.takeIf { it.isNotBlank() },
                    isCompleted = current.isCompleted,
                    type = current.type
                )

                saveTodoUseCase(todo)

                _uiState.update {
                    it.copy(
                        isSaving = false,
                        saveSuccess = true  // 通知 UI 可以关闭页面
                    )
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isSaving = false,
                        errorMessage = "保存失败: ${e.message}"
                    )
                }
            }
        }
    }

}
