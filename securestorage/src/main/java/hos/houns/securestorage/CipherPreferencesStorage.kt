/*
 * Copyright 2018 Leonardo Rossetto
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package hos.houns.securestorage

import android.content.Context
import android.util.Base64

class CipherPreferencesStorage @JvmOverloads constructor(
    private val context: Context,
    private val preferenceName: String = SHARED_PREFERENCES_NAME
) : Storage {
    override fun saveString(alias: String, content: String) {
        context.getSharedPreferences(preferenceName, Context.MODE_PRIVATE)
            .edit()
            .putString(alias, content)
            .apply()
    }

    override fun getString(alias: String): String? {
        return context.getSharedPreferences(preferenceName, Context.MODE_PRIVATE)
            .getString(alias, null)
    }

    private fun saveKeyString(alias: String, value: String) {
        context.getSharedPreferences(preferenceName, Context.MODE_PRIVATE)
            .edit()
            .putString(alias, value)
            .apply()
    }

    private fun getKeyString(alias: String): String? {
        return context.getSharedPreferences(preferenceName, Context.MODE_PRIVATE)
            .getString(alias, null)
    }

    /**
     * {@inheritDoc}
     */
    override fun remove(alias: String) {
        context.getSharedPreferences(preferenceName, Context.MODE_PRIVATE)
            .edit()
            .remove(alias)
            .apply()
    }

    /**
     * {@inheritDoc}
     */
    override fun containsAlias(alias: String): Boolean {
        return context.getSharedPreferences(preferenceName, Context.MODE_PRIVATE)
            .contains(alias)
    }

    /**
     * {@inheritDoc}
     */
    override fun getKeyBytes(alias: String): ByteArray? {
        val value = getKeyString(alias)
        return if (value != null) {
            Base64.decode(value, Base64.DEFAULT)
        } else null
    }

    /**
     * {@inheritDoc}
     */
    override fun saveKeyBytes(alias: String, content: ByteArray) {
        saveKeyString(alias, Base64.encodeToString(content, Base64.DEFAULT))
    }

    companion object {
        private val SHARED_PREFERENCES_NAME = CipherPreferencesStorage::class.java.name + "_security_storage"
    }
}
