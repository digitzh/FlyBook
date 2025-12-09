package com.example.myhomepage.ui

import androidx.activity.compose.LocalOnBackPressedDispatcherOwner
import androidx.compose.animation.core.Animatable
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.Role.Companion.Button
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.myhomepage.R
import com.example.myhomepage.WeViewModel
import com.example.myhomepage.data.Backlog
import com.example.myhomepage.ui.theme.TodoType
import com.example.myhomepage.ui.theme.WeComposeTheme
import kotlinx.serialization.Serializable

import com.example.myhomepage.todolist.presentation.TodoListViewModel
import com.example.myhomepage.todolist.data.toBacklog
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue

@Serializable
data class TodoDetails(val todoId: Long)

@Composable
fun TodoDetailsPage(
    listViewModel: TodoListViewModel,
    todoId: Long,
    onBack: () -> Unit,
    onEditClick: () -> Unit  // 从这里跳到 AddTodoPage 做编辑
) {
    val uiState by listViewModel.uiState.collectAsState()
    val task = uiState.todos.firstOrNull { it.id == todoId }

    if (task == null) {
        // 任务找不到，直接返回列表
        LaunchedEffect(Unit) {
            onBack()
        }
        return
    }

    val affair = task.toBacklog()   // 用刚才的 mapper 转成 Backlog
    Column(
        Modifier
            .background(WeComposeTheme.colors.background)
            .fillMaxSize()
            .statusBarsPadding()
            .verticalScroll(rememberScrollState())
            .imePadding()
    ) {
        val backDispatcher = LocalOnBackPressedDispatcherOwner.current?.onBackPressedDispatcher
        WeTopBar(affair.title, onBack = { backDispatcher?.onBackPressed() })

        Row (
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.Top,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Image(
                painter = painterResource(
                    when (affair.type) {
                    TodoType.FILE -> R.drawable.ic_contact_tag
                    TodoType.CONF -> R.drawable.ic_contact_official
                    TodoType.MSG -> affair.avatar
                    TodoType.OTHER -> R.drawable.ic_photos
                }),
                contentDescription = "待办图标",
                modifier = Modifier
                    .size(160.dp)
                    .clip(RoundedCornerShape(12.dp)),
                contentScale = ContentScale.Crop
            )

            // 右上：标题 + 截止时间
            Column(
                modifier = Modifier.weight(1f), // 占剩余宽度
                verticalArrangement = Arrangement.Top
            ) {
                // 标题
                Text(
                    text = affair.title,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    color = WeComposeTheme.colors.textPrimary,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(8.dp))
                // 截止时间（带图标）
                Text(
                    text = "截止时间：${affair.time}",
                    fontSize = 18.sp,
                    color = WeComposeTheme.colors.redletter
                )

                // 类型标签（根据type显示标签）
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "类型：${affair.type}",
                    fontSize = 16.sp,
                    color = WeComposeTheme.colors.textPrimary
                )

                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "状态：${if (affair.complete) "已完成" else "未完成"}",
                    fontSize = 16.sp,
                    color = WeComposeTheme.colors.textPrimary
                )
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        Text(
            text = affair.text,
            fontSize = 18.sp,
            color = WeComposeTheme.colors.textPrimary,
            lineHeight = 24.sp,
            modifier = Modifier
                .fillMaxWidth()
                .clickable{ onEditClick() }
        )

        Spacer(
            Modifier
                .background(WeComposeTheme.colors.background)
                .fillMaxWidth()
                .height(300.dp)
        )

        DoneButton(affair.complete, onToggle = { listViewModel.onToggleCompleted(todoId) }) //onToggle--标记事务已完成的函数
        Spacer(
            modifier = Modifier.height(20.dp)
        )
        DeleteButton(deleteClick = {listViewModel.onDeleteTodo(todoId) }) //删除事务返回上一页的逻辑

    }
}

@Composable
fun DoneButton(complete: Boolean, onToggle: () -> Unit){
    Row(Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center
    ) {
        Checkbox(
            checked = complete,
            onCheckedChange = { onToggle() }
        )
        Text(
            "标记为已完成",
            fontSize = 20.sp,
            color = WeComposeTheme.colors.textSecondary,
            fontWeight = FontWeight.Bold,
        )
    }
}

@Composable
fun DeleteButton(deleteClick : () -> Unit){
    Row(Modifier.fillMaxWidth().clickable { deleteClick() },
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center
    ) {
        Image(
            painterResource(R.drawable.delete), "delete", Modifier.size(25.dp),
        )
        Text(
            "删除",
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
        )
    }
}
