package com.coomix.app.all.model.response

/**
 * File Description:
 *
 * @author felixqiu
 * @since 2019-12-12.
 */
class RespTypeCmds(val data: MutableList<TypeCmd>) : RespBase()

data class TypeCmd(
    val type: String,
    val cmd: MutableList<Cmd>
)

data class Cmd(
    val cmd_index: Int,
    val cmd_name: String,
    val head: String,
    val tail: String,
    val pwd: Boolean,
    val remark: String,
    val cmd_type: Int,
    val cmd_pri: Int,
    val param: MutableList<CmdParam>
)

data class CmdParam(
    val param_index: Int,
    val sp: String,
    val param_name: String,
    val param_type: Int,
    val def: String,
    val min: String,
    val max: String,
    var pval: String,
    val enum: MutableList<ParamEnum>
)

data class ParamEnum(
    val enum_index: Int,
    val ekey: String,
    val eval: String
)

data class SendCmd(val data: MutableList<TypeCmd>)