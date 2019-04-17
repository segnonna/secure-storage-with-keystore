package hos.houns.lib

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import hos.houns.seckeystore.SimpleKeystore
import timber.log.Timber
import java.util.*

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        with(SimpleKeystore(this)) {
            saveSensitiveData("name", "Hospice HOUNSOU")
            saveSensitiveData("age", 50)
             saveSensitiveData("date", Date())
            // saveSensitiveData("weight", 70.0)
            /*saveSensitiveData("list", mutableListOf("One", "Two", "Three"))*/

            Timber.e("name: ${getSensitiveData<String>("name")}")
            Timber.e("age: ${getSensitiveData<Int>("age")}")
            Timber.e("date: ${getSensitiveData<Date>("date")}")
        }

    }
}
