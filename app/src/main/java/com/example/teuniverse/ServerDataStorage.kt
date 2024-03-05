package com.example.teuniverse

import android.os.Build
import android.os.Parcel
import android.os.Parcelable
import androidx.annotation.RequiresApi

// 커뮤니티 리사이클러뷰 아이템 데이터
data class CommunityPostItem(
    val feedId: Int,
    val userImg: String?,
    val fandomName: String?,
    val postTerm: String?,
    val postImg: String?,
    val postSummary: String?,
    val heartCount: Int,
    val commentCount: Int,
    val likeImg: Boolean = false
): Parcelable {
    @RequiresApi(Build.VERSION_CODES.Q)
    constructor(parcel: Parcel) : this(
        parcel.readInt(),
        parcel.readString(),
        parcel.readString(),
        parcel.readString(),
        parcel.readString(),
        parcel.readString(),
        parcel.readInt(),
        parcel.readInt(),
        parcel.readBoolean()
    ) {
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeInt(feedId)
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
        @RequiresApi(Build.VERSION_CODES.Q)
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

// 커뮤니티 상세 페이지 댓글 리사이클러뷰 아이템 데이터
data class CommentItem(
    val userImg: String,
    val nickName: String,
    val postTime: String,
    val comment: String,
    val commendId: Int
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

// 일정 응답 코드
data class EventResponse(
    val success: Boolean,
    val statusCode: Int,
    val message: String,
    val data: Map<String, List<Event>>
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
    val id: String,
    val nickName: String,
    val thumbnailUrl: String,
    val favoriteArtistId: String
)

// 커뮤니티 피드 조회
data class ArtistProfile(
    val id: Int,
    val name: String,
    val thumbnailUrl: String
)

data class Feeds(
    val id: Int,
    val content: String,
    val thumbnailUrl: String,
    val commentCount: Int,
    val likeCount: Int,
    val createdAt: String,
    val updatedAt: String,
    val userProfile: UserProfileData
)

data class CommunityData(
    val artistProfile: ArtistProfile,
    val feeds: List<Feeds>
)

// 상세 피드 조회
data class CommunityDetailData(
    val id: Int,
    val content: String,
    val thumbnailUrl: String,
    val likeCount: Int,
    val commentCount: Int,
    val createdAt: String,
    val updatedAt: String,
    val userProfile: UserProfileData,
    val comments: List<DetailComments>
)

// 댓글 조회
data class DetailComments(
    val id: Int,
    val content: String,
    val createdAt: String,
    val updatedAt: String,
    val userProfile: UserProfileData
)

// 댓글 생성 requestBody
data class CreateComment(
    val content: String
)

// 댓글 생성 responseBody
data class CreateCommentResponse(
    val commentCount: Int,
    val comment: CommentAfterCreate
)

data class CommentAfterCreate(
    val id: Int,
    val content: String,
    val createdAt: String,
    val updatedAt: String
)

// 댓글 삭제
data class AfterDeleteComment(
    val commentCount: Int
)

// 좋아요 생성
data class CreateHeart(
    val likeCount: Int
)

// 미디어 콘텐츠
data class MediaContent(
    val title: String,
    val thumbnailUrl: String,
    val publishedAt: String,
    val url: String,
    val views: Long
)

// 홈 아이템
data class HomeItem(
    val votes: List<VotesItem>,
    val communities: List<HomeCommunityItem>,
    val medias: List<HomeMediaItem>,
    val schedules: Map<String, List<HomeSchedule>>
)

data class HomeSchedule(
    val content: String,
    val type: String,
    val startAt: String
)
data class Event(
    val content: String,
    val type: String,
    val startAt: String
)

// 홈 투표
data class VotesItem(
    val id: Int,
    val name: String,
    val thumbnailUrl: String,
    val voteCount: Long,
    val rank: Int,
    val isFavorite: Boolean
)


// 홈 미디어 어댑터 아이템
data class HomeMediaItem(
    val url: String,
    val thumbnailUrl: String
)

// 홈 커뮤니티 어댑터 아이템
data class HomeCommunityItem(
    val id: Int,
    val content: String,
    val thumbnailUrl: String
)

// 프로필
data class ProfileItem(
    val userProfile: UserProfile,
    val favoriteArtistProfile: FavoriteProfile
)

// 프로필 user
data class UserProfile(
    val id: Long,
    val nickName: String,
    val thumbnailUrl: String,
    val registedAt: Int,
    val voteCount: Int,
    val contribution: Float,
    val rank: Int,
    val artistRank: Int
)

// 프로필 최애
data class FavoriteProfile(
    val id: Int,
    val name: String,
    val thumbnailUrl: String
)

// 최애 아티스트 선택
data class FavoriteArtist(
    val name: String,
    val imgUrl: String
)