package hos.houns.seckeystore.encryption

import android.content.Context
import android.os.Build
import android.security.KeyPairGeneratorSpec
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.text.TextUtils
import android.util.Base64
import androidx.annotation.RequiresApi
import hos.houns.seckeystore.PreferenceStorage
import timber.log.Timber
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.IOException
import java.math.BigInteger
import java.security.*
import java.security.cert.CertificateException
import java.util.*
import javax.crypto.*
import javax.crypto.spec.SecretKeySpec
import javax.security.auth.x500.X500Principal


/**
 * This class wraps [KeyStore] class apis with some additional possibilities.
 */
class KeyStoreWrapper(private val context: Context) {
    private val s_keyInitLock = Any()
    private val RSA_ALGORITHM_NAME = "RSA"
    private val ANDROID_KEY_STORE_NAME = "AndroidKeyStore"
    private val KEY_ALIAS = "YourAliasForEncryption"
    private val RSA_MODE = "RSA/ECB/PKCS1Padding"
    private val CIPHER_PROVIDER_NAME_ENCRYPTION_DECRYPTION_RSA = "AndroidOpenSSL"

    private val mPreferenceStorage: PreferenceStorage by lazy(LazyThreadSafetyMode.NONE) { PreferenceStorage(context) }


    @Throws(
        CertificateException::class,
        NoSuchPaddingException::class,
        InvalidKeyException::class,
        NoSuchAlgorithmException::class,
        KeyStoreException::class,
        NoSuchProviderException::class,
        UnrecoverableEntryException::class,
        IOException::class
    )

    // Generate Random AES KEY
    private fun saveEncryptedKey() {
        with(mPreferenceStorage) {
            if (getAesEncryptionKey().isEmpty()) {
                val key = ByteArray(16)
                val secureRandom = SecureRandom()
                secureRandom.nextBytes(key)
                val encryptedKey = rsaEncryptKey(key)

                if (saveAesEncryptionKey(Base64.encodeToString(encryptedKey, Base64.DEFAULT))) {
                    Timber.e("Saved keys successfully")
                } else {
                    Timber.e("Saved keys unsuccessfully")
                    throw IOException("Could not save keys")
                }
            }
        }

    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Throws(NoSuchAlgorithmException::class, NoSuchProviderException::class, InvalidAlgorithmParameterException::class)
    private fun generateKeysForAPIMOrGreater() {
        val keyGenerator = KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, ANDROID_KEY_STORE_NAME)
        keyGenerator.init(
            KeyGenParameterSpec.Builder(
                KEY_ALIAS,
                KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT
            )
                .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
                .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
                // NOTE no Random IV. According to above this is less secure but acceptably so.
                .setRandomizedEncryptionRequired(false)
                .build()
        )

        keyGenerator.generateKey()
    }

    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR2)
    @Throws(
        NoSuchProviderException::class,
        NoSuchAlgorithmException::class,
        InvalidAlgorithmParameterException::class,
        CertificateException::class,
        UnrecoverableEntryException::class,
        NoSuchPaddingException::class,
        KeyStoreException::class,
        InvalidKeyException::class,
        IOException::class
    )
    private fun generateKeysForAPILessThanM() {
        // Generate a key pair for encryption
        val start = Calendar.getInstance()
        val end = Calendar.getInstance()
        end.add(Calendar.YEAR, 30)
        val spec = KeyPairGeneratorSpec.Builder(context)
            .setAlias(KEY_ALIAS)
            .setSubject(X500Principal("CN=$KEY_ALIAS"))
            .setSerialNumber(BigInteger.TEN)
            .setStartDate(start.time)
            .setEndDate(end.time)
            .build()
        val kpg = KeyPairGenerator.getInstance(RSA_ALGORITHM_NAME, ANDROID_KEY_STORE_NAME)
        kpg.initialize(spec)
        kpg.generateKeyPair()

        saveEncryptedKey()
    }


    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR2)
    @Throws(
        NoSuchAlgorithmException::class,
        NoSuchProviderException::class,
        InvalidAlgorithmParameterException::class,
        CertificateException::class,
        UnrecoverableEntryException::class,
        NoSuchPaddingException::class,
        KeyStoreException::class,
        InvalidKeyException::class,
        IOException::class
    )
    private fun initValidKeys() {
        synchronized(s_keyInitLock) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                generateKeysForAPIMOrGreater()
            } else {
                generateKeysForAPILessThanM()
            }
        }
    }


    @Throws(
        CertificateException::class,
        NoSuchPaddingException::class,
        InvalidKeyException::class,
        NoSuchAlgorithmException::class,
        KeyStoreException::class,
        NoSuchProviderException::class,
        UnrecoverableEntryException::class,
        IOException::class
    )

    internal fun getSecretKeyAPILessThanM(): Key {
        val encryptedKeyBase64Encoded = mPreferenceStorage.getAesEncryptionKey()
        if (TextUtils.isEmpty(encryptedKeyBase64Encoded)) {
            throw InvalidKeyException("Saved key missing from shared preferences")
        }
        val encryptedKey = Base64.decode(encryptedKeyBase64Encoded, Base64.DEFAULT)
        val key = rsaDecryptKey(encryptedKey)
        return SecretKeySpec(key, "AES")
    }

    @Throws(
        CertificateException::class,
        NoSuchAlgorithmException::class,
        IOException::class,
        KeyStoreException::class,
        UnrecoverableKeyException::class
    )
    internal fun getSecretKeyAPIMorGreater(): Key {
        val keyStore = KeyStore.getInstance(ANDROID_KEY_STORE_NAME)
        keyStore.load(null)
        return keyStore.getKey(KEY_ALIAS, null)

    }


    private fun rsaEncryptKey(secret: ByteArray): ByteArray {

        val keyStore = KeyStore.getInstance(ANDROID_KEY_STORE_NAME)
        keyStore.load(null)

        val privateKeyEntry = keyStore.getEntry(KEY_ALIAS, null) as KeyStore.PrivateKeyEntry
        val inputCipher = Cipher.getInstance(
            RSA_MODE, CIPHER_PROVIDER_NAME_ENCRYPTION_DECRYPTION_RSA
        )
        inputCipher.init(Cipher.ENCRYPT_MODE, privateKeyEntry.certificate.publicKey)

        val outputStream = ByteArrayOutputStream()
        val cipherOutputStream = CipherOutputStream(outputStream, inputCipher)
        cipherOutputStream.write(secret)
        cipherOutputStream.close()

        return outputStream.toByteArray()
    }

    @Throws(
        KeyStoreException::class,
        CertificateException::class,
        NoSuchAlgorithmException::class,
        IOException::class,
        UnrecoverableEntryException::class,
        NoSuchProviderException::class,
        NoSuchPaddingException::class,
        InvalidKeyException::class
    )
    private fun rsaDecryptKey(encrypted: ByteArray): ByteArray {

        val keyStore = KeyStore.getInstance(ANDROID_KEY_STORE_NAME)
        keyStore.load(null)

        val privateKeyEntry = keyStore.getEntry(KEY_ALIAS, null) as KeyStore.PrivateKeyEntry
        val output = Cipher.getInstance(RSA_MODE, CIPHER_PROVIDER_NAME_ENCRYPTION_DECRYPTION_RSA)
        output.init(Cipher.DECRYPT_MODE, privateKeyEntry.privateKey)
        val cipherInputStream = CipherInputStream(
            ByteArrayInputStream(encrypted), output
        )
        val values = ArrayList<Byte>()

        while (true) {
            val byteCount = cipherInputStream.read()
            if (byteCount < 0) break
            values.add(byteCount.toByte())
        }

        val decryptedKeyAsBytes = ByteArray(values.size)
        for (i in decryptedKeyAsBytes.indices) {
            decryptedKeyAsBytes[i] = values[i]
        }
        return decryptedKeyAsBytes
    }

    @Throws(KeyStoreException::class, CertificateException::class, NoSuchAlgorithmException::class, IOException::class)
    internal fun removeKeys() {
        synchronized(s_keyInitLock) {
            val keyStore = KeyStore.getInstance(ANDROID_KEY_STORE_NAME)
            keyStore.load(null)
            removeKeys(keyStore)
        }
    }


    @Throws(KeyStoreException::class)
    private fun removeKeys(keyStore: KeyStore) {
        keyStore.deleteEntry(KEY_ALIAS)
        mPreferenceStorage.removeAesEncryptionKey()
    }

    @RequiresApi(Build.VERSION_CODES.JELLY_BEAN_MR2)
    internal fun initKeys() {
        val keyStore = KeyStore.getInstance(ANDROID_KEY_STORE_NAME)
        keyStore.load(null)

        if (!keyStore.containsAlias(KEY_ALIAS)) {
            initValidKeys()
        } else {
            var keyValid = false
            try {
                val keyEntry = keyStore.getEntry(KEY_ALIAS, null)
                if (keyEntry is KeyStore.SecretKeyEntry && Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    keyValid = true
                }

                if (keyEntry is KeyStore.PrivateKeyEntry && Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
                    val secretKey = mPreferenceStorage.getAesEncryptionKey()
                    // When doing "Clear data" on Android 4.x it removes the shared preferences (where
                    // we have stored our encrypted secret key) but not the key entry. Check for existence
                    // of key here as well.
                    if (!TextUtils.isEmpty(secretKey)) {
                        keyValid = true
                    }
                }
            } catch (e: NullPointerException) {
                e.printStackTrace()
                // Bad to catch null pointer exception, but looks like Android 4.4.x
                // pin switch to password Keystore bug.
                // https://issuetracker.google.com/issues/36983155
                // Log.e(LOG_TAG, "Failed to get key store entry", e);
            } catch (e: UnrecoverableKeyException) {
                e.printStackTrace()
            }

            if (!keyValid) {
                synchronized(s_keyInitLock) {
                    // System upgrade or something made key invalid
                    removeKeys(keyStore)
                    initValidKeys()
                }
            }

        }

    }

}

