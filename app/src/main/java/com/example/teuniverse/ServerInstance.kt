package com.example.teuniverse

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

const val BASE_URL = BuildConfig.BASE_URL

// 최애 아티스트 조회
object SelectArtistInstance {
    // 싱글톤 패턴으로 retrofit 객체 생성
    object RetrofitClass {

        private val retrofit = Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        val apiService: SelectArtistInterface = retrofit.create(SelectArtistInterface::class.java)
    }
}