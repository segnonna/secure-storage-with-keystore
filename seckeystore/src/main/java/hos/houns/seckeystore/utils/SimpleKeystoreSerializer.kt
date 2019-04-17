package hos.houns.seckeystore.utils

/**
 * Created by hospicehounsou on 28,June,2018
 * Dakar, Senegal.
 */


class SimpleKeystoreSerializer : Serializer {

    override fun <T> serialize(cipherText: String, value: T): String {

        var keyClassName = ""
        var valueClassName = ""
        val dataType: Char

        if (Generic<MutableList<Any>>().checkType(value!!)) {
            val list = value as MutableList<*>

            if (!list.isEmpty()) {
                keyClassName = list.first()?.javaClass?.name!!
            }
            dataType = DataInfo.TYPE_LIST

        } else if (Generic<Map<Any, Any>>().checkType(value)) {
            dataType = DataInfo.TYPE_MAP
            val map = value as Map<*, *>
            if (!map.isEmpty()) {
                for ((key, value) in map) {

                    keyClassName = key?.javaClass?.name!!
                    valueClassName = value?.javaClass?.name!!
                    break
                }
            }
        } else if (Generic<Set<Any>>().checkType(value)) {
            val set = value as Set<*>
            if (!set.isEmpty()) {
                val iterator = set.iterator()
                if (iterator.hasNext()) {
                    keyClassName = iterator.next()?.javaClass?.name!!
                }
            }
            dataType = DataInfo.TYPE_SET
        } else {
            dataType = DataInfo.TYPE_OBJECT

            keyClassName = (value as Any).javaClass.name
        }

        return keyClassName + INFO_DELIMITER +
                valueClassName + INFO_DELIMITER +
                dataType + NEW_VERSION + DELIMITER +
                cipherText
    }

    override fun deserialize(serializedText: String): DataInfo {
        val infos = serializedText.split(INFO_DELIMITER.toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()

        infos.forEach {
            // Timber.e("infos -> $it")
        }

        val type = infos[2][0]

        //Timber.e("type -> $type")

        // if it is collection, no need to create the class object
        var keyClazz: Class<*>? = null
        val firstElement = infos.first()
        if (firstElement.isNotEmpty()) {
            try {
                keyClazz = Class.forName(firstElement)
            } catch (e: ClassNotFoundException) {
                //Timber.e("HawkSerializer -> ${e.message}")
            }

        }

        var valueClazz: Class<*>? = null
        val secondElement = infos.first()
        // Timber.e("infos.first() -> ${infos.first()}")
        // Timber.e("infos[2] -> ${infos[2]}")
        //  Timber.e("infos.last() -> ${infos.last()}")
        if (secondElement.isNotEmpty()) {
            try {
                valueClazz = Class.forName(secondElement)
            } catch (e: ClassNotFoundException) {
                //     Timber.e("HawkSerializer -> ${e.message}")
            }
        }

        val cipherText = getCipherText(infos[infos.size - 1])
        //Timber.e("type : $type")
        //Timber.e("cipherText : $cipherText")
        //Timber.e("keyClazz : ${keyClazz!!}")
        //Timber.e("valueClazz : ${valueClazz!!}")

        return DataInfo(
                type,
                cipherText,
                keyClazz!!,
                valueClazz!!)
    }

    private fun getCipherText(serializedText: String): String {
        val index = serializedText.indexOf(DELIMITER)
        if (index == -1) {
            throw IllegalArgumentException("Text should contain delimiter")
        }
        return serializedText.substring(index + 1)
    }

    companion object {
        private val DELIMITER = '@'
        private val INFO_DELIMITER = "#"
        private val NEW_VERSION = 'V'
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