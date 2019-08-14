package melomancool.fungui.todomvc

import melomancool.fungui.Button
import melomancool.fungui.Checkbox
import melomancool.fungui.HorizontalLayout
import melomancool.fungui.Label
import melomancool.fungui.TextField
import melomancool.fungui.VerticalLayout
import melomancool.fungui.View

import melomancool.fungui.run

data class Todo(val text: String, val isDone: Boolean)

data class Model(val newTodoText: String, val todos: List<Todo>)

sealed class Msg
data class SetNewTodoText(val text: String): Msg()
object AddNewTodo: Msg()
data class DeleteTodo(val id: Int): Msg()
data class SetTodoStatus(val id: Int, val isDone: Boolean): Msg()

fun view(model: Model): View<Msg> =
    VerticalLayout(
        *model.todos
            .mapIndexed { i, it -> HorizontalLayout(
                Checkbox(
                    label = it.text,
                    isChecked = it.isDone,
                    onClick = { isChecked -> SetTodoStatus(i, isDone = isChecked) }
                ),
                Button(text = "X", onClick = DeleteTodo(id = i))
            )}
            .toTypedArray(),
        HorizontalLayout(
            TextField(label = "", text = model.newTodoText, onInput = ::SetNewTodoText),
            Button(text = "Add Todo", onClick = AddNewTodo)
        )
    )

fun update(msg: Msg, model: Model): Model =
    when (msg) {
        is SetNewTodoText ->
            model.copy(newTodoText = msg.text)
        is AddNewTodo ->
            if (model.newTodoText.isNotBlank()) {
                Model(
                    newTodoText = "",
                    todos = model.todos + Todo(text = model.newTodoText, isDone = false)
                )
            } else {
                model
            }
        is DeleteTodo ->
            model.copy(todos = model.todos.filterIndexed{ i, _ -> i != msg.id })
        is SetTodoStatus ->
            model.copy(todos = model.todos.mapIndexed{ i, td ->
                if (i == msg.id) {
                    td.copy(isDone = msg.isDone)
                } else {
                    td
                }
            })
    }

fun main() {
    run(
        initialModel = Model(newTodoText = "", todos = emptyList()),
        view = ::view,
        update = ::update
    )
}
