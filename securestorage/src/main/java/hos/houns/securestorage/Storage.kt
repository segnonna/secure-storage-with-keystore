package hos.houns.securestorage

import hos.houns.securestorage.utils.SensitiveData

/**
 * Created by hospicehounsou on 28,June,2018
 * Dakar, Senegal.
 */

interface Storage {

    fun <T> put(key: String?, value: SensitiveData<T>): Boolean

    fun get(key: String?): SensitiveData<*>
    fun getAll(): MutableMap<String, *>?

    fun delete(key: String?): Boolean

    fun deleteAll(): Boolean

    fun count(): Long

    operator fun contains(key: String?): Boolean

}