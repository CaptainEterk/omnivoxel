package omnivoxel.tools;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class SplineMapper {

    public record Point(double location, double value, double derivative) {
    }

    private final List<Point> points = new ArrayList<>();

    public SplineMapper() {
        points.add(new Point(0.0, 0.0, 1.0));
        points.add(new Point(0.25, 0.25, 1.0));
        points.add(new Point(0.5, 0.5, 1.0));
        points.add(new Point(0.75, 0.75, 1.0));
        points.add(new Point(1.0, 1.0, 1.0));
    }

    public List<Point> getPoints() {
        return points;
    }

    public double map(double x) {
        if (points.isEmpty()) {
            return x;
        }

        sort();

        if (points.size() == 1) {
            return points.getFirst().value();
        }

        if (x <= points.getFirst().location()) {
            return points.getFirst().value();
        }

        if (x >= points.getLast().location()) {
            return points.getLast().value();
        }

        for (int i = 0; i < points.size() - 1; i++) {
            Point p0 = points.get(i);
            Point p1 = points.get(i + 1);

            if (x >= p0.location() && x <= p1.location()) {
                return interpolateHermite(
                        x,
                        p0.location(),
                        p0.value(),
                        p0.derivative(),
                        p1.location(),
                        p1.value(),
                        p1.derivative()
                );
            }
        }

        return 0.0;
    }

    private double interpolateHermite(
            double x,
            double x0,
            double v0,
            double m0,
            double x1,
            double v1,
            double m1
    ) {
        double t = (x - x0) / (x1 - x0);

        double t2 = t * t;
        double t3 = t2 * t;

        double h00 = 2.0 * t3 - 3.0 * t2 + 1.0;
        double h10 = t3 - 2.0 * t2 + t;
        double h01 = -2.0 * t3 + 3.0 * t2;
        double h11 = t3 - t2;

        return h00 * v0
                + h10 * (x1 - x0) * m0
                + h01 * v1
                + h11 * (x1 - x0) * m1;
    }

    public int findPoint(
            double screenX,
            double screenY,
            double graphX,
            double graphY,
            double graphWidth,
            double graphHeight
    ) {
        double radius = 10.0;

        for (int i = 0; i < points.size(); i++) {
            Point point = points.get(i);

            double px =
                    graphX
                            + point.location() * graphWidth;

            double py =
                    graphY
                            + (1.0 - point.value()) * graphHeight;

            double dx = screenX - px;
            double dy = screenY - py;

            if (dx * dx + dy * dy <= radius * radius) {
                return i;
            }
        }

        return -1;
    }

    public void movePoint(
            int index,
            double location,
            double value
    ) {
        if (index < 0 || index >= points.size()) {
            return;
        }

        Point old = points.get(index);

        if (index == 0) {
            location = 0.0;
        } else if (index == points.size() - 1) {
            location = 1.0;
        }

        location = clamp(location);
        value = clamp(value);

        /*
         * Don't sort here.

         * The selected index is held by the UI while dragging.
         * Sorting here could cause that index to refer to a
         * different point while the mouse is being dragged.
         */
        points.set(
                index,
                new Point(
                        location,
                        value,
                        old.derivative()
                )
        );
    }

    public void addPoint(
            double location,
            double value
    ) {
        location = clamp(location);
        value = clamp(value);

        if (location <= 0.0 || location >= 1.0) {
            return;
        }

        points.add(
                new Point(
                        location,
                        value,
                        calculateDerivative(location, value)
                )
        );

        sort();

        recalculateDerivatives();
    }

    public void removePoint(int index) {
        /*
         * Keep the endpoints.
         */
        if (index <= 0 || index >= points.size() - 1) {
            return;
        }

        points.remove(index);

        recalculateDerivatives();
    }

    private double calculateDerivative(
            double location,
            double value
    ) {
        if (points.isEmpty()) {
            return 0.0;
        }

        sort();

        Point previous = null;
        Point next = null;

        for (Point point : points) {
            if (point.location() < location) {
                previous = point;
            }

            if (point.location() > location) {
                next = point;
                break;
            }
        }

        if (previous != null && next != null) {
            return (next.value() - previous.value())
                    / (next.location() - previous.location());
        }

        if (previous != null) {
            return (value - previous.value())
                    / (location - previous.location());
        }

        if (next != null) {
            return (next.value() - value)
                    / (next.location() - location);
        }

        return 0.0;
    }

    /*
     * Recalculate derivatives so that the editor's spline
     * corresponds to the Hermite spline used by
     * SplineDensityFunction.
     */
    public void recalculateDerivatives() {
        sort();

        if (points.size() == 1) {
            Point p = points.getFirst();

            points.set(
                    0,
                    new Point(
                            p.location(),
                            p.value(),
                            0.0
                    )
            );

            return;
        }

        List<Point> updated = new ArrayList<>(
                points.size()
        );

        for (int i = 0; i < points.size(); i++) {
            Point current = points.get(i);

            double derivative;

            if (i == 0) {
                Point next = points.get(i + 1);

                derivative =
                        (next.value() - current.value())
                                / (next.location() - current.location());

            } else if (i == points.size() - 1) {
                Point previous = points.get(i - 1);

                derivative =
                        (current.value() - previous.value())
                                / (current.location() - previous.location());

            } else {
                Point previous = points.get(i - 1);
                Point next = points.get(i + 1);

                derivative =
                        (next.value() - previous.value())
                                / (next.location() - previous.location());
            }

            updated.add(
                    new Point(
                            current.location(),
                            current.value(),
                            derivative
                    )
            );
        }

        points.clear();
        points.addAll(updated);
    }

    public List<Point> generateCurve(int samples) {
        List<Point> curve =
                new ArrayList<>(samples);

        for (int i = 0; i < samples; i++) {
            double location =
                    (double) i / (samples - 1);

            double value = map(location);

            curve.add(
                    new Point(
                            location,
                            value,
                            0.0
                    )
            );
        }

        return curve;
    }

    /**
     * Prints the spline in the format expected by the
     * SplineDensityFunction configuration.
     *
     * The "value" field is printed as a numeric constant.
     * Replace it with your actual DensityFunction when
     * transferring the points into worldgen JSON.
     */
    public void printPoints() {
        recalculateDerivatives();

        System.out.println();
        System.out.println("Spline points:");

        for (Point point : points) {
            System.out.printf(
                    """
                    {
                        "location": %.8f,
                        "derivative": %.8f,
                        "value": %.8f
                    }
                    """,
                    point.location(),
                    point.derivative(),
                    point.value()
            );
        }

        System.out.println();
    }

    private void sort() {
        points.sort(
                Comparator.comparingDouble(
                        Point::location
                )
        );
    }

    private double clamp(double value) {
        return Math.max(
                0.0,
                Math.min(1.0, value)
        );
    }
}