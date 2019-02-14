package hos.houns.seckeystore

/**
 * Created by hospicehounsou on 28,June,2018
 * Dakar, Senegal.
 */

interface Storage {

    fun <T> put(key: String?, value: SimpleKeystore.SensitiveData<T>): Boolean

    fun get(key: String?): SimpleKeystore.SensitiveData<*>
    fun getAll(): MutableMap<String, *>?

    fun delete(key: String?): Boolean

    fun deleteAll(): Boolean

    fun count(): Long

    operator fun contains(key: String?): Boolean

}