package com.side.project.foodmap.data.remote.tdx

data class TdxTokenReq(
    val grant_type: String,
    val client_id: String,
    val client_secret: String
) {
    constructor() : this("", "", "")
}

data class TdxTokenRes(
    val access_token: String,
    val expires_in: Int,
    val not_before_policy: Int,
    val refresh_expires_in: Int,
    val scope: String,
    val token_type: String
) {
    constructor() : this("", 0, 0, 0, "", "")
}