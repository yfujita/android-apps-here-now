package io.github.yfujita.herenow.data.service

import android.util.Log
import retrofit2.http.GET
import retrofit2.http.Query

class ElevationService(private val api: ElevationApi) {
    suspend fun getElevation(
        lat: Double,
        lon: Double,
    ): Double? {
        return try {
            val response: ElevationResponse = api.getElevation(lon = lon, lat = lat)
            val elevationVal: Any? = response.elevation

            when (elevationVal) {
                is Double -> elevationVal
                is String -> {
                    if (elevationVal == "-----") {
                        null
                    } else {
                        elevationVal.toDoubleOrNull()
                    }
                }
                else -> null
            }
        } catch (e: Exception) {
            Log.e("ElevationService", "Error fetching elevation", e)
            null
        }
    }
}

data class ElevationResponse(
    val elevation: Any?,
    val hsrc: String?,
)

interface ElevationApi {
    @GET("general/dem/scripts/getelevation.php")
    suspend fun getElevation(
        @Query("lon") lon: Double,
        @Query("lat") lat: Double,
        @Query("outtype") outtype: String = "JSON",
    ): ElevationResponse
}
