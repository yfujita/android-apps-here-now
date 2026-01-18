package io.github.yfujita.herenow.data

import com.google.gson.GsonBuilder
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Query

class AddressService {
    private val retrofit: Retrofit = Retrofit.Builder()
        .baseUrl("https://geoapi.heartrails.com/")
        .addConverterFactory(GsonConverterFactory.create(GsonBuilder().setLenient().create()))
        .build()

    private val api: AddressApi = retrofit.create(AddressApi::class.java)

    suspend fun getAddress(lat: Double, lon: Double): String? {
        return try {
            val response = api.searchByGeoLocation(x = lon, y = lat)
            val location = response.response?.location?.firstOrNull()
            if (location != null) {
                "${location.prefecture}${location.city}${location.town}"
            } else {
                null
            }
        } catch (e: Exception) {
            android.util.Log.e("AddressService", "Error fetching address", e)
            null
        }
    }
}

// Wrapper for the API response structure
data class AddressResponse(
    val response: ResponseContent?
)

data class ResponseContent(
    val location: List<LocationInfo>?
)

data class LocationInfo(
    val city: String?,
    val town: String?,
    val prefecture: String?
)

interface AddressApi {
    @GET("api/json?method=searchByGeoLocation")
    suspend fun searchByGeoLocation(
        @Query("x") x: Double,
        @Query("y") y: Double
    ): AddressResponse
}
