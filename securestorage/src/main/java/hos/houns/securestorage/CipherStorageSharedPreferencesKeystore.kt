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

import android.annotation.TargetApi
import android.content.Context
import android.os.Build
import android.security.KeyPairGeneratorSpec
import hos.houns.securestorage.SecureStorage.gsonParser
import hos.houns.securestorage.SecureStorage.secureStorageSerializer
import java.math.BigInteger
import java.nio.charset.Charset
import java.security.*
import java.security.spec.AlgorithmParameterSpec
import java.util.*
import javax.crypto.*
import javax.crypto.spec.SecretKeySpec
import javax.security.auth.x500.X500Principal

@TargetApi(Build.VERSION_CODES.LOLLIPOP_MR1)
internal class CipherStorageSharedPreferencesKeystore(context: Context, storage: Storage) :
    BaseCipherStorage(context, storage) {

    /**
     * {@inheritDoc}
     */
    override fun <T> encrypt(alias: String, value: T) {

        val entry =
            getKeyStoreEntry(true, alias) ?: throw CryptoFailedException("Unable to generate key for alias $alias")

        val key = entry as KeyStore.PrivateKeyEntry
        val encryptedData = encryptData(alias, value, key.certificate.publicKey)

        storage.saveKeyBytes(alias, encryptedData)
    }

    /**
     * {@inheritDoc}
     */
    override fun <T> decrypt(alias: String): T? {
        val entry = getKeyStoreEntry(false, alias) ?: return null
        val key = entry as KeyStore.PrivateKeyEntry
        return decryptData(alias, key.privateKey)
    }

    /**
     * {@inheritDoc}
     */
    override fun containsAlias(alias: String): Boolean {
        return super.containsAlias(alias) && storage.containsAlias(
            makeAesTagForAlias(
                alias
            )
        )
    }

    /**
     * {@inheritDoc}
     */
    override fun removeKey(alias: String) {
        super.removeKey(alias)
        storage.remove(makeAesTagForAlias(alias))
    }

    private fun <T> decryptData(alias: String, privateKey: PrivateKey): T? {
        val encryptedData = storage.getKeyBytes(alias)
        val secretData = storage.getKeyBytes(
            makeAesTagForAlias(
                alias
            )
        )
        if (encryptedData == null || secretData == null) {
            return null
        }

        val decryptedData = cipherEncryption(
            TRANSFORMATION,
            Cipher.PRIVATE_KEY,
            privateKey,
            secretData
        )
        val secretKey = SecretKeySpec(
            decryptedData, 0, decryptedData.size,
            KEY_ALGORITHM_AES
        )
        val finalData = cipherEncryption(
            KEY_ALGORITHM_AES,
            Cipher.DECRYPT_MODE,
            secretKey,
            encryptedData
        )

        /* Timber.e(storage.getString(
             makeTypeTagForAlias(
                 alias
             )
         ))

         Timber.e(storage.getString(
             makeTypeTagForAlias(
                 alias
             )
         ))*/

        return gsonParser.fromJson(
            String(
                finalData,
                DEFAULT_CHARSET
            ), secureStorageSerializer.getClassType(
                storage.getString(
                    makeTypeTagForAlias(
                        alias
                    )
                )!!
            )
        )
    }

    private fun generateKeyRsa(alias: String) {
        try {
            val keyPairGenerator = KeyPairGenerator.getInstance(
                KEY_ALGORITHM_RSA,
                ANDROID_KEY_STORE
            )
            keyPairGenerator.initialize(getParameterSpec(alias))
            keyPairGenerator.generateKeyPair()
        } catch (e: NoSuchAlgorithmException) {
            throw KeyStoreAccessException("Unable to access keystore", e)
        } catch (e: NoSuchProviderException) {
            throw KeyStoreAccessException("Unable to access keystore", e)
        } catch (e: InvalidAlgorithmParameterException) {
            throw KeyStoreAccessException("Unable to access keystore", e)
        }

    }

    private fun getParameterSpec(alias: String): AlgorithmParameterSpec {
        val start = GregorianCalendar()
        val end = GregorianCalendar()
        end.add(Calendar.YEAR, 5)

        return KeyPairGeneratorSpec.Builder(context)
            .setAlias(alias)
            .setSubject(X500Principal("CN=$alias"))
            .setSerialNumber(KEY_SERIAL_NUMBER)
            .setStartDate(start.time)
            .setEndDate(end.time)
            .build()
    }

    private fun getKeyStoreEntry(shouldGenerateKey: Boolean, alias: String): KeyStore.Entry? {
        try {
            val keyStore = keyStoreAndLoad

            var entry: KeyStore.Entry? = keyStore.getEntry(alias, null)

            if (entry == null) {
                if (shouldGenerateKey) {
                    generateKeyRsa(alias)
                    entry = keyStore.getEntry(alias, null)
                }
            }
            return entry
        } catch (e: KeyStoreException) {
            throw KeyStoreAccessException("Unable to access keystore", e)
        } catch (e: NoSuchAlgorithmException) {
            throw KeyStoreAccessException("Unable to access keystore", e)
        } catch (e: UnrecoverableEntryException) {
            throw KeyStoreAccessException("Unable to access keystore", e)
        }

    }

    private fun <T> encryptData(alias: String, value: T, publicKey: PublicKey): ByteArray {
        val secret = generateKeyAes(alias)
        val rsaEncrypted = cipherEncryption(
            TRANSFORMATION,
            Cipher.PUBLIC_KEY,
            publicKey,
            secret.encoded
        )
        storage.saveKeyBytes(
            makeAesTagForAlias(
                alias
            ), rsaEncrypted
        )
        storage.saveString(
            makeTypeTagForAlias(
                alias
            ), secureStorageSerializer.getType(value)
        )

        //Timber.e( storage.getString(makeTypeTagForAlias(alias)))

        return cipherEncryption(
            KEY_ALGORITHM_AES,
            Cipher.ENCRYPT_MODE,
            secret,
            gsonParser.toJson(value).toByteArray(DEFAULT_CHARSET)
        )
    }

    private fun generateKeyAes(alias: String): SecretKey {
        try {
            val generator = KeyGenerator.getInstance(KEY_ALGORITHM_AES)
            generator.init(ENCRYPTION_KEY_SIZE)
            return generator.generateKey()
        } catch (e: NoSuchAlgorithmException) {
            throw CryptoFailedException("Unable to generate key for alias $alias", e)
        }

    }

    companion object {
        private const val KEY_ALGORITHM_RSA = "RSA"
        private const val KEY_ALGORITHM_AES = "AES"
        private const val TRANSFORMATION = "RSA/ECB/PKCS1Padding"
        private const val ENCRYPTION_KEY_SIZE = 256
        private val DEFAULT_CHARSET = Charset.forName("UTF-8")
        private val KEY_SERIAL_NUMBER = BigInteger.valueOf(1338)



        private fun cipherEncryption(
            transformation: String, mode: Int, key: Key,
            inputByteArray: ByteArray
        ): ByteArray {
            try {
                val cipher = Cipher.getInstance(transformation)
                cipher.init(mode, key)
                return cipher.doFinal(inputByteArray)
            } catch (e: NoSuchPaddingException) {
                throw CryptoFailedException(
                    String.format(
                        Locale.US,
                        "Unable to do cipher for transformation %s and mode %d", transformation, mode
                    ), e
                )
            } catch (e: NoSuchAlgorithmException) {
                throw CryptoFailedException(
                    String.format(
                        Locale.US,
                        "Unable to do cipher for transformation %s and mode %d",
                        transformation,
                        mode
                    ), e
                )
            } catch (e: InvalidKeyException) {
                throw CryptoFailedException(
                    String.format(
                        Locale.US,
                        "Unable to do cipher for transformation %s and mode %d",
                        transformation,
                        mode
                    ), e
                )
            } catch (e: BadPaddingException) {
                throw CryptoFailedException(
                    String.format(
                        Locale.US,
                        "Unable to do cipher for transformation %s and mode %d",
                        transformation,
                        mode
                    ), e
                )
            } catch (e: IllegalBlockSizeException) {
                throw CryptoFailedException(
                    String.format(
                        Locale.US,
                        "Unable to do cipher for transformation %s and mode %d",
                        transformation,
                        mode
                    ), e
                )
            }

        }
    }
}
