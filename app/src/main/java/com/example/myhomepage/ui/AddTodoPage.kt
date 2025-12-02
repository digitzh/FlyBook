package com.example.myhomepage.ui

import androidx.activity.compose.LocalOnBackPressedDispatcherOwner
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.myhomepage.R
import com.example.myhomepage.WeViewModel
import com.example.myhomepage.ui.theme.TodoType
import com.example.myhomepage.ui.theme.WeComposeTheme
import kotlinx.serialization.Serializable
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

@Serializable
object todoAdd


private val LightBlueBg = Color(0xFFF0F8FF)       // 浅蓝背景
private val LightPurple = Color(0xFF9C27B0)       // 浅紫主色
private val LightBlueAccent = Color(0xFF2196F3)   // 浅蓝强调色
private val CardBg = Color(0xFFFFFFFF)

@Composable
fun AddTodoPage(viewModel: WeViewModel, addTodo:()->Unit){ //TODO addTodo增加待办事项，可能需要传参数
    var title by remember { mutableStateOf("") }
    var deadline by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var selectedType by remember { mutableStateOf(TodoType.FILE) }
    var showDatePicker by remember { mutableStateOf(false) }
    val datePickerState = rememberDatePickerState()

    Column(
        Modifier
            .background(WeComposeTheme.colors.background)
            .fillMaxSize()
            .statusBarsPadding()
    ) {
        // 标题
        val backDispatcher = LocalOnBackPressedDispatcherOwner.current?.onBackPressedDispatcher
        WeTopBar("增加待办", onBack = { backDispatcher?.onBackPressed() })

        // 卡片容器（主体内容）
        val CardBg = null
        Card(
            modifier = Modifier.fillMaxSize(),
            shape = RoundedCornerShape(12.dp),
            elevation = CardDefaults.cardElevation(4.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(40.dp)
            ) {
                // 1. 待办标题输入
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("待办标题", color = LightBlueAccent) },
                    modifier = Modifier.fillMaxWidth(),
                    maxLines = 1,
                )

                // 2. 截止日期选择
                OutlinedTextField(
                    value = deadline,
                    onValueChange = {}, // 只读，仅点击选择
                    label = { Text("截止日期", color = LightBlueAccent) },
                    modifier = Modifier.fillMaxWidth(),
                    readOnly = true,
                    trailingIcon = {
                        Image(
                            painterResource(R.drawable.date), "date",
                            Modifier.size(25.dp).clickable{showDatePicker = true},
                        )
                    },

                )

                // 3. 详细描述输入
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("详细描述", color = LightBlueAccent) },
                    modifier = Modifier.fillMaxWidth(),
                    maxLines = 3,

                )

                // 4. 待办类型选择（FILE/CONF/OTHER）
                Column {
                    Text(
                        text = "待办类型",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium,
                        color = LightPurple,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        TodoType.values().forEach { type ->
                            if(type != TodoType.MSG)
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.clickable { selectedType = type }
                                ) {
                                    Checkbox(
                                        checked = selectedType == type,
                                        onCheckedChange = { selectedType = type },
                                    )
                                    Text(
                                        text = type.name,
                                        color = if (selectedType == type) LightPurple else LightBlueAccent
                                    )
                                }
                        }
                    }
                }

                // 5. 添加按钮
                Row(
                    modifier = Modifier.fillMaxWidth().clickable{ addTodo()}, //TODO 添加待办事项的逻辑
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    Text(text = "添加待办", fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(8.dp))
                }
            }
        }
    }

    // 日期选择弹窗
    if (showDatePicker) {
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        deadline = formatDate(datePickerState.selectedDateMillis)
                        showDatePicker = false
                    }
                ) {
                    Text("确认", color = LightPurple)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) {
                    Text("取消", color = LightBlueAccent)
                }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }
}


fun formatDate(millis: Long?): String {
    millis ?: return ""
    val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    val calendar = Calendar.getInstance().apply { timeInMillis = millis }
    return sdf.format(calendar.time)
}

//@Preview(showBackground = true)
//@Composable
//fun NewTodoScreenPreview() {
//    AddTodoPage()
//}