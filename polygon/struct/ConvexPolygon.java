package polygon.struct;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Polygon;

import java.util.ArrayList;
import java.util.Arrays;
import polygon.util.Utils;

public class ConvexPolygon {

    private Polygon polygon;

    private Coordinate[] upperChain;
    private Coordinate[] lowerChain;

    private double minX, maxX;
    private double minY, maxY;

    public ConvexPolygon(Polygon polygon) {
        this.polygon = polygon;
        preprocess();
    }

    /**
     * NOTE: This assumes the vertices are listed in clockwise order.
     */
    private void preprocess() {
        Coordinate[] vertices = this.polygon.getCoordinates();
        vertices = Arrays.copyOfRange(vertices, 0, vertices.length-1);


        this.minX = Double.POSITIVE_INFINITY;
        this.minY = Double.POSITIVE_INFINITY;
        this.maxX = Double.NEGATIVE_INFINITY;
        this.maxY = Double.NEGATIVE_INFINITY;
        int minXIdx = 0, maxXIdx = 0;

        // Find bounding box
        for (int i = 0; i < vertices.length; i++) {
            Coordinate vertex = vertices[i];
            double xCoord = vertex.x, yCoord = vertex.y;

            if (xCoord < minX) {
                minX = xCoord;
                minXIdx = i;
            }
            if (yCoord < minY) {
                minY = yCoord;
            }
            if (xCoord > maxX) {
                maxX = xCoord;
                maxXIdx = i;
            }
            if (yCoord > maxY) {
                maxY = yCoord;
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
    }

    public boolean contains(Coordinate c) {
        // Return false if c isn't within the bounding box of the current polygon
        if (c.x < minX || c.x > maxX || c.y < minY || c.y > maxY) {
            return false;
        }

        // Some point on the vertical ray to infinity from c
        Coordinate rayCoord = new Coordinate(c.x, maxY + 1);

        int upperChainIdx = Utils.binarySearch(c, this.upperChain, true);
        // Need to handle case when c is on right edge of bounding box
        boolean intersectsUpperChain = (upperChainIdx < upperChain.length - 1) ?
                Utils.doLineSegmentsIntersect(c, rayCoord, upperChain[upperChainIdx], upperChain[upperChainIdx + 1]) :
                Utils.doLineSegmentsIntersect(c, rayCoord, upperChain[upperChainIdx], upperChain[upperChainIdx]);

        // Every vertical ray cast from within any point in polygon intersects the upper chain.
        if (! intersectsUpperChain) {
            return false;
        }

        int lowerChainIdx = Utils.binarySearch(c, this.lowerChain, false);
        // Need to handle case when c is on left edge of bounding box
        boolean intersectsLowerChain = (lowerChainIdx < lowerChain.length - 1) ?
                Utils.doLineSegmentsIntersect(c, rayCoord, lowerChain[lowerChainIdx], lowerChain[lowerChainIdx + 1]) :
                Utils.doLineSegmentsIntersect(c, rayCoord, lowerChain[lowerChainIdx], lowerChain[lowerChainIdx]);

        // Every vertical ray cast from within any point in polygon does not the upper chain.
        // This isn't strictly true on the boundary of the lower chain but who cares.
        if (intersectsLowerChain) {
            return false;
        }

        return true;
    }
}
