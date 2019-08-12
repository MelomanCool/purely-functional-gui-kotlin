package melomancool.fungui

import glm_.vec2.Vec2
import glm_.vec4.Vec4

import gln.checkError
import gln.glClearColor
import gln.glViewport

import imgui.ConfigFlag
import imgui.FontConfig
import imgui.ImGui
import imgui.NUL
import imgui.WindowFlag as Wf
import imgui.imgui.Context
import imgui.impl.ImplGL3
import imgui.impl.ImplGlfw
import imgui.impl.glslVersion

import org.lwjgl.glfw.GLFW.glfwPollEvents
import org.lwjgl.glfw.GLFW.glfwSwapBuffers
import org.lwjgl.opengl.GL
import org.lwjgl.opengl.GL11.GL_COLOR_BUFFER_BIT
import org.lwjgl.opengl.GL11.glClear
import org.lwjgl.system.Configuration
import org.lwjgl.system.Platform

import uno.glfw.GlfwWindow
import uno.glfw.VSync
import uno.glfw.glfw
import uno.glfw.windowHint.Profile.core

 // Would not be visible, we use it only as id
val imguiWindowTitle = "MainWindow"

val glfwWindowTitle = "Purely Functional GUI"

val imguiWindowFlags = Wf.NoTitleBar.i or Wf.NoCollapse.i or Wf.NoMove.i or Wf.NoResize.i or Wf.NoSavedSettings.i or Wf.NoBackground.i or Wf.AlwaysAutoResize.i

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

sealed class View<out T>
data class Button<T>(val text: String, val onClick: T? = null): View<T>()
data class Label(val text: String): View<Nothing>()
data class VerticalLayout<T>(val children: List<View<T>>): View<T>() {
    constructor(vararg children: View<T>) : this(children.toList())
}
data class HorizontalLayout<T>(val children: List<View<T>>): View<T>() {
    constructor(vararg children: View<T>) : this(children.toList())
}
data class TextField<T>(val label: String, val text: String, val onInput: ((String) -> T)? = null): View<T>()

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
        is HorizontalLayout -> render(v)
        is VerticalLayout -> render(v)
        is Button -> render(v)
        is Label -> render(v)
        is TextField -> render(v)
    }
}

fun <T> render(vl: HorizontalLayout<T>): T? {
    val last = vl.children.lastOrNull()
    return if (last != null) {
        vl.children
            .dropLast(1)
            .map {
                var r = renderGeneric(it)
                ImGui.sameLine()
                r
            }
            .plusElement(renderGeneric(last))
            .firstOrNull{ it != null }
    } else {
        null
    }
}

fun <T> render(vl: VerticalLayout<T>): T? {
    return vl.children.map { renderGeneric(it) }.firstOrNull{ it != null }
}

fun <T> render(b: Button<T>): T? {
    if (ImGui.button(b.text) and (b.onClick != null)) {
        return b.onClick
    } else {
        return null
    }
}

fun <T> render(l: Label): T? {
    ImGui.text(l.text)
    return null
}

fun <T> render(tf: TextField<T>): T? {
    val inputBuf = tf.text.toCharArray(CharArray(64))
    if (ImGui.inputText(tf.label, inputBuf) and (tf.onInput != null)) {
        return tf.onInput!!(inputBuf.takeWhile { it != NUL }.joinToString(""))
    } else {
        return null
    }
}
