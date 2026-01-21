package io.github.yfujita.herenow.data.service

import android.util.Log
import retrofit2.http.GET
import retrofit2.http.Query

class StationService(private val api: StationApi) {
    suspend fun getNearestStation(
        lat: Double,
        lon: Double,
    ): Station? {
        return try {
            val response: StationResponse = api.getStations(x = lon, y = lat)
            response.response?.station?.firstOrNull()
        } catch (e: Exception) {
            Log.e("StationService", "Error fetching station", e)
            null
        }
    }

    suspend fun getNearestStations(
        lat: Double,
        lon: Double,
        limit: Int = 5,
    ): List<Station> {
        return try {
            val response: StationResponse = api.getStations(x = lon, y = lat)
            response.response?.station?.take(limit) ?: emptyList()
        } catch (e: Exception) {
            Log.e("StationService", "Error fetching stations", e)
            emptyList()
        }
    }
}

data class StationResponse(
    val response: StationResponseContent?,
)

data class StationResponseContent(
    val station: List<Station>?,
)

data class Station(
    val name: String?,
    val distance: String?,
    val line: String?,
    val x: Double?,
    val y: Double?,
)

interface StationApi {
    @GET("api/json?method=getStations")
    suspend fun getStations(
        @Query("x") x: Double,
        @Query("y") y: Double,
    ): StationResponse
}
