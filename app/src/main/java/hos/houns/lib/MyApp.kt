package hos.houns.lib

import android.app.Application
import androidx.multidex.MultiDex
import timber.log.Timber

class MyApp : Application() {
    override fun onCreate() {
        super.onCreate()
        MultiDex.install(this)
        if (BuildConfig.DEBUG)
            Timber.plant(Timber.DebugTree())
    }
}