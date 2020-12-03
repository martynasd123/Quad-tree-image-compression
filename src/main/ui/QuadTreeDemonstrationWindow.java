package main.ui;

import main.core.Quad;
import main.core.QuadTree;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;

public class QuadTreeDemonstrationWindow {


    private final QuadTree quadTree = new QuadTree(512);

    public void run() {
        EventQueue.invokeLater(new Runnable() {
            @Override
            public void run() {
                try {
                    UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
                } catch (ClassNotFoundException | InstantiationException | IllegalAccessException | UnsupportedLookAndFeelException ex) {
                    ex.printStackTrace();
                }

                JFrame frame = new JFrame("Quad tree demonstration");
                frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
                frame.add(new PaintSection());
                frame.pack();
                frame.setLocationRelativeTo(null);
                frame.setVisible(true);

                QuadTreeDemonstrationWindow.this.quadTree.set(new Quad(500, 500, null));
                QuadTreeDemonstrationWindow.this.quadTree.set(new Quad(0, 0, null));
            }
        });
    }

    private class PaintSection extends JPanel {

        ArrayList<Point> points = new ArrayList<>();

        @Override
        public Dimension getPreferredSize() {
            return new Dimension(500, 500);
        }

        public PaintSection() {
            MouseAdapter ma = new MouseAdapter() {

                @Override
                public void mousePressed(MouseEvent e) {
                    QuadTreeDemonstrationWindow.this.quadTree.set(new Quad(e.getX(), e.getY(), null));
                    points.add(new Point(e.getX(), e.getY()));
                    repaint();
                }

                @Override
                public void mouseDragged(MouseEvent e) {
                    QuadTreeDemonstrationWindow.this.quadTree.set(new Quad(e.getX(), e.getY(), null));
                    points.add(new Point(e.getX(), e.getY()));
                    repaint();
                }

                @Override
                public void mouseReleased(MouseEvent e) {
                }
            };
            addMouseListener(ma);
            addMouseMotionListener(ma);
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);

            Graphics2D g2d = (Graphics2D) g.create();
            g2d.setColor(Color.BLACK);
            List<Rectangle> rectangles = quadTree.getDisplaySubdivisions(false);
            for(Rectangle rect: rectangles){
                g2d.drawRect(rect.x, rect.y, rect.width, rect.height);
            }

            g2d.setColor(Color.RED);
            for(Point p: points){
                g2d.fillOval((int) p.getX(), (int) p.getY(), 3, 3);
            }
            g2d.dispose();
        }
    }
}
