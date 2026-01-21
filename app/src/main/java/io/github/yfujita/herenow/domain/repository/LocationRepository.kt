package io.github.yfujita.herenow.domain.repository

import io.github.yfujita.herenow.domain.model.AddressData
import io.github.yfujita.herenow.domain.model.ElevationData
import io.github.yfujita.herenow.domain.model.GravityData
import io.github.yfujita.herenow.domain.model.LocationData
import io.github.yfujita.herenow.domain.model.PressureData
import io.github.yfujita.herenow.domain.model.Result
import io.github.yfujita.herenow.domain.model.StationData
import kotlinx.coroutines.flow.Flow

interface LocationRepository {
    fun getLocationUpdates(intervalMillis: Long): Flow<LocationData>
}

interface ElevationRepository {
    suspend fun getElevation(
        latitude: Double,
        longitude: Double,
    ): Result<ElevationData?>
}

interface AddressRepository {
    suspend fun getAddress(
        latitude: Double,
        longitude: Double,
    ): Result<AddressData?>
}

interface StationRepository {
    suspend fun getNearestStation(
        latitude: Double,
        longitude: Double,
    ): Result<StationData?>

    suspend fun getNearestStations(
        latitude: Double,
        longitude: Double,
    ): Result<List<StationData>>
}

interface GravityRepository {
    fun calculateGravity(
        latitude: Double,
        elevation: Double,
    ): GravityData
}

interface SensorRepository {
    fun getPressureFlow(): Flow<PressureData?>
}
