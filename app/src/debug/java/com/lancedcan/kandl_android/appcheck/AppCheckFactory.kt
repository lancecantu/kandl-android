package com.lancedcan.kandl_android.appcheck

import com.google.firebase.appcheck.AppCheckProviderFactory
import com.google.firebase.appcheck.debug.DebugAppCheckProviderFactory

/**
 * Sideloaded builds cannot produce a Play Integrity verdict, so they present
 * a debug token instead. It is printed to logcat on first launch and has to
 * be registered in the Firebase console before this build can reach an
 * App Check enforced backend.
 */
fun appCheckProviderFactory(): AppCheckProviderFactory =
    DebugAppCheckProviderFactory.getInstance()
