package com.chancehee.unsplashapp_tutorial.utils

// 로그 설정을 위한 변수
object Constant {
    const val TAG : String = "로그"
}

// 선택된 것이 사진인가 사용자인가 판단 하기 위한 변수
enum class SEARCH_TYPE {
    PHOTO,
    USER
}