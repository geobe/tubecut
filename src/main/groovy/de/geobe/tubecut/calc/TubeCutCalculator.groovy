package de.geobe.tubecut.calc

import static java.lang.Math.*

/**
 * Base class providing commonly used parameters for calculation of tube cut
 */
abstract class TubeCutCalculator {
    double smallTubeRadius //mm
    double largeTubeRadius // mm
    double intersectionAngle // degrees
    double alpha // angle in radians
    double a // ellipse semi-major axis
    double b // ellipse semi-minor axis
    double aSq
    double bSq
    double rLSq
    double scaleFactor

    TubeCutCalculator(double rS = 25, rL = 78, double angle = 30) {
        smallTubeRadius = rS
        largeTubeRadius = rL
        intersectionAngle = angle
        alpha = intersectionAngle * PI / 180.0
        a = smallTubeRadius / cos(alpha)
        b = smallTubeRadius
        aSq = a**2
        bSq = b**2
        rLSq = largeTubeRadius**2
        scaleFactor = 1.0 / tan(alpha)
    }
}

