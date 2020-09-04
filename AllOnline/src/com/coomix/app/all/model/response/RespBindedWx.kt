package com.coomix.app.all.model.response

/**
 * File Description:
 *
 * @author felixqiu
 * @since 2020-01-03.
 */
data class RespBindedWx(val data: BindedWxAccount) : RespBase()

data class BindedWxAccount(
    val openid: String,
    val account_type: Int,
    val login_name: String,
    val imei: String
)