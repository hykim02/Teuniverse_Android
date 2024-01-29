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

// 커뮤니티 피드 조회
object CommunityFeedsInstance {
    fun communityFeedsService(): CommunityFeedsInterface {
        return Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(CommunityFeedsInterface::class.java)
    }
}

// 커뮤니티 상세 페이지 조회
object CommunityDetailInstance {
    fun communityDetailService(): CommunityDetailInterface {
        return Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(CommunityDetailInterface::class.java)
    }

//    private val retrofit: Retrofit = provideRetrofit(BASE_URL)
//
//    fun communityDetailService(): CommunityDetailInterface {
//        return retrofit.create(CommunityDetailInterface::class.java)
//    }
//
//    private fun provideRetrofit(baseUrl: String): Retrofit {
//        val loggingInterceptor = HttpLoggingInterceptor(object : HttpLoggingInterceptor.Logger {
//            override fun log(message: String) {
//                // HTTP 요청 로그를 출력
//                println("OkHttp: $message")
//            }
//        }).apply {
//            level = HttpLoggingInterceptor.Level.BODY // 로그 수준 설정
//        }
//
//        val okHttpClient = OkHttpClient.Builder()
//            .addInterceptor(loggingInterceptor) // 인터셉터 추가
//            .build()
//
//        return Retrofit.Builder()
//            .baseUrl(baseUrl)
//            .client(okHttpClient)
//            .addConverterFactory(GsonConverterFactory.create())
//            .build()
//    }
}
// 게시물 작성
object CommunityPostInstance {
//    fun communityPostService(): CommunityPostInterface {
//        return Retrofit.Builder()
//            .baseUrl(BASE_URL)
//            .addConverterFactory(GsonConverterFactory.create())
//            .build()
//            .create(CommunityPostInterface::class.java)
//    }

    private val retrofit: Retrofit = provideRetrofit(BASE_URL)

    fun communityPostService(): CommunityPostInterface {
        return retrofit.create(CommunityPostInterface::class.java)
    }

    private fun provideRetrofit(baseUrl: String): Retrofit {
        val loggingInterceptor = HttpLoggingInterceptor(object : HttpLoggingInterceptor.Logger {
            override fun log(message: String) {
                // HTTP 요청 로그를 출력
                println("OkHttp: $message")
            }
        }).apply {
            level = HttpLoggingInterceptor.Level.BODY // 로그 수준 설정
        }

        val okHttpClient = OkHttpClient.Builder()
            .addInterceptor(loggingInterceptor) // 인터셉터 추가
            .build()

        return Retrofit.Builder()
            .baseUrl(baseUrl)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }
}
