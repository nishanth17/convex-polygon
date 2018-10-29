package polygon.util;

import com.vividsolutions.jts.geom.Coordinate;

public class Utils {


    private final static double EPSILON = 1e-14;

    /**
     * Find first index 'idx' in 'arr' for which c.x ∈ [arr[idx].x, arr[idx+1].x) if ascending or
     *
     * @param c
     * @param array
     * @param ascending
     * @return
     */
    public static int binarySearch(Coordinate c, Coordinate[] array, boolean ascending) {
        // Return -1 if no idx in arr
        if (ascending && (c.x < array[0].x || c.x > array[array.length - 1].x)) {
            return -1;
        } else if (!ascending && (c.x > array[0].x || c.x < array[array.length - 1].x)) {
            return -1;
        }

        int left = 0, right = array.length - 1;
        while (left <= right) {
            int mid = (left + right) >> 1;
            double diff = array[mid].x - c.x;

            if (diff < 0) {
                if (ascending) {
                    left = mid + 1;
                } else {
                    right = mid - 1;
                }
            } else if (diff == 0) {
                return mid;
            } else {
                if (ascending) {
                    right = mid - 1;
                } else {
                    left = mid + 1;
                }
            }
        }

        /*
         * Note that r < l and l = r + 1
         * When in ascending order, array[r] ≤ x < array[r+1], so x ∈ [r, r+1)
         * When in descending order, array[r] ≥ x > array[r+1], so x ∈ [r, r+1)
         */
        return right;
    }

    /**
     * Checks whether the line segments [p1, q2] and [p2, q2] intersect.
     * Taken from <a href="https://www.geeksforgeeks.org/check-if-two-given-line-segments-intersect/">https://www.geeks
     * forgeeks.org/check-if-two-given-line-segments-intersect/</a>.
     *
     * @param p1
     * @param p2
     * @param q1
     * @param q2
     * @return
     */
    public static boolean doLineSegmentsIntersect(Coordinate p1, Coordinate q1, Coordinate p2, Coordinate q2) {
        // Find the four orientations needed for general and special cases
        int o1 = crossProduct(p1, q1, p2);
        int o2 = crossProduct(p1, q1, q2);
        int o3 = crossProduct(p2, q2, p1);
        int o4 = crossProduct(p2, q2, q1);

        // General case
        if (o1 != o2 && o3 != o4)
            return true;

        // Special Cases
        // p1, q1 and p2 are co-linear and p2 lies on segment p1q1
        if (eq(o1, 0) && onSegment(p1, p2, q1))
            return true;

        // p1, q1 and q2 are co-linear and q2 lies on segment p1q1
        if (eq(o2, 0) && onSegment(p1, q2, q1))
            return true;

        // p2, q2 and p1 are co-linear and p1 lies on segment p2q2
        if (eq(o3, 0) && onSegment(p2, p1, q2))
            return true;

        // p2, q2 and q1 are co-linear and q1 lies on segment p2q2
        if (eq(o4, 0) && onSegment(p2, q1, q2))
            return true;

        return false;
    }

    /**
     * Determines whether a the vertices of a polygon, specified as an array of coordinates, is in clockwise
     * order or not.
     * @param vertices
     * @param lowestRightIdx
     * @return true if clockwise and false otherwise.
     */
    public static boolean isClockwise(Coordinate[] vertices, int lowestRightIdx) {
        int leftIdx = (lowestRightIdx == 0)? vertices.length - 1 : lowestRightIdx - 1;
        int rightIdx = (lowestRightIdx == vertices.length - 1)? 0 : lowestRightIdx + 1;

        Coordinate a = vertices[leftIdx];
        Coordinate b = vertices[lowestRightIdx];
        Coordinate c = vertices[rightIdx];

        double area = (a.x * b.y - b.x * a.y) + (b.x * c.y - c.x * b.y) + (c.x * a.y - a.x * c.y);
        // Clockwise if signed area is negative else counter-clockwise
        return Utils.le(area, 0);
    }

    /**
     * Given three co-linear points p, q, r, check q lies on the segment [p, r]
     * Taken from <a href="https://www.geeksforgeeks.org/check-if-two-given-line-segments-intersect/">https://www.geeks
     * forgeeks.org/check-if-two-given-line-segments-intersect/</a>.
     *
     * @param p
     * @param q
     * @param r
     * @return
     */
    private static boolean onSegment(Coordinate p, Coordinate q, Coordinate r) {
        return leq(q.x, Math.max(p.x, r.x)) &&
                geq(q.x, Math.min(p.x, r.x)) &&
                leq(q.y, Math.max(p.y, r.y)) &&
                geq(q.y, Math.min(p.y, r.y));
    }

    /**
     * Checks orientation of p -> q -> r -> p.
     * Taken from <a href="https://www.geeksforgeeks.org/check-if-two-given-line-segments-intersect/">https://www.geeks
     * forgeeks.org/check-if-two-given-line-segments-intersect/</a>
     *
     * @return 0 if co-linear, 1 if clockwise, -1 if anti-clockwise
     */
    private static int crossProduct(Coordinate p, Coordinate q, Coordinate r) {
        double det = (q.y - p.y) * (r.x - q.x) - (q.x - p.x) * (r.y - q.y);
        return (eq(det, 0)) ? 0 : (int) Math.signum(det);
    }

    /**
     * Checks whether a < b i.e. (b - a) > eps
     *
     * @param a
     * @param b
     * @return
     */
    public static boolean le(double a, double b) {
        return (b - a) > EPSILON;
    }

    /**
     * Checks whether a > b i.e. (a - b) > eps
     *
     * @param a
     * @param b
     * @return
     */
    public static boolean ge(double a, double b) {
        return (a - b) > EPSILON;
    }

    /**
     * Checks whether a ≤ b i.e. a ≤ b + eps
     *
     * @param a
     * @param b
     * @return
     */
    public static boolean leq(double a, double b) {
        return (a - b) <= EPSILON;
    }

    /**
     * Checks whether a ≥ b i.e. a ≥ b - eps
     *
     * @param a
     * @param b
     * @return
     */
    public static boolean geq(double a, double b) {
        return (b - a) <= EPSILON;
    }

    /**
     * Checks whether a = b i.e. a - eps ≤ b ≤ a + eps
     *
     * @param a
     * @param b
     * @return
     */
    public static boolean eq(double a, double b) {
        return Math.abs(a - b) <= EPSILON;
    }
}
