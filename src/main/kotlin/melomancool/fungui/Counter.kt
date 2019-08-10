package melomancool.fungui.counter

import melomancool.fungui.View
import melomancool.fungui.VerticalLayout
import melomancool.fungui.Button
import melomancool.fungui.Label
import melomancool.fungui.run

typealias Model = Int

sealed class Msg
object Increment : Msg()
object Decrement : Msg()

fun view(model: Model): View<Msg> =
    VerticalLayout(
        Button("-", onClick = Decrement),
        Label(model.toString()),
        Button("+", onClick = Increment)
    )

fun update(msg: Msg, model: Model): Model =
    when (msg) {
        is Increment ->
            model + 1
        is Decrement ->
            model - 1
    }

fun main() {
    run(
        initialModel = 0,
        view = ::view,
        update = ::update
    )
}
