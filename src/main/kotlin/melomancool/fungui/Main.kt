package melomancool.fungui

import glm_.vec4.Vec4
import glm_.vec2.Vec2
import gln.checkError
import gln.glClearColor
import gln.glViewport

import imgui.imgui.Context
import imgui.impl.ImplGL3
import imgui.impl.ImplGlfw
import imgui.impl.glslVersion
import imgui.ImGui
import imgui.FontConfig
import imgui.ConfigFlag
import imgui.WindowFlag as Wf

import org.lwjgl.opengl.GL
import org.lwjgl.opengl.GL11.GL_COLOR_BUFFER_BIT
import org.lwjgl.opengl.GL11.glClear
import org.lwjgl.system.Configuration
import org.lwjgl.system.Platform
import org.lwjgl.glfw.GLFW.glfwPollEvents
import org.lwjgl.glfw.GLFW.glfwSwapBuffers

import uno.glfw.GlfwWindow
import uno.glfw.VSync
import uno.glfw.glfw
import uno.glfw.windowHint.Profile.core

 // Would not be visible, we use it only as id
val imguiWindowTitle = "MainWindow"

val glfwWindowTitle = "Purely Functional GUI"

val imguiWindowFlags = Wf.NoTitleBar.i or Wf.NoCollapse.i or Wf.NoMove.i or Wf.NoResize.i or Wf.NoSavedSettings.i or Wf.NoBackground.i

val DEBUG = true

fun <Mdl, Ms> run(initialModel: Mdl, view: (Mdl) -> View<Ms>, update: (Ms, Mdl) -> Mdl) {
    glfw {
        errorCallback = { error, description -> println("Glfw Error $error: $description") }
        init()
        windowHint {
            debug = DEBUG

            // Decide GL+GLSL versions
            when (Platform.get()) {
                Platform.MACOSX -> {    // GL 3.2 + GLSL 150
                    glslVersion = 150
                    context.version = "3.2"
                    profile = core      // 3.2+ only
                    forwardComp = true  // Required on Mac
                }
                else -> {   // GL 3.0 + GLSL 130
                    glslVersion = 130
                    context.version = "3.0"
                    //profile = core      // 3.2+ only
                    //forwardComp = true  // 3.0+ only
                }
            }
        }
    }

    // Create window with graphics context
    val window = GlfwWindow(500, 500, glfwWindowTitle)
    window.makeContextCurrent()
    glfw.swapInterval = VSync.ON

    // Initialize OpenGL loader
    GL.createCapabilities()

    // Setup Dear ImGui context
    val ctx = Context()

    // Enable Keyboard Controls
    ImGui.io.configFlags = ImGui.io.configFlags or ConfigFlag.NavEnableKeyboard.i

    ImGui.styleColorsDark()

    // Setup Platform/Renderer bindings
    val implGlfw = ImplGlfw(window, true)
    val implGl3 = ImplGL3()

    ImGui.style.scaleAllSizes(2.0f)

    // Load Fonts
    FontConfig().let {
        it.oversample.put(1, 1)
        it.pixelSnapH = true
        it.sizePixels = 26f
        ImGui.io.fonts.addFontDefault(it)
    }

    var model = initialModel
    
    // "Pre-render" to get initial window size from ImGui.
    // Why repeat(2) is necessary:
    // https://discourse.dearimgui.org/t/os-window-size-adaptive-to-imgui-window-size/58/5
    run {
        var windowSize = window.size
        repeat(2) {
            glfwPollEvents()
            implGl3.newFrame()
            implGlfw.newFrame()
            ImGui.newFrame()
            ImGui.begin(imguiWindowTitle, flags_ = imguiWindowFlags)
            model = runOnce(model, view, update)
            windowSize = glm_.vec2.Vec2i(ImGui.windowSize)
            ImGui.end()
        }
        window.size = windowSize
    }
    
    while (window.isOpen) {
        glfwPollEvents()

        implGl3.newFrame()
        implGlfw.newFrame()
        ImGui.newFrame()

        ImGui.setNextWindowPos(Vec2(0, 0))
        ImGui.setNextWindowSize(Vec2(window.size))

        ImGui.begin(imguiWindowTitle, flags_ = imguiWindowFlags)
        model = runOnce(model, view, update)
        window.size = glm_.vec2.Vec2i(ImGui.windowSize)
        ImGui.end()

        ImGui.render()
        glViewport(window.framebufferSize)
        glClearColor(Vec4(0f, 0f, 0f, 1f))
        glClear(GL_COLOR_BUFFER_BIT)
        
        implGl3.renderDrawData(ImGui.drawData!!)

        if (DEBUG) {
            checkError("mainLoop")
        }
            
        glfwSwapBuffers(window.handle.L)
    }

    implGl3.shutdown()
    implGlfw.shutdown()
    ctx.destroy()
    window.destroy()
    glfw.terminate()
}

data class GuiDelta<T>(val isChanged: Boolean, val value: T)

fun checkbox(label: String, isChecked: Boolean): GuiDelta<Boolean> {
    val box = booleanArrayOf(isChecked)
    val isChanged = ImGui.checkbox(label, box)
    return GuiDelta(isChanged, box[0])
}

sealed class View<out T>
data class Button<T>(val text: String, val onClick: T? = null): View<T>()
data class Label<T>(val text: String): View<T>()
data class VerticalLayout<T>(val children: List<View<T>>): View<T>() {
    constructor(vararg children: View<T>) : this(children.toList())
}

fun <Mdl, Ms> runOnce(model: Mdl, view: (Mdl) -> View<Ms>, update: (Ms, Mdl) -> Mdl): Mdl {
    val v = view(model)
    val msg = renderGeneric(v)
    if (msg != null) {
        return update(msg, model)
    } else {
        return model
    }
}

// A hack to emulate generic overloading
fun <T> renderGeneric(v: View<T>): T? {
    return when (v) {
        is VerticalLayout -> render(v)
        is Button -> render(v)
        is Label -> render(v)
    }
}

fun <T> render(vl: VerticalLayout<T>): T? {
    return vl.children.map { renderGeneric(it) }.asSequence().firstOrNull{ it != null }
}

fun <T> render(b: Button<T>): T? {
    if (ImGui.button(b.text) and (b.onClick != null)) {
        return b.onClick
    } else {
        return null
    }
}

fun <T> render(l: Label<T>): T? {
    ImGui.text(l.text)
    return null
}


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
