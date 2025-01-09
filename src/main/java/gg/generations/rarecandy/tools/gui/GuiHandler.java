package gg.generations.rarecandy.tools.gui;

import gg.generations.rarecandy.pokeutils.PixelAsset;
import gg.generations.rarecandy.tools.pkcreator.PixelmonArchiveBuilder;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.tukaani.xz.LZMA2Options;

import javax.swing.*;
import java.awt.event.*;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

import static gg.generations.rarecandy.tools.gui.RareCandyCanvas.lightLevel;

public class GuiHandler implements KeyListener {
    public static final Path TEMP = Path.of("temp");
    public static final LZMA2Options OPTIONS = new LZMA2Options();
    private static final String BASE_TITLE = "Pk Explorer";
    private final PokeUtilsGui gui;
    private final JFrame frame;
    private final Set<Integer> pressedKeys = new HashSet<>();
    private final ArcballOrbit arcBall;

    public PixelAsset asset;
    public Path assetPath;
    public int index = 0;
    public int amount;
    private String currentTitle = BASE_TITLE;
    private boolean dirty = false;
    public List<Path> filesToOpen = new ArrayList<>();

    public GuiHandler(JFrame frame, PokeUtilsGui gui) {
        this.frame = frame;
        this.gui = gui;

        gui.setHandler(this);
        frame.setTitle(currentTitle);
        frame.setVisible(true);
        frame.pack();
        frame.transferFocus();
        arcBall = new ArcballOrbit(getCanvas(), 3f, 0.125f, 0f);
//        getCanvas().attachArcBall(arcBall);
//        getCanvas().addKeyListener(this);
//        setup(getCanvas());
    }

    public RareCandyCanvas getCanvas() {
        return gui.getRenderingWindow();
    }

    public void initializeAsset(PixelAsset asset, Path path) {
        this.asset = asset;
        this.assetPath = path;
    }

    public void save() {
        save(assetPath);
    }

    public void save(Path savePath) {
        try {
            PixelmonArchiveBuilder.convertToPk(TEMP, Files.walk(TEMP).toList(), savePath, getCanvas().scaleModifier);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

//        if (dirty) {
//
//
//
//            dirty = false;
//        }
//
//        LoggerUtil.print("Saving disabled for now.");
    }

    public void markDirty() {
        if (!dirty) {
            this.dirty = true;
            currentTitle = frame.getTitle() + "*";
            frame.setTitle(frame.getTitle() + "*");
        }
    }

    public void openAsset(Path filePath) {
        try {

            initializeAsset(new PixelAsset(move(filePath), filePath.getFileName().toString()), filePath);
            var title = BASE_TITLE + " - " + filePath.getFileName().toString();
            frame.setTitle(title);
            this.currentTitle = title;
            getCanvas().openFile(asset, FilenameUtils.getBaseName(filePath.getFileName().toString()), () -> ((PixelAssetTree) gui.fileViewer).initializeAsset(asset, assetPath, getCanvas().loadedModel.objects.get(0).animations.keySet()), true);

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void reloadCurrent() throws IOException {
        var filePath = TEMP;

        initializeAsset(new PixelAsset(filePath, assetPath.getFileName().toString()), assetPath);
        getCanvas().openFile(asset, FilenameUtils.getBaseName(assetPath.getFileName().toString()));
    }

    public static Map<String, byte[]> move(Path path) throws IOException {
        FileUtils.deleteDirectory(TEMP.toFile());
        Files.createDirectories(TEMP);

        var seven = PixelAsset.getSevenZipFile(path);

        var files = new HashMap<String, byte[]>();

        for (var entry : seven.getEntries()) {
            files.put(entry.getName(), seven.getInputStream(entry).readAllBytes());
        }

//        System.out.println(files.keySet());

        for (var file : files.entrySet()) {
            if(file.getKey().isEmpty()) continue;

            var filePath = TEMP.resolve(file.getKey());
//            System.out.println(filePath);

            if(Files.isDirectory(filePath)) continue;
            Files.createFile(filePath);
            Files.write(filePath, file.getValue());
        }

        return files;
    }

    @Override
    public void keyTyped(KeyEvent e) {
    }

    @Override
    public void keyPressed(KeyEvent e) {
        pressedKeys.add(e.getKeyCode());

        var isCtrlPressed = e.isControlDown();
        var isShiftPressed = e.isShiftDown();
        var isAltPressed = e.isAltDown();
        var code = e.getKeyCode();

        if (isCtrlPressed) {
            switch (code) {
                case KeyEvent.VK_S -> save();
                case KeyEvent.VK_SPACE -> arcBall.reset();
            }
        } else if(isAltPressed) {
//            switch (code) {
//                case KeyEvent.VK_A -> new RareCandyCanvas.CycleVariants(getCanvas(), true);
//                case KeyEvent.VK_Z -> new RareCandyCanvas.CycleVariants(getCanvas(), false);
//            }
        } else if(isShiftPressed) {
            switch (code) {
                case KeyEvent.VK_P -> {
                    new RareCandyCanvas.CycleVariants(getCanvas(), true);
                }
                case KeyEvent.VK_O -> {
                    var chosenFiles = DialogueUtils.chooseMultipleFiles("PK;pk");
                    if (chosenFiles != null) openAsset(chosenFiles);
                }
                case KeyEvent.VK_SPACE -> {
                    try {
                        reloadCurrent();
                    } catch (IOException ex) {
                        throw new RuntimeException(ex);
                    }
                }
            }
        } else {
            switch (code) {
                case KeyEvent.VK_P -> {
                    new RareCandyCanvas.CycleVariants(getCanvas(), false);
                }
                case KeyEvent.VK_O -> {
                    Path chosenFile;
                    if (filesToOpen.isEmpty()) {

                        chosenFile = DialogueUtils.chooseFile("PK;pk");
                    }
                    else {
                        index++;
                        System.out.println("Selecting: " + (index + 1) + "/" + amount);
                        chosenFile = filesToOpen.remove(0);
                    }

                    if (chosenFile != null) openAsset(chosenFile);
                }
                case KeyEvent.VK_OPEN_BRACKET -> RareCandyCanvas.setLightLevel((float) Math.max(lightLevel - 0.01, 0));
                case KeyEvent.VK_CLOSE_BRACKET -> RareCandyCanvas.setLightLevel((float) Math.min(lightLevel + 0.01, 1));

                case KeyEvent.VK_SPACE -> RareCandyCanvas.animate = !RareCandyCanvas.animate;
                default -> arcBall.keyPressed(code);
            }
        }

//        System.out.println(pressedKeys);
    }

    @Override
    public void keyReleased(KeyEvent e) {
//        System.out.println("Before: " + pressedKeys);
//        pressedKeys.remove((Integer) e.getKeyCode());
//        System.out.println("After: " + pressedKeys);
    }

    public void convertGlb(Path chosenFile) {
        try {
            var is = Files.newInputStream(chosenFile);
            var filePath = Path.of(chosenFile.toString().replace(".glb", ".pk"));
            initializeAsset(new PixelAsset(chosenFile.getFileName().toString(), is.readAllBytes()), filePath);
            var title = BASE_TITLE + " - " + filePath.getFileName().toString();
            frame.setTitle(title);
            this.currentTitle = title;
            getCanvas().openFile(asset, "");

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void openAsset(List<Path> chosenFiles) {
        System.out.println("Loading " + chosenFiles.size() + " into queue.");
        index = 0;
        amount = chosenFiles.size();

        filesToOpen.addAll(chosenFiles);

        System.out.println("Selecting: " + (index + 1) + "/" + amount);
        openAsset(filesToOpen.remove(0));
    }

    public class ArcballOrbit implements MouseMotionListener, MouseWheelListener, MouseListener {
        private final Matrix4f viewMatrix;
        private final RareCandyCanvas canvas;
        private float radius;
        private float angleX;
        private float angleY;
        private float lastX, lastY, offsetX, offsetY;

        private final Vector3f centerOffset = new Vector3f();

        public ArcballOrbit(RareCandyCanvas canvas, float radius, float angleX, float angleY) {
            this.viewMatrix = canvas.viewMatrix;
            this.canvas = canvas;
            this.radius = radius;
            this.angleX = angleX;
            this.angleY = angleY;
            update();
        }

        public void update() {
            viewMatrix.identity().arcball(radius, centerOffset.x, centerOffset.y, centerOffset.z, (angleY + offsetY) * (float) Math.PI * 2f, (angleX + offsetX) * (float) Math.PI * 2f);
        }

        @Override
        public void mouseDragged(MouseEvent e) {
            int x = e.getX();
            int y = e.getY();
            offsetX = (x - lastX) * 0.001f;
            offsetY = (y - lastY) * 0.001f;
            update();
        }

        @Override
        public void mouseMoved(MouseEvent e) {

        }

        @Override
        public void mouseWheelMoved(MouseWheelEvent e) {
            int scrollAmount = e.getWheelRotation();
            radius += scrollAmount * 0.1f;
            update();
        }

        @Override
        public void mouseClicked(MouseEvent e) {
        }

        @Override
        public void mousePressed(MouseEvent e) {
            offsetX = 0;
            offsetY = 0;

            lastX = e.getX();
            lastY = e.getY();
        }

        @Override
        public void mouseReleased(MouseEvent e) {
            angleX += offsetX;
            angleY += offsetY;
            offsetX = 0;
            offsetY = 0;
            lastX = 0;
            lastY = 0;

            update();
        }

        @Override
        public void mouseEntered(MouseEvent e) {
        }

        @Override
        public void mouseExited(MouseEvent e) {
        }


        public void keyPressed(int code) {
            float lateralStep = 0.01f; // Adjust the step size as needed

            if(!RareCandyCanvas.cycling) {

                switch (code) {
                    case KeyEvent.VK_LEFT, KeyEvent.VK_A -> centerOffset.x -= lateralStep;
                    case KeyEvent.VK_RIGHT, KeyEvent.VK_D -> centerOffset.x += lateralStep;
                    case KeyEvent.VK_UP, KeyEvent.VK_W -> centerOffset.z += lateralStep;
                    case KeyEvent.VK_DOWN, KeyEvent.VK_S -> centerOffset.z -= lateralStep;
                    case KeyEvent.VK_PAGE_UP, KeyEvent.VK_Q -> centerOffset.y += lateralStep;
                    case KeyEvent.VK_PAGE_DOWN, KeyEvent.VK_E -> centerOffset.y -= lateralStep;
                }

                update();
            }
        }

        public void reset() {
            if(canvas.loadedModel == null) {
                radius = 2f;
                centerOffset.set(0, 0, 0);
            } else {
                radius = ((canvas.loadedModel.dimensions.get(canvas.loadedModel.dimensions.maxComponent())) * canvas.loadedModel.scale)/2f;
                centerOffset.set(0, radius, 0);
            }

            lastX = lastY = 0;
            angleX = -0.125f;
            angleY = 0.125f;
        }

    }
}