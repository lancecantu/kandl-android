package com.lancedcan.kandl_android

import android.app.Application
import com.google.firebase.FirebaseApp
import com.google.firebase.appcheck.FirebaseAppCheck
import com.lancedcan.kandl_android.appcheck.appCheckProviderFactory

class KandlApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        FirebaseApp.initializeApp(this)
        FirebaseAppCheck.getInstance()
            .installAppCheckProviderFactory(appCheckProviderFactory())
    }
}
