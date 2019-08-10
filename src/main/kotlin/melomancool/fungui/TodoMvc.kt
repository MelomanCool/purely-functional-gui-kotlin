package melomancool.fungui.todomvc

import arrow.optics.optics

import melomancool.fungui.Button
import melomancool.fungui.Label
import melomancool.fungui.TextField
import melomancool.fungui.VerticalLayout
import melomancool.fungui.View

import melomancool.fungui.run

@optics
data class Todo(val text: String) {
    companion object
}

@optics
data class Model(val newTodoText: String, val todos: List<Todo>) {
    companion object
}

sealed class Msg
data class SetNewTodoText(val text: String): Msg()
object AddNewTodo: Msg()

fun view(model: Model): View<Msg> =
    VerticalLayout(
        *model.todos.map { Label(it.text) }.toTypedArray(),
        TextField(label = "", text = model.newTodoText, onInput = ::SetNewTodoText),
        Button(text = "Add Todo", onClick = AddNewTodo)
    )

fun update(msg: Msg, model: Model): Model =
    when (msg) {
        is SetNewTodoText ->
            model.copy(newTodoText = msg.text)
        is AddNewTodo ->
            if (model.newTodoText.isNotBlank()) {
                model.copy(
                    newTodoText = "",
                    todos = model.todos + Todo(model.newTodoText)
                )
            } else {
                model
            }
    }

fun main() {
    run(
        initialModel = Model(newTodoText = "", todos = emptyList()),
        view = ::view,
        update = ::update
    )
}
