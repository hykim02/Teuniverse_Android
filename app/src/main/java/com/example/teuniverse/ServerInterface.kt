package com.example.teuniverse

import android.telecom.Call
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Headers
import retrofit2.http.POST
import retrofit2.http.Query

// suspend 함수-> 비동기 작업 수행, UI 스레드 차단 X, 비동기 코드 효율적으로 작성
// 최애 아티스트 조회
interface SelectArtistInterface {
    @GET("artist")
    @Headers("accept: application/json")
    suspend fun getArtist(
        @Header("Authorization") authorization: String
    ): Response<SelectArtistResponse>
}

// 월간 아티스트 투표수 조회
interface MonthlyArtistRankingInterface {
    @GET("vote/monthly-artist")
    @Headers("accept: application/json")
    suspend fun getVoteCount(
        @Header("Authorization") authorization: String
    ): Response<MonthlyRankingResponse>
}

// 월간 팬 투표수 조회
interface MonthlyFanRankingInterface {
    @GET("vote/monthly-fan")
    @Headers("accept: application/json")
    suspend fun getVoteCount(
        @Header("Authorization") authorization: String,
        @Query("type") type: Int
    ): Response<MonthlyRankingResponse>
}
// 로그인 토큰 전송 요청 & 응답
interface LoginInterface {
    @POST("user/login")
    @Headers("accept: application/json",
        "Content-Type: application/json")
    suspend fun userLogin(
        @Body request: LoginRequest
    ): Response<LoginResponse>
}
