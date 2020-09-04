package com.coomix.app.all.model.bean

import java.io.Serializable

/**
 * File Description:
 *
 * @author felixqiu
 * @since 2019-12-20.
 */
data class DevImeiId(
    val type: String,
    val imei: String,
    val id: String
) {
    var response: String? = null
}

data class DevPowerMode(
    val imei: String,
    var mode: Int
) : Serializable {
    companion object {
        private const val serialVersionUID = -7958241056763114609L
        public const val OFF = 0
        public const val LOW = 1
        public const val MEDIUM = 2
        public const val HIGH = 3
    }
}