package io.github.yfujita.herenow.data

import org.junit.Assert.assertEquals
import org.junit.Test

class GravityServiceTest {

    private val gravityService = GravityService()

    @Test
    fun calculateGravity_equator_seaLevel() {
        val latitude = 0.0
        val elevation = 0.0
        val expected = 9.780327
        val result = gravityService.calculateGravity(latitude, elevation)
        assertEquals(expected, result, 0.000001)
    }

    @Test
    fun calculateGravity_standard_45deg_seaLevel() {
        // At 45 degrees, sin^2(45) = 0.5, sin(90) = 1
        // g0 = 9.780327 * (1 + 0.0053024 * 0.5 - 0.0000058 * 1)
        // g0 = 9.780327 * (1 + 0.0026512 - 0.0000058)
        // g0 = 9.780327 * 1.0026454
        // g0 ≈ 9.806199...
        
        val latitude = 45.0
        val elevation = 0.0
        val result = gravityService.calculateGravity(latitude, elevation)
        
        // Manual calculation check:
        // 9.780327 * (1 + 0.0053024 * 0.5 - 0.0000058 * 1.0) = 9.8061992 (approx)
        assertEquals(9.806199, result, 0.0001)
    }

    @Test
    fun calculateGravity_elevation_correction() {
        val latitude = 0.0
        val elevation = 1000.0 // 1000m
        // FAC = -3.086e-6 * 1000 = -0.003086
        // g = 9.780327 - 0.003086 = 9.777241
        
        val result = gravityService.calculateGravity(latitude, elevation)
        assertEquals(9.777241, result, 0.000001)
    }
}
