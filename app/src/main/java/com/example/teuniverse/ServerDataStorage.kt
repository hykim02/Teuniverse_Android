package com.example.teuniverse

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

// 최애 아티스트 조회
data class SelectArtistResponse(
    val success: Boolean,
    val statusCode: Int,
    val message: String,
    val data: List<ArtistData>
)

data class ArtistData(
    val id: Int,
    val name: String,
    val thumbnailUrl: String
)

// 월간 아티스트 투표수 조회
data class MonthlyRankingResponse(
    val success: Boolean,
    val statusCode: Int,
    val message: String,
    val data: List<VoteData>
)

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

data class LoginResponse(
    val accessToken: String,
    val refreshToken: String,
    val isExistUser: Boolean,
    val userProfileData: UserProfileData?
)

data class UserProfileData(
    val id: Long,
    val nickName: String,
    val thumbnailUrl: String
)





