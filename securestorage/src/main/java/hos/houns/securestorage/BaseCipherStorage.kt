package hos.houns.securestorage

import android.content.Context

import java.io.IOException
import java.security.KeyStore
import java.security.KeyStoreException
import java.security.NoSuchAlgorithmException
import java.security.cert.CertificateException

internal abstract class BaseCipherStorage(val context: Context, val storage: Storage) :
    CipherStorage {

    /**
     * {@inheritDoc}
     */
    override fun containsAlias(alias: String): Boolean {
        try {
            val keyStore = keyStoreAndLoad
            return keyStore.containsAlias(alias) && storage.containsAlias(alias)
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
                val keyStore = keyStoreAndLoad
                keyStore.deleteEntry(alias)
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
        val keyStoreAndLoad: KeyStore
            get() {
                try {
                    val keyStore = KeyStore.getInstance(ANDROID_KEY_STORE)
                    keyStore.load(null)
                    return keyStore
                } catch (e: NoSuchAlgorithmException) {
                    throw KeyStoreAccessException("Could not access Keystore", e)
                } catch (e: CertificateException) {
                    throw KeyStoreAccessException("Could not access Keystore", e)
                } catch (e: KeyStoreException) {
                    e.printStackTrace()
                    throw KeyStoreAccessException("Could not access Keystore", e)

                } catch (e: IOException) {
                    throw KeyStoreAccessException("Could not access Keystore", e)
                }

            }
    }
}
