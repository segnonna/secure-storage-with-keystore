package hos.houns.securestorage

import android.content.Context
import java.security.KeyStore
import java.security.KeyStoreException

internal abstract class BaseCipherStorage(val context: Context, val storage: Storage) :
    CipherStorage {

    /**
     * {@inheritDoc}
     */
    override fun containsAlias(alias: String): Boolean {
        try {
            return keyStoreAndLoad.containsAlias(alias) && storage.containsAlias(alias)
        } catch (e: KeyStoreException) {
            throw KeyStoreAccessException("Failed to access Keystore", e)
        }

    }

    /**
     * {@inheritDoc}
     */
    override fun removeKey(alias: String) {
        try {
            if (containsAlias(alias)) {
                keyStoreAndLoad.deleteEntry(alias)
                storage.remove(alias)
            }
        } catch (e: KeyStoreException) {
            throw KeyStoreAccessException("Failed to access Keystore", e)
        }

    }

    override fun removeAll(): Boolean {
        return storage.removeAll()
    }

    companion object {
        private const val AES_TAG_PREFIX = "aes!"
        private const val TYPE_TAG_PREFIX = "type!"
        fun makeAesTagForAlias(alias: String): String {
            return AES_TAG_PREFIX + alias
        }

        fun makeTypeTagForAlias(alias: String): String {
            return TYPE_TAG_PREFIX + alias
        }
        const val ANDROID_KEY_STORE = "AndroidKeyStore"

        val keyStoreAndLoad: KeyStore = KeyStore.getInstance(ANDROID_KEY_STORE).apply {
            load(null)
        }

    }
}
