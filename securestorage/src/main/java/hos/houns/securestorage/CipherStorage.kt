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

/**
 * Main interface for access the KeyStore implementations
 */
interface CipherStorage {
    /**
     * @param alias the key for the value
     * @param value the value to store
     */
    fun <T> encrypt(alias: String, value: T)

    /**
     * @param alias the key for access this value
     * @return null if no value has founded for this alias or the decrypted value
     */
    fun <T> decrypt(alias: String): T?

    /**
     * @param alias the key for access this value
     * @return true if there is a value associated with this key
     */
    fun containsAlias(alias: String): Boolean

    /**
     * @param alias the key for access this value
     */
    fun removeKey(alias: String)

    /**
     * @return true if success
     */
    fun removeAll(): Boolean
}
