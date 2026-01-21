package io.github.yfujita.herenow.ui

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import io.github.yfujita.herenow.domain.model.AddressData
import io.github.yfujita.herenow.domain.model.ElevationData
import io.github.yfujita.herenow.domain.model.GravityData
import io.github.yfujita.herenow.domain.model.PressureData
import io.github.yfujita.herenow.domain.model.Result
import io.github.yfujita.herenow.domain.model.StationData
import io.github.yfujita.herenow.domain.repository.AddressRepository
import io.github.yfujita.herenow.domain.repository.ElevationRepository
import io.github.yfujita.herenow.domain.repository.GravityRepository
import io.github.yfujita.herenow.domain.repository.LocationRepository
import io.github.yfujita.herenow.domain.repository.SensorRepository
import io.github.yfujita.herenow.domain.repository.StationRepository
import io.github.yfujita.herenow.util.AppConstants
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.Job
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MainViewModel
    @Inject
    constructor(
        private val locationRepository: LocationRepository,
        private val elevationRepository: ElevationRepository,
        private val addressRepository: AddressRepository,
        private val gravityRepository: GravityRepository,
        private val sensorRepository: SensorRepository,
        private val stationRepository: StationRepository,
    ) : ViewModel() {
        private val _uiState: MutableStateFlow<UiState> = MutableStateFlow(UiState())
        val uiState: StateFlow<UiState> = _uiState.asStateFlow()

        private var locationJob: Job? = null
        private var pressureJob: Job? = null

        companion object {
            private const val TAG = "HereNowDebug"
        }

        fun startUpdates() {
            if (locationJob?.isActive == true) return

            locationJob =
                viewModelScope.launch {
                    Log.d(TAG, "Start location updates (1 minute interval)")

                    // Fetch location updates with explicit interval
                    locationRepository.getLocationUpdates(AppConstants.LOCATION_UPDATE_INTERVAL_MS).collectLatest {
                            location: io.github.yfujita.herenow.domain.model.LocationData ->
                        Log.d(TAG, "Location update: lat=${location.latitude}, lon=${location.longitude}")
                        _uiState.value =
                            _uiState.value.copy(
                                latitude = location.latitude,
                                longitude = location.longitude,
                                locationStatus = "Location Updated",
                                error = null,
                            )

                        Log.d(TAG, "Fetching location details...")
                        // Fetch details concurrently for better performance
                        val elevationDeferred: Deferred<Result<ElevationData?>> =
                            async {
                                elevationRepository.getElevation(location.latitude, location.longitude)
                            }
                        val addressDeferred: Deferred<Result<AddressData?>> =
                            async {
                                addressRepository.getAddress(location.latitude, location.longitude)
                            }
                        val stationsDeferred: Deferred<Result<List<StationData>>> =
                            async {
                                stationRepository.getNearestStations(location.latitude, location.longitude)
                            }

                        val elevationResult: Result<ElevationData?> = elevationDeferred.await()
                        val addressResult: Result<AddressData?> = addressDeferred.await()
                        val stationsResult: Result<List<StationData>> = stationsDeferred.await()

                        val elevation: ElevationData? = elevationResult.getOrNull()
                        val address: AddressData? = addressResult.getOrNull()
                        val stations: List<StationData> = stationsResult.getOrNull() ?: emptyList()
                        val nearestStation: StationData? = stations.firstOrNull()

                        Log.d(TAG, "Elevation: ${elevation?.elevation} m")
                        Log.d(TAG, "Address: ${address?.fullAddress}")
                        Log.d(TAG, "Stations: ${stations.size} found, nearest: ${nearestStation?.name}")

                        // Calculate gravity if elevation is available
                        val gravity: GravityData? =
                            elevation?.let { elevationData: ElevationData ->
                                gravityRepository.calculateGravity(location.latitude, elevationData.elevation)
                            }

                        val errorMessage: String? = buildErrorMessage(elevationResult, addressResult, stationsResult)

                        _uiState.value =
                            _uiState.value.copy(
                                elevation = elevation?.elevation,
                                elevationStatus = if (elevation != null) "Success" else "Error/Unknown",
                                address = address?.fullAddress,
                                stationName = nearestStation?.name,
                                stationDistance = nearestStation?.distance,
                                stationLine = nearestStation?.line,
                                stationLatitude = nearestStation?.latitude,
                                stationLongitude = nearestStation?.longitude,
                                stations = stations,
                                isStationListExpanded = false,
                                gravity = gravity?.gravity,
                                error = errorMessage,
                            )
                    }
                }

            if (pressureJob?.isActive != true) {
                pressureJob =
                    viewModelScope.launch {
                        // Observe pressure sensor updates
                        sensorRepository.getPressureFlow().collectLatest { pressure: PressureData? ->
                            _uiState.value =
                                _uiState.value.copy(
                                    pressure = pressure?.pressure,
                                    pressureStatus = if (pressure != null) "Success" else "No Sensor",
                                )
                        }
                    }
            }
        }

        fun stopUpdates() {
            Log.d(TAG, "Location updates stopped")
            locationJob?.cancel()
            locationJob = null
            pressureJob?.cancel()
            pressureJob = null
        }

        fun clearError() {
            _uiState.value = _uiState.value.copy(error = null)
        }

        fun toggleStationListExpanded() {
            _uiState.value =
                _uiState.value.copy(
                    isStationListExpanded = !_uiState.value.isStationListExpanded,
                )
        }

        private fun buildErrorMessage(
            elevationResult: Result<*>,
            addressResult: Result<*>,
            stationResult: Result<*>,
        ): String? {
            val errors: MutableList<String> = mutableListOf()

            if (elevationResult is Result.Error) {
                errors.add(elevationResult.message)
            }
            if (addressResult is Result.Error) {
                errors.add(addressResult.message)
            }
            if (stationResult is Result.Error) {
                errors.add(stationResult.message)
            }

            return if (errors.isNotEmpty()) errors.joinToString("\n") else null
        }
    }

data class UiState(
    val latitude: Double? = null,
    val longitude: Double? = null,
    val elevation: Double? = null,
    val address: String? = null,
    val gravity: Double? = null,
    val locationStatus: String = "Waiting...",
    val elevationStatus: String = "-",
    val pressure: Float? = null,
    val pressureStatus: String = "-",
    val stationName: String? = null,
    val stationDistance: String? = null,
    val stationLine: String? = null,
    val stationLatitude: Double? = null,
    val stationLongitude: Double? = null,
    val stations: List<StationData> = emptyList(),
    val isStationListExpanded: Boolean = false,
    val error: String? = null,
)
