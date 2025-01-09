package gg.generations.rarecandy.tools.gui;

import com.spinyowl.legui.DefaultInitializer;
import com.spinyowl.legui.animation.AnimatorProvider;
import com.spinyowl.legui.component.*;
import com.spinyowl.legui.listener.processor.EventProcessorProvider;
import com.spinyowl.legui.system.context.CallbackKeeper;
import com.spinyowl.legui.system.context.Context;
import com.spinyowl.legui.system.layout.LayoutManager;
import com.spinyowl.legui.system.renderer.Renderer;
import gg.generations.rarecandy.tools.gui.MenuUtils.MenuBar;
import gg.generations.rarecandy.tools.gui.MenuUtils.MenuBarItem;
import gg.generations.rarecandy.tools.gui.MenuUtils.MenuBarItemOption;

import static org.lwjgl.opengl.GL11.*;

/**
 * Example usage of FrameBuffer with GLFWCanvas and NanoVG.
 */
public class GuiCanvas extends GLFWCanvas {
    private Frame frame;
    private DefaultInitializer initializer;
    private Context context;
    private Renderer renderer;

    public GuiCanvas(int width, int height, String title) {
        super(width, height, title);

        initWindow();
    }

    public static void main(String[] args) {
        GuiCanvas gui = new GuiCanvas(512, 512, "Blep");
        gui.run();
    }

    @Override
    protected CallbackKeeper getCallBackKeeper() {
        return initializer.getCallbackKeeper();
    }

    @Override
    public void initGL() {
        frame = new Frame(width, height);

//        MenuUtils.MenuContainer menuContainer = MenuUtils.createMenuContainer();
//
//         Add items to the menu bar
//        MenuBarItem fileMenu = new MenuBarItem("File");
//        fileMenu.addOption(new MenuBarItemOption("Open"));
//        fileMenu.addOption(new MenuBarItemOption("Save"));
//        fileMenu.addOption(new MenuBarItemOption("Exit"));
//
//        menuContainer.getMenuBar().addMenu(fileMenu);
//
//        MenuBarItem helpMenu = new MenuBarItem("Help");
//        helpMenu.addOption(new MenuBarItemOption("About"));
//        helpMenu.addOption(new MenuBarItemOption("Support"));
//        menuContainer.getMenuBar().addMenu(helpMenu);
//
//         Add the MenuContainer to the frame
//        frame.getContainer().add(menuContainer);

        frame.getContainer().add(new Label("AH"));


        initializer = new DefaultInitializer(window, frame);
        initializer.getRenderer().initialize();

        initCallbacks();

        context = initializer.getContext();
        renderer = initializer.getRenderer();

        glEnable(GL_BLEND);
        glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
        glDisable(GL_DEPTH_TEST);
    }

    @Override
    public void paintGL() {
        glClearColor(1.0f, 1.0f, 1.0f, 1.0f);
        glClear(GL_COLOR_BUFFER_BIT | GL_STENCIL_BUFFER_BIT);

        context.updateGlfwWindow();
        renderer.render(frame, context);
    }

    @Override
    protected void afterRender() {
        super.afterRender();
        initializer.getSystemEventProcessor().processEvents(frame, context);
        EventProcessorProvider.getInstance().processEvents();

        // When everything done we need to relayout components.
        LayoutManager.getInstance().layout(frame);

        // Run animations. Should be also called cause some components use animations for updating state.
        AnimatorProvider.getAnimator().runAnimations();
    }

    @Override
    protected void cleanup() {
        super.cleanup();
        initializer.getRenderer().destroy();
    }
}
