package hos.houns.seckeystore.utils

import android.text.TextUtils

import com.google.gson.Gson
import com.google.gson.JsonSyntaxException

import java.lang.reflect.Type

/**
 * Created by hospicehounsou on 28,June,2018
 * Dakar, Senegal.
 */


class GsonParser(private val gson: Gson) : Parser {
    @Throws(JsonSyntaxException::class)
    override fun <T> fromJson(content: String?, type: Type?): T? {
        return if (TextUtils.isEmpty(content)) {
            null
        } else gson.fromJson<T>(content, type)
    }

    override fun toJson(body: Any?): String {
        return gson.toJson(body)
    }

}