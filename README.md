# Introduction

## Securely (simple key-value) Storing Secrets in an Android Application


To store securely data on a device, you probably don't want to rely only on
the separation of processes of the Android OS but make sure the stored values are also encrypted.

A general rule for mobile development is you should not use any hardcoded keys because a hacker can easily
decompile your code and obtain the key, thereby rendering the encryption useless. You need a key management framework,
and that’s what the Android KeyStore API is designed for.

To make it possible, i use the Android Keystore and the SharedPreferences.The keystore is used for generating cryptographic keys,
 the values are then encrypted with these keys and subsequently
  securely stored in the SharedPreferences.


KeyStore provides two functions:

Randomly generates keys; and

Securely stores the keys

With these, storing secrets becomes easy. All you have to do is:

    - Generate a random key when the app runs the first time;
    - When you want to store a secret, retrieve the key from KeyStore, encrypt the data with it, and then store the encrypted data in Preferences.
    - When you want to read a secret, read the encrypted data from Preferences, get the key from KeyStore and then use the key to decrypt the data.

For more information:
    -Android keystore system (https://developer.android.com/training/articles/keystore)
    -Basic Android Keystore (https://github.com/googlesamples/android-BasicAndroidKeyStore)

# For Pre Android M, the library does follow things:

## Key Generation

    * Generate a pair of RSA keys;
    * Generate a random AES key;
    * Encrypt the AES key using the RSA public key;
    * Store the encrypted AES key in Preferences.

## Encrypting and Storing the data

    * Retrieve the encrypted AES key from Preferences;
    * Decrypt the above to obtain the AES key using the private RSA key;
    * Encrypt the data using the AES key;

## Retrieving and decrypting the data

    * Retrieve the encrypted AES key from Preferences;
    * Decrypt the above to obtain the AES key using the private RSA key;
    * Decrypt the data using the AES key


In your project root build.gradle with:

```
allprojects {
    repositories {
        maven { url "https://jitpack.io" }
    }
}
```

and in the app or module build.gradle:

```
dependencies {
    api 'com.github.hounsouh:simplekeystore:$releaseVersion'
}
```

# Simple Keystore

To securely store your data,

```
with(SimpleKeystore(this)) {
            saveSensitiveData("name", "Hospice HOUNSOU")
            saveSensitiveData("age", 50)
            saveSensitiveData("date", Date())
            saveSensitiveData("weight", 70.0)
        }

```
In this exemple, name is stored in sharedPreferences as **n7QHzGOUs2iDkNFZDU/HQ0sflqFh0HWCkqcV8I5kKMxU**

To read secured data,

```
 with(SimpleKeystore(this)) {
            Timber.e(getSensitiveData<String>("name") )
            Timber.e(getSensitiveData<Int>("age").toString() )
            Timber.e(getSensitiveData<Date>("date")?.time.toString() )
}

```

Note that this library is generic. It allows you to store and read any type of objects





