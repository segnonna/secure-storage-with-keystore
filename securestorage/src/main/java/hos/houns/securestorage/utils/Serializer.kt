package hos.houns.securestorage.utils

/**
 * Created by hospicehounsou on 28,June,2018
 * Dakar, Senegal.
 */

interface Serializer {
    fun <T> getType(value: T): String
    fun getClassType(value: String): Class<*>
}