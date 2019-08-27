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
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import hos.houns.securestorage.SecureStorage.gsonParser
import hos.houns.securestorage.SecureStorage.secureStorageSerializer
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.IOException
import java.nio.charset.Charset
import java.security.*
import java.security.spec.AlgorithmParameterSpec
import javax.crypto.*
import javax.crypto.spec.IvParameterSpec

@TargetApi(Build.VERSION_CODES.M)
internal class CipherStorageAndroidKeystore(context: Context, storage: Storage) :
    BaseCipherStorage(context, storage) {


    /**
     * {@inheritDoc}
     */
    override fun <T> encrypt(alias: String, value: T) {
        try {
            val keyStore = keyStoreAndLoad

            if (!keyStore.containsAlias(alias)) {
                //Timber.e("keystore does not exist")
                val generator = KeyGenerator.getInstance(
                    ENCRYPTION_ALGORITHM,
                    ANDROID_KEY_STORE
                )
                generator.init(generateParameterSpec(alias))
                generator.generateKey()
            } else {
                val aliases = keyStore.aliases().toList().filter { it == alias }
                if (aliases.isEmpty()) {
                    // Timber.e("keystore  exist")
                } else {
                    //Timber.e("keystore   ${aliases.first()}  exist")
                }

            }
            val key = keyStore.getKey(alias, null)
            val encryptedData = encryptString(key, value)

            storage.saveKeyBytes(alias, encryptedData)

            storage.saveString(
                makeTypeTagForAlias(
                    alias
                ), secureStorageSerializer.getType(value)
            )

        } catch (e: NoSuchAlgorithmException) {
            throw CryptoFailedException("Could not encrypt data", e)
        } catch (e: InvalidAlgorithmParameterException) {
            throw CryptoFailedException("Could not encrypt data", e)
        } catch (e: NoSuchProviderException) {
            throw CryptoFailedException("Could not encrypt data", e)
        } catch (e: UnrecoverableKeyException) {
            throw CryptoFailedException("Could not encrypt data", e)
        } catch (e: KeyStoreException) {
            throw CryptoFailedException("Could not access Keystore", e)
        } catch (e: KeyStoreAccessException) {
            throw CryptoFailedException("Could not access Keystore", e)
        } catch (e: BadPaddingException) {
            throw CryptoFailedException("Could not access Keystore", e)
        } catch (e: NullPointerException) {
            throw CryptoFailedException("Could not access Keystore", e)
        }

    }

    /**
     * {@inheritDoc}
     */
    override fun <T> decrypt(alias: String): T? {
        try {
            val storedData = storage.getKeyBytes(alias) ?: return null
            val keyStore = keyStoreAndLoad
            val key = keyStore.getKey(alias, null)
                ?: /* Well this should not happen if you do not have a stored byte data, but just in case */
                return null
            return decryptBytes(alias, key, storedData, storage)
        } catch (e: KeyStoreException) {
            return null
        } catch (e: UnrecoverableKeyException) {
            return null
        } catch (e: NoSuchAlgorithmException) {
            return null
        } catch (e: KeyStoreAccessException) {
            return null
        } catch (e: CryptoFailedException) {
            e.printStackTrace()
            return null
        } catch (e: KotlinNullPointerException) {
            e.printStackTrace()
            return null
        }

    }

    companion object {
        private const val ENCRYPTION_ALGORITHM = KeyProperties.KEY_ALGORITHM_AES
        private const val ENCRYPTION_BLOCK_MODE = KeyProperties.BLOCK_MODE_CBC
        private const val ENCRYPTION_PADDING = KeyProperties.ENCRYPTION_PADDING_PKCS7
        private const val ENCRYPTION_TRANSFORMATION = ENCRYPTION_ALGORITHM + "/" +
                ENCRYPTION_BLOCK_MODE + "/" +
                ENCRYPTION_PADDING
        private const val ENCRYPTION_KEY_SIZE = 256
        private val DEFAULT_CHARSET = Charset.forName("UTF-8")

        private fun generateParameterSpec(alias: String): AlgorithmParameterSpec {
            return KeyGenParameterSpec.Builder(
                alias,
                KeyProperties.PURPOSE_DECRYPT or KeyProperties.PURPOSE_ENCRYPT
            )
                .setBlockModes(ENCRYPTION_BLOCK_MODE)
                .setEncryptionPaddings(ENCRYPTION_PADDING)
                .setRandomizedEncryptionRequired(true)
                .setKeySize(ENCRYPTION_KEY_SIZE)
                .build()
        }

        @Throws(CryptoFailedException::class)
        private fun <T> decryptBytes(
            alias: String,
            key: Key,
            bytes: ByteArray,
            storage: Storage
        ): T? {
            try {
                val cipher = Cipher.getInstance(ENCRYPTION_TRANSFORMATION)
                val inputStream = ByteArrayInputStream(bytes)
                // read the initialization vector from the beginning of the stream
                val ivParams =
                    readIvFromStream(inputStream)
                cipher.init(Cipher.DECRYPT_MODE, key, ivParams)
                // decrypt the bytes using a CipherInputStream
                val cipherInputStream = CipherInputStream(inputStream, cipher)
                val output = ByteArrayOutputStream()
                val buffer = ByteArray(1024)
                while (true) {
                    val n = cipherInputStream.read(buffer, 0, buffer.size)
                    if (n <= 0) {
                        break
                    }
                    output.write(buffer, 0, n)
                }


                return gsonParser.fromJson(
                    String(
                        output.toByteArray(),
                        DEFAULT_CHARSET
                    ),
                    secureStorageSerializer.getClassType(storage.getString(makeTypeTagForAlias(alias))!!)
                )

            } catch (e: IOException) {
                throw CryptoFailedException("Could not decrypt bytes", e)
            } catch (e: NoSuchAlgorithmException) {
                throw CryptoFailedException("Could not decrypt bytes", e)
            } catch (e: NoSuchPaddingException) {
                throw CryptoFailedException("Could not decrypt bytes", e)
            } catch (e: InvalidKeyException) {
                throw CryptoFailedException("Could not decrypt bytes", e)
            } catch (e: InvalidAlgorithmParameterException) {
                throw CryptoFailedException("Could not decrypt bytes", e)
            } catch (e: BadPaddingException) {
                throw CryptoFailedException("Could not access Keystore", e)
            }
        }

        private fun readIvFromStream(inputStream: ByteArrayInputStream): IvParameterSpec {
            val iv = ByteArray(16)
            inputStream.read(iv, 0, iv.size)
            return IvParameterSpec(iv)
        }


        @Throws(CryptoFailedException::class)
        private fun <T> encryptString(key: Key, value: T): ByteArray {
            try {
                val cipher = Cipher.getInstance(ENCRYPTION_TRANSFORMATION)
                cipher.init(Cipher.ENCRYPT_MODE, key)
                val outputStream = ByteArrayOutputStream()
                // write initialization vector to the beginning of the stream
                val iv = cipher.iv
                outputStream.write(iv, 0, iv.size)
                // encrypt the value using a CipherOutputStream
                val cipherOutputStream = CipherOutputStream(outputStream, cipher)

                cipherOutputStream.write((gsonParser.toJson(value)).toByteArray(DEFAULT_CHARSET))
                cipherOutputStream.close()
                return outputStream.toByteArray()
            } catch (e: IOException) {
                throw CryptoFailedException("Could not encrypt value", e)
            } catch (e: NoSuchAlgorithmException) {
                throw CryptoFailedException("Could not encrypt value", e)
            } catch (e: InvalidKeyException) {
                throw CryptoFailedException("Could not encrypt value", e)
            } catch (e: NoSuchPaddingException) {
                throw CryptoFailedException("Could not encrypt value", e)
            } catch (e: BadPaddingException) {
                throw CryptoFailedException("Could not access Keystore", e)
            }
        }
    }
}
