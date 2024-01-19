package com.example.teuniverse

import android.os.Parcel
import android.os.Parcelable

// 커뮤니티 리사이클러뷰 아이템 데이터
data class CommunityPostItem(
    val userImg: String?,
    val fandomName: String?,
    val postTerm: String?,
    val postImg: String?,
    val postSummary: String?,
    val heartCount: Int,
    val commentCount: Int
): Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.readString(),
        parcel.readString(),
        parcel.readString(),
        parcel.readString(),
        parcel.readString(),
        parcel.readInt(),
        parcel.readInt()
    ) {
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(userImg)
        parcel.writeString(fandomName)
        parcel.writeString(postTerm)
        parcel.writeString(postImg)
        parcel.writeString(postSummary)
        parcel.writeInt(heartCount)
        parcel.writeInt(commentCount)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<CommunityPostItem> {
        override fun createFromParcel(parcel: Parcel): CommunityPostItem {
            return CommunityPostItem(parcel)
        }

        override fun newArray(size: Int): Array<CommunityPostItem?> {
            return arrayOfNulls(size)
        }
    }
}

// 투표 랭킹 리사이클러뷰 아이템 데이터
data class VoteRankingItem(
    val rank: String,
    val img: String,
    val name: String,
    val count: String
)

// 커뮤니티 게시물 댓글 리사이클러뷰 아이템 데이터
data class CommentItem(
    val userImg: String,
    val nickName: String,
    val postTime: String,
    val comment: String
)

// 서버 응답 코드(data가 객체인 경우)
data class ServerResponse<T>(
    val success: Boolean,
    val statusCode: Int,
    val message: String,
    val data: T
)
// 서버 응답 코드(회원 가입)
data class SignUpResponse(
    val success: Boolean,
    val statusCode: Int,
    val message: String,
)
// 서버 응답 코드(data가 리스트인 경우)
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
    val voteCount: Int?
)

// 투표하기 현황 조회
data class PopupVoteData(
    val voteCount: Int,
    val remainVoteCount: Int,
    val month: Int,
    val artistName: String,
    val rank: Float
)

// 회원 가입 완료 요청
data class SignUpRequest(
    val id: Long,
    val nickName: String,
    val thumbnailUrl: String,
    val favoriteArtistId: Int
)



// 피드 조회
data class ArtistProfile(
    val id: Int,
    val name: String,
    val thumbnailUrl: String
)

data class Feeds(
    val id: Int,
    val content: String,
    val thumbnailUrl: String,
    val likeCount: Int,
    val createdAt: String,
    val updatedAt: String,
    val userProfile: UserProfileData
)

data class CommunityData(
    val artistProfile: ArtistProfile,
    val feeds: List<Feeds>
)