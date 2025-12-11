package com.example.myhomepage.todolist.presentation

import com.example.myhomepage.MainDispatcherRule
import com.example.myhomepage.todolist.data.repository.InMemoryTodoRepository
import com.example.myhomepage.todolist.domain.TodoTask
import com.example.myhomepage.todolist.domain.usecase.ObserveTodoDetailUseCase
import com.example.myhomepage.todolist.domain.usecase.SaveTodoUseCase
import com.example.myhomepage.ui.theme.TodoType
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class TodoDetailViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    @Test
    fun `loadExistingTask should fill UiState from repository`() =
        runTest(mainDispatcherRule.testDispatcher) {
            val repo = InMemoryTodoRepository()
            val sample = TodoTask(
                id = 1L,
                title = "Existing",
                description = "old desc",
                deadline = "2025-12-31",
                isCompleted = false,
                type = TodoType.OTHER
            )
            repo.upsertTodo(sample)

            val viewModel = TodoDetailViewModel(
                observeTodoDetailUseCase = ObserveTodoDetailUseCase(repo),
                saveTodoUseCase = SaveTodoUseCase(repo),
                saveSharedTodoUseCase = SaveTodoUseCase(repo) // 重用同一个即可
            )

            viewModel.loadExistingTask(1L)
            advanceUntilIdle()

            val state = viewModel.uiState.value
            assertEquals(sample.id, state.id)
            assertEquals("Existing", state.title)
            assertEquals("old desc", state.description)
            assertEquals("2025-12-31", state.deadline)
            assertFalse(state.isNewTask)
            assertFalse(state.isLoading)
            assertNull(state.errorMessage)
        }

    @Test
    fun `onSaveClicked should save new task and set saveSuccess`() =
        runTest(mainDispatcherRule.testDispatcher) {
            val repo = InMemoryTodoRepository()
            val viewModel = TodoDetailViewModel(
                observeTodoDetailUseCase = ObserveTodoDetailUseCase(repo),
                saveTodoUseCase = SaveTodoUseCase(repo),
                saveSharedTodoUseCase = SaveTodoUseCase(repo)
            )

            // 模拟用户输入
            viewModel.onTitleChange("New Task")
            viewModel.onDescriptionChange("some desc")
            viewModel.onDeadlineChange("2025-01-01")
            viewModel.onTypeChange(TodoType.OTHER)

            viewModel.onSaveClicked()
            advanceUntilIdle()

            // 1) UI 状态
            val state = viewModel.uiState.value
            assertFalse(state.isSaving)
            assertTrue(state.saveSuccess)
            assertNull(state.errorMessage)

            // 2) Repository 里确实有这条任务
            val list = repo.observeTodos().first()
            assertEquals(1, list.size)
            assertEquals("New Task", list[0].title)
        }

    @Test
    fun `onSaveClicked with empty title should set errorMessage`() =
        runTest(mainDispatcherRule.testDispatcher) {
            val repo = InMemoryTodoRepository()
            val viewModel = TodoDetailViewModel(
                observeTodoDetailUseCase = ObserveTodoDetailUseCase(repo),
                saveTodoUseCase = SaveTodoUseCase(repo),
                saveSharedTodoUseCase = SaveTodoUseCase(repo)
            )

            // 标题保持空字符串
            viewModel.onDescriptionChange("desc")
            viewModel.onSaveClicked()
            advanceUntilIdle()

            val state = viewModel.uiState.value
            assertFalse(state.saveSuccess)
            assertNotNull(state.errorMessage)
        }

    @Test
    fun `startNewTask should reset UiState`() =
        runTest(mainDispatcherRule.testDispatcher) {
            val repo = InMemoryTodoRepository()
            val viewModel = TodoDetailViewModel(
                observeTodoDetailUseCase = ObserveTodoDetailUseCase(repo),
                saveTodoUseCase = SaveTodoUseCase(repo),
                saveSharedTodoUseCase = SaveTodoUseCase(repo)
            )

            viewModel.onTitleChange("xxx")
            viewModel.onDescriptionChange("yyy")
            viewModel.onDeadlineChange("2025-01-01")

            viewModel.startNewTask()

            val state = viewModel.uiState.value
            assertNull(state.id)
            assertEquals("", state.title)
            assertEquals("", state.description)
            assertEquals("", state.deadline)
            assertTrue(state.isNewTask)
        }
}
