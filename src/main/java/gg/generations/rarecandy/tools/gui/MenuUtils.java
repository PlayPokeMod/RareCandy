package gg.generations.rarecandy.tools.gui;

import com.spinyowl.legui.component.Button;
import com.spinyowl.legui.component.Layer;
import com.spinyowl.legui.component.Panel;
import com.spinyowl.legui.component.ToggleButton;
import com.spinyowl.legui.event.MouseClickEvent;
import com.spinyowl.legui.listener.MouseClickEventListener;
import com.spinyowl.legui.style.Style.DisplayType;
import com.spinyowl.legui.style.Style.PositionType;
import com.spinyowl.legui.style.color.ColorConstants;
import com.spinyowl.legui.style.flex.FlexStyle.AlignContent;
import com.spinyowl.legui.style.flex.FlexStyle.AlignItems;
import com.spinyowl.legui.style.flex.FlexStyle.AlignSelf;
import com.spinyowl.legui.style.flex.FlexStyle.FlexDirection;
import com.spinyowl.legui.style.flex.FlexStyle.JustifyContent;
import com.spinyowl.legui.style.util.StyleUtilities;
import com.spinyowl.legui.theme.Themes;
import org.joml.Vector2f;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class MenuUtils {

    public static final float MENU_HEIGHT = 20;

    /**
     * Creates a reusable menu bar.
     *
     * @return a new MenuBar instance.
     */
    public static MenuBar createMenuBar() {
        return new MenuBar();
    }

    /**
     * Creates a reusable menu container with a menu bar and content panel.
     *
     * @return a new MenuContainer instance.
     */
    public static MenuContainer createMenuContainer() {
        return new MenuContainer();
    }

    public static class MenuContainer extends Layer {
        private final MenuBar menuBar;
        private final Panel contentPanel;

        public MenuContainer() {
            this.menuBar = new MenuBar();
            this.contentPanel = new Panel();
            initialize();
        }

        private void initialize() {
            contentPanel.getStyle().getFlexStyle().setAlignSelf(AlignSelf.STRETCH);
            contentPanel.getStyle().getFlexStyle().setFlexGrow(1);
            contentPanel.getStyle().getFlexStyle().setFlexShrink(1);
            contentPanel.getStyle().setPosition(PositionType.RELATIVE);
            contentPanel.getStyle().getBackground().setColor(ColorConstants.lightRed());

            this.add(menuBar);
            this.add(contentPanel);

            this.getStyle().setDisplay(DisplayType.FLEX);
            this.getStyle().setPadding(0f);
            this.getStyle().getFlexStyle().setFlexDirection(FlexDirection.COLUMN);
            this.getStyle().getFlexStyle().setAlignItems(AlignItems.FLEX_START);
            this.getStyle().getFlexStyle().setJustifyContent(JustifyContent.FLEX_START);
            this.getStyle().getFlexStyle().setAlignContent(AlignContent.STRETCH);
        }

        public MenuBar getMenuBar() {
            return menuBar;
        }

        public Panel getContentPanel() {
            return contentPanel;
        }
    }

    public static class MenuBar extends Panel {
        private final List<MenuBarItem> menuBarItems;

        public MenuBar() {
            menuBarItems = new CopyOnWriteArrayList<>();
            initialize();
        }

        private void initialize() {
            this.getStyle().setHeight(MENU_HEIGHT);
            this.getStyle().setMinHeight(MENU_HEIGHT);
            this.getStyle().setMaxHeight(MENU_HEIGHT);
            this.getStyle().setPosition(PositionType.RELATIVE);
            this.getStyle().setDisplay(DisplayType.FLEX);
            this.getStyle().getFlexStyle().setFlexDirection(FlexDirection.ROW);
            this.getStyle().getFlexStyle().setAlignItems(AlignItems.STRETCH);
            this.getStyle().getFlexStyle().setJustifyContent(JustifyContent.FLEX_START);
            this.getStyle().getFlexStyle().setAlignContent(AlignContent.STRETCH);
        }

        public void addMenu(MenuBarItem menuBarItem) {
            if (add(menuBarItem)) {
                menuBarItems.add(menuBarItem);
            }
        }

        public List<MenuBarItem> getMenuBarItems() {
            return menuBarItems;
        }
    }

    public static class MenuBarItem extends ToggleButton {
        private final Layer menuLayer;
        private final MenuBarItemOptionPanel optionPanel;

        public MenuBarItem(String text) {
            super(text);
            this.menuLayer = new Layer();
            this.optionPanel = new MenuBarItemOptionPanel(menuLayer, this);
            initialize();
        }

        public void addOption(MenuBarItemOption option) {
            optionPanel.addOption(option);
        }

        private void initialize() {
            menuLayer.setEventPassable(true);
            menuLayer.add(optionPanel);

            this.getStyle().setHeight(MENU_HEIGHT);
            this.getStyle().setMinHeight(MENU_HEIGHT);
            this.getStyle().setMaxHeight(MENU_HEIGHT);
            this.getListenerMap().addListener(MouseClickEvent.class, event -> {
                if (MouseClickEvent.MouseClickAction.CLICK.equals(event.getAction())) {
                    toggleMenu(event);
                }
            });
        }

        private void toggleMenu(MouseClickEvent event) {
            if (this.isToggled()) {
                Vector2f size = event.getFrame().getContainer().getSize();
                menuLayer.setSize(size.x, size.y - MENU_HEIGHT);
                event.getFrame().addLayer(menuLayer);
                optionPanel.setPosition(this.getPosition().x, MENU_HEIGHT);
            } else {
                event.getFrame().removeLayer(menuLayer);
            }
        }
    }

    public static class MenuBarItemOptionPanel extends Panel {
        private final Layer parentLayer;
        private final List<MenuBarItemOption> options;

        public MenuBarItemOptionPanel(Layer parentLayer, MenuBarItem menuBarItem) {
            this.parentLayer = parentLayer;
            this.options = new CopyOnWriteArrayList<>();
            initialize();
        }

        private void initialize() {
            this.getStyle().setDisplay(DisplayType.FLEX);
            this.getStyle().getFlexStyle().setFlexDirection(FlexDirection.COLUMN);
        }

        public void addOption(MenuBarItemOption option) {
            if (this.add(option)) {
                this.getSize().x = Math.max(this.getSize().x, StyleUtilities.getFloatLengthNullSafe(option.getStyle().getWidth(), this.getParent().getSize().x));
                this.getSize().y += MENU_HEIGHT;
                options.add(option);
                option.setLayer(parentLayer);
            }
        }
    }

    public static class MenuBarItemOption extends Button {
        private Layer layer;

        public MenuBarItemOption(String text) {
            super(text);
            initialize();
        }

        private void initialize() {
            this.getStyle().setHeight(MENU_HEIGHT);
            this.getListenerMap().addListener(MouseClickEvent.class, (MouseClickEventListener) event -> {
                if (MouseClickEvent.MouseClickAction.RELEASE.equals(event.getAction())) {
                    event.getFrame().removeLayer(layer);
                }
            });
        }

        public void setLayer(Layer layer) {
            this.layer = layer;
        }
    }
}
