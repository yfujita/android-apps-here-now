package io.github.yfujita.herenow.ui

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import io.github.yfujita.herenow.data.ElevationService
import io.github.yfujita.herenow.data.LocationService
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class MainViewModel(
    private val locationService: LocationService,
    private val elevationService: ElevationService
) : ViewModel() {

    private val _uiState: MutableStateFlow<UiState> = MutableStateFlow(UiState())
    val uiState: StateFlow<UiState> = _uiState.asStateFlow()

    private var locationJob: Job? = null

    private val TAG: String = "HereNowDebug"

    fun startUpdates() {
        if (locationJob?.isActive == true) return

        locationJob = viewModelScope.launch {
            Log.d(TAG, "位置情報の取得を開始します (1分間隔)")
            // Update every 1 minute = 60000ms
            locationService.getLocationUpdates(10000).collectLatest { location ->
                Log.d(TAG, "位置情報取得成功: lat=${location.latitude}, lon=${location.longitude}")
                _uiState.value = _uiState.value.copy(
                    latitude = location.latitude,
                    longitude = location.longitude,
                    locationStatus = "Location Updated" // Debug info or user feedback
                )

                Log.d(TAG, "標高APIへのリクエスト開始...")
                // Fetch elevation
                val elevation: Double? = elevationService.getElevation(location.latitude, location.longitude)
                Log.d(TAG, "標高取得完了: $elevation m")
                _uiState.value = _uiState.value.copy(
                    elevation = elevation,
                    elevationStatus = if (elevation != null) "Success" else "Error/Unknown"
                )
            }
        }
    }

    fun stopUpdates() {
        Log.d(TAG, "位置情報の取得を停止しました")
        locationJob?.cancel()
        locationJob = null
    }
}

data class UiState(
    val latitude: Double? = null,
    val longitude: Double? = null,
    val elevation: Double? = null,
    val locationStatus: String = "Waiting...",
    val elevationStatus: String = "-"
)

class MainViewModelFactory(
    private val locationService: LocationService,
    private val elevationService: ElevationService
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(MainViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return MainViewModel(locationService, elevationService) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
