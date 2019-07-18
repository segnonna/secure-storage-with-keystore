package hos.houns.securestorage

import android.content.Context
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.nhaarman.mockitokotlin2.mock
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Rule
import org.junit.Test

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * @see [Testing documentation](http://d.android.com/tools/testing)
 */
class ExampleUnitTest {

    @Rule
    @JvmField
    var instantExecutorRule = InstantTaskExecutorRule()

    private val mcontext = mock<Context>()
    @Before
    fun before() {
        SecureStorage.init(mcontext)
        SecureStorage.setValue<String>("test", "urrrrrlllllll")
    }


    @Test
    fun addition_isCorrect() {
        SecureStorage.getValue<String>("test")
        assertEquals(4, (2 + 2).toLong())
    }
}