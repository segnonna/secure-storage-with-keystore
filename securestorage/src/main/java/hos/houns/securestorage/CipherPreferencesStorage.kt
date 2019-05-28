package hos.houns.securestorage

import android.content.Context
import android.util.Base64

class CipherPreferencesStorage @JvmOverloads constructor(
    private val context: Context,
    private val preferenceName: String = SHARED_PREFERENCES_NAME
) : Storage {


    override fun saveString(alias: String, content: String) {
        context.getSharedPreferences(preferenceName, Context.MODE_PRIVATE)
            .edit()
            .putString(alias, content)
            .apply()
    }

    override fun getString(alias: String): String? {
        return context.getSharedPreferences(preferenceName, Context.MODE_PRIVATE)
            .getString(alias, null)
    }

    private fun saveKeyString(alias: String, value: String) {
        context.getSharedPreferences(preferenceName, Context.MODE_PRIVATE)
            .edit()
            .putString(alias, value)
            .apply()
    }

    private fun getKeyString(alias: String): String? {
        return context.getSharedPreferences(preferenceName, Context.MODE_PRIVATE)
            .getString(alias, null)
    }

    /**
     * {@inheritDoc}
     */
    override fun remove(alias: String) {
        context.getSharedPreferences(preferenceName, Context.MODE_PRIVATE)
            .edit()
            .remove(alias)
            .apply()
    }

    override fun removeAll(): Boolean {
        return context.getSharedPreferences(preferenceName, Context.MODE_PRIVATE)
            .edit()
            .clear().commit()
    }

    /**
     * {@inheritDoc}
     */
    override fun containsAlias(alias: String): Boolean {
        return context.getSharedPreferences(preferenceName, Context.MODE_PRIVATE)
            .contains(alias)
    }

    /**
     * {@inheritDoc}
     */
    override fun getKeyBytes(alias: String): ByteArray? {
        val value = getKeyString(alias)
        return if (value != null) {
            Base64.decode(value, Base64.DEFAULT)
        } else null
    }

    /**
     * {@inheritDoc}
     */
    override fun saveKeyBytes(alias: String, content: ByteArray) {
        saveKeyString(alias, Base64.encodeToString(content, Base64.DEFAULT))
    }

    companion object {
        private val SHARED_PREFERENCES_NAME = CipherPreferencesStorage::class.java.name + "_secure_storage"
    }
}
