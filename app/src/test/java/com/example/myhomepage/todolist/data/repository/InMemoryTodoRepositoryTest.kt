package com.example.myhomepage.todolist.data.repository

import com.example.myhomepage.todolist.domain.TodoTask
import com.example.myhomepage.ui.theme.TodoType
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class InMemoryTodoRepositoryTest {

    @Test
    fun `upsertTodo should insert task and observable list contains it`() = runTest {
        val repo = InMemoryTodoRepository()

        val task = TodoTask(
            id = 1L,
            title = "Test task",
            description = "desc",
            deadline = null,
            isCompleted = false,
            type = TodoType.OTHER
        )

        repo.upsertTodo(task)

        val list = repo.observeTodos().first()
        assertEquals(1, list.size)
        assertEquals(task, list[0])
    }

    @Test
    fun `upsertTodo with same id should update existing task`() = runTest {
        val repo = InMemoryTodoRepository()

        val original = TodoTask(
            id = 1L,
            title = "Original",
            description = "",
            deadline = null,
            isCompleted = false,
            type = TodoType.OTHER
        )
        val updated = original.copy(title = "Updated")

        repo.upsertTodo(original)
        repo.upsertTodo(updated)

        val list = repo.observeTodos().first()
        assertEquals(1, list.size)
        assertEquals("Updated", list[0].title)
    }

    @Test
    fun `deleteTodo should remove task from list`() = runTest {
        val repo = InMemoryTodoRepository()

        val task = TodoTask(
            id = 1L,
            title = "To be deleted",
            description = "",
            deadline = null,
            isCompleted = false,
            type = TodoType.OTHER
        )
        repo.upsertTodo(task)

        repo.deleteTodo(task.id)

        val list = repo.observeTodos().first()
        assertTrue(list.isEmpty())
    }

    @Test
    fun `toggleTodoCompleted should flip isCompleted`() = runTest {
        val repo = InMemoryTodoRepository()

        val task = TodoTask(
            id = 1L,
            title = "Toggle me",
            description = "",
            deadline = null,
            isCompleted = false,
            type = TodoType.OTHER
        )
        repo.upsertTodo(task)

        repo.toggleCompleted(task.id)

        val list = repo.observeTodos().first()
        assertTrue(list[0].isCompleted)
    }
}
