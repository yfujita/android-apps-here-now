package io.github.yfujita.herenow.di

import android.content.Context
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import io.github.yfujita.herenow.data.service.AddressApi
import io.github.yfujita.herenow.data.service.AddressService
import io.github.yfujita.herenow.data.service.ElevationApi
import io.github.yfujita.herenow.data.service.ElevationService
import io.github.yfujita.herenow.data.service.GravityService
import io.github.yfujita.herenow.data.service.LocationService
import io.github.yfujita.herenow.data.service.SensorService
import io.github.yfujita.herenow.data.service.StationApi
import io.github.yfujita.herenow.data.service.StationService
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object ServiceModule {
    @Provides
    @Singleton
    fun provideFusedLocationProviderClient(
        @ApplicationContext context: Context,
    ): FusedLocationProviderClient {
        return LocationServices.getFusedLocationProviderClient(context)
    }

    @Provides
    @Singleton
    fun provideLocationService(fusedLocationProviderClient: FusedLocationProviderClient): LocationService {
        return LocationService(fusedLocationProviderClient)
    }

    @Provides
    @Singleton
    fun provideSensorService(
        @ApplicationContext context: Context,
    ): SensorService {
        return SensorService(context)
    }

    @Provides
    @Singleton
    fun provideGravityService(): GravityService {
        return GravityService()
    }

    @Provides
    @Singleton
    fun provideAddressService(api: AddressApi): AddressService {
        return AddressService(api)
    }

    @Provides
    @Singleton
    fun provideElevationService(api: ElevationApi): ElevationService {
        return ElevationService(api)
    }

    @Provides
    @Singleton
    fun provideStationService(api: StationApi): StationService {
        return StationService(api)
    }
}
