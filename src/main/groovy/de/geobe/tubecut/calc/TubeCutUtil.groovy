package de.geobe.tubecut.calc

import groovy.transform.AutoClone

/**
 * Data structure of calculation results from centric cut
 */
class TubeCutCentric {
    double cutLength
    ArrayList<PointAt> outline = new ArrayList()
}

/**
 * Data structure of calculation results from centric cut
 */
class TubeCutExcentric {
    double cutLength, cutStart, cutEnd, cutTop, cutBottom
    ArrayList<ArrayList<PointAt>> outlines = []
}

/**
 * Data structure with x, y  coordinates in the plane of intersection of ellipse and circle
 * and resulting length position and arc length on larger tube
 */
class PointAt extends Point {
    double arc  // on the large tube from center line to intersection point plot as +/- y
    double len  // length on the center line, plot as x

    @Override
    String toString() {
        sprintf("x: %6.2f, y: %6.2f, arc: %6.2f, len: %6.2f", x, y, arc, len)
    }
}

@AutoClone
class Point {
    double x    // intersection x
    double y    // intersection y

    void set(Point other) {
        x = other.x
        y = other.y
    }
}

class BorderPoints {
    PointAt upper
    PointAt lower
}
