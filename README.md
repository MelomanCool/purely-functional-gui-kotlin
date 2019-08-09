# Purely Functional GUI Prototype in Kotlin

Inspired by The Elm Archirecture / Model-View-Update (MVU) / Redux / Unidirectional Data Flow.

## How does it look?

<img src="counter.png" align="right" width="100"/>

```kotlin
typealias Model = Int

sealed class Msg
object Increment : Msg()
object Decrement : Msg()

fun view(model: Model): View<Msg> =
    VerticalLayout(
        Button("+", onClick = Increment),
        Label(model.toString()),
        Button("-", onClick = Decrement)
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
```

Model is immutable. This is the state of the application.

View and Update are pure functions.

View returns a data structure which represents the GUI.

Update replaces the state when something happens (for example, a button was pressed).
