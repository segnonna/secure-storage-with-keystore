package hos.houns.lib

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import java.util.*

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        with(SimpleKeystore(this)) {
            saveSensitiveData("name", "Hospice HOUNSOU")
            saveSensitiveData("age", 50)
            saveSensitiveData("date", Date())
            saveSensitiveData("weight", 70.0)
            saveSensitiveData("list", mutableListOf("One", "Two", "Three"))
        }

        with(SimpleKeystore(this)) {
            Timber.e(getSensitiveData<String>("name"))
            Timber.e(getSensitiveData<Int>("age").toString())
            Timber.e(getSensitiveData<Date>("date").time.toString())
            Timber.e(getSensitiveData<MutableList<String>>("list").toString())

        }
    }
}
