# Introduction

##Securely (simple key-value) Storing Secrets in an Android Application

A general rule for mobile development is you should not use any hardcoded keys because a hacker can easily
 decompile your code and obtain the key, thereby rendering the encryption useless. You need a key management framework,
 and that’s what the Android KeyStore API is designed for.

KeyStore provides two functions:

Randomly generates keys; and

Securely stores the keys

With these, storing secrets becomes easy. All you have to do is:

    - Generate a random key when the app runs the first time;
    - When you want to store a secret, retrieve the key from KeyStore, encrypt the data with it, and then store the encrypted data in Preferences.
    - When you want to read a secret, read the encrypted data from Preferences, get the key from KeyStore and then use the key to decrypt the data.



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

#Simple Keystore

To securely store your data,

```
with(PreferenceStorage(this)) {
            saveSensitiveData("name", "Hospice HOUNSOU")
            saveSensitiveData("age", 50)
            saveSensitiveData("date", Date())
            saveSensitiveData("weight", 70.0)
        }

```
In this exemple, name is stored in sharedPreferences as **n7QHzGOUs2iDkNFZDU/HQ0sflqFh0HWCkqcV8I5kKMxU**

To read secured data,

```
 with(PreferenceStorage(this)) {
            Timber.e(getSensitiveData("n7QHzGOUs2iDkNFZDU/HQ0sflqFh0HWCkqcV8I5kKMxU", String::class.java))
            Timber.e(getSensitiveData("jsyub+zGpmW2bU5pra54K3j1", Int::class.java).toString())
}

```

Note that this library is generic. It allows you to store and read any type of objects





