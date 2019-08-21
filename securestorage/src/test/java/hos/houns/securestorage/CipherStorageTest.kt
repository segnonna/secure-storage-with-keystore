package hos.houns.securestorage

import android.content.Context
import android.content.SharedPreferences
import android.util.Base64
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.nhaarman.mockitokotlin2.any
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.mockito.ArgumentMatchers.anyInt
import org.mockito.ArgumentMatchers.anyString
import org.mockito.Mockito
import org.mockito.Mockito.`when`
import org.powermock.api.mockito.PowerMockito
import org.powermock.core.classloader.annotations.PrepareForTest


@PrepareForTest(Base64::class)
class CipherStorageTest {

    private lateinit var mCipherPreferencesStorage: CipherPreferencesStorage
    @Rule
    @JvmField
    var instantExecutorRule = InstantTaskExecutorRule()

    private var sharedPrefs: SharedPreferences = Mockito.mock(SharedPreferences::class.java)
    private var editor: SharedPreferences.Editor =
        Mockito.mock(SharedPreferences.Editor::class.java)
    private var context: Context = Mockito.mock(Context::class.java)
    private var mbase64: Base64 = Mockito.mock(Base64::class.java)

    init {
        `when`(context.getSharedPreferences(anyString(), anyInt())).thenReturn(sharedPrefs)
        mCipherPreferencesStorage = CipherPreferencesStorage(context)
    }

    @org.junit.jupiter.api.Test
    @Throws(Exception::class)
    fun testGetString() {
        Mockito.`when`(sharedPrefs.getString("test", null)).thenReturn("foobar")
        assertEquals("foobar", mCipherPreferencesStorage.getString("test"))
    }


    @org.junit.jupiter.api.Test
    @Throws(Exception::class)
    fun testGetKeyString() {
        PowerMockito.mockStatic(Base64::class.java)
        `when`(
            Base64.encode(
                any(),
                anyInt()
            )
        ).thenAnswer { invocation ->
            java.util.Base64.getEncoder().encode(invocation.arguments[0] as ByteArray)
        }
        `when`(
            Base64.decode(
                anyString(),
                anyInt()
            )
        ).thenAnswer { invocation ->
            java.util.Base64.getMimeDecoder().decode(invocation.arguments[0] as String)
        }
//        Mockito.`when`(Base64.decode(anyString(), anyInt())).thenReturn(ByteArray(10))
        Mockito.`when`(sharedPrefs.getString("test", null)).thenReturn("foobar")
        assertEquals(ByteArray(10), mCipherPreferencesStorage.getKeyBytes("test"))
    }


    @org.junit.jupiter.api.Test
    @Throws(Exception::class)
    fun testSaveString() {

    }

    @org.junit.jupiter.api.Test
    fun addition_isCorrect() {
        assertEquals(4, (2 + 2).toLong())
    }
}