package com.example.teuniverse

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


