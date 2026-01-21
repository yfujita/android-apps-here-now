package io.github.yfujita.herenow.data.repository

import io.github.yfujita.herenow.data.service.GravityService
import io.github.yfujita.herenow.domain.model.GravityData
import io.github.yfujita.herenow.domain.repository.GravityRepository
import javax.inject.Inject

class GravityRepositoryImpl
    @Inject
    constructor(
        private val gravityService: GravityService,
    ) : GravityRepository {
        override fun calculateGravity(
            latitude: Double,
            elevation: Double,
        ): GravityData {
            val gravity = gravityService.calculateGravity(latitude, elevation)
            return GravityData(gravity)
        }
    }
