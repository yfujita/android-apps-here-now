package io.github.yfujita.herenow.data.repository

import android.util.Log
import io.github.yfujita.herenow.data.service.AddressService
import io.github.yfujita.herenow.domain.model.AddressData
import io.github.yfujita.herenow.domain.model.Result
import io.github.yfujita.herenow.domain.repository.AddressRepository
import javax.inject.Inject

class AddressRepositoryImpl
    @Inject
    constructor(
        private val addressService: AddressService,
    ) : AddressRepository {
        override suspend fun getAddress(
            latitude: Double,
            longitude: Double,
        ): Result<AddressData?> {
            return try {
                val address = addressService.getAddress(latitude, longitude)
                Result.success(
                    address?.let {
                        AddressData(
                            fullAddress = it,
                            prefecture = null,
                            city = null,
                            town = null,
                        )
                    },
                )
            } catch (e: Exception) {
                Log.e(TAG, "Error fetching address", e)
                Result.error("住所の取得に失敗しました", e)
            }
        }

        companion object {
            private const val TAG = "AddressRepository"
        }
    }
