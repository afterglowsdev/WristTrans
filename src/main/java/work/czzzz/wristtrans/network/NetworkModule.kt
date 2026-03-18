package work.czzzz.wisttrans.network

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object NetworkModule {
    private const val BASE_URL = "https://api.urlf.eu.org/"
    
    val translationService: TranslationService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(TranslationService::class.java)
    }
}
