package com.example.teuniverse

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.example.teuniverse.databinding.FragmentProfileUserTabBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import retrofit2.Response

class ProfileUserTabFragment : Fragment() {
    private lateinit var binding: FragmentProfileUserTabBinding
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentProfileUserTabBinding.inflate(inflater, container, false)

        lifecycleScope.launch {
            profileApi()
        }

        // 수정하기 버튼
        binding.editBtn.setOnClickListener {
            showPopupEditDialog()
        }

        binding.editTv.setOnClickListener {
            showPopupEditDialog()
        }

        // 최애 아티스트 변경
        binding.modifyBtn.setOnClickListener {
            val intent = Intent(requireContext(), SignupSelectArtistActivity::class.java)
            startActivity(intent)
        }

        binding.modifyTv.setOnClickListener {
            val intent = Intent(requireContext(), SignupSelectArtistActivity::class.java)
            startActivity(intent)
        }
        // Inflate the layout for this fragment
        return binding.root
    }

    // 프로필 api
    private suspend fun profileApi() {
        Log.d("profileApi", "호출 성공")
        val accessToken = getAccessToken()
        try {
            if (accessToken != null) {
                val response: Response<ServerResponse<ProfileItem>> = withContext(
                    Dispatchers.IO) {
                    ProfileInstance.profileService().getProfileData(accessToken)
                }
                if (response.isSuccessful) {
                    val theProfile: ServerResponse<ProfileItem>? = response.body()
                    if (theProfile != null) {
                        Log.d("profileApi", "${theProfile.statusCode} ${theProfile.message}")
                        handleResponse(theProfile)
                    } else {
                        handleError("Response body is null.")
                    }
                } else {
                    handleError("profileApi Error: ${response.code()} - ${response.message()}")
                }
            }
        }
        catch (e: Exception) {
            handleError(e.message ?: "Unknown error occurred.")
        }
    }

    private fun handleResponse(response: ServerResponse<ProfileItem>) {
        val userData = response.data.userProfile
        val favoriteData = response.data.favoriteArtistProfile

        // user
        Glide.with(this)
            .load(userData.thumbnailUrl)
            .apply(RequestOptions.circleCropTransform()) // 이미지뷰 모양에 맞추기
            .into(binding.userProfile)

        binding.userNickname.text = userData.nickName
        binding.registedAt.text = "D+${userData.registedAt}"
        binding.vote.text = userData.voteCount.toString()
        binding.contribution.text = String.format("%.2f", userData.contribution) + "%"
        binding.allRank.text = "(전체)" + userData.rank + "위"
        binding.bestRank.text = "(최애)" + userData.artistRank + "위"

        // profile
        Glide.with(this)
            .load(favoriteData.thumbnailUrl)
            .apply(RequestOptions.circleCropTransform()) // 이미지뷰 모양에 맞추기
            .into(binding.artistImg)

        binding.artistName.text = favoriteData.name
    }

    private fun handleError(errorMessage: String) {
        // 에러를 처리하는 코드
        Log.d("프로필 Api Error", errorMessage)
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

    // 수정하기 팝업창
    private fun showPopupEditDialog() {
        val popupProfileEdit = PopupProfileEdit(requireContext())
        popupProfileEdit.show()

        popupProfileEdit.setOnDismissListener {
            Log.d("popupProfileEdit", "popupProfileEdit dialog dismissed.")
        }
    }
}