package hos.houns.securestorage

import android.content.SharedPreferences
import com.google.gson.Gson
import hos.houns.securestorage.encryption.CipherWrapper
import hos.houns.securestorage.utils.GsonParser
import hos.houns.securestorage.utils.SecureStorageSerializer
import hos.houns.securestorage.utils.SensitiveData
import timber.log.Timber
import java.io.IOException
import java.security.InvalidKeyException
import java.security.KeyStoreException
import java.security.NoSuchAlgorithmException
import java.security.UnrecoverableEntryException
import java.security.cert.CertificateException
import java.util.*
import javax.crypto.AEADBadTagException
import javax.crypto.IllegalBlockSizeException


/**
 * Created by hospicehounsou on 03,May,2019
 * Dakar, Senegal.
 */
class StorageImpl : Storage {

    private val STORAGE_SETTINGS: String = "settings"
    private val STORAGE_ENCRYPTION_KEY: String = "encryption_key"
    private val STORAGE_SECRETS: String = "secrets"
    private lateinit var settings: SharedPreferences
    private lateinit var sensitiveDataPrefs: SharedPreferences

    private val gson: Gson by lazy(LazyThreadSafetyMode.NONE) { Gson() }
    internal val gsonParser: GsonParser by lazy(LazyThreadSafetyMode.NONE) { GsonParser(gson) }
    private val secureStorageSerializer: SecureStorageSerializer by lazy(LazyThreadSafetyMode.NONE) { SecureStorageSerializer() }



    init {
        SecureStorage.mContext.get()
            ?.getSharedPreferences(STORAGE_SETTINGS, android.content.Context.MODE_PRIVATE)?.let {
            settings = it
        }
        SecureStorage.mContext.get()
            ?.getSharedPreferences(STORAGE_SECRETS, android.content.Context.MODE_PRIVATE)?.let {
            sensitiveDataPrefs = it
        }
    }

    internal fun saveAesEncryptionKey(key: String): Boolean {
        return settings.edit().putString(STORAGE_ENCRYPTION_KEY, key).commit()
    }

    internal fun getAesEncryptionKey(): String? = settings.getString(STORAGE_ENCRYPTION_KEY, "")
    internal fun removeAesEncryptionKey(): Boolean = settings.edit().remove(STORAGE_ENCRYPTION_KEY).commit()

    fun getSensitiveDatas(): List<SensitiveData<*>> {
        val secretsList = ArrayList<SensitiveData<*>>()
        val secretsAliases = sensitiveDataPrefs.all
        secretsAliases
            .map {
                gson.fromJson(
                    it.value as String,
                    SensitiveData::class.java
                )
            }
            .forEach { secretsList.add(it) }

        secretsList.sortByDescending { it.createDate }
        return secretsList
    }

    fun clear(): Boolean {
        return settings.edit().clear().commit() && sensitiveDataPrefs.edit().clear().commit()
    }


    fun SensitiveData<*>.editSensitiveData(secret: String): SensitiveData<*> {
        this.let {
            delete(it.alias)
            val newSecret = createSecretData(it.alias, secret, it.createDate)
            put(newSecret.alias, newSecret)
            return newSecret
        }
    }

    fun removeSensitiveData(alias: String): Boolean = sensitiveDataPrefs.edit().remove(alias).commit()

    private fun getSensitiveDataFromSharedPrefs(alias: String): SensitiveData<*>? =
        gson.fromJson(sensitiveDataPrefs.getString(alias, "")!!, SensitiveData::class.java)

    fun <T> saveSensitiveData(alias: String, secret: T) {
        put(alias, createSecretData(alias, secret, Date()))
    }


    private fun <T> createSecretData(alias: String, secret: T, createDate: Date): SensitiveData<*> {
        val encryptedSecret = encryptSecret(secret)
        // val plaintext = secureSharedConverter.toString(secret)
        val serialize = secureStorageSerializer.serialize(encryptedSecret, secret)
        //Timber.e("serialize : $plaintext")
        // Timber.e("serialize : $serialize")

        return SensitiveData(
            alias,
            serialize,
            createDate,
            updateDate = Date()
        )
    }

    /**
     * Encrypt secret before saving it.
     */
    private fun <T> encryptSecret(secret: T): String {
        return CipherWrapper(SecureStorage.mContext.get()!!).encryptData(secret)
    }


    /**
     * Decrypt secret before showing it.
     */

    fun <T> getSensitiveData(alias: String): T? {
        //Timber.i("------- getSensitiveDataString")
        val value = getSensitiveDataFromSharedPrefs(alias)

        val secretSerialized = value?.secret
        secretSerialized?.let {
            val dataInfo = secureStorageSerializer.deserialize(secretSerialized as String)
            //Timber.e(dataInfo.cipherText)
            // Timber.e(dataInfo.keyClazz.name)

            return try {
                return CipherWrapper(SecureStorage.mContext.get()!!).decryptData(
                    dataInfo.cipherText,
                    dataInfo.keyClazz
                )
            } catch (e: KeyStoreException) {
                null
            } catch (e: CertificateException) {
                null
            } catch (e: AEADBadTagException) {
                null
            } catch (e: NoSuchAlgorithmException) {
                null
            } catch (e: IOException) {
                null
            } catch (e: UnrecoverableEntryException) {
                null
            } catch (e: InvalidKeyException) {
                null
            } catch (e: IllegalBlockSizeException) {
                null
            }

        } ?: return null
    }

    override fun <T> put(key: String?, value: SensitiveData<T>): Boolean {
        return sensitiveDataPrefs.edit().putString(key, gson.toJson(value)).commit()
    }


    override fun get(key: String?): SensitiveData<*> {
        Timber.e(sensitiveDataPrefs.getString(key, null))
        return gson.fromJson(sensitiveDataPrefs.getString(key, null), SensitiveData::class.java)
    }

    override fun getAll(): MutableMap<String, *>? {
        return sensitiveDataPrefs.all
    }

    override fun delete(key: String?): Boolean {
        return sensitiveDataPrefs.edit().remove(key).commit()
    }

    override fun deleteAll(): Boolean {
        return sensitiveDataPrefs.edit().clear().commit()
    }

    override fun count(): Long {
        return sensitiveDataPrefs.all.size.toLong()
    }

    override fun contains(key: String?): Boolean {
        return sensitiveDataPrefs.contains(key)
    }

    inline fun <T> tryCatch(blockTry: () -> T, blockCatch: () -> Unit = {}) = try {
        blockTry()
    } catch (e: Throwable) {
        blockCatch()
    }

    inline fun <T> justTry(block: () -> T) = try {
        block()
    } catch (e: Throwable) {
    }

}