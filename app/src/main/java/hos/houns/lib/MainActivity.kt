package hos.houns.lib

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import hos.houns.seckeystore.PreferenceStorage
import timber.log.Timber
import java.util.*

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        with(PreferenceStorage(this)) {
            saveSensitiveData("name", "Hospice HOUNSOU")
            saveSensitiveData("age", 50)
            saveSensitiveData("date", Date())
            saveSensitiveData("weight", 70.0)
        }

        with(PreferenceStorage(this)) {
            Timber.e(getSensitiveData<String>("name"))
            Timber.e(getSensitiveData<Int>("age").toString())
            Timber.e(getSensitiveData<Date>("date")?.time.toString())

        }
    }
}
