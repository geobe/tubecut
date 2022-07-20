package de.geobe.tubecut.calc

import static java.lang.Math.PI
import static java.lang.Math.asin
import static java.lang.Math.cos
import static java.lang.Math.floor
import static java.lang.Math.sin
import static java.lang.Math.sqrt
import static java.lang.Math.sqrt
import static java.lang.Math.tan

/**
 * calculate outline of the hole where the smaller tube goes centrally into the
 * larger tube.
 */
class TubeCutCalculatorCentric extends TubeCutCalculator {

    /**
     * Calculate an ellipse (outline of inclined smaller tube) stepwise entering
     * into a circle (outline of larger tube).
     * @param step stepwidth in mm
     * @return
     */
    TubeCutCentric calculate(double step = 1.0) {
        // some helper variables
        // semi-major- and -minor-axis of small tube
//        def alpha = intersectionAngle * PI / 180.0
//        def a = smallTubeRadius / cos(alpha)
//        def b = smallTubeRadius
        // parameters used in quadratic equation
//        def aSq = a**2
//        def bSq = b**2
//        def rLSq = largeTubeRadius**2
        def epsilon = bSq - aSq
        def xi = bSq / epsilon
        def distMax = largeTubeRadius + a   // outlines just touch
        def distMin = largeTubeRadius - a   // tube fully inside
//        def lCut = 2 * smallTubeRadius / sin(alpha) // overall length of cut in large tube
//        def scaleFactor = 1.0 / tan(alpha)      // scale depth to length with cot(alpha)
        // calculate result

        // d distance between centers of ellipse and circle
        def intersect = { double d ->
            // xi*d+WURZEL(d^2*(xi^2-xi)+aSq*xi-rSq*aSq/epsilon)
            def x = d * xi + sqrt(d**2 * (xi**2 - xi) + aSq * xi - rLSq * aSq / epsilon)
            def y = sqrt(rLSq - x**2)
            def arc = largeTubeRadius * asin(y / largeTubeRadius)
            def len = scaleFactor * (distMax - d)
            new PointAt(x: x, y: y, arc: arc, len: len)
        }
        TubeCutCentric res = new TubeCutCentric()
        res.cutLength = 2.0 * smallTubeRadius / sin(alpha)
        res.outline.add(intersect(distMax))
        for (def dist = floor(distMax); dist > distMin; dist -= step) {
            res.outline.add(intersect(dist))
        }
        res.outline.add(intersect(distMin))
        return res
    }

    static void main(String[] args) {
        TubeCutCentric tc = new TubeCutCalculatorCentric().calculate()
        println("cutLength: ${sprintf('%6.2f', tc.cutLength)}")
        tc.outline.each {
            println(it)
        }
    }
}
