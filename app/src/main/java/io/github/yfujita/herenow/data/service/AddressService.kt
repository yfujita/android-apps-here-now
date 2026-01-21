package io.github.yfujita.herenow.data.service

import android.util.Log
import retrofit2.http.GET
import retrofit2.http.Query

class AddressService(private val api: AddressApi) {
    suspend fun getAddress(
        lat: Double,
        lon: Double,
    ): String? {
        return try {
            val response = api.searchByGeoLocation(x = lon, y = lat)
            val location = response.response?.location?.firstOrNull()
            if (location != null) {
                "${location.prefecture}${location.city}${location.town}"
            } else {
                null
            }
        } catch (e: Exception) {
            Log.e("AddressService", "Error fetching address", e)
            null
        }
    }
}

data class AddressResponse(
    val response: ResponseContent?,
)

data class ResponseContent(
    val location: List<LocationInfo>?,
)

data class LocationInfo(
    val city: String?,
    val town: String?,
    val prefecture: String?,
)

interface AddressApi {
    @GET("api/json?method=searchByGeoLocation")
    suspend fun searchByGeoLocation(
        @Query("x") x: Double,
        @Query("y") y: Double,
    ): AddressResponse
}
