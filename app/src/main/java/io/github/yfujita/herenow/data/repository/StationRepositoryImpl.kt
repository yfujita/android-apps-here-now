package io.github.yfujita.herenow.data.repository

import android.util.Log
import io.github.yfujita.herenow.data.service.StationService
import io.github.yfujita.herenow.domain.model.Result
import io.github.yfujita.herenow.domain.model.StationData
import io.github.yfujita.herenow.domain.repository.StationRepository
import javax.inject.Inject

class StationRepositoryImpl
    @Inject
    constructor(
        private val stationService: StationService,
    ) : StationRepository {
        override suspend fun getNearestStation(
            latitude: Double,
            longitude: Double,
        ): Result<StationData?> {
            return try {
                val station = stationService.getNearestStation(latitude, longitude)
                Result.success(
                    station?.let {
                        StationData(
                            name = it.name ?: "",
                            distance = it.distance ?: "",
                            line = it.line,
                            latitude = it.y ?: 0.0,
                            longitude = it.x ?: 0.0,
                        )
                    },
                )
            } catch (e: Exception) {
                Log.e(TAG, "Error fetching station", e)
                Result.error("最寄り駅の取得に失敗しました", e)
            }
        }

        override suspend fun getNearestStations(
            latitude: Double,
            longitude: Double,
        ): Result<List<StationData>> {
            return try {
                val stations = stationService.getNearestStations(latitude, longitude)
                Result.success(
                    stations.map { station ->
                        StationData(
                            name = station.name ?: "",
                            distance = station.distance ?: "",
                            line = station.line,
                            latitude = station.y ?: 0.0,
                            longitude = station.x ?: 0.0,
                        )
                    },
                )
            } catch (e: Exception) {
                Log.e(TAG, "Error fetching stations", e)
                Result.error("最寄り駅の取得に失敗しました", e)
            }
        }

        companion object {
            private const val TAG = "StationRepository"
        }
    }
