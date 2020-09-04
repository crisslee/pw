package com.coomix.app.all.model.response

/**
 * File Description:
 *
 * @author felixqiu
 * @since 2019-12-20.
 */
data class RespResponse(val data: Response) : RespBase()

data class Response(val response: String)