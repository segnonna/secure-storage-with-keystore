package hos.houns.securestorage

import com.google.gson.Gson
import hos.houns.securestorage.utils.GsonParser
import hos.houns.securestorage.utils.SecureStorageProvider.Companion.mContext
import hos.houns.securestorage.utils.SecureStorageSerializer

/**
 * Stores application data like password hash.
 */

object SecureStorage {
    private lateinit var cipherStorage: CipherStorage
    private val gson: Gson by lazy(LazyThreadSafetyMode.NONE) { Gson() }
    val gsonParser: GsonParser by lazy(LazyThreadSafetyMode.NONE) { GsonParser(gson) }
    val secureStorageSerializer: SecureStorageSerializer by lazy(LazyThreadSafetyMode.NONE) { SecureStorageSerializer() }

    fun init() {
        cipherStorage = CipherStorageFactory.newInstance(mContext.get()!!)
    }

    fun <T> setValue(alias: String, secret: T) {
        cipherStorage.encrypt(alias, secret)
    }

    fun <T> getValue(alias: String): T? {
        return cipherStorage.decrypt<T>(alias)
    }

    fun clearAll(): Boolean {
        return cipherStorage.removeAll()
    }

}
