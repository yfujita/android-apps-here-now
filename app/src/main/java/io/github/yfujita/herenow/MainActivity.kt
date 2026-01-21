package io.github.yfujita.herenow

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.hilt.navigation.compose.hiltViewModel
import dagger.hilt.android.AndroidEntryPoint
import io.github.yfujita.herenow.ui.MainScreen
import io.github.yfujita.herenow.ui.MainViewModel
import io.github.yfujita.herenow.ui.theme.HereNowTheme

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            HereNowTheme {
                val viewModel: MainViewModel = hiltViewModel()
                MainScreen(viewModel = viewModel)
            }
        }
    }
}
