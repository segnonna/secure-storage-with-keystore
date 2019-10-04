package hos.houns.securestorage

import android.content.Context
import com.orhanobut.hawk.Hawk


/**
 * Created by hospicehounsou on 03,October,2019
 * Dakar, Senegal.
 */
internal class StorageSharedPreferencesPreJellyBean(context: Context) :
    CipherStorage {

    init {
        Hawk.init(context).build()
    }

    override fun containsAlias(alias: String): Boolean {
        return Hawk.contains(alias)
    }

    override fun removeKey(alias: String) {
        Hawk.delete(alias)
    }

    override fun removeAll(): Boolean {
        return Hawk.deleteAll()
    }

    override fun <T> encrypt(alias: String, value: T) {
        Hawk.put(alias, value)
    }

    override fun <T> decrypt(alias: String): T? {
        return Hawk.get(alias)
    }
}