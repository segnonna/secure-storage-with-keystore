package hos.houns.seckeystore.utils

/**
 * Created by hospicehounsou on 28,June,2018
 * Dakar, Senegal.
 */

interface Serializer {

    /**
     * Serialize the cipher text along with the given data type
     *
     * @return serialized string
     */
    fun <T> serialize(cipherText: String, value: T): String

    /**
     * Deserialize the given text according to given DataInfo
     *
     * @return original object
     */
    fun deserialize(plainText: String): DataInfo
}