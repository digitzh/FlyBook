package com.example.todolist.domain.usecase

import com.example.todolist.domain.model.TodoTask
import com.example.todolist.domain.repository.TodoRepository

/**
 * 保存（新增或更新）待办任务的 UseCase
 */
class SaveTodoUseCase(
    private val repository: TodoRepository
) {
    /**
     * 保存待办任务前进行校验
     */
    suspend operator fun invoke(todo: TodoTask) {
        // 1. 校验主题不能为空
        if (todo.title.isBlank()) {
            throw IllegalArgumentException("任务主题不能为空")
        }

        // 2. 校验主题长度
        if (todo.title.length > 100) {
            throw IllegalArgumentException("任务主题不能超过100个字符")
        }

        // 3. 校验描述长度
        if (todo.description.length > 2000) {
            throw IllegalArgumentException("任务描述不能超过2000个字符")
        }

        // 4. 校验截止日期格式和合法性
        todo.deadline?.let {
            if (it.isNotBlank()) {
                validateDeadline(todo.deadline)
            }
        }

        // 校验通过，保存任务
        repository.upsertTodo(todo)
    }

    /**
     * 校验截止日期格式和合法性
     * 截止日期字符串，格式为 "2025-12-03"
     */
    private fun validateDeadline(deadline: String) {
        // 校验日期格式：YYYY-MM-DD
        val datePattern = """^\d{4}-\d{2}-\d{2}$""".toRegex()
        if (!deadline.matches(datePattern)) {
            throw IllegalArgumentException("截止日期格式不正确，应为 YYYY-MM-DD 格式，例如：2025-12-03")
        }

        try {
            // 解析日期各部分
            val parts = deadline.split("-")
            val year = parts[0].toInt()
            val month = parts[1].toInt()
            val day = parts[2].toInt()

            // 校验年份范围（
            if (year !in 2025..2100) {
                throw IllegalArgumentException("截止日期年份不在合理范围内（2025-2100）")
            }

            // 校验月份范围
            if (month !in 1..12) {
                throw IllegalArgumentException("截止日期月份不合法，应在 1-12 之间")
            }

            // 校验日期范围
            val maxDay = when (month) {
                2 -> if (isLeapYear(year)) 29 else 28
                4, 6, 9, 11 -> 30
                else -> 31
            }

            if (day !in 1..maxDay) {
                throw IllegalArgumentException("截止日期天数不合法，$year 年 $month 月应在 1-$maxDay 之间")
            }


        } catch (_: NumberFormatException) {
            throw IllegalArgumentException("截止日期格式错误，包含非数字字符")
        } catch (e: IllegalArgumentException) {
            // 重新抛出自定义的异常
            throw e
        } catch (e: Exception) {
            throw IllegalArgumentException("截止日期校验失败：${e.message}")
        }
    }

    // 判断是否为闰年
    private fun isLeapYear(year: Int): Boolean {
        return (year % 4 == 0 && year % 100 != 0) || (year % 400 == 0)
    }
}
