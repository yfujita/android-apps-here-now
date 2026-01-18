package io.github.yfujita.herenow.data

import com.google.gson.GsonBuilder
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Query

class ElevationService {
    private val retrofit: Retrofit = Retrofit.Builder()
        .baseUrl("https://cyberjapandata2.gsi.go.jp/")
        .addConverterFactory(GsonConverterFactory.create(GsonBuilder().setLenient().create()))
        .build()

    private val api: ElevationApi = retrofit.create(ElevationApi::class.java)

    suspend fun getElevation(lat: Double, lon: Double): Double? {
        return try {
            val response: ElevationResponse = api.getElevation(lon = lon, lat = lat)
            val elevationVal: Any? = response.elevation
            
            when (elevationVal) {
                is Double -> elevationVal
                is String -> {
                    if (elevationVal == "-----") null 
                    else elevationVal.toDoubleOrNull()
                }
                else -> null
            }
        } catch (e: Exception) {
            android.util.Log.e("ElevationService", "Error fetching elevation", e)
            null
        }
    }
}

data class ElevationResponse(
    val elevation: Any?,
    val hsrc: String?
)

interface ElevationApi {
    @GET("general/dem/scripts/getelevation.php")
    suspend fun getElevation(
        @Query("lon") lon: Double,
        @Query("lat") lat: Double,
        @Query("outtype") outtype: String = "JSON"
    ): ElevationResponse
}
