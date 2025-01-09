package gg.generations.rarecandy.tools.gui;

import com.spinyowl.legui.system.context.CallbackKeeper;
import com.spinyowl.legui.system.context.DefaultCallbackKeeper;
import gg.generations.rarecandy.pokeutils.MaterialReference;
import gg.generations.rarecandy.pokeutils.PixelAsset;
import gg.generations.rarecandy.renderer.loading.ModelLoader;
import gg.generations.rarecandy.renderer.rendering.FrameBuffer;
import org.liquidengine.cbchain.IChainCharCallback;
import org.lwjgl.glfw.GLFWKeyCallbackI;
import org.lwjgl.glfw.GLFWWindowCloseCallbackI;
import org.lwjgl.nanovg.*;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL30;
import org.lwjgl.util.nfd.NativeFileDialog;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.function.Supplier;

import static gg.generations.rarecandy.tools.gui.MinimalQuad.mapDuplicatesWithStreams;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_ESCAPE;
import static org.lwjgl.glfw.GLFW.GLFW_RELEASE;
import static org.lwjgl.nanovg.NanoVG.*;
import static org.lwjgl.nanovg.NanoVGGL2.*;
import static org.lwjgl.nanovg.NanoVGGL3.*;
import static org.lwjgl.nanovg.NanoVGGL3.NVG_ANTIALIAS;
import static org.lwjgl.nanovg.NanoVGGL3.NVG_STENCIL_STROKES;
import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL30.*;

/**
 * Example usage of FrameBuffer with GLFWCanvas and NanoVG.
 */
public class FBOCanvasUsingFrameBuffer extends GLFWCanvas {

    private FrameBuffer fbo;
    private final int fboWidth = 200;
    private final int fboHeight = 200;

    private long nvgContext;
    private int fboImageID;

    private List<String> materialNames = new ArrayList<>();


    // Track rotation
    private long lastTime = System.currentTimeMillis();

    public FBOCanvasUsingFrameBuffer(int width, int height, String title) {
        super(width, height, title);
        initCallbacks();
    }

    public static void main(String[] args) {


        FBOCanvasUsingFrameBuffer canvas = new FBOCanvasUsingFrameBuffer(1024, 1024, "Using FrameBuffer");
        canvas.run();
    }

    @Override
    public void initGL() {
        // Determine NanoVG version

        int flags = NVG_STENCIL_STROKES | NVG_ANTIALIAS;
        nvgContext = NanoVGGL3.nvgCreate(flags);
        if (nvgContext == 0) {
            throw new RuntimeException("Failed to create NanoVG context");
        }

        // Create our off-screen FrameBuffer
        fbo = new FrameBuffer(fboWidth, fboHeight);

        // Tell NanoVG about the FrameBuffer’s texture so we can draw it on screen later
        fboImageID = NanoVGGL3.nnvglCreateImageFromHandle(nvgContext, fbo.getTextureId(),
                fboWidth, fboHeight, 0);

        glEnable(GL_BLEND);
        glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
    }

    @Override
    public void paintGL() {
        // Compute rotation angle
        long thisTime = System.currentTimeMillis();
        float angle = (lastTime - thisTime)/10000000f;
        lastTime = thisTime;

        System.out.println(angle);

        // 1) Render the rotating rectangle onto our FrameBuffer
        renderToFrameBuffer(angle);

        // Clear main window
        glClearColor(0.3f, 0.3f, 0.5f, 1.0f);
        glClear(GL_COLOR_BUFFER_BIT | GL_STENCIL_BUFFER_BIT);

        renderBuffer();


    }

    private void renderBuffer() {
        // Paint the FBO image as a fullscreen quad using NanoVG
        nvgBeginFrame(nvgContext, width, height, 1f);
        try (NVGPaint paint = NVGPaint.calloc()) {
            nvgImagePattern(nvgContext, width/4, height/4, width/2, height/4, 0f, fboImageID, 1f, paint);

            nvgBeginPath(nvgContext);
            nvgRect(nvgContext, width/4, height/4, width/2, height/4);
            nvgFillPaint(nvgContext, paint);
            nvgFill(nvgContext);
        }
        nvgEndFrame(nvgContext);
    }

    private void renderToFrameBuffer(float angle) {
        // Bind off-screen FBO for drawing
        fbo.bindFramebuffer();
        glViewport(0, 0, fboWidth, fboHeight);

        // Clear off-screen area
        glClearColor(0.3f, 0.5f, 0.7f, 1f);
        glClear(GL_COLOR_BUFFER_BIT | GL_STENCIL_BUFFER_BIT);

        // Use NanoVG to draw rotating rectangle
        nvgBeginFrame(nvgContext, fboWidth, fboHeight, 1f);
        try (NVGColor green = NVGColor.calloc(); NVGColor black = NVGColor.calloc()) {
            green.r(0f).g(1f).b(0f).a(1f);
            black.r(0f).g(0f).b(0f).a(1f);

            nvgTranslate(nvgContext, fboWidth / 2f, fboHeight / 2f);
            nvgRotate(nvgContext, angle);

            nvgBeginPath(nvgContext);
            nvgRect(nvgContext, -fboWidth / 4f, -fboHeight / 4f, fboWidth / 2f, fboHeight / 2f);
            nvgStrokeColor(nvgContext, black);
            nvgStroke(nvgContext);

            nvgBeginPath(nvgContext);
            nvgRect(nvgContext, -fboWidth / 4f, -fboHeight / 4f, fboWidth / 2f, fboHeight / 2f);
            nvgFillColor(nvgContext, green);
            nvgFill(nvgContext);
        }
        nvgEndFrame(nvgContext);

        // Unbind FBO to return to default
        fbo.unbindFramebuffer();

        glViewport(0, 0, width, height);
    }

    @Override
    protected void cleanup() {
        super.cleanup();  // destroys window and terminates GLFW
        if (nvgContext != 0) {
            boolean isVersionNew = (glGetInteger(GL30.GL_MAJOR_VERSION) > 3)
                    || (glGetInteger(GL30.GL_MAJOR_VERSION) == 3 && glGetInteger(GL30.GL_MINOR_VERSION) >= 2);
            if (isVersionNew) {
                NanoVGGL3.nnvgDelete(nvgContext);
            } else {
                NanoVGGL2.nnvgDelete(nvgContext);
            }
        }
        if (fbo != null) {
            fbo.close(); // clean up GPU resources
        }
    }

    protected void initCallbacks() {
        var keeper = new DefaultCallbackKeeper();
        CallbackKeeper.registerCallbacks(window, keeper);

        GLFWKeyCallbackI glfwKeyCallbackI = (w, key, scancode, action, mods) -> {
            if (key == GLFW_KEY_ESCAPE && action != GLFW_RELEASE) {
                running = false;
            }
        };

        keeper.getChainKeyCallback().add(glfwKeyCallbackI);

        GLFWWindowCloseCallbackI glfwWindowCloseCallbackI = w -> running = false;
        keeper.getChainWindowCloseCallback().add(glfwWindowCloseCallbackI);
    }

    @Override
    protected CallbackKeeper getCallBackKeeper() {
        return null;
    }
}
