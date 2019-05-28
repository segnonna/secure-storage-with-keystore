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
     * Encrypt the value associating with this alias
     * the alias it's the only way to access the value,
     * note if you already have the value stored this alias it will
     * override the current value
     *
     * @param alias the key for the value
     * @param value the value to store
     */
    fun <T> encrypt(alias: String, value: T)

    /**
     * Decrypt the value previously associated with this alias
     *
     * @param alias the key for access this value
     * @return null if no value has founded for this alias or the decrypted value
     */
    fun <T> decrypt(alias: String): T?

    /**
     * Check if there is an value for this alias stored,
     * including a check into the keystore and the Android Shared Preferences
     *
     * @param alias the key for access this value
     * @return true if there is a value associated with this key
     */
    fun containsAlias(alias: String): Boolean

    /**
     * Remove the current value previously associated with this key
     * it'll remove the value from the KeyStore and from the Android Shared Preferences
     *
     * @param alias the key for access this value
     */
    fun removeKey(alias: String)

    fun removeAll(): Boolean
}
