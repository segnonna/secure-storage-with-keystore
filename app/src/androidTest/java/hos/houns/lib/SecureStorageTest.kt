package hos.houns.lib

import androidx.test.platform.app.InstrumentationRegistry
import hos.houns.securestorage.SecureStorage
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith


@RunWith(androidx.test.ext.junit.runners.AndroidJUnit4::class)
class SecureStorageTest {

    @Test
    fun useAppContext() {
        // Context of the app under test.
        val appContext = InstrumentationRegistry.getInstrumentation().targetContext
        assertEquals("hos.houns.lib", appContext.packageName)
    }

    @Before
    fun init_storage() {
        //SecureStorage.init(androidx.test.platform.app.InstrumentationRegistry.getInstrumentation().targetContext)
    }

    @Test
    fun getValue_WithoutInitSecureStorageReturnNull() {
        assertEquals(null, SecureStorage.getValue<String>("test"))
    }

    @Test
    fun setValue_WithoutInitSecureStorageDontCrash() {
        SecureStorage.setValue("test1", "myTest")
    }

}
