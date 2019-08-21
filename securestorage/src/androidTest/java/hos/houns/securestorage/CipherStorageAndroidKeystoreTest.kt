package hos.houns.securestorage

import com.nhaarman.mockitokotlin2.mock
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith


@RunWith(androidx.test.ext.junit.runners.AndroidJUnit4::class)
class CipherStorageAndroidKeystoreTest {
    private lateinit var mCipherStorage: CipherStorageAndroidKeystore

    @Before
    fun initApp() {
        val appContext =
            androidx.test.platform.app.InstrumentationRegistry.getInstrumentation().targetContext
        mCipherStorage = CipherStorageAndroidKeystore(appContext, mock())
    }

    @Test
    fun init_storage() {

        println("toto")
    }


}
