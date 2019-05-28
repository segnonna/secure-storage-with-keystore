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

interface Storage {
    /**
     * Responsible for save the current value associated with this alias
     * @param alias something unique to retrieve the value from the system
     * @param content the actual content of the key (encrypted)
     */
    fun saveKeyBytes(alias: String, content: ByteArray)

    fun saveString(alias: String, content: String)

    /**
     * Return the current encrypted value of the current alias
     * @param alias the unique alias to retrieve
     * @return the key bytes in success null otherwise
     */
    fun getKeyBytes(alias: String): ByteArray?

    fun getString(alias: String): String?

    /**
     * Check if the value for this alias exists
     * @param alias the unique alias to check
     * @return true if the value exists
     */
    fun containsAlias(alias: String): Boolean

    /**
     * Remove the current value associated with this alias
     * @param alias the unique alias to check
     */
    fun remove(alias: String)

    fun removeAll(): Boolean
}
