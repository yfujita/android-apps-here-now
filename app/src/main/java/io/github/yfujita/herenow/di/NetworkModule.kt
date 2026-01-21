package io.github.yfujita.herenow.di

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import io.github.yfujita.herenow.data.service.AddressApi
import io.github.yfujita.herenow.data.service.ElevationApi
import io.github.yfujita.herenow.data.service.StationApi
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit
import javax.inject.Named
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {
    private const val HEARTRAILS_GEO_BASE_URL = "https://geoapi.heartrails.com/"
    private const val HEARTRAILS_EXPRESS_BASE_URL = "https://express.heartrails.com/"
    private const val GSI_BASE_URL = "https://cyberjapandata2.gsi.go.jp/"

    @Provides
    @Singleton
    fun provideGson(): Gson {
        return GsonBuilder()
            .setLenient()
            .create()
    }

    @Provides
    @Singleton
    fun provideOkHttpClient(): OkHttpClient {
        return OkHttpClient.Builder()
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .build()
    }

    @Provides
    @Singleton
    @Named("HeartRailsGeo")
    fun provideHeartRailsGeoRetrofit(
        okHttpClient: OkHttpClient,
        gson: Gson,
    ): Retrofit {
        return Retrofit.Builder()
            .baseUrl(HEARTRAILS_GEO_BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create(gson))
            .build()
    }

    @Provides
    @Singleton
    @Named("HeartRailsExpress")
    fun provideHeartRailsExpressRetrofit(
        okHttpClient: OkHttpClient,
        gson: Gson,
    ): Retrofit {
        return Retrofit.Builder()
            .baseUrl(HEARTRAILS_EXPRESS_BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create(gson))
            .build()
    }

    @Provides
    @Singleton
    @Named("GSI")
    fun provideGSIRetrofit(
        okHttpClient: OkHttpClient,
        gson: Gson,
    ): Retrofit {
        return Retrofit.Builder()
            .baseUrl(GSI_BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create(gson))
            .build()
    }

    @Provides
    @Singleton
    fun provideAddressApi(
        @Named("HeartRailsGeo") retrofit: Retrofit,
    ): AddressApi {
        return retrofit.create(AddressApi::class.java)
    }

    @Provides
    @Singleton
    fun provideStationApi(
        @Named("HeartRailsExpress") retrofit: Retrofit,
    ): StationApi {
        return retrofit.create(StationApi::class.java)
    }

    @Provides
    @Singleton
    fun provideElevationApi(
        @Named("GSI") retrofit: Retrofit,
    ): ElevationApi {
        return retrofit.create(ElevationApi::class.java)
    }
}
