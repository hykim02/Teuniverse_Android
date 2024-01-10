package com.example.teuniverse

import androidx.annotation.RestrictTo
import com.google.gson.Gson
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

const val BASE_URL = BuildConfig.BASE_URL

// 최애 아티스트 조회
object SelectArtistInstance {
    // 싱글톤 패턴으로 retrofit 객체 생성
    fun getArtistService(): SelectArtistInterface {
        return Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(SelectArtistInterface::class.java)
    }
}

// 월간 아티스트 투표수 조회
object MonthlyRankingInstance {
    fun getVoteCountService(): MonthlyRankingInterface {
        return Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(MonthlyRankingInterface::class.java)
    }
}

// 로그인 토큰 전송 요청 & 응답
object LoginInstance {
    fun userLoginService(): LoginInterface {
        return Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(LoginInterface::class.java)
    }
}