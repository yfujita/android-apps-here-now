package io.github.yfujita.herenow.data.service

import io.github.yfujita.herenow.util.GravityConstants
import kotlin.math.pow
import kotlin.math.sin

class GravityService {
    /**
     * Calculates the gravitational acceleration at a given latitude and elevation.
     *
     * Formula used:
     * 1. GRS80 Normal Gravity (IGF 1980 series expansion):
     *    g0 = 9.780327 * (1 + 0.0053024 * sin^2(phi) - 0.0000058 * sin^2(2*phi))
     * 2. Free-Air Correction:
     *    FAC = -3.086e-6 * h
     *
     * @param latitude Latitude in degrees.
     * @param elevation Elevation in meters.
     * @return Gravitational acceleration in m/s^2.
     */
    fun calculateGravity(
        latitude: Double,
        elevation: Double,
    ): Double {
        val phi = Math.toRadians(latitude)
        val sinPhi = sin(phi)
        val sin2Phi = sin(2 * phi)

        val g0 =
            GravityConstants.GRS80_BASE_GRAVITY * (
                1 + GravityConstants.GRS80_FIRST_CORRECTION * sinPhi.pow(2) -
                    GravityConstants.GRS80_SECOND_CORRECTION * sin2Phi.pow(2)
            )

        val fac = GravityConstants.FREE_AIR_CORRECTION * elevation

        return g0 + fac
    }
}
