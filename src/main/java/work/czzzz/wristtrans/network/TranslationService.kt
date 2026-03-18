package work.czzzz.wisttrans.network

import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Query

interface TranslationService {
    @GET("/")
    fun translate(
        @Query("q") query: String,
        @Query("to") to: String = "zh-CN",
        @Query("from") from: String = "auto"
    ): Call<TranslationResponse>
}
