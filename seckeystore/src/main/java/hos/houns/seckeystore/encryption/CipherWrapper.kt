package hos.houns.seckeystore.encryption

import android.annotation.SuppressLint
import android.content.Context
import android.os.Build
import android.util.Base64
import hos.houns.seckeystore.SimpleKeystore
import java.io.IOException
import java.nio.charset.StandardCharsets
import java.security.*
import java.security.cert.CertificateException
import javax.crypto.*
import javax.crypto.spec.GCMParameterSpec


@SuppressLint("GetInstance")
class CipherWrapper(var context: Context) {
    private val CHARSET_NAME = "UTF-8"
    private val keyStoreWrapper = KeyStoreWrapper(context)
    private val AES_MODE_M_OR_GREATER = "AES/GCM/NoPadding"
    private val AES_MODE_LESS_THAN_M = "AES/ECB/PKCS7Padding"

    // TODO update these bytes to be random for IV of encryption
    private val FIXED_IV = byteArrayOf(55, 54, 53, 52, 51, 50, 49, 48, 47, 46, 45, 44)

    private val CIPHER_PROVIDER_NAME_ENCRYPTION_DECRYPTION_AES = "BC"
    private var cipher: Cipher


    init {
        cipher = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            Cipher.getInstance(AES_MODE_M_OR_GREATER)
        } else {
            Cipher.getInstance(AES_MODE_LESS_THAN_M, CIPHER_PROVIDER_NAME_ENCRYPTION_DECRYPTION_AES)
        }


    }

    // @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR2)
    @Throws(
        NoSuchPaddingException::class,
        NoSuchAlgorithmException::class,
        UnrecoverableEntryException::class,
        CertificateException::class,
        KeyStoreException::class,
        IOException::class,
        InvalidAlgorithmParameterException::class,
        InvalidKeyException::class,
        NoSuchProviderException::class,
        BadPaddingException::class,
        IllegalBlockSizeException::class,
        AEADBadTagException::class,
        KeyStoreException::class
    )
    fun <T> encryptData(stringToEncrypt: T, alias: String): String {

        stringToEncrypt.let {
            keyStoreWrapper.initKeys()
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                cipher.init(
                    Cipher.ENCRYPT_MODE, keyStoreWrapper.getSecretKeyAPIMorGreater(),
                    GCMParameterSpec(128, FIXED_IV)
                )
            } else {
                try {
                    cipher.init(Cipher.ENCRYPT_MODE, keyStoreWrapper.getSecretKeyAPILessThanM())
                } catch (e: InvalidKeyException) {
                    // Since the keys can become bad (perhaps because of lock screen change)
                    // drop keys in this case.
                    keyStoreWrapper.removeKeys()
                    throw e
                } catch (e: IOException) {
                    keyStoreWrapper.removeKeys()
                    throw e
                } catch (e: IllegalArgumentException) {
                    keyStoreWrapper.removeKeys()
                    throw e
                }

            }
        }
        val encodedBytes = cipher.doFinal(
                SimpleKeystore(context).gsonParser.toJson(stringToEncrypt).toByteArray(
                charset(CHARSET_NAME)
            )
        )
        return Base64.encodeToString(encodedBytes, Base64.DEFAULT)

    }


    @Throws(
        NoSuchPaddingException::class,
        NoSuchAlgorithmException::class,
        UnrecoverableEntryException::class,
        CertificateException::class,
        KeyStoreException::class,
        IOException::class,
        InvalidAlgorithmParameterException::class,
        InvalidKeyException::class,
        NoSuchProviderException::class,
        BadPaddingException::class,
        IllegalBlockSizeException::class,
        AEADBadTagException::class,
        KeyStoreException::class)
    fun <T> decryptData(encryptedData: String, alias: String, type: Class<*>): T? {

        encryptedData.let {
            keyStoreWrapper.initKeys()

            val encryptedDecodedData = Base64.decode(encryptedData, Base64.DEFAULT)

            try {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    cipher.init(
                        Cipher.DECRYPT_MODE,
                        keyStoreWrapper.getSecretKeyAPIMorGreater(),
                        GCMParameterSpec(128, FIXED_IV)
                    )
                } else {

                    cipher.init(Cipher.DECRYPT_MODE, keyStoreWrapper.getSecretKeyAPILessThanM())
                }
            } catch (e: InvalidKeyException) {
                // Since the keys can become bad (perhaps because of lock screen change)
                // drop keys in this case.
                keyStoreWrapper.removeKeys()
                throw e
            } catch (e: IOException) {
                keyStoreWrapper.removeKeys()
                throw e
            }

            val decodedBytes = cipher.doFinal(encryptedDecodedData)

            return SimpleKeystore(context).gsonParser.fromJson(String(decodedBytes, StandardCharsets.UTF_8), type)

        }

    }

}

