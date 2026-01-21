package io.github.yfujita.herenow.ui

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import androidx.activity.compose.ManagedActivityResultLauncher
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Place
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Train
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.outlined.ExpandLess
import androidx.compose.material.icons.outlined.ExpandMore
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.Map
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import io.github.yfujita.herenow.R

@Composable
fun MainScreen(viewModel: MainViewModel) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    var hasPermissions by remember { mutableStateOf(checkPermissions(context)) }

    val permissionLauncher: ManagedActivityResultLauncher<Array<String>, Map<String, Boolean>> =
        rememberLauncherForActivityResult(
            contract = ActivityResultContracts.RequestMultiplePermissions(),
        ) { permissions ->
            hasPermissions = permissions.values.all { it }
            if (hasPermissions) {
                viewModel.startUpdates()
            }
        }

    DisposableEffect(lifecycleOwner) {
        val observer =
            LifecycleEventObserver { _, event ->
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

    Box(modifier = Modifier.fillMaxSize()) {
        AnimatedAuroraBackground()

        Scaffold(
            containerColor = Color.Transparent,
            modifier = Modifier.fillMaxSize(),
        ) { innerPadding ->
            Box(
                modifier =
                    Modifier
                        .padding(innerPadding)
                        .fillMaxSize(),
            ) {
                Column(
                    modifier =
                        Modifier
                            .fillMaxSize()
                            .verticalScroll(rememberScrollState()),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center,
                ) {
                    if (hasPermissions) {
                        MainContent(
                            uiState = uiState,
                            context = context,
                            onToggleStationList = { viewModel.toggleStationListExpanded() },
                        )
                    } else {
                        PermissionRequestCard(
                            onRequestPermission = {
                                permissionLauncher.launch(
                                    arrayOf(
                                        Manifest.permission.ACCESS_FINE_LOCATION,
                                        Manifest.permission.ACCESS_COARSE_LOCATION,
                                    ),
                                )
                            },
                        )
                    }
                }

                AnimatedVisibility(
                    visible = uiState.error != null,
                    enter = slideInVertically(initialOffsetY = { it }) + fadeIn(),
                    exit = slideOutVertically(targetOffsetY = { it }) + fadeOut(),
                    modifier = Modifier.align(Alignment.BottomCenter),
                ) {
                    ErrorBanner(
                        message = uiState.error ?: "",
                        onDismiss = { viewModel.clearError() },
                    )
                }
            }
        }
    }
}

@Composable
private fun MainContent(
    uiState: UiState,
    context: Context,
    onToggleStationList: () -> Unit,
) {
    val appTitleDescription = stringResource(R.string.app_title_description)
    val labelAddress = stringResource(R.string.label_address)
    val labelStation = stringResource(R.string.label_nearest_station)
    val labelElevation = stringResource(R.string.label_elevation)
    val labelPressure = stringResource(R.string.label_pressure)
    val labelGravity = stringResource(R.string.label_gravity)
    val labelLatitude = stringResource(R.string.label_latitude)
    val labelLongitude = stringResource(R.string.label_longitude)
    val statusLoading = stringResource(R.string.status_loading)
    val statusLoadingAddress = stringResource(R.string.status_loading_address)
    val statusUnknown = stringResource(R.string.status_unknown)
    var showCreditDialog by remember { mutableStateOf(false) }

    // Title
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center,
        modifier = Modifier.fillMaxWidth(),
    ) {
        Text(
            text = stringResource(R.string.app_title),
            style =
                MaterialTheme.typography.headlineMedium.copy(
                    color = Color.White,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 2.sp,
                    shadow =
                        Shadow(
                            color = Color.Black.copy(alpha = 0.5f),
                            offset = Offset(0f, 4f),
                            blurRadius = 8f,
                        ),
                ),
            modifier =
                Modifier.semantics {
                    contentDescription = appTitleDescription
                },
        )
        Spacer(modifier = Modifier.width(8.dp))

        // Info Button
        IconButton(onClick = { showCreditDialog = true }) {
            Icon(
                imageVector = Icons.Outlined.Info,
                contentDescription = "クレジット情報の表示",
                tint = Color.White.copy(alpha = 0.7f),
            )
        }
    }

    // Credit Dialogs
    if (showCreditDialog) {
        AlertDialog(
            onDismissRequest = { showCreditDialog = false },
            title = { Text("クレジット") },
            text = {
                Column {
                    Text("本アプリは以下のデータを利用しています。")
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("出典：「位置参照情報ダウンロードサービス」（国土交通省）を加工して作成")
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("HeartRails Geo API / Express")
                    Text("国土地理院 標高API")
                }
            },
            confirmButton = {
                TextButton(onClick = { showCreditDialog = false }) {
                    Text("閉じる")
                }
            },
            containerColor = Color(0xFF1E293B),
            titleContentColor = Color.White,
            textContentColor = Color.White.copy(alpha = 0.9f),
        )
    }

    Spacer(modifier = Modifier.height(32.dp))

    GlassCard(
        modifier =
            Modifier
                .fillMaxWidth(0.9f)
                .padding(16.dp),
    ) {
        Column(
            modifier =
                Modifier
                    .padding(24.dp)
                    .fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            InfoRow(
                label = labelAddress,
                value = uiState.address ?: statusLoadingAddress,
                isHighlight = false,
                onMapClick = {
                    if (uiState.latitude != null && uiState.longitude != null) {
                        openGoogleMap(
                            context,
                            uiState.latitude!!,
                            uiState.longitude!!,
                            uiState.address ?: "Current Location",
                        )
                    }
                },
            )

            InfoDivider()

            InfoRow(
                label =
                    if (uiState.stationLine != null) {
                        "$labelStation - ${uiState.stationLine}"
                    } else {
                        labelStation
                    },
                value = "${uiState.stationName ?: statusUnknown} (${uiState.stationDistance ?: "-m"})",
                isHighlight = true,
                icon = Icons.Default.Train,
                onMapClick = {
                    if (uiState.stationLatitude != null && uiState.stationLongitude != null) {
                        openGoogleMap(
                            context,
                            uiState.stationLatitude!!,
                            uiState.stationLongitude!!,
                            uiState.stationName ?: "Station",
                        )
                    }
                },
            )

            // 他の駅を見るボタン（2件以上ある場合のみ表示）
            if (uiState.stations.size > 1) {
                Spacer(modifier = Modifier.height(16.dp))

                if (!uiState.isStationListExpanded) {
                    GlassButton(
                        onClick = onToggleStationList,
                        modifier = Modifier.height(40.dp),
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 16.dp),
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.ExpandMore,
                                contentDescription = null,
                                tint = Color.White.copy(alpha = 0.9f),
                                modifier =
                                    Modifier
                                        .width(18.dp)
                                        .height(18.dp),
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "他の駅を見る (${uiState.stations.size - 1}件)",
                                style =
                                    MaterialTheme.typography.labelMedium.copy(
                                        color = Color.White.copy(alpha = 0.9f),
                                        fontWeight = FontWeight.Bold,
                                    ),
                            )
                        }
                    }
                }

                // 展開時の駅リスト
                AnimatedVisibility(
                    visible = uiState.isStationListExpanded,
                    enter = fadeIn() + slideInVertically(),
                    exit = fadeOut() + slideOutVertically(),
                ) {
                    Column {
                        uiState.stations.drop(1).forEach { station ->
                            Spacer(modifier = Modifier.height(16.dp))
                            HorizontalDivider(
                                color = Color.White.copy(alpha = 0.1f),
                                thickness = 1.dp,
                            )
                            Spacer(modifier = Modifier.height(16.dp))

                            InfoRow(
                                label = station.line ?: "駅",
                                value = "${station.name} (${station.distance})",
                                isHighlight = false,
                                icon = Icons.Default.Train,
                                onMapClick = {
                                    openGoogleMap(
                                        context,
                                        station.latitude,
                                        station.longitude,
                                        station.name,
                                    )
                                },
                            )
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        GlassButton(
                            onClick = onToggleStationList,
                            modifier = Modifier.height(40.dp),
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(horizontal = 16.dp),
                            ) {
                                Icon(
                                    imageVector = Icons.Outlined.ExpandLess,
                                    contentDescription = null,
                                    tint = Color.White.copy(alpha = 0.9f),
                                    modifier =
                                        Modifier
                                            .width(18.dp)
                                            .height(18.dp),
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "表示を減らす",
                                    style =
                                        MaterialTheme.typography.labelMedium.copy(
                                            color = Color.White.copy(alpha = 0.9f),
                                            fontWeight = FontWeight.Bold,
                                        ),
                                )
                            }
                        }
                    }
                }
            }

            InfoDivider()

            InfoRow(
                label = labelElevation,
                value =
                    if (uiState.elevation != null) {
                        "${uiState.elevation} m"
                    } else {
                        uiState.elevationStatus
                    },
                isHighlight = true,
                icon = Icons.Default.ArrowUpward,
            )

            InfoDivider()

            InfoRow(
                label = labelPressure,
                value =
                    if (uiState.pressure != null) {
                        String.format("%.0f hPa", uiState.pressure)
                    } else {
                        uiState.pressureStatus
                    },
                isHighlight = true,
                icon = Icons.Default.Speed,
            )

            InfoDivider()

            InfoRow(
                label = labelGravity,
                value =
                    if (uiState.gravity != null) {
                        String.format("%.4f m/s²", uiState.gravity)
                    } else {
                        "-"
                    },
                isHighlight = true,
                icon = Icons.Default.ArrowDownward,
            )

            InfoDivider()

            InfoRow(
                label = labelLatitude,
                value = uiState.latitude?.toString() ?: statusLoading,
                icon = Icons.Default.Place,
            )
            Spacer(modifier = Modifier.height(16.dp))
            InfoRow(
                label = labelLongitude,
                value = uiState.longitude?.toString() ?: statusLoading,
                icon = Icons.Default.Place,
            )
        }
    }

    Spacer(modifier = Modifier.height(80.dp))
}

@Composable
fun ErrorBanner(
    message: String,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val errorDescription = stringResource(R.string.accessibility_error_format, message)
    val warningDescription = stringResource(R.string.accessibility_warning)
    val closeDescription = stringResource(R.string.accessibility_close)
    val closeErrorDescription = stringResource(R.string.accessibility_close_error)

    GlassCard(
        modifier =
            modifier
                .fillMaxWidth()
                .padding(16.dp)
                .semantics {
                    contentDescription = errorDescription
                },
        glassAlpha = 0.15f,
        borderColor = Color(0xFFEF4444),
    ) {
        Row(
            modifier =
                Modifier
                    .padding(16.dp)
                    .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                imageVector = Icons.Default.Warning,
                contentDescription = warningDescription,
                tint = Color(0xFFEF4444),
                modifier = Modifier.padding(end = 12.dp),
            )
            Text(
                text = message,
                style =
                    MaterialTheme.typography.bodyMedium.copy(
                        color = Color.White,
                    ),
                modifier = Modifier.weight(1f),
            )
            IconButton(
                onClick = onDismiss,
                modifier =
                    Modifier.semantics {
                        contentDescription = closeErrorDescription
                    },
            ) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = closeDescription,
                    tint = Color.White.copy(alpha = 0.7f),
                )
            }
        }
    }
}

@Composable
fun PermissionRequestCard(onRequestPermission: () -> Unit) {
    val permissionText = stringResource(R.string.permission_required)
    val permissionDescription = stringResource(R.string.permission_required_description)
    val buttonText = stringResource(R.string.button_grant_permission)
    val buttonDescription = stringResource(R.string.permission_button_description)

    GlassCard(
        modifier =
            Modifier
                .fillMaxWidth(0.9f)
                .padding(16.dp),
    ) {
        Column(
            modifier =
                Modifier
                    .padding(24.dp)
                    .fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(
                permissionText,
                style = MaterialTheme.typography.bodyLarge.copy(color = Color.White),
                modifier =
                    Modifier.semantics {
                        contentDescription = permissionDescription
                    },
            )
            Spacer(modifier = Modifier.height(24.dp))
            GlassButton(
                onClick = onRequestPermission,
                modifier =
                    Modifier
                        .height(50.dp)
                        .width(200.dp)
                        .semantics {
                            contentDescription = buttonDescription
                        },
            ) {
                Text(
                    buttonText,
                    style =
                        MaterialTheme.typography.labelLarge.copy(
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                        ),
                )
            }
        }
    }
}

@Composable
fun InfoDivider() {
    Spacer(modifier = Modifier.height(24.dp))
    HorizontalDivider(
        color = Color.White.copy(alpha = 0.1f),
        thickness = 1.dp,
    )
    Spacer(modifier = Modifier.height(24.dp))
}

@Composable
fun InfoRow(
    label: String,
    value: String,
    isHighlight: Boolean = false,
    icon: ImageVector? = null,
    onMapClick: (() -> Unit)? = null,
) {
    val openMapText = stringResource(R.string.button_open_map)
    val openMapDescription = stringResource(R.string.accessibility_open_map_format, label)
    val mapIconDescription = stringResource(R.string.accessibility_map_icon)

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier =
            Modifier
                .padding(4.dp)
                .fillMaxWidth()
                .semantics(mergeDescendants = true) {
                    contentDescription = "$label: $value"
                },
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,
        ) {
            if (icon != null) {
                Icon(
                    imageVector = icon,
                    contentDescription = label,
                    tint = Color.White.copy(alpha = 0.7f),
                    modifier =
                        Modifier
                            .padding(end = 12.dp)
                            .width(20.dp)
                            .height(20.dp),
                )
            }

            Column(horizontalAlignment = Alignment.Start) {
                Text(
                    text = label,
                    style =
                        MaterialTheme.typography.labelSmall.copy(
                            color = Color.White.copy(alpha = 0.5f),
                            letterSpacing = 2.sp,
                            fontFamily = FontFamily.Monospace,
                        ),
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = value,
                    style =
                        if (isHighlight) {
                            MaterialTheme.typography.headlineMedium.copy(
                                color = Color.White,
                                fontFamily = FontFamily.Monospace,
                                shadow =
                                    Shadow(
                                        color = Color(0xFF6366F1).copy(alpha = 0.8f),
                                        offset = Offset(0f, 0f),
                                        blurRadius = 12f,
                                    ),
                            )
                        } else {
                            MaterialTheme.typography.titleMedium.copy(
                                color = Color.White.copy(alpha = 0.9f),
                                fontFamily = FontFamily.Monospace,
                            )
                        },
                )
            }
        }

        if (onMapClick != null) {
            Spacer(modifier = Modifier.height(12.dp))
            GlassButton(
                onClick = onMapClick,
                modifier =
                    Modifier
                        .height(36.dp)
                        .semantics {
                            contentDescription = openMapDescription
                        },
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(horizontal = 12.dp),
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Map,
                        contentDescription = mapIconDescription,
                        tint = Color.White.copy(alpha = 0.9f),
                        modifier =
                            Modifier
                                .width(16.dp)
                                .height(16.dp),
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = openMapText,
                        style =
                            MaterialTheme.typography.labelSmall.copy(
                                color = Color.White.copy(alpha = 0.9f),
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.SansSerif,
                            ),
                    )
                }
            }
        }
    }
}

fun openGoogleMap(
    context: Context,
    lat: Double,
    lon: Double,
    label: String,
) {
    val uri = "geo:0,0?q=$lat,$lon($label)"
    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(uri))
    intent.setPackage("com.google.android.apps.maps")
    if (intent.resolveActivity(context.packageManager) != null) {
        context.startActivity(intent)
    } else {
        val browserIntent =
            Intent(
                Intent.ACTION_VIEW,
                Uri.parse("https://www.google.com/maps/search/?api=1&query=$lat,$lon"),
            )
        context.startActivity(browserIntent)
    }
}

fun checkPermissions(context: Context): Boolean {
    return ContextCompat.checkSelfPermission(
        context,
        Manifest.permission.ACCESS_FINE_LOCATION,
    ) == PackageManager.PERMISSION_GRANTED
}
