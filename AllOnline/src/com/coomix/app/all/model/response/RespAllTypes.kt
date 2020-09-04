package com.coomix.app.all.model.response

/**
 * File Description:
 *
 * @author felixqiu
 * @since 2019-12-11.
 */
data class RespAllTypes(val data: TypeList) : RespBase()

data class TypeList(val types: MutableList<DevType>)

data class DevType(
    val type: String,
    val eid: Int,
    val create_time: Long,
    val modify_time: Long,
    val tcp: Int,
    val wire: Int,
    val offline: Int,
    val lbs: Int,
    val acc: Int,
    val power: Int,
    val lifelong: Int,
    val efence: Int,
    val head: Int,
    val test: Int,
    val gtype: String,
    val docurl: String,
    val remark: String,
    val is_goometype: Boolean
)