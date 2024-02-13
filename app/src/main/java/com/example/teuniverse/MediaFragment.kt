package com.example.teuniverse

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.teuniverse.databinding.FragmentMediaBinding
import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import retrofit2.Response

class MediaFragment : Fragment() {
    private lateinit var binding: FragmentMediaBinding
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = FragmentMediaBinding.inflate(inflater, container, false)
        return binding.root
    }

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

    private fun handleResponse(response: EventResponse) {
        
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
}