package com.example.myhomepage.todolist.presentation

import com.example.myhomepage.MainDispatcherRule
import com.example.myhomepage.todolist.data.repository.InMemoryTodoRepository
import com.example.myhomepage.todolist.domain.TodoTask
import com.example.myhomepage.todolist.domain.usecase.DeleteTodoUseCase
import com.example.myhomepage.todolist.domain.usecase.ObserveTodoListUseCase
import com.example.myhomepage.todolist.domain.usecase.ToggleTodoCompletedUseCase
import com.example.myhomepage.ui.theme.TodoType
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class TodoListViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    @Test
    fun `init should load todos from usecase`() = runTest(mainDispatcherRule.testDispatcher) {
        val repo = InMemoryTodoRepository()
        val sample = TodoTask(
            id = 1L,
            title = "Sample",
            description = "",
            deadline = null,
            isCompleted = false,
            type = TodoType.OTHER
        )
        repo.upsertTodo(sample)

        val viewModel = TodoListViewModel(
            observeTodoListUseCase = ObserveTodoListUseCase(repo),
            toggleTodoCompletedUseCase = ToggleTodoCompletedUseCase(repo),
            deleteTodoUseCase = DeleteTodoUseCase(repo)
        )

        // 等待 collect Flow 的协程跑完一轮
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertFalse(state.isLoading)
        assertEquals(1, state.todos.size)
        assertEquals(sample, state.todos.first())
        assertNull(state.errorMessage)
    }

    @Test
    fun `onToggleCompleted should update repository`() =
        runTest(mainDispatcherRule.testDispatcher) {
            val repo = InMemoryTodoRepository()
            val sample = TodoTask(
                id = 1L,
                title = "Toggle",
                description = "",
                deadline = null,
                isCompleted = false,
                type = TodoType.OTHER
            )
            repo.upsertTodo(sample)

            val viewModel = TodoListViewModel(
                observeTodoListUseCase = ObserveTodoListUseCase(repo),
                toggleTodoCompletedUseCase = ToggleTodoCompletedUseCase(repo),
                deleteTodoUseCase = DeleteTodoUseCase(repo)
            )

            viewModel.onToggleCompleted(sample.id)
            advanceUntilIdle()

            val newState = viewModel.uiState.value
            assertTrue(newState.todos.first().isCompleted)
        }

    @Test
    fun `onDeleteTodo should remove item`() =
        runTest(mainDispatcherRule.testDispatcher) {
            val repo = InMemoryTodoRepository()
            val sample = TodoTask(
                id = 1L,
                title = "Delete",
                description = "",
                deadline = null,
                isCompleted = false,
                type = TodoType.OTHER
            )
            repo.upsertTodo(sample)

            val viewModel = TodoListViewModel(
                observeTodoListUseCase = ObserveTodoListUseCase(repo),
                toggleTodoCompletedUseCase = ToggleTodoCompletedUseCase(repo),
                deleteTodoUseCase = DeleteTodoUseCase(repo)
            )

            viewModel.onDeleteTodo(sample.id)
            advanceUntilIdle()

            val newState = viewModel.uiState.value
            assertTrue(newState.todos.isEmpty())
        }
}
