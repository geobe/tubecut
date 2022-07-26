package de.geobe.tubecut.calc

import static java.lang.Math.*

/**
 * use normal xy coordinate system for parameters and transform it to SVG coordinates
 */
class TubeCutCalculatorExcentric extends TubeCutCalculator {
    double excentricity = 30.0 // mm
    ArrayList<Point> ellipseSegment
    double rSq

    TubeCutCalculatorExcentric(double rS = 25, double rL = 78, double angle = 30, double excen = 30) {
        super(rS, rL, angle)
        excentricity = excen
        ellipseSegment = ellipseFromTube()
        rSq = largeTubeRadius**2
    }

    /**
     *
     * @param step x resolution for perimeter calculation
     * @return arraylist of points on the ellipse perimeter
     */
    def ellipseFromTube(double step = 0.02) {
        double aSq = a**2
        double bSq = b**2
        double x = -a
        def points = []
        while (x <= a) {
            points.add(new Point(x: x, y: (b * sqrt(1 - x**2 / aSq))))
            x += step
        }
        points
    }

    /**
     * test if a point (from the ellipse perimeter) is inside the circle
     * @param p perimeter point in ellipse central coordinate system
     * @param dx x displacement of ellipse center from circle center
     * @param dy y displacement of ellipse center from circle center
     * @return true, if inside
     */
    def isPointInCircle(Point p, double dx, double dy = excentricity) {
        (p.x + dx)**2 + (p.y + dy)**2 <= rSq
    }

    /**
     * approximate the cutting points on ellipse perimeter that are closest to the circle perimeter, i.e. where the
     * ellipse cuts into the circle
     * @param dx x displacement of ellipse center from circle center
     * @return BorderPoint data structure that holds zero, one or two points where circle and ellipse cut
     */
    def borderPoints(double dx) {
        def bp = new BorderPoints()
        boolean lastUpWasInner, lastLoWasInner
        Point lastUpperPoint, lastLowerPoint
        def cutPoints = []
        if (dx <= largeTubeRadius + a && dx >= -a) {
            // iterate over upper and lower half of ellipse arc simultanously
            int upperCrossings = 0, lowerCrossings = 0
            lastLowerPoint = ellipseSegment[0].clone()
            lastUpperPoint = ellipseSegment[0].clone()
            lastUpWasInner = lastLoWasInner = isPointInCircle(lastUpperPoint, dx, excentricity)
            ellipseSegment.eachWithIndex { Point point, int ix ->
                if (ix > 0) {
                    // first work on upper half of ellipse
                    boolean isInner = isPointInCircle(point, dx, excentricity)
                    // upper arc just crossed circle
                    if (upperCrossings < 2 && (isInner && !lastUpWasInner || !isInner && lastUpWasInner)) {
                        // count crossing
                        upperCrossings++
                        // relative to center of ellipse
                        def x = (point.x + lastUpperPoint.x) / 2
                        def y = (point.y + lastUpperPoint.y) / 2
                        cutPoints << new PointAt(
                                x: x,
                                y: y,
                                arc: largeTubeRadius * asin((y + excentricity) / largeTubeRadius),
                                len: dx * scaleFactor
                        )
                    }
                    lastUpWasInner = isInner
                    lastUpperPoint = point.clone()
                    // then work on lower half of ellipse
                    point.y = -point.y
                    isInner = isPointInCircle(point, dx, excentricity)
                    // lower arc just crossed circle
                    if (lowerCrossings < 2 && (isInner && !lastLoWasInner || !isInner && lastLoWasInner)) {
                        // count crossing
                        lowerCrossings++
                        // relative to center of ellipse
                        def x = (point.x + lastLowerPoint.x) / 2
                        def y = (point.y + lastLowerPoint.y) / 2
                        cutPoints << new PointAt(
                                x: x,
                                y: y,
                                arc: largeTubeRadius * asin((y + excentricity) / largeTubeRadius),
                                len: dx * scaleFactor
                        )
                    }
                    lastLoWasInner = isInner
                    lastLowerPoint = point.clone()
                }
            }
        }
        if (cutPoints.size() == 1) {
            bp.upper = cutPoints[0]
        } else if (cutPoints.size() == 2) {
            if (cutPoints[0].y >= cutPoints[1].y) {
                bp.upper = cutPoints[0]
                bp.lower = cutPoints[1]
            } else {
                bp.upper = cutPoints[1]
                bp.lower = cutPoints[0]
            }
        } else if (cutPoints.size() > 2) {
            println "should never happen!"
        }
        cutPoints.clear()
        bp
    }

    /**
     * calculate all the cutting points between circle and ellipse that result from ellipse stepwise
     * immersing into circle
     * @param step step width, controlling accuracy of approximation
     * @return all the cutting points
     */
    TubeCutExcentric calculate(double step = 0.1) {
        double x0max = largeTubeRadius + a
        double x0min = -a
        double cutmax = x0min, cutmin = x0max
        double ycutmin = largeTubeRadius * PI / 2
        double ycutmax = -ycutmin
        def tubecut = new TubeCutExcentric()
        def tcup = []
        def tclo = []
        def outsideCircle = true
        for (def dx = x0max.round(0); dx > 2*x0min; dx -= step) {
            def bp = borderPoints(dx)
            if (bp.upper) {
                cutmax = max(cutmax, bp.upper.len)
                cutmin = min(cutmin, bp.upper.len)
                ycutmax = max(ycutmax, bp.upper.arc)
                ycutmin = min(ycutmin, bp.upper.arc)
                tcup.add(bp.upper)
            }
            if (bp.lower) {
                cutmax = max(cutmax, bp.lower.len)
                cutmin = min(cutmin, bp.lower.len)
                ycutmax = max(ycutmax, bp.lower.arc)
                ycutmin = min(ycutmin, bp.lower.arc)
                tclo.add(bp.lower)
            }
            if (outsideCircle && (bp.upper || bp.lower)) {
                // just started overlapping
                outsideCircle = false
            }
            if (!outsideCircle && !bp.upper && !bp.lower) {
                // fully inside circle, stop processing
                break
            }
        }
        def cl = (cutmax - cutmin)
        tubecut.cutLength = cl
        tubecut.cutStart = cutmin
        tubecut.cutEnd = cutmax
        tubecut.cutTop = ycutmax
        tubecut.cutBottom = ycutmin
        tubecut.outlines << tcup
        tubecut.outlines << tclo
        tubecut
    }
}