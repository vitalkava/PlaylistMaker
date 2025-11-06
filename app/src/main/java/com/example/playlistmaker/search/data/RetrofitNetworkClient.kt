package com.example.playlistmaker.search.data

import android.content.Context
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import retrofit2.HttpException
import retrofit2.Response as RetrofitResponse

class RetrofitNetworkClient(
    private val iTunesService: ITunesApi,
    private val context: Context? = null
) : NetworkClient {
    override suspend fun doRequest(dto: Any): Response {
        if (dto !is TrackSearchRequest) {
            return Response().apply { resultCode = 400 }
        }

        return withContext(Dispatchers.IO) {
            try {
                val retrofitResponse: RetrofitResponse<TrackSearchResponse> =
                    iTunesService.search(dto.expression)
                if (retrofitResponse.isSuccessful) {
                    val body = retrofitResponse.body()
                    if (body != null) {
                        body.apply { resultCode = retrofitResponse.code() }
                    } else {
                        Response().apply { resultCode = retrofitResponse.code() ?: 500 }
                    }
                } else {
                    Response().apply { resultCode = retrofitResponse.code() ?: 500 }
                }
            } catch (e: HttpException) {
                e.printStackTrace()
                Response().apply { resultCode = e.code() ?: 500 }
            } catch (e: Throwable) {
                e.printStackTrace()
                Response().apply { resultCode = 500 }
            }
        }
    }
}