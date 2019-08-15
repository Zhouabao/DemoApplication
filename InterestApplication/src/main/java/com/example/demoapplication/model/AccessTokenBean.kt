package com.example.demoapplication.model

data class AccessTokenBean(
    val access_token: String? = "",
    val error: String? = "",
    val error_description: String? = "",
    val expires_in: Int? = 0,
    val refresh_token: String? = "",
    val scope: String? = "",
    val session_key: String? = "",
    val session_secret: String? = ""
)

data class MatchFaceBean(
    val cached: Int? = 0,
    val error_code: Int? = 0,
    val error_msg: String? = "",
    val log_id: Long? = 0,
    val result: Result? = Result(),
    val timestamp: Int? = 0
)

data class Result(
    val face_list: MutableList<ResultBean>? = mutableListOf(),
    val score: Double? = 0.0
)

data class ResultBean(
    val face_token: String?=""
)

