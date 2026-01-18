package io.github.yfujita.herenow.data

import com.google.gson.GsonBuilder
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Query

class StationService {
    private val retrofit: Retrofit = Retrofit.Builder()
        .baseUrl("https://express.heartrails.com/")
        .addConverterFactory(GsonConverterFactory.create(GsonBuilder().setLenient().create()))
        .build()

    private val api: StationApi = retrofit.create(StationApi::class.java)

    suspend fun getNearestStation(lat: Double, lon: Double): Station? {
        return try {
            val response = api.getStations(x = lon, y = lat)
            response.response?.station?.firstOrNull()
        } catch (e: Exception) {
            android.util.Log.e("StationService", "Error fetching station", e)
            null
        }
    }
}

data class StationResponse(
    val response: StationResponseContent?
)

data class StationResponseContent(
    val station: List<Station>?
)

data class Station(
    val name: String?,
    val distance: String?,
    val line: String?,
    val x: Double?,
    val y: Double?
)

interface StationApi {
    @GET("api/json?method=getStations")
    suspend fun getStations(
        @Query("x") x: Double,
        @Query("y") y: Double
    ): StationResponse
}
