package io.github.yfujita.herenow.data.repository

import io.github.yfujita.herenow.data.service.SensorService
import io.github.yfujita.herenow.domain.model.PressureData
import io.github.yfujita.herenow.domain.repository.SensorRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class SensorRepositoryImpl
    @Inject
    constructor(
        private val sensorService: SensorService,
    ) : SensorRepository {
        override fun getPressureFlow(): Flow<PressureData?> {
            return sensorService.getPressureFlow().map { pressure ->
                pressure?.let { PressureData(it) }
            }
        }
    }
