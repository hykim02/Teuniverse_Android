package com.example.teuniverse

import android.content.Context
import android.content.SharedPreferences

// 1-12월 모든 DB 관리하는 object
object MonthDBManager {
    private const val MONTH_COUNT = 12
    private val monthDBMap: MutableMap<Int, SharedPreferences> = mutableMapOf()

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
//object Month1_DB {
//    private lateinit var sharedPreferences: SharedPreferences
//    // 초기화
//    fun init(context: Context) {
//        sharedPreferences = context.getSharedPreferences("1", Context.MODE_PRIVATE)
//    }
//
//    // 객체 반환
//    fun getInstance(): SharedPreferences {
//        if(!this::sharedPreferences.isInitialized) {
//            throw IllegalStateException("SharedPreferencesSingleton is not initialized")
//        }
//        return sharedPreferences
//    }
//}
//
//object Month2_DB {
//    private lateinit var sharedPreferences: SharedPreferences
//    // 초기화
//    fun init(context: Context) {
//        sharedPreferences = context.getSharedPreferences("2", Context.MODE_PRIVATE)
//    }
//
//    // 객체 반환
//    fun getInstance(): SharedPreferences {
//        if(!this::sharedPreferences.isInitialized) {
//            throw IllegalStateException("SharedPreferencesSingleton is not initialized")
//        }
//        return sharedPreferences
//    }
//}
//
//object Month3_DB {
//    private lateinit var sharedPreferences: SharedPreferences
//    // 초기화
//    fun init(context: Context) {
//        sharedPreferences = context.getSharedPreferences("3", Context.MODE_PRIVATE)
//    }
//
//    // 객체 반환
//    fun getInstance(): SharedPreferences {
//        if(!this::sharedPreferences.isInitialized) {
//            throw IllegalStateException("SharedPreferencesSingleton is not initialized")
//        }
//        return sharedPreferences
//    }
//}
//
//object Month4_DB {
//    private lateinit var sharedPreferences: SharedPreferences
//    // 초기화
//    fun init(context: Context) {
//        sharedPreferences = context.getSharedPreferences("4", Context.MODE_PRIVATE)
//    }
//
//    // 객체 반환
//    fun getInstance(): SharedPreferences {
//        if(!this::sharedPreferences.isInitialized) {
//            throw IllegalStateException("SharedPreferencesSingleton is not initialized")
//        }
//        return sharedPreferences
//    }
//}
//
//object Month5_DB {
//    private lateinit var sharedPreferences: SharedPreferences
//    // 초기화
//    fun init(context: Context) {
//        sharedPreferences = context.getSharedPreferences("5", Context.MODE_PRIVATE)
//    }
//
//    // 객체 반환
//    fun getInstance(): SharedPreferences {
//        if(!this::sharedPreferences.isInitialized) {
//            throw IllegalStateException("SharedPreferencesSingleton is not initialized")
//        }
//        return sharedPreferences
//    }
//}
//
//object Month6_DB {
//    private lateinit var sharedPreferences: SharedPreferences
//    // 초기화
//    fun init(context: Context) {
//        sharedPreferences = context.getSharedPreferences("6", Context.MODE_PRIVATE)
//    }
//
//    // 객체 반환
//    fun getInstance(): SharedPreferences {
//        if(!this::sharedPreferences.isInitialized) {
//            throw IllegalStateException("SharedPreferencesSingleton is not initialized")
//        }
//        return sharedPreferences
//    }
//}
//
//object Month7_DB {
//    private lateinit var sharedPreferences: SharedPreferences
//    // 초기화
//    fun init(context: Context) {
//        sharedPreferences = context.getSharedPreferences("7", Context.MODE_PRIVATE)
//    }
//
//    // 객체 반환
//    fun getInstance(): SharedPreferences {
//        if(!this::sharedPreferences.isInitialized) {
//            throw IllegalStateException("SharedPreferencesSingleton is not initialized")
//        }
//        return sharedPreferences
//    }
//}
//
//object Month8_DB {
//    private lateinit var sharedPreferences: SharedPreferences
//    // 초기화
//    fun init(context: Context) {
//        sharedPreferences = context.getSharedPreferences("8", Context.MODE_PRIVATE)
//    }
//
//    // 객체 반환
//    fun getInstance(): SharedPreferences {
//        if(!this::sharedPreferences.isInitialized) {
//            throw IllegalStateException("SharedPreferencesSingleton is not initialized")
//        }
//        return sharedPreferences
//    }
//}
//
//object Month9_DB {
//    private lateinit var sharedPreferences: SharedPreferences
//    // 초기화
//    fun init(context: Context) {
//        sharedPreferences = context.getSharedPreferences("9", Context.MODE_PRIVATE)
//    }
//
//    // 객체 반환
//    fun getInstance(): SharedPreferences {
//        if(!this::sharedPreferences.isInitialized) {
//            throw IllegalStateException("SharedPreferencesSingleton is not initialized")
//        }
//        return sharedPreferences
//    }
//}
//
//object Month10_DB {
//    private lateinit var sharedPreferences: SharedPreferences
//    // 초기화
//    fun init(context: Context) {
//        sharedPreferences = context.getSharedPreferences("10", Context.MODE_PRIVATE)
//    }
//
//    // 객체 반환
//    fun getInstance(): SharedPreferences {
//        if(!this::sharedPreferences.isInitialized) {
//            throw IllegalStateException("SharedPreferencesSingleton is not initialized")
//        }
//        return sharedPreferences
//    }
//}
//
//object Month11_DB {
//    private lateinit var sharedPreferences: SharedPreferences
//    // 초기화
//    fun init(context: Context) {
//        sharedPreferences = context.getSharedPreferences("11", Context.MODE_PRIVATE)
//    }
//
//    // 객체 반환
//    fun getInstance(): SharedPreferences {
//        if(!this::sharedPreferences.isInitialized) {
//            throw IllegalStateException("SharedPreferencesSingleton is not initialized")
//        }
//        return sharedPreferences
//    }
//}
//
//object Month12_DB {
//    private lateinit var sharedPreferences: SharedPreferences
//    // 초기화
//    fun init(context: Context) {
//        sharedPreferences = context.getSharedPreferences("12", Context.MODE_PRIVATE)
//    }
//
//    // 객체 반환
//    fun getInstance(): SharedPreferences {
//        if(!this::sharedPreferences.isInitialized) {
//            throw IllegalStateException("SharedPreferencesSingleton is not initialized")
//        }
//        return sharedPreferences
//    }
//}