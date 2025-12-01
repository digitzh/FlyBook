# ToDoList

第一次提交（基本功能实现）
- 使用Gradle Kotlin DSL和版本目录配置项目结构
- Domain层，包含`TodoTask`实体、`TodoRepository`接口及标准用例（保存、删除、观察、切换）
- Data层，使用`MutableStateFlow`实现`InMemoryTodoRepository`仅在内存中短时储存
- UI层，简易的UI界面



第二次提交

- 增添任务类别到任务属性；在增改页面增加选择任务类别
- 简单的依赖注入容器实现，封装到`TodoApp`作为待办模块入口