package com.example.teuniverse

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity

class PopupVoteCheck(context: Context, private val okCallback: (String) -> Unit): Dialog(context) {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.popup_vote_check)
    }
}