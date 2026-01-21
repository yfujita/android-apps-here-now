package io.github.yfujita.herenow.data.repository

import android.util.Log
import io.github.yfujita.herenow.data.service.Station
import io.github.yfujita.herenow.data.service.StationService
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

class StationRepositoryImplTest {
    private lateinit var stationService: StationService
    private lateinit var repository: StationRepositoryImpl

    @Before
    fun setup() {
        mockkStatic(Log::class)
        every { Log.e(any(), any(), any()) } returns 0
        stationService = mockk()
        repository = StationRepositoryImpl(stationService)
    }

    @Test
    fun `getNearestStation returns success with station data`() =
        runTest {
            // Arrange
            val latitude = 35.6812
            val longitude = 139.7671
            val station =
                Station(
                    name = "東京駅",
                    distance = "500m",
                    line = "JR山手線",
                    x = 139.7671,
                    y = 35.6812,
                )
            coEvery { stationService.getNearestStation(latitude, longitude) } returns station

            // Act
            val result = repository.getNearestStation(latitude, longitude)

            // Assert
            assertTrue(result is Result.Success)
            val data = (result as Result.Success).data
            assertEquals("東京駅", data?.name)
            assertEquals("500m", data?.distance)
            assertEquals("JR山手線", data?.line)
        }

    @Test
    fun `getNearestStation returns success with null when service returns null`() =
        runTest {
            // Arrange
            val latitude = 35.6812
            val longitude = 139.7671
            coEvery { stationService.getNearestStation(latitude, longitude) } returns null

            // Act
            val result = repository.getNearestStation(latitude, longitude)

            // Assert
            assertTrue(result is Result.Success)
            assertNull((result as Result.Success).data)
        }

    @Test
    fun `getNearestStation returns error when service throws exception`() =
        runTest {
            // Arrange
            val latitude = 35.6812
            val longitude = 139.7671
            coEvery { stationService.getNearestStation(latitude, longitude) } throws RuntimeException("Network error")

            // Act
            val result = repository.getNearestStation(latitude, longitude)

            // Assert
            assertTrue(result is Result.Error)
            assertEquals("最寄り駅の取得に失敗しました", (result as Result.Error).message)
        }
}
