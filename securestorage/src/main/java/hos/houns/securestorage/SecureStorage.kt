package hos.houns.securestorage

/**
 * Stores application data like password hash.
 */

object SecureStorage {

    fun <T> setValue(alias: String, secret: T) {
        StorageImpl().saveSensitiveData(alias, secret)
    }

    fun <T> getValue(alias: String): T? {
        return StorageImpl().getSensitiveData<T>(alias)
    }

    fun clearAll() {
        StorageImpl().clear()
    }


}
