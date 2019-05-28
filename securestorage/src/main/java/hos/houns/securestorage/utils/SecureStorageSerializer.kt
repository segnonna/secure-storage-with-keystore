package hos.houns.securestorage.utils

/**
 * Created by hospicehounsou on 28,June,2018
 * Dakar, Senegal.
 */


class SecureStorageSerializer : Serializer {

    override fun getClassType(value: String): Class<*> {
        return Class.forName(value)
    }

    override fun <T> getType(value: T): String {
        return when {
            Generic<MutableList<Any>>().checkType(value!!) -> (value as MutableList<*>).javaClass.name
            else -> (value as Any).javaClass.name
        }

    }

    class Generic<T : Any>(private val klass: Class<T>) {
        companion object {
            inline operator fun <reified T : Any> invoke() = Generic(T::class.java)
        }

        fun checkType(t: Any): Boolean {
            return klass.isAssignableFrom(t.javaClass)
        }
    }
}