package gg.generations.rarecandy.tools.gui;

import com.bedrockk.molang.MoLang;
import com.bedrockk.molang.runtime.value.DoubleValue;
import com.github.weisj.darklaf.LafManager;
import gg.generations.rarecandy.pokeutils.reader.ITextureLoader;
import gg.generations.rarecandy.tools.TextureLoader;

import javax.swing.*;
import java.awt.*;
import java.text.DecimalFormat;
import java.util.Map;
import java.util.function.DoubleConsumer;
import java.util.function.DoubleSupplier;

public class PokeUtilsGui extends JPanel {


    public GuiHandler handler;
    public JTree fileViewer;
    public JPanel canvasPanel;
    private RareCandyCanvas renderingWindow;
    public FloatInputComponent scale;

    public PokeUtilsGui() {
        ITextureLoader.setInstance(new TextureLoader());
        LafManager.install(LafManager.themeForPreferredStyle(LafManager.getPreferredThemeStyle()));
        initComponents();
        canvasPanel.add(renderingWindow);
        fileViewer.setFocusable(true);
        canvasPanel.setFocusable(true);

        var renderLoop = new Runnable() {
            @Override
            public void run() {
                if (renderingWindow.isValid()) renderingWindow.render();
                SwingUtilities.invokeLater(this);
            }
        };

        SwingUtilities.invokeLater(renderLoop);
    }

    public static void main(String[] args) {
        try {
            System.loadLibrary("renderdoc");
        } catch (Exception e) {
            System.out.println("Renderdoc not loaded. Continuing without.");
        }
        var frame = new JFrame();
        var gui = new PokeUtilsGui();
        frame.setSize(new Dimension(250+512 + (512 - 482), 512));
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setContentPane(gui);
        new GuiHandler(frame, gui);
    }

    public void setHandler(GuiHandler handler) {
        this.handler = handler;
        addKeyListener(handler);
        fileViewer.addKeyListener(handler);
        canvasPanel.addKeyListener(handler);
    }

    private void createUIComponents() {
//        System.load("C:/Program Files/RenderDoc/renderdoc.dll");
        this.fileViewer = new PixelAssetTree(this);
        this.renderingWindow = new RareCandyCanvas(this);
    }
    // JFormDesigner - End of variables declaration  //GEN-END:variables  @formatter:on

    private void initComponents() {
        createUIComponents();

        var toolbar = new AdvancedMenuBar(29, 20, ComponentOrientation.LEFT_TO_RIGHT).addMenu("File").addMenuItem("Open Archive (.pk)", e -> {
            var chosenFile = DialogueUtils.chooseFile("PK;pk");
            if (chosenFile != null) handler.openAsset(chosenFile);
        }).addMenuItem("Create Archive (.glb)", e -> {
            var chosenFile = DialogueUtils.chooseFile("GLB;glb");
            if (chosenFile != null) handler.convertGlb(chosenFile);
        }).addMenuItem("Open Multiple Archives in sequence (.pk)", e -> {
            var chosenFiles = DialogueUtils.chooseMultipleFiles("PK;pk");
            if (chosenFiles != null) handler.openAsset(chosenFiles);
        }).addMenuItem("Save", e -> handler.save()).addMenuItem("Save As", e -> {
            var chosenFile = DialogueUtils.saveFile("PK;pk");
            if (chosenFile != null) {
                handler.markDirty();
                handler.save(chosenFile);
            }
        }).finish();
        var splitPane1 = new JSplitPane();
        // JFormDesigner - Variables declaration - DO NOT MODIFY  //GEN-BEGIN:variables  @formatter:off
        JScrollPane scrollPane1 = new JScrollPane();
        canvasPanel = new JPanel();

        //======== this ========
        setMinimumSize(null);
        setMaximumSize(null);
        setLayout(new BorderLayout());

        add(toolbar, BorderLayout.NORTH);

        //======== splitPane1 ========
        {
            splitPane1.setMaximumSize(null);
            splitPane1.setMinimumSize(null);
            splitPane1.setDividerSize(4);
            splitPane1.setComponentOrientation(ComponentOrientation.LEFT_TO_RIGHT);


            JSlider scaleSlider = new JSlider(JSlider.HORIZONTAL, 1, 100, 50);
            scaleSlider.setMajorTickSpacing(10);
            scaleSlider.setMinorTickSpacing(1);
            scaleSlider.setPaintTicks(true);
            scaleSlider.setPaintLabels(true);

            //======== scrollPane1 ========
            {
                JPanel panel = new JPanel();
                panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
                panel.add(scrollPane1);
                panel.add(scale = new FloatInputComponent(() -> renderingWindow.originalScaleModifer, value-> renderingWindow.scaleModifier = (float) value));


                scrollPane1.setMinimumSize(new Dimension(250, 512));
                scrollPane1.setPreferredSize(new Dimension(262, 512));

                //---- fileViewer ----
                fileViewer.setPreferredSize(new Dimension(250, 2000));
                fileViewer.setMaximumSize(null);
                fileViewer.setMinimumSize(new Dimension(200, 512));
                scrollPane1.setViewportView(fileViewer);
                splitPane1.setLeftComponent(panel);
            }

            //======== canvasPanel ========
            {
                canvasPanel.setMaximumSize(new Dimension(1920, 1080));
                canvasPanel.setPreferredSize(null);
                canvasPanel.setLayout(new BorderLayout());
            }
            splitPane1.setRightComponent(canvasPanel);
        }
        add(splitPane1, BorderLayout.CENTER);
        // JFormDesigner - End of component initialization  //GEN-END:initComponents  @formatter:on
    }

    public class FloatInputComponent extends JPanel {

        private final DoubleSupplier originalValue;
        private final JLabel label;

        private JTextField scaleTextField;
        private final DoubleConsumer consumer;
        private JButton enterButton;
        private JButton resetButton;

        private DecimalFormat decimalFormat = new DecimalFormat("#.####");

        public FloatInputComponent(DoubleSupplier originalValue, DoubleConsumer consumer) {
            this.originalValue = originalValue;
            // Initialize components
            scaleTextField = new JTextField("context.base", 10);
            this.consumer = consumer;
            scaleTextField.setCaretColor(Color.BLACK);

            enterButton = new JButton("Enter");
            resetButton = new JButton("Reset");

            // Set layout
            setLayout(new FlowLayout());

            // Add components to the panel
            add(label = new JLabel("Scale:" + originalValue.getAsDouble()));
            add(scaleTextField);
            add(enterButton);
            add(resetButton);

            // Add action listener for Enter button
            enterButton.addActionListener(e -> {
                String inputText = scaleTextField.getText();
                try {
                    // Attempt to parse the input as a float


                    var runtime = MoLang.createRuntime();

                    float newValue = (float) runtime.execute(MoLang.parse(inputText.replace("base", "context.base")), Map.of("base", new DoubleValue(originalValue.getAsDouble()))).asDouble();

//                    float newValue = Float.parseFloat(inputText);
                    if (newValue > 0) {  // Ensure the scale is positive
                        consumer.accept(newValue);
                        label.setText("Scale: " + formatScaleValue(newValue));
                        scaleTextField.setBackground(Color.WHITE);  // Set background to green for valid input
                    } else {

                        System.out.println("WHARK??!?!" + " " + newValue);
                        scaleTextField.setBackground(Color.RED);  // Set background to red for invalid input
                    }
                } catch (Exception ex) {
                    ex.printStackTrace();
                    scaleTextField.setBackground(Color.RED);  // Set background to red for invalid input
                }
            });

            // Add action listener for Reset button
            resetButton.addActionListener(e -> {
                reset();
                scaleTextField.setBackground(Color.WHITE);  // Reset background color
            });
        }

        public void reset() {
            var number = formatScaleValue((float) originalValue.getAsDouble());

            label.setText("Scale: " + number);
            label.setText(String.valueOf(number));

            consumer.accept(originalValue.getAsDouble());

            scaleTextField.setText("base");
            scaleTextField.setBackground(Color.WHITE);  // Reset background color
        }

        private String formatScaleValue(float value) {
            return decimalFormat.format(value);
        }
    }
}