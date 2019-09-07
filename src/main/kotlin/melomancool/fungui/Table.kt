package melomancool.fungui.table

import melomancool.fungui.utils.PMatrix
import melomancool.fungui.utils.pvectorOf

import melomancool.fungui.GridLayout
import melomancool.fungui.TextField
import melomancool.fungui.View
import melomancool.fungui.WithAttrs
import melomancool.fungui.FullWidth

import melomancool.fungui.run

data class Model(val cells: PMatrix<String>)

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
        initialModel = Model(PMatrix(pvectorOf(
            pvectorOf(   "top-left",    "top-right"),
            pvectorOf("bottom-left", "bottom-right")
        ))),
        view = ::view,
        update = ::update
    )
}
