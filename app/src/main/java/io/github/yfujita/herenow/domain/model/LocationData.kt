package io.github.yfujita.herenow.domain.model

data class LocationData(
    val latitude: Double,
    val longitude: Double,
)

data class AddressData(
    val fullAddress: String,
    val prefecture: String?,
    val city: String?,
    val town: String?,
)

data class ElevationData(
    val elevation: Double,
)

data class StationData(
    val name: String,
    val distance: String,
    val line: String?,
    val latitude: Double,
    val longitude: Double,
)

data class GravityData(
    val gravity: Double,
)

data class PressureData(
    val pressure: Float,
)
