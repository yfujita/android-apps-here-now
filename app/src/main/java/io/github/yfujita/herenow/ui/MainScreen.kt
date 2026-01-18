package io.github.yfujita.herenow.ui

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import androidx.activity.compose.ManagedActivityResultLauncher
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Divider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.unit.sp
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Place
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Train
import androidx.compose.material.icons.outlined.Map
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.foundation.layout.Row
import androidx.compose.ui.graphics.vector.ImageVector
import android.content.Intent
import android.net.Uri
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.compose.LocalLifecycleOwner

@Composable
fun MainScreen(viewModel: MainViewModel) {
    val uiState: UiState by viewModel.uiState.collectAsState()
    val context: Context = LocalContext.current
    val lifecycleOwner: LifecycleOwner = LocalLifecycleOwner.current
    var hasPermissions: Boolean by remember { mutableStateOf(checkPermissions(context)) }

    val permissionLauncher: ManagedActivityResultLauncher<Array<String>, Map<String, Boolean>> = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        hasPermissions = permissions.values.all { it }
        if (hasPermissions) {
            viewModel.startUpdates()
        }
    }

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                if (checkPermissions(context)) {
                    hasPermissions = true
                    viewModel.startUpdates()
                }
            } else if (event == Lifecycle.Event.ON_PAUSE) {
                viewModel.stopUpdates()
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    // Use a Box to layer the background and content
    Box(modifier = Modifier.fillMaxSize()) {
        AnimatedAuroraBackground()

        Scaffold(
            containerColor = Color.Transparent, // Make Scaffold transparent
            modifier = Modifier.fillMaxSize()
        ) { innerPadding ->
            Column(
                modifier = Modifier
                    .padding(innerPadding)
                    .fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                if (hasPermissions) {
                    Text(
                        text = "HERE NOW",
                        style = MaterialTheme.typography.headlineMedium.copy(
                            color = Color.White,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 2.sp,
                            shadow = Shadow(
                                color = Color.Black.copy(alpha = 0.5f),
                                offset = Offset(0f, 4f),
                                blurRadius = 8f
                            )
                        )
                    )
                    Spacer(modifier = Modifier.height(32.dp))
                    
                    GlassCard(
                        modifier = Modifier
                            .fillMaxWidth(0.9f)
                            .padding(16.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(24.dp).fillMaxWidth(),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            InfoRow(
                                label = "住所",
                                value = uiState.address ?: "住所を取得中...",
                                isHighlight = false, // Address isn't highlighted per design above
                                onMapClick = {
                                    if (uiState.latitude != null && uiState.longitude != null) {
                                        openGoogleMap(context, uiState.latitude!!, uiState.longitude!!, uiState.address ?: "Current Location")
                                    }
                                }
                            )

                            Spacer(modifier = Modifier.height(24.dp))
                            
                            Divider(
                                color = Color.White.copy(alpha = 0.1f),
                                thickness = 1.dp
                            )

                            Spacer(modifier = Modifier.height(24.dp))

                            InfoRow(
                                label = "最寄駅",
                                value = "${uiState.stationName ?: "不明"} (${uiState.stationDistance ?: "-m"})",
                                isHighlight = true,
                                icon = Icons.Default.Train,
                                onMapClick = {
                                    if (uiState.stationLatitude != null && uiState.stationLongitude != null) {
                                        openGoogleMap(context, uiState.stationLatitude!!, uiState.stationLongitude!!, uiState.stationName ?: "Station")
                                    }
                                }
                            )
                            Spacer(modifier = Modifier.height(24.dp))
                            Divider(
                                color = Color.White.copy(alpha = 0.1f),
                                thickness = 1.dp
                            )
                            Spacer(modifier = Modifier.height(24.dp))

                            InfoRow(
                                label = "標高",
                                value = if (uiState.elevation != null) "${uiState.elevation} m" else uiState.elevationStatus,
                                isHighlight = true,
                                icon = Icons.Default.ArrowUpward
                            )

                            Spacer(modifier = Modifier.height(24.dp))

                            Divider(
                                color = Color.White.copy(alpha = 0.1f),
                                thickness = 1.dp
                            )

                            Spacer(modifier = Modifier.height(24.dp))

                            InfoRow(
                                label = "気圧",
                                value = if (uiState.pressure != null) String.format("%.1f hPa", uiState.pressure) else uiState.pressureStatus,
                                isHighlight = true,
                                icon = Icons.Default.Speed
                            )

                            Spacer(modifier = Modifier.height(24.dp))

                            Divider(
                                color = Color.White.copy(alpha = 0.1f),
                                thickness = 1.dp
                            )

                            Spacer(modifier = Modifier.height(24.dp))

                            InfoRow(
                                label = "重力加速度",
                                value = if (uiState.gravity != null) String.format("%.4f m/s²", uiState.gravity) else "-",
                                isHighlight = true,
                                icon = Icons.Default.ArrowDownward
                            )

                            Spacer(modifier = Modifier.height(24.dp))

                            Divider(
                                color = Color.White.copy(alpha = 0.1f),
                                thickness = 1.dp
                            )

                            Spacer(modifier = Modifier.height(24.dp))

                            InfoRow(
                                label = "緯度",
                                value = uiState.latitude?.toString() ?: "Loading...",
                                icon = Icons.Default.Place
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            InfoRow(
                                label = "経度",
                                value = uiState.longitude?.toString() ?: "Loading...",
                                icon = Icons.Default.Place
                            )
                        }
                    }
                    
                } else {
                    GlassCard(
                         modifier = Modifier
                            .fillMaxWidth(0.9f)
                            .padding(16.dp)
                    ) {
                         Column(
                            modifier = Modifier.padding(24.dp).fillMaxWidth(),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                "Permission is required to show elevation.",
                                style = MaterialTheme.typography.bodyLarge.copy(color = Color.White)
                            )
                            Spacer(modifier = Modifier.height(24.dp))
                            GlassButton(
                                onClick = {
                                    permissionLauncher.launch(
                                        arrayOf(
                                            Manifest.permission.ACCESS_FINE_LOCATION,
                                            Manifest.permission.ACCESS_COARSE_LOCATION
                                        )
                                    )
                                },
                                modifier = Modifier.height(50.dp).width(200.dp)
                            ) {
                                Text(
                                    "Grant Permissions",
                                    style = MaterialTheme.typography.labelLarge.copy(
                                        color = Color.White,
                                        fontWeight = androidx.compose.ui.text.font.FontWeight.Bold
                                    )
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun InfoRow(
    label: String, 
    value: String, 
    isHighlight: Boolean = false,
    icon: ImageVector? = null,
    onMapClick: (() -> Unit)? = null
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.padding(4.dp).fillMaxWidth()
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
             modifier = Modifier.fillMaxWidth(), // Fill width to center content
             horizontalArrangement = Arrangement.Center
        ) {
            if (icon != null) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = Color.White.copy(alpha = 0.7f),
                    modifier = Modifier.padding(end = 12.dp)
                        .width(20.dp).height(20.dp)
                )
            }
            
            Column(horizontalAlignment = Alignment.Start) {
                Text(
                    text = label, 
                    style = MaterialTheme.typography.labelSmall.copy(
                        color = Color.White.copy(alpha = 0.5f),
                        letterSpacing = 2.sp,
                        fontFamily = FontFamily.Monospace
                    )
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = value, 
                    style = if (isHighlight) {
                        MaterialTheme.typography.headlineMedium.copy(
                            color = Color.White,
                            fontFamily = FontFamily.Monospace,
                            shadow = Shadow(
                                color = Color(0xFF6366F1).copy(alpha = 0.8f),
                                offset = Offset(0f, 0f),
                                blurRadius = 12f
                            )
                        )
                    } else {
                        MaterialTheme.typography.titleMedium.copy(
                            color = Color.White.copy(alpha = 0.9f),
                            fontFamily = FontFamily.Monospace
                        )
                    }
                )
            }
        }
        
        if (onMapClick != null) {
            Spacer(modifier = Modifier.height(12.dp))
            GlassButton(
                onClick = onMapClick,
                modifier = Modifier
                    .height(36.dp)
            ) {
                Row(
                   verticalAlignment = Alignment.CenterVertically,
                   modifier = Modifier.padding(horizontal = 12.dp)
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Map,
                        contentDescription = "Open Map",
                        tint = Color.White.copy(alpha = 0.9f),
                        modifier = Modifier.width(16.dp).height(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "地図で開く",
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = Color.White.copy(alpha = 0.9f),
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.SansSerif
                        )
                    )
                }
            }
        }
    }
}

fun openGoogleMap(context: Context, lat: Double, lon: Double, label: String) {
    val uri = "geo:0,0?q=$lat,$lon($label)"
    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(uri))
    intent.setPackage("com.google.android.apps.maps")
    if (intent.resolveActivity(context.packageManager) != null) {
        context.startActivity(intent)
    } else {
        // Fallback to browser if Maps app is not available
        val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse("https://www.google.com/maps/search/?api=1&query=$lat,$lon"))
        context.startActivity(browserIntent)
    }
}

fun checkPermissions(context: Context): Boolean {
    return ContextCompat.checkSelfPermission(
        context,
        Manifest.permission.ACCESS_FINE_LOCATION
    ) == PackageManager.PERMISSION_GRANTED
}
