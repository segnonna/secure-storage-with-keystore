package hos.houns.securestorage.utils

/**
 * Created by hospicehounsou on 28,June,2018
 * Dakar, Senegal.
 */


import java.lang.reflect.Type

interface Parser {

    @Throws(Exception::class)
    fun <T> fromJson(content: String?, type: Type?): T?

    fun toJson(body: Any?): String?

}