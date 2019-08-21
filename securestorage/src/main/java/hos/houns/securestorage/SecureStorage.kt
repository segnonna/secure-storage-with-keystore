package hos.houns.securestorage

import android.content.Context
import com.google.gson.Gson
import hos.houns.securestorage.utils.GsonParser
import hos.houns.securestorage.utils.SecureStorageProvider.Companion.mContext
import hos.houns.securestorage.utils.SecureStorageSerializer

/**
 * Stores application data like password hash.
 */

object SecureStorage {
    private var cipherStorage: CipherStorage? = null
    private val gson: Gson by lazy(LazyThreadSafetyMode.NONE) { Gson() }
    internal val gsonParser: GsonParser by lazy(LazyThreadSafetyMode.NONE) { GsonParser(gson) }
    internal val secureStorageSerializer: SecureStorageSerializer by lazy(LazyThreadSafetyMode.NONE) { SecureStorageSerializer() }

    fun init(context: Context? = null) {
        context?.let {
            cipherStorage = CipherStorageFactory.newInstance(context)
        } ?: run {
            cipherStorage = CipherStorageFactory.newInstance(mContext.get()!!)
        }
    }

    fun <T> setValue(alias: String, secret: T) {
        try {
            cipherStorage?.encrypt(alias, secret)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun <T> getValue(alias: String): T? {
        return cipherStorage?.decrypt<T>(alias)
    }

    fun clearAll(): Boolean? {
        return cipherStorage?.removeAll()
    }

}
