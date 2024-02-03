package com.example.teuniverse

import android.telecom.Call
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Headers
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Part
import retrofit2.http.Path
import retrofit2.http.Query

// suspend 함수-> 비동기 작업 수행, UI 스레드 차단 X, 비동기 코드 효율적으로 작성
// 최애 아티스트 조회
interface SelectArtistInterface {
    @GET("artist")
    @Headers("accept: application/json")
    suspend fun getArtist(
        @Header("Authorization") authorization: String
    ): Response<ArtistServerResponse<ArtistData>>
}

// 월간 아티스트 투표수 조회
interface MonthlyArtistRankingInterface {
    @GET("vote/monthly-artist")
    @Headers("accept: application/json")
    suspend fun getVoteCount(
        @Header("Authorization") authorization: String
    ): Response<ArtistServerResponse<VoteData>>
}

// 월간 팬 투표수 조회
interface MonthlyFanRankingInterface {
    @GET("vote/monthly-fan")
    @Headers("accept: application/json")
    suspend fun getVoteCount(
        @Header("Authorization") authorization: String,
        @Query("type") type: Int
    ): Response<ArtistServerResponse<VoteData>>
}
// 로그인 토큰 전송 요청 & 응답
interface LoginInterface {
    @POST("user/login")
    @Headers("accept: application/json",
        "Content-Type: application/json")
    suspend fun userLogin(
        @Body request: LoginRequest
    ): Response<ServerResponse<LoginData>>
}

// 보유 투표권 개수 조회
interface VoteCountInterface {
    @GET("user/vote")
    @Headers("accept: application/json")
    suspend fun getVotes(
        @Header("Authorization") authorization: String
    ): Response<ServerResponse<NumberOfVote>>
}

// 투표하기 팝업창 데이터 조회
interface PopupVoteInterface {
    @POST("vote/artist")
    @Headers("accept: application/json",
        "Content-Type: application/json")
    suspend fun getCurrentVoteInfo(
        @Header("Authorization") authorization: String,
        @Body request: NumberOfVote
    ): Response<ServerResponse<PopupVoteData>>
}

// 회원 가입 완료
interface SignUpInterface {
    @POST("user/register")
    @Headers("accept: application/json",
        "Content-Type: application/json")
    suspend fun signUpSuccess(
        @Body request: SignUpRequest
    ): Response<SignUpResponse>
}

// 피드 조회
interface CommunityFeedsInterface {
    @GET("community")
    @Headers("accept: application/json")
    suspend fun getFeeds(
        @Header("Authorization") authorization: String
    ): Response<ServerResponse<CommunityData>>
}

// 상세 피드 조회
interface CommunityDetailInterface {
    @GET("community/{feedId}")
    @Headers("accept: application/json")
    suspend fun getDetailFeeds(
        @Path("feedId") feedId: String,
        @Header("Authorization") authorization: String
    ): Response<ServerResponse<CommunityDetailData>>
}

// 게시물 작성
interface CommunityPostInterface {
    @Multipart
    @POST("community")
    @Headers("accept: */*")
    suspend fun postContent(
        @Header("Authorization") authorization: String?,
        @Part("content") content: RequestBody,
        @Part imageFile: MultipartBody.Part?
    ): Response<SignUpResponse>
}

// 게시물 삭제
interface DeleteFeedInterface {
    @DELETE("community/{feedId}")
    @Headers("accept: */*")
    suspend fun deleteFeed(
        @Path("feedId") feedId: String,
        @Header("Authorization") authorization: String?
    ): Response<SignUpResponse>
}

// 게시물 수정
interface EditFeedInterface {
    @Multipart
    @PUT("community/{feedId}")
    @Headers("accept: */*")
    suspend fun editFeed(
        @Path("feedId") feedId: Int,
        @Header("Authorization") authorization: String?,
        @Part("content") content: RequestBody,
        @Part imageFile: MultipartBody.Part?
    ): Response<SignUpResponse>
}

// 댓글 생성
interface CreateCommentInterface {
    @POST("community/{feedId}/comment")
    @Headers("accept: */*",
        "Content-Type: application/json")
    suspend fun createComment(
        @Path("feedId") feedId: String,
        @Header("Authorization") authorization: String?,
        @Body request: CreateComment
    ): Response<SignUpResponse>
}

// 댓글 수정
interface EditCommentInterface {
    @PUT("community/comment/{commentId}")
    @Headers("accept: */*",
        "Content-Type: application/json")
    suspend fun editComment(
        @Path("commentId") commentId: Int,
        @Header("Authorization") authorization: String?,
        @Body request: CreateComment
    ): Response<SignUpResponse>
}

// 댓글 삭제
interface DeleteCommentInterface {
    @DELETE("community/comment/{commentId}")
    @Headers("accept: */*")
    suspend fun deleteComment(
        @Path("commentId") commentId: Int,
        @Header("Authorization") authorization: String?
    ): Response<SignUpResponse>
}

// 좋아요 생성
interface ClickLikeInterface {
    @POST("community/{feedId}/like")
    @Headers("accept: application/json")
    suspend fun clickLike(
        @Path("feedId") feedId: Int,
        @Header("Authorization") authorization: String?
    ): Response<ServerResponse<CreateHeart>>
}

// 일정
interface CalendarInterface {
    @GET("schedule/{year}/{month}")
    @Headers("accept: */*")
    suspend fun getSchedule(
        @Path("year") year: Int,
        @Path("month") month: Int,
        @Header("Authorization") authorization: String?
    ): Response<EventResponse>
}