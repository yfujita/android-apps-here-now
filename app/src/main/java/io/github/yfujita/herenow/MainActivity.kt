package io.github.yfujita.herenow

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import io.github.yfujita.herenow.ui.theme.HereNowTheme

import androidx.lifecycle.ViewModelProvider
import io.github.yfujita.herenow.data.AddressService
import io.github.yfujita.herenow.data.ElevationService
import io.github.yfujita.herenow.data.GravityService
import io.github.yfujita.herenow.data.LocationService
import io.github.yfujita.herenow.data.SensorService
import io.github.yfujita.herenow.data.StationService
import io.github.yfujita.herenow.ui.MainScreen
import io.github.yfujita.herenow.ui.MainViewModel
import io.github.yfujita.herenow.ui.MainViewModelFactory

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val locationService = LocationService(applicationContext)
        val elevationService = ElevationService()
        val addressService = AddressService()
        val gravityService = GravityService()
        val sensorService = SensorService(applicationContext)
        val stationService = StationService()
        val factory = MainViewModelFactory(locationService, elevationService, addressService, gravityService, sensorService, stationService)
        val viewModel: MainViewModel = ViewModelProvider(this, factory)[MainViewModel::class.java]

        setContent {
            HereNowTheme {
                MainScreen(viewModel = viewModel)
            }
        }
    }
}