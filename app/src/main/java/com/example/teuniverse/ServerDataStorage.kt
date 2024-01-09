package com.example.teuniverse

// 커뮤니티 리사이클러뷰 아이템 데이터
data class CommunityPostItem(
    val userImg: String,
    val fandomName: String,
    val postTerm: Int,
    val postImg: Int,
    val postSummary: String,
    val heartCount: String,
    val commentCount: String
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
    val id: Int,
    val name: String,
    val thumbnailUrl: String,
    val voteCount: String
)


