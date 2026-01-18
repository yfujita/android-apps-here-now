package io.github.yfujita.herenow

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import io.github.yfujita.herenow.ui.theme.HereNowTheme

import androidx.lifecycle.ViewModelProvider
import io.github.yfujita.herenow.data.ElevationService
import io.github.yfujita.herenow.data.LocationService
import io.github.yfujita.herenow.ui.MainScreen
import io.github.yfujita.herenow.ui.MainViewModel
import io.github.yfujita.herenow.ui.MainViewModelFactory

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val locationService = LocationService(applicationContext)
        val elevationService = ElevationService()
        val factory = MainViewModelFactory(locationService, elevationService)
        val viewModel: MainViewModel = ViewModelProvider(this, factory)[MainViewModel::class.java]

        setContent {
            HereNowTheme {
                MainScreen(viewModel = viewModel)
            }
        }
    }
}