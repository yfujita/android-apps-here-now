package io.github.yfujita.herenow.data.repository

import io.github.yfujita.herenow.data.service.LocationService
import io.github.yfujita.herenow.domain.model.LocationData
import io.github.yfujita.herenow.domain.repository.LocationRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class LocationRepositoryImpl
    @Inject
    constructor(
        private val locationService: LocationService,
    ) : LocationRepository {
        override fun getLocationUpdates(intervalMillis: Long): Flow<LocationData> {
            return locationService.getLocationUpdates(intervalMillis).map { location ->
                LocationData(
                    latitude = location.latitude,
                    longitude = location.longitude,
                )
            }
        }
    }
