package polygon.struct;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Polygon;
import polygon.util.Utils;

import java.util.ArrayList;
import java.util.Arrays;

public class ConvexPolygon {

    private Polygon polygon;

    private Coordinate[] upperChain;
    private Coordinate[] lowerChain;

    private double minX, maxX;
    private double minY, maxY;

    private int numVertices;
    private boolean isClockwise;

    public ConvexPolygon(Polygon polygon) {
        this.polygon = polygon;
        preprocess();
    }

    /**
     * Pre-process the polygon to its minimum bounding box, its upper and lower chain, and whether its vertices are
     * listed in clock-wise order or not.
     */
    private void preprocess() {
        Coordinate[] vertices = this.polygon.getCoordinates();
        vertices = Arrays.copyOfRange(vertices, 0, vertices.length - 1);

        this.numVertices = vertices.length;
        // Index of lowest and rightmost point in vertices
        int lowestRightIdx = 0;

        this.minX = Double.POSITIVE_INFINITY;
        this.minY = Double.POSITIVE_INFINITY;
        this.maxX = Double.NEGATIVE_INFINITY;
        this.maxY = Double.NEGATIVE_INFINITY;
        int minXIdx = 0, maxXIdx = 0;

        // Find bounding box
        for (int i = 0; i < this.numVertices; i++) {
            Coordinate currVertex = vertices[i];
            double currX = currVertex.x, currY = currVertex.y;

            if (currX < minX) {
                minX = currX;
                minXIdx = i;
            }
            if (currY < minY || (Utils.eq(currY, minY) && Utils.ge(currX, vertices[lowestRightIdx].x))) {
                lowestRightIdx = i;
                minY = currY;
            }
            if (currX > maxX) {
                maxX = currX;
                maxXIdx = i;
            }
            if (currY > maxY) {
                maxY = currY;
            }
        }

        // Find upper and lower chains
        int min = Math.min(minXIdx, maxXIdx), max = Math.max(minXIdx, maxXIdx);

        ArrayList<Coordinate> firstChain = new ArrayList<Coordinate>();
        for (int i = min; i <= max; i++) {
            firstChain.add(vertices[i]);
        }

        ArrayList<Coordinate> secondChain = new ArrayList<Coordinate>();
        for (int i = max; i < vertices.length; i++) {
            secondChain.add(vertices[i]);
        }
        for (int i = 0; i <= min; i++) {
            secondChain.add(vertices[i]);
        }

        if (minXIdx < maxXIdx) {
            this.upperChain = firstChain.toArray(new Coordinate[firstChain.size()]);
            this.lowerChain = secondChain.toArray(new Coordinate[secondChain.size()]);
        } else {
            this.lowerChain = firstChain.toArray(new Coordinate[firstChain.size()]);
            this.upperChain = secondChain.toArray(new Coordinate[secondChain.size()]);
        }

        this.isClockwise = Utils.isClockwise(vertices, lowestRightIdx);
    }

    /**
     * Point in polygon query.
     *
     * @param c the specified point
     * @return
     */
    public boolean contains(Coordinate c) {
        // Return false if c isn't within the bounding box of the current polygon
        if (c.x < minX || c.x > maxX || c.y < minY || c.y > maxY) {
            return false;
        }

        // Some point on the vertical ray to infinity from c
        Coordinate rayCoord = new Coordinate(c.x, maxY + 1);

        // Note that the upper chain is in increasing order of x-coordinates if clockwise else decreasing
        int upperChainIdx = (this.isClockwise) ? Utils.binarySearch(c, this.upperChain, true) :
                Utils.binarySearch(c, this.upperChain, false);

        // Need to handle case when c is on right edge of bounding box
        boolean intersectsUpperChain = (upperChainIdx < upperChain.length - 1) ?
                Utils.doLineSegmentsIntersect(c, rayCoord, upperChain[upperChainIdx], upperChain[upperChainIdx + 1]) :
                Utils.doLineSegmentsIntersect(c, rayCoord, upperChain[upperChainIdx], upperChain[upperChainIdx]);

        // Every vertical ray cast from within any point in polygon intersects the upper chain.
        if (!intersectsUpperChain) {
            return false;
        }

        // Note that the upper chain is in decreasing order of x-coordinates if clockwise else increasing
        int lowerChainIdx = (this.isClockwise) ? Utils.binarySearch(c, this.lowerChain, false) :
                Utils.binarySearch(c, this.lowerChain, true);


        // Need to handle case when c is on left edge of bounding box
        boolean intersectsLowerChain = (lowerChainIdx < lowerChain.length - 1) ?
                Utils.doLineSegmentsIntersect(c, rayCoord, lowerChain[lowerChainIdx], lowerChain[lowerChainIdx + 1]) :
                Utils.doLineSegmentsIntersect(c, rayCoord, lowerChain[lowerChainIdx], lowerChain[lowerChainIdx]);

        // Every vertical ray cast from within any point in polygon does not the upper chain.
        if (intersectsLowerChain && !
                ((lowerChainIdx < lowerChain.length - 1) ?
                        Utils.onSegment(lowerChain[lowerChainIdx], lowerChain[lowerChainIdx + 1], c) :
                        Utils.onSegment(lowerChain[lowerChainIdx], lowerChain[lowerChainIdx], c)))
            return false;


        return true;
    }
}
