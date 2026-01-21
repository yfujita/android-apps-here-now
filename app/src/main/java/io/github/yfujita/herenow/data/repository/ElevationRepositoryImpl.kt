package io.github.yfujita.herenow.data.repository

import android.util.Log
import io.github.yfujita.herenow.data.service.ElevationService
import io.github.yfujita.herenow.domain.model.ElevationData
import io.github.yfujita.herenow.domain.model.Result
import io.github.yfujita.herenow.domain.repository.ElevationRepository
import javax.inject.Inject

class ElevationRepositoryImpl
    @Inject
    constructor(
        private val elevationService: ElevationService,
    ) : ElevationRepository {
        override suspend fun getElevation(
            latitude: Double,
            longitude: Double,
        ): Result<ElevationData?> {
            return try {
                val elevation = elevationService.getElevation(latitude, longitude)
                Result.success(elevation?.let { ElevationData(it) })
            } catch (e: Exception) {
                Log.e(TAG, "Error fetching elevation", e)
                Result.error("標高データの取得に失敗しました", e)
            }
        }

        companion object {
            private const val TAG = "ElevationRepository"
        }
    }
