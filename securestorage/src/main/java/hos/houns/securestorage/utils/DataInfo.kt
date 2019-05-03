package hos.houns.securestorage.utils

import java.io.Serializable
import java.util.*

/**
 * Created by hospicehounsou on 28,June,2018
 * Dakar, Senegal.
 */

data class DataInfo(val dataType: Char, val cipherText: String, val keyClazz: Class<*>, val valueClazz: Class<*>) {
    companion object {
        const val TYPE_OBJECT = '0'
        const val TYPE_LIST = '1'
        const val TYPE_MAP = '2'
        const val TYPE_SET = '3'
    }
}


data class SensitiveData<T>(
    val alias: String,
    val secret: T,
    val createDate: Date,
    val updateDate: Date
) : Serializable