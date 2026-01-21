package io.github.yfujita.herenow.data.repository

import android.util.Log
import io.github.yfujita.herenow.data.service.ElevationService
import io.github.yfujita.herenow.domain.model.Result
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class ElevationRepositoryImplTest {
    private lateinit var elevationService: ElevationService
    private lateinit var repository: ElevationRepositoryImpl

    @Before
    fun setup() {
        mockkStatic(Log::class)
        every { Log.e(any(), any(), any()) } returns 0
        elevationService = mockk()
        repository = ElevationRepositoryImpl(elevationService)
    }

    @Test
    fun `getElevation returns success with elevation data`() =
        runTest {
            // Arrange
            val latitude = 35.6812
            val longitude = 139.7671
            val expectedElevation = 40.5
            coEvery { elevationService.getElevation(latitude, longitude) } returns expectedElevation

            // Act
            val result = repository.getElevation(latitude, longitude)

            // Assert
            assertTrue(result is Result.Success)
            assertEquals(expectedElevation, (result as Result.Success).data?.elevation)
        }

    @Test
    fun `getElevation returns success with null when service returns null`() =
        runTest {
            // Arrange
            val latitude = 35.6812
            val longitude = 139.7671
            coEvery { elevationService.getElevation(latitude, longitude) } returns null

            // Act
            val result = repository.getElevation(latitude, longitude)

            // Assert
            assertTrue(result is Result.Success)
            assertNull((result as Result.Success).data)
        }

    @Test
    fun `getElevation returns error when service throws exception`() =
        runTest {
            // Arrange
            val latitude = 35.6812
            val longitude = 139.7671
            coEvery { elevationService.getElevation(latitude, longitude) } throws RuntimeException("Network error")

            // Act
            val result = repository.getElevation(latitude, longitude)

            // Assert
            assertTrue(result is Result.Error)
            assertEquals("標高データの取得に失敗しました", (result as Result.Error).message)
        }
}
