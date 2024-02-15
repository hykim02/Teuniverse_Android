package com.example.teuniverse

import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.RequiresApi
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import com.example.teuniverse.databinding.FragmentMediaBinding
import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import retrofit2.Response
import java.text.SimpleDateFormat
import java.time.Instant
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.Locale
import java.util.TimeZone

class MediaFragment : Fragment() {
    private lateinit var binding: FragmentMediaBinding
    private lateinit var mediaList: ArrayList<MediaContent>
    private lateinit var mediaAdapter: MediaAdapter
    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = FragmentMediaBinding.inflate(inflater, container, false)
        mediaList = ArrayList()
        mediaAdapter = MediaAdapter(mediaList)

        binding.imgBtnVote.setOnClickListener {
            showPopupMissionDialog()
        }

        binding.imgBtnPerson.setOnClickListener {
            findNavController().navigate(R.id.action_navigation_media_to_profileFragment)
        }

        lifecycleScope.launch{
            mediaContentsApi()
            getNumberOfVotes()
        }
        return binding.root
    }

    // 일정 시간 추출 함수
    @RequiresApi(Build.VERSION_CODES.O)
    fun setTime(input: String): String {
        val inputFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.getDefault())
        val outputFormat = SimpleDateFormat("yyyy.MM.dd", Locale.getDefault())
        // TimeZone을 UTC로 설정
        inputFormat.timeZone = TimeZone.getTimeZone("UTC")
        // 주어진 문자열을 Date 객체로 변환
        val date = inputFormat.parse(input)
        // 변환된 Date를 원하는 형식으로 포맷
        return outputFormat.format(date!!)
    }

    //요약 보여주기 - 공지 전체내용 중 일부(15자)만 가져옴
    //15자 이상인 경우 일부를 자르고 "..."을 붙임
    private fun getContextPreview(context: String): String {
        return if (context.isNotEmpty()) {
            val trimmedText = if (context.length > 30) "${context.substring(0, 15)}..." else context
            trimmedText
        } else {
            ""
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private suspend fun mediaContentsApi() {
        Log.d("mediaContentsApi", "호출 성공")
        val accessToken = getAccessToken()
        try {
            if (accessToken != null) {
                val response: Response<ArtistServerResponse<MediaContent>> = withContext(
                    Dispatchers.IO) {
                    MediaInstance.mediaService().getContents(accessToken)
                }
                if (response.isSuccessful) {
                    val theContent: ArtistServerResponse<MediaContent>? = response.body()
                    if (theContent != null) {
                        Log.d("mediaContentsApi", "${theContent.statusCode} ${theContent.message}")
                        handleResponse(theContent)
                    } else {
                        handleError("Response body is null.")
                    }
                } else {
                    handleError("mediaContentsApi Error: ${response.code()} - ${response.message()}")
                }
            }
        }
        catch (e: Exception) {
            handleError(e.message ?: "Unknown error occurred.")
        }
    }

    // 투표권 개수 가져오기
    private suspend fun getNumberOfVotes() {
        Log.d("getNumberOfVotes 함수", "호출 성공")
        val accessToken = getAccessToken()
        try {
            if (accessToken != null) {
                val response: Response<ServerResponse<NumberOfVote>> = withContext(Dispatchers.IO) {
                    VoteCountInstance.getVotesService().getVotes(accessToken)
                }
                if (response.isSuccessful) {
                    val theVote: ServerResponse<NumberOfVote>? = response.body()
                    if (theVote != null) {
                        Log.d("response", "${theVote.statusCode} ${theVote.message}")
                        handleTheVotes(theVote)
                    } else {
                        handleError("Response body is null.")
                    }
                } else {
                    handleError("getNumverOfVotes함수 Error: ${response.code()} - ${response.message()}")
                }
            }
        }
        catch (e: Exception) {
            handleError(e.message ?: "Unknown error occurred.")
        }
    }

    // 보유 투표권 개수 조회
    private fun handleTheVotes(votes: ServerResponse<NumberOfVote>) {
        Log.d("handleTheVotes 함수","호출 성공" )
        binding.voteCount.text = votes.data.voteCount.toString()
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun handleResponse(response: ArtistServerResponse<MediaContent>) {
        val serverDatas = response.data
        mediaList.clear()

        for (i in serverDatas.indices) {
            val date = setTime(serverDatas[i].publishedAt)
            val title = getContextPreview(serverDatas[i].title)
            val thumbnail = serverDatas[i].thumbnailUrl
            val link = serverDatas[i].url
            val views = serverDatas[i].views

            mediaList.add(MediaContent(title, thumbnail, date, link, views))
        }
        // 리사이클러뷰 어댑터 연결(2열)
        binding.rvMedia.layoutManager = GridLayoutManager(activity, 2)
        binding.rvMedia.adapter = mediaAdapter
        // 어댑터에 데이터가 변경되었음을 알리기
        mediaAdapter.notifyDataSetChanged()
    }

    private fun handleError(errorMessage: String) {
        // 에러를 처리하는 코드
        Log.d("미디어Api Error", errorMessage)
    }

    // db에서 토큰 가져오기
    private fun getAccessToken(): String? {
        MainActivity.ServiceAccessTokenDB.init(requireContext())
        val serviceTokenDB = MainActivity.ServiceAccessTokenDB.getInstance()
        var accessToken: String? = null

        for ((key, value) in serviceTokenDB.all) {
            if (key == "accessToken") {
                accessToken = "Bearer " + value.toString()
            }
        }
        return accessToken
    }

    private fun showPopupMissionDialog() {
        val popupVoteMission = PopupVoteMission(requireContext())
        popupVoteMission.show()

        popupVoteMission.setOnDismissListener {
            Log.d("popupVoteMission", "PopupVote dialog dismissed.")
        }
    }
}