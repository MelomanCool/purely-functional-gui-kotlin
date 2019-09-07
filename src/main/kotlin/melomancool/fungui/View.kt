package melomancool.fungui

sealed class View<out Mes>

sealed class Element<Mes>: View<Mes>()
sealed class Layout<Mes>: View<Mes>()

data class Button<Mes>(
    val text: String,
    val onClick: Mes? = null
): Element<Mes>()

data class Label(val text: String): Element<Nothing>()

data class VerticalLayout<Mes>(val children: List<View<Mes>>): Layout<Mes>() {
    constructor(vararg children: View<Mes>) : this(children.toList())
}

data class HorizontalLayout<Mes>(val children: List<View<Mes>>): Layout<Mes>() {
    constructor(vararg children: View<Mes>) : this(children.toList())
}

data class TextField<Mes>(
    val label: String,
    val text: String,
    val onInput: ((String) -> Mes)? = null,
    val fullWidth: Boolean = false
): Element<Mes>()

data class Checkbox<Mes>(
    val label: String,
    val isChecked: Boolean,
    val onClick: ((Boolean) -> Mes)? = null
): Element<Mes>()

data class GridLayout<Mes>(val children: List<List<View<Mes>>>): Layout<Mes>()
