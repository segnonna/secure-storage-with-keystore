package hos.houns.securestorage.utils

/**
 * Created by hospicehounsou on 28,June,2018
 * Dakar, Senegal.
 */


import java.lang.reflect.Type

interface Parser {

    /**
     * @param content the string to convert in Object T
     * @param type the class name of the object like String::class.java
     * @return T the converted  object
     */
    @Throws(Exception::class)
    fun <T> fromJson(content: String?, type: Type): T?

    /**
     * @param body the object to be deserialized
     * @return String the converted string
     */
    fun toJson(body: Any?): String?

}