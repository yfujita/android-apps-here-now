package io.github.yfujita.herenow.ui

import android.util.Log
import app.cash.turbine.test
import io.github.yfujita.herenow.domain.model.AddressData
import io.github.yfujita.herenow.domain.model.ElevationData
import io.github.yfujita.herenow.domain.model.GravityData
import io.github.yfujita.herenow.domain.model.LocationData
import io.github.yfujita.herenow.domain.model.PressureData
import io.github.yfujita.herenow.domain.model.Result
import io.github.yfujita.herenow.domain.model.StationData
import io.github.yfujita.herenow.domain.repository.AddressRepository
import io.github.yfujita.herenow.domain.repository.ElevationRepository
import io.github.yfujita.herenow.domain.repository.GravityRepository
import io.github.yfujita.herenow.domain.repository.LocationRepository
import io.github.yfujita.herenow.domain.repository.SensorRepository
import io.github.yfujita.herenow.domain.repository.StationRepository
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class MainViewModelTest {
    private lateinit var locationRepository: LocationRepository
    private lateinit var elevationRepository: ElevationRepository
    private lateinit var addressRepository: AddressRepository
    private lateinit var gravityRepository: GravityRepository
    private lateinit var sensorRepository: SensorRepository
    private lateinit var stationRepository: StationRepository
    private lateinit var viewModel: MainViewModel

    private val testDispatcher = StandardTestDispatcher()

    @Before
    fun setup() {
        mockkStatic(Log::class)
        every { Log.d(any(), any()) } returns 0
        every { Log.e(any(), any()) } returns 0
        every { Log.e(any(), any(), any()) } returns 0

        Dispatchers.setMain(testDispatcher)
        locationRepository = mockk()
        elevationRepository = mockk()
        addressRepository = mockk()
        gravityRepository = mockk()
        sensorRepository = mockk()
        stationRepository = mockk()

        every { sensorRepository.getPressureFlow() } returns flowOf(PressureData(1013.25f))

        viewModel =
            MainViewModel(
                locationRepository = locationRepository,
                elevationRepository = elevationRepository,
                addressRepository = addressRepository,
                gravityRepository = gravityRepository,
                sensorRepository = sensorRepository,
                stationRepository = stationRepository,
            )
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `initial state is correct`() =
        runTest {
            viewModel.uiState.test {
                val state = awaitItem()
                assertNull(state.latitude)
                assertNull(state.longitude)
                assertNull(state.elevation)
                assertNull(state.address)
                assertEquals("Waiting...", state.locationStatus)
            }
        }

    @Test
    fun `startUpdates updates location state`() =
        runTest {
            // Arrange
            val location = LocationData(latitude = 35.6812, longitude = 139.7671)
            every { locationRepository.getLocationUpdates(any()) } returns flowOf(location)
            coEvery { elevationRepository.getElevation(any(), any()) } returns
                Result.success(
                    ElevationData(40.5),
                )
            coEvery { addressRepository.getAddress(any(), any()) } returns
                Result.success(
                    AddressData("東京都", null, null, null),
                )
            coEvery { stationRepository.getNearestStation(any(), any()) } returns
                Result.success(
                    StationData("東京駅", "500m", "JR", 35.6812, 139.7671),
                )
            every { gravityRepository.calculateGravity(any(), any()) } returns GravityData(9.8)

            // Act
            viewModel.startUpdates()
            advanceUntilIdle()

            // Assert
            viewModel.uiState.test {
                val state = awaitItem()
                assertEquals(35.6812, state.latitude!!, 0.0001)
                assertEquals(139.7671, state.longitude!!, 0.0001)
                assertEquals(40.5, state.elevation!!, 0.0001)
                assertEquals("東京都", state.address)
                assertEquals("東京駅", state.stationName)
                assertEquals(9.8, state.gravity!!, 0.0001)
            }
        }

    @Test
    fun `error state is set when API call fails`() =
        runTest {
            // Arrange
            val location = LocationData(latitude = 35.6812, longitude = 139.7671)
            every { locationRepository.getLocationUpdates(any()) } returns flowOf(location)
            coEvery { elevationRepository.getElevation(any(), any()) } returns Result.error("標高エラー")
            coEvery { addressRepository.getAddress(any(), any()) } returns Result.success(null)
            coEvery { stationRepository.getNearestStation(any(), any()) } returns Result.success(null)

            // Act
            viewModel.startUpdates()
            advanceUntilIdle()

            // Assert
            viewModel.uiState.test {
                val state = awaitItem()
                assertNotNull(state.error)
                assertEquals("標高エラー", state.error)
            }
        }

    @Test
    fun `clearError clears error state`() =
        runTest {
            // Arrange
            val location = LocationData(latitude = 35.6812, longitude = 139.7671)
            every { locationRepository.getLocationUpdates(any()) } returns flowOf(location)
            coEvery { elevationRepository.getElevation(any(), any()) } returns Result.error("エラー")
            coEvery { addressRepository.getAddress(any(), any()) } returns Result.success(null)
            coEvery { stationRepository.getNearestStation(any(), any()) } returns Result.success(null)

            viewModel.startUpdates()
            advanceUntilIdle()

            // Act
            viewModel.clearError()

            // Assert
            viewModel.uiState.test {
                val state = awaitItem()
                assertNull(state.error)
            }
        }

    @Test
    fun `pressure is updated from sensor repository`() =
        runTest {
            // Arrange
            every { sensorRepository.getPressureFlow() } returns flowOf(PressureData(1020.0f))
            val location = LocationData(latitude = 35.6812, longitude = 139.7671)
            every { locationRepository.getLocationUpdates(any()) } returns flowOf(location)
            coEvery { elevationRepository.getElevation(any(), any()) } returns Result.success(null)
            coEvery { addressRepository.getAddress(any(), any()) } returns Result.success(null)
            coEvery { stationRepository.getNearestStation(any(), any()) } returns Result.success(null)

            // Recreate ViewModel with new mock
            viewModel =
                MainViewModel(
                    locationRepository = locationRepository,
                    elevationRepository = elevationRepository,
                    addressRepository = addressRepository,
                    gravityRepository = gravityRepository,
                    sensorRepository = sensorRepository,
                    stationRepository = stationRepository,
                )

            // Act
            viewModel.startUpdates()
            advanceUntilIdle()

            // Assert
            viewModel.uiState.test {
                val state = awaitItem()
                assertEquals(1020.0f, state.pressure!!, 0.0001f)
            }
        }
}
