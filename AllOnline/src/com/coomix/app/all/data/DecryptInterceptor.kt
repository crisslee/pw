package com.coomix.app.all.data

import android.util.Log
import com.coomix.app.all.AllOnlineApp
import com.coomix.app.framework.util.OSUtil
import com.coomix.security.Security
import okhttp3.Interceptor
import okhttp3.Interceptor.Chain
import okhttp3.Response
import okhttp3.ResponseBody
import java.nio.charset.Charset

/**
 * File Description:
 *
 * @author felixqiu
 * @since 2019-12-12.
 */
class DecryptInterceptor : Interceptor {
    val TAG = DecryptInterceptor::class.java.simpleName
    private val text = "l1pPukuVJikaU5ge"
    private val pac = "bus.coomix.com"
    private val ver = "3.7.0"
    private val resultEncryptMethods = mutableListOf<String>("/1/devices/tracking")
    val security: Security = AllOnlineApp.getInstantce().security

    override fun intercept(chain: Chain): Response {
        val request = chain.request()
        val decrypt = resultEncryptMethods.any {
            request.url().toString().contains(it)
        }
        if (!decrypt) {
            return chain.proceed(request)
        }
        var response: Response
        try {
            response = chain.proceed(request)
        } catch (e: Exception) {
            e.printStackTrace()
            throw e
        }
        if (response.isSuccessful) {
            val responseBody = response.body()
            if (responseBody != null) {
                try {
                    val source = responseBody.source()
                    source.request(Long.MAX_VALUE)
                    val buffer = source.buffer()
                    var charset = Charset.forName("UTF-8")
                    val contentType = responseBody.contentType()
                    charset = contentType?.charset(charset) ?: charset
                    val bodyString = buffer.clone().readString(charset)
                    //解密
                    val salt = OSUtil.toMD5((System.currentTimeMillis() / 1000).toString() + text + pac)
                    val newData = security.decodeProcess(bodyString, salt, ver)
                    val newBody = ResponseBody.create(contentType, newData.trim())
                    response = response.newBuilder().body(newBody).build()
                } catch (e: Exception) {
                    e.printStackTrace()
                    return response
                }
            } else {
                Log.i(TAG, "response body is null")
            }
        }
        return response
    }
}