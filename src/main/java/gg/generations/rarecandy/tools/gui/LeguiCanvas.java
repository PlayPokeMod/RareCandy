package gg.generations.rarecandy.tools.gui;

import com.spinyowl.legui.system.context.CallbackKeeper;
import gg.generations.rarecandy.renderer.rendering.FrameBuffer;
import org.lwjgl.nanovg.NanoVGGL3;

public class LeguiCanvas extends GLFWCanvas {
    FrameBuffer buffer;



    public LeguiCanvas(int width, int height, String title) {
        super(width, height, title);


    }

    @Override
    protected CallbackKeeper getCallBackKeeper() {
        return null;
    }

    @Override
    public void initGL() {
        buffer = new FrameBuffer(width, height);

        int flags = NanoVGGL3.NVG_STENCIL_STROKES | NanoVGGL3.NVG_ANTIALIAS;
        long nvgContext = NanoVGGL3.nvgCreate(flags);


    }

    @Override
    public void paintGL() {

    }

    public static void main(String args[]) {
        new LeguiCanvas(1024, 1024, "test").run();
    }
}
