package io.github.yfujita.herenow.di

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import io.github.yfujita.herenow.data.repository.AddressRepositoryImpl
import io.github.yfujita.herenow.data.repository.ElevationRepositoryImpl
import io.github.yfujita.herenow.data.repository.GravityRepositoryImpl
import io.github.yfujita.herenow.data.repository.LocationRepositoryImpl
import io.github.yfujita.herenow.data.repository.SensorRepositoryImpl
import io.github.yfujita.herenow.data.repository.StationRepositoryImpl
import io.github.yfujita.herenow.domain.repository.AddressRepository
import io.github.yfujita.herenow.domain.repository.ElevationRepository
import io.github.yfujita.herenow.domain.repository.GravityRepository
import io.github.yfujita.herenow.domain.repository.LocationRepository
import io.github.yfujita.herenow.domain.repository.SensorRepository
import io.github.yfujita.herenow.domain.repository.StationRepository
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {
    @Binds
    @Singleton
    abstract fun bindLocationRepository(impl: LocationRepositoryImpl): LocationRepository

    @Binds
    @Singleton
    abstract fun bindElevationRepository(impl: ElevationRepositoryImpl): ElevationRepository

    @Binds
    @Singleton
    abstract fun bindAddressRepository(impl: AddressRepositoryImpl): AddressRepository

    @Binds
    @Singleton
    abstract fun bindStationRepository(impl: StationRepositoryImpl): StationRepository

    @Binds
    @Singleton
    abstract fun bindGravityRepository(impl: GravityRepositoryImpl): GravityRepository

    @Binds
    @Singleton
    abstract fun bindSensorRepository(impl: SensorRepositoryImpl): SensorRepository
}
