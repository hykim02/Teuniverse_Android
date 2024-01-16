package com.example.teuniverse

import com.google.gson.annotations.SerializedName

// 커뮤니티 리사이클러뷰 아이템 데이터
data class CommunityPostItem(
    val userImg: String,
    val fandomName: String,
    val postTerm: Int,
    val postImg: String,
    val postSummary: String,
    val heartCount: Int,
    val commentCount: Int
)

// 투표 랭킹 리사이클러뷰 아이템 데이터
data class VoteRankingItem(
    val rank: String,
    val img: String,
    val name: String,
    val count: String
)

// 서버 응답 코드
data class ServerResponse<T>(
    val success: Boolean,
    val statusCode: Int,
    val message: String,
    val data: T
)

data class ArtistServerResponse<T>(
    val success: Boolean,
    val statusCode: Int,
    val message: String,
    val data: List<T>
)
// 최애 아티스트 조회
data class ArtistData(
    val id: Int,
    val name: String,
    val thumbnailUrl: String
)

// 월간 아티스트 투표수 조회
data class VoteData(
    val id: Long,
    val name: String,
    val thumbnailUrl: String,
    val voteCount: String
)

// 로그인 토큰 전송 요청 & 응답
data class LoginRequest(
    val loginType: Int,
    val accessToken: String
)

data class LoginData(
    val accessToken: String,
    val refreshToken: String,
    val isExistUser: Boolean,
    val userProfileData: UserProfileData
)

data class UserProfileData(
    val id: Long,
    val nickName: String,
    val thumbnailUrl: String
)

// 투표권 개수 조회
data class NumberOfVote(
    val voteCount: Int
)

// 투표하기 현황 조회
data class PopupVoteData(
    val voteCount: Int,
    val remainVoteCount: Int,
    val month: Int,
    val artistName: String,
    val rank: Int
)




