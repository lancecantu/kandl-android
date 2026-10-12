package com.lancedcan.kandl_android

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.credentials.CredentialManager
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth
import com.google.firebase.Firebase
import com.lancedcan.kandl_android.designsystem.KandlandroidTheme

class MainActivity : ComponentActivity() {
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        auth = Firebase.auth
        enableEdgeToEdge()
        setContent {
            KandlandroidTheme {
                Box(modifier = Modifier.fillMaxSize()) {
                    App()
                    DevMenuOverlay(modifier = Modifier.align(Alignment.TopStart))
                }
            }
        }
    }

    override fun onStart() {
        super.onStart()
        auth.currentUser?.let { }
    }
}
