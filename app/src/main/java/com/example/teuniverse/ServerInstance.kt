package com.example.teuniverse

import androidx.annotation.RestrictTo
import com.google.gson.Gson
import okhttp3.OkHttpClient
import okhttp3.ResponseBody
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Converter
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.lang.reflect.Type

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
object MonthlyArtistRankingInstance {
    fun getVoteCountService(): MonthlyArtistRankingInterface {
        return Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(MonthlyArtistRankingInterface::class.java)
    }
}

// 월간 팬 투표수 조회
object MonthlyFanRankingInstance {
    fun getVoteCountService(): MonthlyFanRankingInterface {
        return Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(MonthlyFanRankingInterface::class.java)
    }
}



// 로그인 토큰 전송 요청 & 응답
object LoginInstance {
    fun userLoginService(): LoginInterface {
        return Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
//            .client(client)
            .build()
            .create(LoginInterface::class.java)
    }
}

// 투표권 개수 조회
object VoteCountInstance {
    fun getVotesService(): VoteCountInterface {
        return Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(VoteCountInterface::class.java)
    }
}

// 투표하기 팝업창 데이터
object PopupVoteInstance {
    fun getCurrentVoteInfoService(): PopupVoteInterface {
        return Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(PopupVoteInterface::class.java)
    }
}

// 회원 가입 완료
object SignUpInstance {
    fun signUpSuccessService(): SignUpInterface {
        return Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(SignUpInterface::class.java)
    }
}

object CommunityFeedsInstance {
    fun communityFeedsService(): CommunityFeedsInterface {
        return Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(CommunityFeedsInterface::class.java)
    }
}