package com.example.teuniverse

import android.content.Context
import android.content.SharedPreferences
import java.io.File

// 1-12월 모든 DB 관리하는 object
object MonthDBManager {
    private const val MONTH_COUNT = 12
    private val monthDBMap: MutableMap<Int, SharedPreferences> = mutableMapOf()

    fun doesSharedPreferencesFileExist(context: Context, monthNumber: String): Boolean {
        val sharedPrefsFile = File("${context.filesDir.parent}/shared_prefs/$monthNumber.xml")
        return sharedPrefsFile.exists()
    }

    // Initialize all Month_DB instances
    fun initAll(context: Context) {
        for (monthNumber in 1..MONTH_COUNT) {
            initMonth(context, monthNumber)
        }
    }

    // Initialize a specific Month_DB instance
    fun initMonth(context: Context, monthNumber: Int) {
        val sharedPreferences = context.getSharedPreferences("$monthNumber", Context.MODE_PRIVATE)
        monthDBMap[monthNumber] = sharedPreferences
    }

    // Get a specific Month_DB instance
    fun getMonthInstance(monthNumber: Int): SharedPreferences {
        return monthDBMap[monthNumber] ?: throw IllegalStateException("Month_DB $monthNumber is not initialized")
    }
}

// 스케줄 타입 상태 DB
object ScheduleTypeDB {
    private lateinit var sharedPreferences: SharedPreferences
    // 초기화
    fun init(context: Context) {
        sharedPreferences = context.getSharedPreferences("ScheduleType", Context.MODE_PRIVATE)
    }

    // 객체 반환
    fun getInstance(): SharedPreferences {
        if(!this::sharedPreferences.isInitialized) {
            throw IllegalStateException("SharedPreferencesSingleton is not initialized")
        }
        return sharedPreferences
    }
}

// 하트 클릭 상태 DB
object HeartStateDB {
    private lateinit var sharedPreferences: SharedPreferences
    // 초기화
    fun init(context: Context) {
        sharedPreferences = context.getSharedPreferences("HeartState", Context.MODE_PRIVATE)
    }

    // 객체 반환
    fun getInstance(): SharedPreferences {
        if(!this::sharedPreferences.isInitialized) {
            throw IllegalStateException("SharedPreferencesSingleton is not initialized")
        }
        return sharedPreferences
    }
}

// 미션 제한 설정 DB
object VoteMissionDB {
    private lateinit var sharedPreferences: SharedPreferences
    // 초기화
    fun init(context: Context) {
        sharedPreferences = context.getSharedPreferences("VoteMission", Context.MODE_PRIVATE)
    }

    // 객체 반환
    fun getInstance(): SharedPreferences {
        if(!this::sharedPreferences.isInitialized) {
            throw IllegalStateException("SharedPreferencesSingleton is not initialized")
        }
        return sharedPreferences
    }

    fun doesFileExist(context: Context): Boolean {
        val sharedPrefsFile = File("${context.filesDir.parent}/shared_prefs/VoteMission.xml")
        return sharedPrefsFile.exists()
    }
}

// 서버에서 받은 서비스 access 토큰 저장
object ServiceAccessTokenDB {
    private lateinit var sharedPreferences: SharedPreferences
    // 초기화
    fun init(context: Context) {
        sharedPreferences = context.getSharedPreferences("ServiceToken", Context.MODE_PRIVATE)
    }

    // 객체 반환
    fun getInstance(): SharedPreferences {
        if(!this::sharedPreferences.isInitialized) {
            throw IllegalStateException("SharedPreferencesSingleton is not initialized")
        }
        return sharedPreferences
    }
}

// 로그인 후 서버에서 유저ID 받아와서 저장
object UserInfoDB {
    private lateinit var sharedPreferences: SharedPreferences
    // 초기화
    fun init(context: Context) {
        sharedPreferences = context.getSharedPreferences("UserInfo", Context.MODE_PRIVATE)
    }

    // 객체 반환
    fun getInstance(): SharedPreferences {
        if(!this::sharedPreferences.isInitialized) {
            throw IllegalStateException("SharedPreferencesSingleton is not initialized")
        }
        return sharedPreferences
    }

    fun doesFileExist(context: Context): Boolean {
        val sharedPrefsFile = File("${context.filesDir.parent}/shared_prefs/UserInfo.xml")
        return sharedPrefsFile.exists()
    }
}