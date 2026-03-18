package work.czzzz.wristtrans.data.network

import retrofit2.http.GET
import retrofit2.http.Query
import work.czzzz.wristtrans.data.model.TranslationResponse

interface TranslationService {
    @GET("/")
    suspend fun translate(
        @Query("text") text: String,
        @Query("to") targetLanguage: String = "zh-CN",
        @Query("from") sourceLanguage: String = "auto",
    ): TranslationResponse
}
