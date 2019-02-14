package hos.houns.seckeystore

import android.content.Context
import android.content.SharedPreferences
import com.google.gson.Gson
import hos.houns.seckeystore.encryption.CipherWrapper
import hos.houns.seckeystore.utils.GsonParser
import hos.houns.seckeystore.utils.SimpleKeystoreSerializer
import timber.log.Timber
import java.io.Serializable
import java.util.*

/**
 * Stores application data like password hash.
 */
class SimpleKeystore constructor(var context: Context) : Storage {

    private val STORAGE_SETTINGS: String = "settings"
    private val STORAGE_ENCRYPTION_KEY: String = "encryption_key"
    private val STORAGE_SECRETS: String = "secrets"
    private val settings: SharedPreferences
    private val sensitiveDataPrefs: SharedPreferences

    private val gson: Gson by lazy(LazyThreadSafetyMode.NONE) { Gson() }
    internal val gsonParser: GsonParser by lazy(LazyThreadSafetyMode.NONE) { GsonParser(gson) }
    val simpleKeystoreSerializer: SimpleKeystoreSerializer by lazy(LazyThreadSafetyMode.NONE) { SimpleKeystoreSerializer() }

    data class SensitiveData<T>(
        val alias: String,
        val secret: T,
        val createDate: Date,
        val updateDate: Date
    ) : Serializable


    init {
        settings = context.getSharedPreferences(STORAGE_SETTINGS, android.content.Context.MODE_PRIVATE)
        sensitiveDataPrefs = context.getSharedPreferences(STORAGE_SECRETS, android.content.Context.MODE_PRIVATE)
    }

    fun saveAesEncryptionKey(key: String): Boolean {
        return settings.edit().putString(STORAGE_ENCRYPTION_KEY, key).commit()
    }
    fun getAesEncryptionKey(): String = settings.getString(STORAGE_ENCRYPTION_KEY, "")!!
    fun removeAesEncryptionKey(): Boolean = settings.edit().remove(STORAGE_ENCRYPTION_KEY).commit()

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

    fun clear() {
        settings.edit().clear().apply()
        sensitiveDataPrefs.edit().clear().apply()
    }


    fun SensitiveData<*>.editSensitiveData(secret: String): SensitiveData<*> {
        this.let {
            delete(it.alias)
            val newSecret = createSecretData(it.alias, secret, it.createDate)
            put(newSecret.alias, newSecret)
            return newSecret
        }
    }

    private fun getSensitiveDataFromSharedPrefs(alias: String): SensitiveData<*>? =
            gson.fromJson(sensitiveDataPrefs.getString(alias, "")!!, SensitiveData::class.java)

    fun <T> saveSensitiveData(alias: String, secret: T) {
        put(alias, createSecretData(alias, secret, Date()))
    }


    private fun <T> createSecretData(alias: String, secret: T, createDate: Date): SensitiveData<*> {
        val encryptedSecret = encryptSecret(secret)
        // val plaintext = secureSharedConverter.toString(secret)
        val serialize = simpleKeystoreSerializer.serialize(encryptedSecret, secret)
        //Timber.e("serialize : $plaintext")
        // Timber.e("serialize : $serialize")
        return SensitiveData(
            alias.capitalize(),
                serialize,
            createDate,
            updateDate = Date()
        )
    }

    /**
     * Encrypt secret before saving it.
     */
    private fun <T> encryptSecret(secret: T): String {
        return CipherWrapper(context).encryptData(secret)
    }


    /**
     * Decrypt secret before showing it.
     */
    fun <T> getSensitiveData(alias: String): T? {
        Timber.i("------- getSensitiveDataString")
        val value = getSensitiveDataFromSharedPrefs(alias)

        val secretSerialized = value?.secret
        val dataInfo = simpleKeystoreSerializer.deserialize(secretSerialized as String)
        //Timber.e(dataInfo.cipherText)
        Timber.e(dataInfo.keyClazz.name)
        return CipherWrapper(context).decryptData(dataInfo.cipherText, dataInfo.keyClazz)
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

}