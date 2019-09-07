package melomancool.fungui.utils

import org.pcollections.PVector
import org.pcollections.TreePVector

data class PMatrix<E>(val cells: PVector<PVector<E>>) {
    fun with(x: Int, y: Int, e: E): PMatrix<E> =
        this.copy(cells = this.cells.with(y, this.cells[y].with(x, e)))

    fun get(x: Int, y: Int): E =
        this.cells[y][x]
}

fun <E> pvectorOf(vararg els: E): PVector<E> = TreePVector.from(els.toList())
