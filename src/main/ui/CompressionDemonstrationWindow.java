package main.ui;

import main.core.QuadTree;
import main.util.ImageUtils;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.util.logging.Handler;

public class CompressionDemonstrationWindow implements ActionListener, ChangeListener {

    //Core
    private QuadTree tree;
    private Color[][] image;

    //Options
    private int errorTolerance = 0;

    //UI
    private JFrame rootFrame;
    private QuadTreeDisplay quadTreeDisplay;
    private JSlider errorSlider;
    private JLabel nodesCountDisplay;

    public void run() {
        EventQueue.invokeLater(new Runnable() {
            @Override
            public void run() {
                try {
                    UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
                } catch (ClassNotFoundException | InstantiationException | IllegalAccessException | UnsupportedLookAndFeelException ex) {
                    ex.printStackTrace();
                }

                JFrame frame = new JFrame("Compression demonstration");
                CompressionDemonstrationWindow.this.rootFrame = frame;
                frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

                JPanel bottomPanel = new JPanel();
                bottomPanel.setLayout(new BoxLayout(bottomPanel, BoxLayout.Y_AXIS));

                JButton uploadFileBtn = new JButton("Choose image");
                uploadFileBtn.addActionListener(CompressionDemonstrationWindow.this);
                bottomPanel.add(uploadFileBtn);

                JSlider errorToleranceSlider = new JSlider(1, 99999, 1);
                errorToleranceSlider.setSnapToTicks(true);
                errorToleranceSlider.setPaintTicks(true);
                errorToleranceSlider.setMinorTickSpacing(50);
                errorToleranceSlider.setMajorTickSpacing(100);
                errorToleranceSlider.setValue(0);
                errorToleranceSlider.addChangeListener(CompressionDemonstrationWindow.this);

                bottomPanel.add(errorToleranceSlider);

                CompressionDemonstrationWindow.this.errorSlider = errorToleranceSlider;

                frame.setLayout(new BorderLayout());

                frame.add(bottomPanel, BorderLayout.SOUTH);

                JCheckBox subdivisionBoundariesCheckbox = new JCheckBox("Display quad tree subdivision boundaries");
                subdivisionBoundariesCheckbox.addChangeListener(new ChangeListener() {
                    @Override
                    public void stateChanged(ChangeEvent e) {
                        quadTreeDisplay.setDisplaySubdivisionBoundaries(subdivisionBoundariesCheckbox.isSelected());
                        CompressionDemonstrationWindow.this.repaintTree();
                    }
                });

                bottomPanel.add(subdivisionBoundariesCheckbox);

                JCheckBox circleCheckbox = new JCheckBox("Circles instead of rectangles");
                circleCheckbox.addChangeListener(new ChangeListener() {
                    @Override
                    public void stateChanged(ChangeEvent e) {
                        if(circleCheckbox.isSelected())
                            quadTreeDisplay.setDisplayMode(QuadTreeDisplay.DISPLAY_CIRCLES);
                        else
                            quadTreeDisplay.setDisplayMode(QuadTreeDisplay.DISPLAY_RECTANGLES);
                        CompressionDemonstrationWindow.this.repaintTree();
                    }
                });

                bottomPanel.add(circleCheckbox);

                JLabel label = new JLabel("Number of leaves: 0\nTotal number of nodes: 0");
                CompressionDemonstrationWindow.this.nodesCountDisplay = label;
                bottomPanel.add(label);

                JButton saveBtn = new JButton("Save this image");
                saveBtn.addActionListener(e -> {
                    JFileChooser chooser = new JFileChooser();
                    chooser.setFileSelectionMode( JFileChooser.DIRECTORIES_ONLY );
                    chooser.showSaveDialog( null );

                    try {
                        ImageIO.write(quadTreeDisplay.getBufferedImage(), "png", new File(chooser.getSelectedFile().toPath().toString() + "/result.png"));
                    } catch (IOException ioException) {
                        ioException.printStackTrace();
                    }

                });

                bottomPanel.add(saveBtn);

                CompressionDemonstrationWindow.this.quadTreeDisplay = new QuadTreeDisplay(tree);

                frame.add(quadTreeDisplay);

                frame.pack();
                frame.setLocationRelativeTo(null);
                frame.setVisible(true);
            }
        });
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        //Upload button listener
        final JFileChooser fc = new JFileChooser();

        //In response to a button click:
        int returnVal = fc.showOpenDialog(rootFrame);

        if (returnVal == JFileChooser.APPROVE_OPTION) {
            File file = fc.getSelectedFile();
            try {
                image = ImageUtils.LoadImageAsArray(file);
                repaintTree();
            } catch (IOException ioException) {
                JOptionPane.showMessageDialog(rootFrame, "Nepavyko užkrauti failo :c");
            } catch(UnsupportedOperationException unsupportedOperationException){
                JOptionPane.showMessageDialog(rootFrame, "Paveikslėlio ilgis ir plotis turi būti vienodi.");
            }
        }
    }

    private void repaintTree(){
        tree = new QuadTree(image, errorTolerance);
        quadTreeDisplay.setTree(tree);
        quadTreeDisplay.repaint();
        quadTreeDisplay.invalidate();
        nodesCountDisplay.setText("Number of leaves: " + quadTreeDisplay.getNumberOfLeaves() + "\nTotal number of nodes: " + quadTreeDisplay.getNumberOfNodes());
    }

    @Override
    public void stateChanged(ChangeEvent e) {
        //Slider listener
        JSlider source = (JSlider)e.getSource();
        if(source.getValue() != errorTolerance){
            errorTolerance = errorSlider.getValue();
            repaintTree();
        }
    }
}
