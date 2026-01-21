package io.github.yfujita.herenow.ui

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import io.github.yfujita.herenow.domain.model.Result
import io.github.yfujita.herenow.domain.repository.AddressRepository
import io.github.yfujita.herenow.domain.repository.ElevationRepository
import io.github.yfujita.herenow.domain.repository.GravityRepository
import io.github.yfujita.herenow.domain.repository.LocationRepository
import io.github.yfujita.herenow.domain.repository.SensorRepository
import io.github.yfujita.herenow.domain.repository.StationRepository
import io.github.yfujita.herenow.util.AppConstants
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

                    locationRepository.getLocationUpdates(AppConstants.LOCATION_UPDATE_INTERVAL_MS).collectLatest { location ->
                        Log.d(TAG, "Location update: lat=${location.latitude}, lon=${location.longitude}")
                        _uiState.value =
                            _uiState.value.copy(
                                latitude = location.latitude,
                                longitude = location.longitude,
                                locationStatus = "Location Updated",
                                error = null,
                            )

                        Log.d(TAG, "Fetching location details...")
                        val elevationDeferred =
                            async {
                                elevationRepository.getElevation(location.latitude, location.longitude)
                            }
                        val addressDeferred =
                            async {
                                addressRepository.getAddress(location.latitude, location.longitude)
                            }
                        val stationDeferred =
                            async {
                                stationRepository.getNearestStation(location.latitude, location.longitude)
                            }

                        val elevationResult = elevationDeferred.await()
                        val addressResult = addressDeferred.await()
                        val stationResult = stationDeferred.await()

                        val elevation = elevationResult.getOrNull()
                        val address = addressResult.getOrNull()
                        val station = stationResult.getOrNull()

                        Log.d(TAG, "Elevation: ${elevation?.elevation} m")
                        Log.d(TAG, "Address: ${address?.fullAddress}")
                        Log.d(TAG, "Station: ${station?.name}")

                        val gravity =
                            elevation?.let {
                                gravityRepository.calculateGravity(location.latitude, it.elevation)
                            }

                        val errorMessage = buildErrorMessage(elevationResult, addressResult, stationResult)

                        _uiState.value =
                            _uiState.value.copy(
                                elevation = elevation?.elevation,
                                elevationStatus = if (elevation != null) "Success" else "Error/Unknown",
                                address = address?.fullAddress,
                                stationName = station?.name,
                                stationDistance = station?.distance,
                                stationLatitude = station?.latitude,
                                stationLongitude = station?.longitude,
                                gravity = gravity?.gravity,
                                error = errorMessage,
                            )
                    }
                }

            if (pressureJob?.isActive != true) {
                pressureJob =
                    viewModelScope.launch {
                        sensorRepository.getPressureFlow().collectLatest { pressure ->
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

        private fun buildErrorMessage(
            elevationResult: Result<*>,
            addressResult: Result<*>,
            stationResult: Result<*>,
        ): String? {
            val errors = mutableListOf<String>()

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
    val stationLatitude: Double? = null,
    val stationLongitude: Double? = null,
    val error: String? = null,
)
