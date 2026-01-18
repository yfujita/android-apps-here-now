package io.github.yfujita.herenow.ui

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import io.github.yfujita.herenow.data.AddressService
import io.github.yfujita.herenow.data.ElevationService
import io.github.yfujita.herenow.data.GravityService
import io.github.yfujita.herenow.data.LocationService
import io.github.yfujita.herenow.data.SensorService
import io.github.yfujita.herenow.data.StationService
import kotlinx.coroutines.Job
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class MainViewModel(
    private val locationService: LocationService,
    private val elevationService: ElevationService,
    private val addressService: AddressService,
    private val gravityService: GravityService,
    private val sensorService: SensorService,
    private val stationService: StationService
) : ViewModel() {

    private val _uiState: MutableStateFlow<UiState> = MutableStateFlow(UiState())
    val uiState: StateFlow<UiState> = _uiState.asStateFlow()

    private var locationJob: Job? = null
    private var pressureJob: Job? = null

    private val TAG: String = "HereNowDebug"

    companion object {
        private const val UPDATE_INTERVAL_MS: Long = 60000L
    }

    fun startUpdates() {
        if (locationJob?.isActive == true) return

        locationJob = viewModelScope.launch {
            Log.d(TAG, "Start location updates (1 minute interval)")
            // Update every 1 minute = 60000ms
            locationService.getLocationUpdates(UPDATE_INTERVAL_MS).collectLatest { location ->
                Log.d(TAG, "Location update success: lat=${location.latitude}, lon=${location.longitude}")
                _uiState.value = _uiState.value.copy(
                    latitude = location.latitude,
                    longitude = location.longitude,
                    locationStatus = "Location Updated" // Debug info or user feedback
                )

                Log.d(TAG, "Start get location infos")
                Log.d(TAG, "Start requesting elevation API...")
                val elevationDeferred = async { elevationService.getElevation(location.latitude, location.longitude) }
                Log.d(TAG, "Start requesting address API...")
                val addressDeferred = async { addressService.getAddress(location.latitude, location.longitude) }
                Log.d(TAG, "Start requesting station API...")
                val stationDeferred = async { stationService.getNearestStation(location.latitude, location.longitude) }

                val elevation: Double? = elevationDeferred.await()
                val address: String? = addressDeferred.await()
                val station = stationDeferred.await()
                Log.d(TAG, "Elevation fetch complete: $elevation m")
                Log.d(TAG, "Address fetch complete: $address")
                Log.d(TAG, "Station fetch complete: ${station?.name}")

                _uiState.value = _uiState.value.copy(
                    elevation = elevation,
                    elevationStatus = if (elevation != null) "Success" else "Error/Unknown",
                    address = address,
                    stationName = station?.name,
                    stationDistance = station?.distance,
                    stationLatitude = station?.y,
                    stationLongitude = station?.x,
                    gravity = if (elevation != null) gravityService.calculateGravity(location.latitude, elevation) else null
                )
            }
    }

        if (pressureJob?.isActive != true) {
            pressureJob = viewModelScope.launch {
                sensorService.getPressureFlow().collectLatest { pressure ->
                    _uiState.value = _uiState.value.copy(
                        pressure = pressure,
                        pressureStatus = if (pressure != null) "Success" else "No Sensor"
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
    val stationLongitude: Double? = null
)

class MainViewModelFactory(
    private val locationService: LocationService,
    private val elevationService: ElevationService,
    private val addressService: AddressService,
    private val gravityService: GravityService,
    private val sensorService: SensorService,
    private val stationService: StationService
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(MainViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return MainViewModel(locationService, elevationService, addressService, gravityService, sensorService, stationService) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
