/*
 * Copyright 2018 Leonardo Rossetto
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
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

        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.

    }

    companion object {
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
                    throw KeyStoreAccessException("Could not access Keystore", e)
                } catch (e: IOException) {
                    throw KeyStoreAccessException("Could not access Keystore", e)
                }

            }
    }
}
