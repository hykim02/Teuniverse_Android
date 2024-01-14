package com.example.teuniverse

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class CommunityFragment : Fragment() {

    private lateinit var rvCommunity: RecyclerView
    private lateinit var postList: ArrayList<CommunityPostItem>
    private lateinit var communityAdapter: CommunityPostAdapter
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_community, container, false)

        rvCommunity = view.findViewById(R.id.rv_post)
        postList = ArrayList()
        communityAdapter = CommunityPostAdapter(postList)

        // 리사이클러뷰 어댑터 연결
        rvCommunity.adapter = communityAdapter
        rvCommunity.layoutManager = LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)

        return view
    }
}