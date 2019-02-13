package hos.houns.lib

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import hos.houns.seckeystore.PreferenceStorage
import timber.log.Timber

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        with(PreferenceStorage(this)) {
            saveSensitiveData("name", "Hospice HOUNSOU")
            saveSensitiveData("age", 30)
        }

        with(PreferenceStorage(this)) {
            Timber.e(getSensitiveData("n7QHzGOUs2iDkNFZDU/HQ0sflqFh0HWCkqcV8I5kKMxU", kotlin.String::class.java))
            Timber.e(getSensitiveData("jsyub+zGpmW2bU5pra54K3j1", kotlin.Int::class.java).toString())
        }
    }
}
