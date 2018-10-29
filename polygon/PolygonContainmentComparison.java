package polygon;

import com.vividsolutions.jts.geom.*;
import com.vividsolutions.jts.geom.impl.CoordinateArraySequence;
import polygon.struct.ConvexPolygon;

import java.util.ArrayList;
import java.util.Random;

public class PolygonContainmentComparison {

    private final static double SCALE_FACTOR = 500000;

    static GeometryFactory geometryFactory = new GeometryFactory();

    /* Test whether a specified polygon contains a specified point */
    public static boolean doesPolygonContainPoint(Polygon polygon, Coordinate point) {
        return polygon.contains(geometryFactory.createPoint(new Coordinate(point.x, point.y)));
    }

    /* Create a polygon with a specified list of vertices */
    public static Polygon createPolygonWithVertices(ArrayList<Coordinate> vertices) {
        final Polygon polygon = geometryFactory.createPolygon(
                new LinearRing(new CoordinateArraySequence(vertices.toArray(new Coordinate[vertices.size()])),
                        geometryFactory));
        return polygon;
    }

    /* Create list of random points */
    public static ArrayList<Coordinate> getRandomPoints(int numPoints, double scaleFactor) {
        Random random = new Random();
        ArrayList<Coordinate> points = new ArrayList<Coordinate>();

        for (int i = 0; i < numPoints; i++) {
            points.add(new Coordinate(random.nextDouble() * scaleFactor, random.nextDouble() * scaleFactor));
        }

        return points;
    }

    /* Create random convex polygon */
    public static Polygon createRandomConvexPolygon(int numVertices) {
        ArrayList<Coordinate> points = getRandomPoints(numVertices, SCALE_FACTOR);
        // Close the polygon
        points.add((Coordinate) points.get(0).clone());

        Polygon polygon = createPolygonWithVertices(points);
        return (Polygon) polygon.convexHull();
    }


    /* Test JTS */
    public static ArrayList<Boolean> doJTS(Polygon polygon,  ArrayList<Coordinate> queryPoints) {
        ArrayList<Boolean> responses = new ArrayList<Boolean>();

        for (Coordinate queryPoint : queryPoints) {
            responses.add(doesPolygonContainPoint(polygon, queryPoint));
        }

        return responses;
    }

    /* Test binary search */
    public static ArrayList<Boolean> doBinarySearch(Polygon polygon,  ArrayList<Coordinate> queryPoints) {
        ArrayList<Boolean> responses = new ArrayList<Boolean>();

        ConvexPolygon convexPolygon = new ConvexPolygon(polygon);
        for (Coordinate queryPoint : queryPoints) {
            responses.add(convexPolygon.contains(queryPoint));
        }

        return responses;
    }

    /* Test equality of JTS and binary search responses */
    public static boolean testResponses(ArrayList<Boolean> jtsResponses, ArrayList<Boolean> binSearchResponses) {
        for (int i = 0; i < jtsResponses.size(); i++) {
            if (jtsResponses.get(i) != binSearchResponses.get(i))
                return false;
        }

        return true;
    }


    public static void main(String[] args) {
        final int numVertices = 50000, numPoints = 1000000;
        Polygon polygon = createRandomConvexPolygon(numVertices);
        ArrayList<Coordinate> queryPoints = getRandomPoints(numPoints, SCALE_FACTOR);

        System.out.println("Number of vertices: " + numVertices);
        System.out.println("Number of queries: " + numPoints + "\n");

        double startTime = System.nanoTime();
        ArrayList<Boolean> jtsResponses = doJTS(polygon, queryPoints);
        double endTime = System.nanoTime();
        System.out.println("JTS: " + ((endTime - startTime) / 1e9)+ " s");

        startTime = System.nanoTime();
        ArrayList<Boolean> binSearchResponses = doBinarySearch(polygon, queryPoints);
        endTime = System.nanoTime();
        System.out.println("Binary Search: " + ((endTime - startTime) / 1e9)+ " s\n");

        System.out.println("Equal: " + testResponses(jtsResponses, binSearchResponses));
    }
}
