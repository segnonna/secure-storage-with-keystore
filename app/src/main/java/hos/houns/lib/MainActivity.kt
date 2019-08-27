package hos.houns.lib

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import hos.houns.securestorage.SecureStorage
import timber.log.Timber
import java.util.*

class MainActivity : AppCompatActivity() {

    var test: String? = ""
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        SecureStorage.init(this)
        test = null

        SecureStorage.setValue("name", "Hospice HOUNSOU")
        SecureStorage.setValue("age", 50)
        SecureStorage.setValue("date", Date())
        SecureStorage.setValue("weight", 70.0)
        SecureStorage.setValue("list", mutableListOf("One", "Two", "Three"))
        Timber.e("name: ${SecureStorage.getValue<String>("name")}")
        Timber.e("name: ${SecureStorage.getValue<String>("name")}")
        Timber.e("age: ${SecureStorage.getValue<Int>("age")}")

        Timber.e("nameee: ${SecureStorage.setValue("nameee", null)}")
        Timber.e("date: ${SecureStorage.getValue<Date>("date")}")
        Timber.e("weight: ${SecureStorage.getValue<Double>("weight")}")
        Timber.e("list: ${SecureStorage.getValue<MutableList<String>>("list")?.first()}")
        SecureStorage.clearAll()
        Timber.e("name: ${SecureStorage.getValue<String>("name")}")


        // Timber.e("clearAll: ${SecureStorage.clearAll()}")
        // Timber.e("name: ${SecureStorage.getValue<String>("name")}")

    }
}
