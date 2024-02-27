package com.example.teuniverse

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.lifecycleScope
import com.example.teuniverse.databinding.FragmentProfileBinding
import com.google.android.material.tabs.TabLayoutMediator
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import retrofit2.Response

class ProfileFragment : Fragment() {
    private lateinit var  binding: FragmentProfileBinding
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = FragmentProfileBinding.inflate(inflater, container, false)

        binding.imgBtnVote.setOnClickListener {
            showPopupMissionDialog()
        }

        lifecycleScope.launch {
            getNumberOfVotesApi()
        }

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val profilePagerAdapter = ProfilePagerAdapter(childFragmentManager, lifecycle)
        profilePagerAdapter.addFragment(ProfileCommunityTabFragment(), "커뮤니티")
        profilePagerAdapter.addFragment(ProfileUserTabFragment(), "회원정보")

        binding.tabViewPager.adapter = profilePagerAdapter

        TabLayoutMediator(binding.profileTablayout, binding.tabViewPager) { tab, position ->
            tab.text = profilePagerAdapter.getTitle(position)
        }.attach()
    }

    // 보유 투표권 조회 api
    private suspend fun getNumberOfVotesApi() {
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

    private fun handleError(errorMessage: String) {
        // 에러를 처리하는 코드
        Log.d("communityFeedsApi 함수 Error", errorMessage)
    }

    private fun showPopupMissionDialog() {
        val popupVoteMission = PopupVoteMission(requireContext())
        popupVoteMission.show()

        popupVoteMission.setOnDismissListener {
            Log.d("popupVoteMission", "PopupVote dialog dismissed.")
        }
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