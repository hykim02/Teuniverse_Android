package com.example.teuniverse

import android.telecom.Call
import retrofit2.http.GET

// suspend 함수-> 비동기 작업 수행, UI 스레드 차단 X, 비동기 코드 효율적으로 작성
// 최애 아티스트 조회
interface SelectArtistInterface {
    @GET("artist")
    suspend fun getArtist(): SelectArtistResponse
}
