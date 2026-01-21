package io.github.yfujita.herenow.data.repository

import android.util.Log
import io.github.yfujita.herenow.data.service.AddressService
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

class AddressRepositoryImplTest {
    private lateinit var addressService: AddressService
    private lateinit var repository: AddressRepositoryImpl

    @Before
    fun setup() {
        mockkStatic(Log::class)
        every { Log.e(any(), any(), any()) } returns 0
        addressService = mockk()
        repository = AddressRepositoryImpl(addressService)
    }

    @Test
    fun `getAddress returns success with address data`() =
        runTest {
            // Arrange
            val latitude = 35.6812
            val longitude = 139.7671
            val expectedAddress = "東京都千代田区丸の内"
            coEvery { addressService.getAddress(latitude, longitude) } returns expectedAddress

            // Act
            val result = repository.getAddress(latitude, longitude)

            // Assert
            assertTrue(result is Result.Success)
            assertEquals(expectedAddress, (result as Result.Success).data?.fullAddress)
        }

    @Test
    fun `getAddress returns success with null when service returns null`() =
        runTest {
            // Arrange
            val latitude = 35.6812
            val longitude = 139.7671
            coEvery { addressService.getAddress(latitude, longitude) } returns null

            // Act
            val result = repository.getAddress(latitude, longitude)

            // Assert
            assertTrue(result is Result.Success)
            assertNull((result as Result.Success).data)
        }

    @Test
    fun `getAddress returns error when service throws exception`() =
        runTest {
            // Arrange
            val latitude = 35.6812
            val longitude = 139.7671
            coEvery { addressService.getAddress(latitude, longitude) } throws RuntimeException("Network error")

            // Act
            val result = repository.getAddress(latitude, longitude)

            // Assert
            assertTrue(result is Result.Error)
            assertEquals("住所の取得に失敗しました", (result as Result.Error).message)
        }
}
