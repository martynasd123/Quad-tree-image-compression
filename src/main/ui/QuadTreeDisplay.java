package main.ui;

import main.core.QuadTree;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.List;

/**
 * A java swing JPanel to display quad tree as an image
 */
public class QuadTreeDisplay extends JPanel {

    private QuadTree tree;

    public QuadTreeDisplay(QuadTree tree){
        this.tree = tree;
    }

    public QuadTreeDisplay(){}

    public void setTree(QuadTree tree){
        this.tree = tree;
    }

    private int numberOfNodes = 0;
    private int numberOfLeaves = 0;

    public int getNumberOfNodes() {
        return numberOfNodes;
    }

    public int getNumberOfLeaves() {
        return numberOfLeaves;
    }

    private boolean displaySubdivisionBoundaries = false;

    public static final int DISPLAY_CIRCLES = 0;
    public static final int DISPLAY_RECTANGLES = 1;

    private int displayMode = DISPLAY_RECTANGLES;

    public int getDisplayMode() {
        return displayMode;
    }

    public void setDisplayMode(int displayMode) {
        this.displayMode = displayMode;
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g.create();
        if(tree != null) {
            float scale = ((float)getWidth() - 100) / ((float)tree.getSize());
            paintTreeContents(g2d, scale, 0);
        }

        g2d.dispose();
    }

    private void paintTreeContents(Graphics2D g2d, float scale, int offset){
        List<Rectangle> rectangleList = tree.getDisplaySubdivisions(true);

        this.numberOfLeaves = rectangleList.size();
        this.numberOfNodes = tree.getNodeCount();

        g2d.scale(scale, scale);

        //Preparing canvas background
        g2d.setColor(Color.BLACK);
        g2d.fillRect(offset,offset,tree.getSize(),tree.getSize());

        System.out.println("num leaves " + numberOfLeaves + "\nnumNodes " + numberOfNodes + "\n\n");

        for(Rectangle rect: rectangleList){
            Color c = tree.get(rect.x + (rect.width / 2), rect.y + (rect.width / 2));
            g2d.setColor(c);

            if(displayMode == DISPLAY_CIRCLES)
                g2d.fillOval(offset + rect.y, offset + rect.x, rect.width, rect.height);
            else if(displayMode == DISPLAY_RECTANGLES)
                g2d.fillRect( offset + rect.y, offset + rect.x, rect.width, rect.height);

            if(displaySubdivisionBoundaries){
                g2d.setColor(Color.CYAN);
                g2d.drawRect( offset + rect.y, offset + rect.x, rect.width, rect.height);
            }
        }
    }

    public BufferedImage getBufferedImage(){
        BufferedImage bi = new BufferedImage(tree.getSize(), tree.getSize(), BufferedImage.TYPE_INT_RGB);

        Graphics g = bi.getGraphics();
        Graphics2D g2d = (Graphics2D) g.create();

        paintTreeContents(g2d, 1f, 0);

        return bi;
    }

    public void setDisplaySubdivisionBoundaries(boolean enabled){
        displaySubdivisionBoundaries = enabled;
    }

    @Override
    public Dimension getPreferredSize() {
        return new Dimension(600, 600);
    }


}
