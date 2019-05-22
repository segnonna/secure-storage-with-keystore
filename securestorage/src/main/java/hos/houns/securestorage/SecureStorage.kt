package hos.houns.securestorage

import android.content.Context
import java.lang.ref.WeakReference

/**
 * Stores application data like password hash.
 */

object SecureStorage {

    internal lateinit var mContext: WeakReference<Context>

    fun init(context: Context) {
        mContext = WeakReference(context)
    }

    fun <T> setValue(alias: String, secret: T) {
        StorageImpl().saveSensitiveData(alias, secret)
    }

    fun <T> getValue(alias: String): T? {
        return StorageImpl().getSensitiveData<T>(alias)
    }

    fun clearAll(): Boolean {
        return StorageImpl().clear()
    }

}
