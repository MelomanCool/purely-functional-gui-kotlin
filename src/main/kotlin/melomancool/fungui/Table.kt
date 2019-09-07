package melomancool.fungui.table

import org.pcollections.PVector
import org.pcollections.TreePVector

import melomancool.fungui.GridLayout
import melomancool.fungui.TextField
import melomancool.fungui.View
import melomancool.fungui.WithAttrs
import melomancool.fungui.FullWidth

import melomancool.fungui.run

data class Model(val cells: PMatrix<String>)

data class PMatrix<E>(val cells: PVector<PVector<E>>) {
    fun with(x: Int, y: Int, e: E): PMatrix<E> =
        this.copy(cells = this.cells.with(y, this.cells[y].with(x, e)))

    fun get(x: Int, y: Int): E =
        this.cells[y][x]
}

sealed class Msg
data class SetCellText(val x: Int, val y: Int, val text: String): Msg()

fun view(model: Model): View<Msg> =
    GridLayout(
        model.cells.cells.mapIndexed { y, row ->
            row.mapIndexed { x, text ->
                WithAttrs(
                    TextField(label = "", text = text, onInput = { SetCellText(x, y, it) }),
                    listOf(FullWidth)
                )
            }
        }
    )

fun update(msg: Msg, model: Model): Model =
    when (msg) {
        is SetCellText -> {
            val (x, y, text) = msg
            model.copy(cells = model.cells.with(x, y, text))
        }
    }

fun main() {
    run(
        initialModel = Model(PMatrix<String>(TreePVector.from(listOf(
            TreePVector.from(listOf(   "top-left",    "top-right")),
            TreePVector.from(listOf("bottom-left", "bottom-right"))
        )))),
        view = ::view,
        update = ::update
    )
}
