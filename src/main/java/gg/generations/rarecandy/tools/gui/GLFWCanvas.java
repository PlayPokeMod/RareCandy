package gg.generations.rarecandy.tools.gui;

import com.spinyowl.legui.system.context.CallbackKeeper;
import org.lwjgl.glfw.*;
import org.lwjgl.opengl.GL;
import org.lwjgl.opengl.GL11;
import org.lwjgl.system.MemoryStack;

import javax.swing.*;
import java.nio.IntBuffer;
import java.util.concurrent.Callable;

public abstract class GLFWCanvas {
    protected int width;
    protected int height;
    protected final String title;
    public long window; // GLFW window handle
    private boolean initCalled;
    protected boolean running = false;

    public GLFWCanvas(int width, int height, String title) {
        this.width = width;
        this.height = height;
        this.title = title;
    }

    protected void initWindow() {
        GLFWErrorCallback.createPrint(System.err).set();
        if (!GLFW.glfwInit()) {
            throw new IllegalStateException("Unable to initialize GLFW");
        }

        GLFW.glfwDefaultWindowHints();
        GLFW.glfwWindowHint(GLFW.GLFW_CONTEXT_VERSION_MAJOR, 3);
        GLFW.glfwWindowHint(GLFW.GLFW_CONTEXT_VERSION_MINOR, 3);
        GLFW.glfwWindowHint(GLFW.GLFW_OPENGL_PROFILE, GLFW.GLFW_OPENGL_CORE_PROFILE);

        window = GLFW.glfwCreateWindow(width, height, title, 0, 0);
        if (window == 0) {
            throw new RuntimeException("Failed to create GLFW window");
        }

        GLFW.glfwMakeContextCurrent(window);
        GLFW.glfwSwapInterval(1); // Enable vsync
        GL.createCapabilities();

        resize(width, height);
    }

    protected void initCallbacks() {
        var keeper = getCallBackKeeper();
        keeper.getChainWindowSizeCallback().add((window, width, height) -> resize(width, height));
        keeper.getChainMouseButtonCallback().add((window, button, action, mods) -> mouseClick(button, action, mods));
        keeper.getChainKeyCallback().add((window, key, scancode, action, mods) -> onKey(key, scancode, action, mods));
        keeper.getChainScrollCallback().add((window, xoffset, yoffset) -> onScroll(xoffset, yoffset));
    }

    protected void onScroll(double xoffset, double yoffset) {
    }

    protected void onKey(int key, int scancode, int action, int mods) {

    }

    protected void mouseClick(int button, int action, int mods) {

    }

    protected abstract CallbackKeeper getCallBackKeeper();


    public void run() {
        if (!initCalled) {
            initGL();
            initCalled = true;
            running = true;
        }

        while(running/*!GLFW.glfwWindowShouldClose(window)*/) {
            render();
        }

        cleanup();
    }

    public void render() {

        beforeRender();

        try {
            paintGL();
        } finally {
            afterRender();
        }
    }

    protected void beforeRender() {
    }

    protected void afterRender() {
        GLFW.glfwSwapBuffers(window);
        GLFW.glfwPollEvents();
    }

    protected void resize(int width, int height) {
        GL11.glViewport(0, 0, width, height);
        this.width = width;
        this.height = height;
    }

    protected void cleanup() {
        GLFW.glfwDestroyWindow(window);
        GLFW.glfwTerminate();
    }

    public <T> T executeInContext(Callable<T> callable) throws Exception {
        beforeRender();
        try {
            return callable.call();
        } finally {
            afterRender();
        }
    }

    public void runInContext(Runnable runnable) {
        beforeRender();
        try {
            runnable.run();
        } finally {
            afterRender();
        }
    }

    public abstract void initGL();

    public abstract void paintGL();

    public void swapBuffers() {
        GLFW.glfwSwapBuffers(window);
    }
}
