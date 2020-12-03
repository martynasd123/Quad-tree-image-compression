package main.core;

import java.awt.*;
import java.util.*;
import java.util.List;

public class QuadTree {

    /**
     * The root node of the quad tree, containing reference to all the other nodes.
     */
    private Node root;

    /**
     * The size of 2D plane this tree represents
     */
    private final int size;


    public QuadTree(int size) {
        if (size != 0 && ((size & (size - 1)) == 0))
            this.size = size;
        else
            throw new UnsupportedOperationException("Size must be a power of two.");
    }

    public int getSize() {
        return size;
    }

    /**
     * Constructs a tree with given image's data.
     *
     * @param image          The image to represent in this tree
     * @param errorTolerance The tolerance for error when compressing image. \n0 for lossless compression.
     */
    public QuadTree(Color[][] image, float errorTolerance) {
        if (image.length == 0 || image[0].length != image.length || ((image.length & (image.length - 1)) != 0))
            throw new UnsupportedOperationException("Quad tree must be a square, and it's size must be a power of two");

        this.size = image.length;
        root = new Node(new Quad(size / 2, size / 2, null));

        root = Node.compress(image, 0, 0, size, size, errorTolerance);
    }

    /**
     * Sets a specific point in a tree to a given color.
     *
     * @param p The point to set
     */
    public void set(Quad p) {
        if (root == null) {
            root = new Node(p);
        } else root.set(p, size / 2, size, size);
    }

    /**
     * Retrieves the color of a point at given coordinates.
     *
     * @param x The x coordinate
     * @param y The y coordinate
     * @return The color of a given point. Null if no color has been set
     */
    public Color get(int x, int y) {
        if (root == null)
            return null;
        return root.get(x, y);
    }
    public Color get(int x, int y, int minDepth) {
        if (root == null)
            return null;
        return root.get(x, y, minDepth);
    }

    /**
     * @return A list of rectangles, that visually represent tree's subdivisions
     */
    public List<Rectangle> getDisplaySubdivisions(boolean leavesOnly) {
        if (root == null)
            return new ArrayList<>();
        else
            return root.getSubdivisionRectangles(size / 2, leavesOnly);
    }

    /**
     * @return The number of nodes this tree has
     */
    public int getNodeCount() {
        if (root == null)
            return 0;
        else return root.numChildren() + 1;
    }


    private static class Node {

        /**
         * Children of the node.
         * Values are interpreted in the 2d space as follows:
         * |
         * 1    |    0
         * |
         * --------+--------
         * |
         * 2    |    3
         * |
         * Where the number above is the index of this array
         */
        private Node[] children = new Node[4];

        public static final int TOP_RIGHT = 0;
        public static final int TOP_LEFT = 1;
        public static final int BOTTOM_LEFT = 2;
        public static final int BOTTOM_RIGHT = 3;

        private Quad quad;

        /**
         * Represents the specified region of a given image in this tree.
         * Compression is applied first.
         *
         * @param image          Color array, that represents the image
         * @param x              Region x coordinate
         * @param y              Region y coordinate
         * @param w              Region width
         * @param h              Region height
         * @param errorTolerance Error tolerance to use when compressing image. 0 for lossless compression
         * @return Node, that represents the given image region
         */
        static Node compress(Color[][] image, int x, int y, int w, int h, float errorTolerance) {

            Node n = new Node(new Quad(x + (w / 2), y + (w / 2), null), new Node[4]);

            Color c = determineRegionColor(image, x, y, w, h, errorTolerance);

            if (c == null) {
                //Region needs to be subdivided further
                n.children[TOP_RIGHT] = compress(image, x + (w / 2), y + (h / 2), w / 2, h / 2, errorTolerance);
                n.children[TOP_LEFT] = compress(image, x, y + (h / 2), w / 2, h / 2, errorTolerance);
                n.children[BOTTOM_LEFT] = compress(image, x, y, w / 2, h / 2, errorTolerance);
                n.children[BOTTOM_RIGHT] = compress(image, x + (w / 2), y, w / 2, h / 2, errorTolerance);
            } else {
                //Region subdivision yields enough detail
                n.quad = new Quad(c);
            }
            return n;
        }

        /**
         * @param x The x coordinate
         * @param y The y coordinate
         * @return Color of a point at the given coordinates. Null if no color was defined
         */
        Color get(int x, int y) {
            if (quad.getColor() == null) {
                assert !isLeaf();

                int q = DetermineQuadrant(quad.getX(), quad.getY(), x, y);

                if (children[q] == null)
                    return null; //Point is not defined
                else
                    return children[q].get(x, y); //Searching further in the determined region
            }

            if (quad.getX() == null || (quad.getX() == x && quad.getY() == y))
                return quad.getColor(); //Point is defined by a quad that either spans one or more pixels
            else
                return null; //Point is not defined
        }

        Color get(int x, int y, int minDepth){
            if (quad.getColor() == null) {
                assert !isLeaf();

                int q = DetermineQuadrant(quad.getX(), quad.getY(), x, y);

                if (children[q] == null)
                    return null; //Point is not defined
                else {
                    return children[q].get(x, y, minDepth - 1); //Searching further in the determined region
                }
            }

            if (quad.getX() == null || (quad.getX() == x && quad.getY() == y)) {
                if(minDepth >= 0)
                    return Color.RED;
                else
                    return quad.getColor(); //Point is defined by a quad that either spans one or more pixels
            }else
                return null; //Point is not defined
        }

        /**
         * Determines whether a given region of a given image has enough detail
         * based on error tolerance.
         *
         * @param image          The image to analyze
         * @param i              The x coordinate of the region to analyze
         * @param j              The y coordinate of the region to analyze
         * @param w              Region width
         * @param h              Region height
         * @param errorTolerance The maximum allowed error tolerance
         * @return Null if region needs to be subdivided further. If region's detail level was determined
         * to not exceed the given error tolerance, it's average color is returned.
         */
        private static Color determineRegionColor(Color[][] image, int i, int j, int w, int h, float errorTolerance) {

            if (w * h == 0)
                return null;

            int r = 0;
            int g = 0;
            int b = 0;

            //Summing red, green and blue colors of the region
            for (int y = j; y < j + h; y++) {
                for (int x = i; x < i + w; x++) {
                    Color c = image[x][y];
                    r += c.getRed();
                    g += c.getGreen();
                    b += c.getBlue();
                }
            }

            //Calculating averages
            int avgR = Math.round(((float) r) / ((float) (w * h)));
            int avgG = Math.round(((float) g) / ((float) (w * h)));
            int avgB = Math.round(((float) b) / ((float) (w * h)));

            int errR = 0;
            int errG = 0;
            int errB = 0;

            //Calculating error for each color channel
            for (int y = j; y < j + h; y++) {
                for (int x = i; x < i + w; x++) {
                    Color c = image[x][y];
                    errR += Math.abs(c.getRed() - avgR);
                    errG += Math.abs(c.getGreen() - avgG);
                    errB += Math.abs(c.getBlue() - avgB);
                }
            }

            //Taking average error of each channel to get the overall region error result.
            float avgErr = ((float) (errR + errG + errB)) / 3f;

            if (avgErr > errorTolerance)
                return null;
            else return new Color(avgR, avgG, avgB);
        }

        public Node(Quad quad) {
            this.quad = quad;
        }

        public Node(Quad quad, Node[] children) {
            this(quad);
            this.children = children;
        }

        public boolean isLeaf() {
            return children[TOP_RIGHT] == null && children[TOP_LEFT] == null && children[BOTTOM_LEFT] == null && children[BOTTOM_RIGHT] == null;
        }

        private int DetermineQuadrant(int centerX, int centerY, int x, int y) {
            if (centerX < x && centerY < y)
                return TOP_RIGHT;
            else if (centerX >= x && centerY < y)
                return TOP_LEFT;
            else if (centerX >= x)
                return BOTTOM_LEFT;
            else
                return BOTTOM_RIGHT;
        }

        /**
         * Sets a point at a given position
         *
         * @param dim width of current node
         * @param pcx parent node's center coordinate x
         * @param pcy parent node's center coordinate y
         */
        public void set(Quad quad, int dim, int pcx, int pcy) {
            if (isLeaf()) {
                int q = DetermineQuadrant(pcx, pcy, this.quad.getX(), this.quad.getY());
                int centerX, centerY;
                if (q == TOP_RIGHT) {
                    centerX = pcx + dim;
                    centerY = pcy + dim;
                } else if (q == TOP_LEFT) {
                    centerX = pcx - dim;
                    centerY = pcy + dim;
                } else if (q == BOTTOM_LEFT) {
                    centerX = pcx - dim;
                    centerY = pcy - dim;
                } else {
                    centerX = pcx + dim;
                    centerY = pcy - dim;
                }

                //Assigning the target quadrants
                int q1 = DetermineQuadrant(centerX, centerY, quad.getX(), quad.getY());
                int q2 = DetermineQuadrant(centerX, centerY, this.quad.getX(), this.quad.getY());

                children[q1] = new Node(quad);
                children[q2] = new Node(this.quad);

                this.quad = new Quad(centerX, centerY, null);

            } else {
                int q1 = DetermineQuadrant(this.quad.getX(), this.quad.getY(), quad.getX(), quad.getY());
                if (children[q1] == null)
                    children[q1] = new Node(quad);
                else {
                    children[q1].set(quad, dim / 2, this.quad.getX(), this.quad.getY());
                }
            }
        }

        /**
         * Returns the number of children (either direct or indirect) this node has.
         *
         * @return A number of children this node has
         */
        public int numChildren() {
            int num = 0;
            for (Node child : children) {
                if (child != null) {
                    num++;
                    num += child.numChildren();
                }
            }
            return num;
        }

        /**
         * Returns a list of rectangles, that represent the quad tree space subdivisions.
         *
         * @param dim        Dimension of upper node
         * @param leavesOnly If true, only the leaves of the tree are returned
         * @return A list of rectangles, that represent the quad tree space subdivisions
         */
        public List<Rectangle> getSubdivisionRectangles(int dim, boolean leavesOnly) {
            ArrayList<Rectangle> rectangles = new ArrayList<>();
            if (!isLeaf()) {
                for (int i = 0; i < 4; i++) {
                    Node child = children[i];
                    List<Rectangle> childRectangles = new ArrayList<>();
                    if (child != null) {
                        childRectangles = child.getSubdivisionRectangles(dim / 2, leavesOnly);
                    }
                    if (childRectangles.size() == 0 || !leavesOnly) {
                        if (i == TOP_RIGHT)
                            rectangles.add(new Rectangle(this.quad.getX(), this.quad.getY(), dim, dim));
                        else if (i == BOTTOM_LEFT)
                            rectangles.add(new Rectangle(this.quad.getX() - dim, this.quad.getY() - dim, dim, dim));
                        else if (i == TOP_LEFT)
                            rectangles.add(new Rectangle(this.quad.getX() - dim, this.quad.getY(), dim, dim));
                        else
                            rectangles.add(new Rectangle(this.quad.getX(), this.quad.getY() - dim, dim, dim));
                    }

                    if (childRectangles.size() != 0)
                        rectangles.addAll(childRectangles);

                }
            }
            return rectangles;
        }
    }
}
