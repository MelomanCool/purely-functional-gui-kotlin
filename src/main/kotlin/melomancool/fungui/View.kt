package melomancool.fungui

sealed class View<out Mes>

data class Button<Mes>(
    val text: String,
    val onClick: Mes? = null
): View<Mes>()

data class Label(val text: String): View<Nothing>()

data class VerticalLayout<Mes>(val children: List<View<Mes>>): View<Mes>() {
    constructor(vararg children: View<Mes>) : this(children.toList())
}

data class HorizontalLayout<Mes>(val children: List<View<Mes>>): View<Mes>() {
    constructor(vararg children: View<Mes>) : this(children.toList())
}

data class TextField<Mes>(
    val label: String,
    val text: String,
    val onInput: ((String) -> Mes)? = null
): View<Mes>()

data class Checkbox<Mes>(
    val label: String,
    val isChecked: Boolean,
    val onClick: ((Boolean) -> Mes)? = null
): View<Mes>()
