package hos.houns.securestorage

import android.content.Context
import com.google.gson.Gson
import hos.houns.securestorage.utils.GsonParser
import hos.houns.securestorage.utils.SecureStorageSerializer
import java.lang.ref.WeakReference

/**
 * Stores application data like password hash.
 */

object SecureStorage {

    private lateinit var cipherStorage: CipherStorage
    private lateinit var mContext: WeakReference<Context>
    private val gson: Gson by lazy(LazyThreadSafetyMode.NONE) { Gson() }
    val gsonParser: GsonParser by lazy(LazyThreadSafetyMode.NONE) { GsonParser(gson) }
    val secureStorageSerializer: SecureStorageSerializer by lazy(LazyThreadSafetyMode.NONE) { SecureStorageSerializer() }


    fun init(context: Context) {
        mContext = WeakReference(context)
        cipherStorage = CipherStorageFactory.newInstance(mContext.get()!!)
    }

    fun <T> setValue(alias: String, secret: T) {
        cipherStorage.encrypt(alias, secret)
        //StorageImpl().saveSensitiveData(alias, secret)
    }

    fun <T> getValue(alias: String): T? {
        return cipherStorage.decrypt<T>(alias)
    }

    fun clearAll(): Boolean {
        return cipherStorage.removeAll()
    }

}
