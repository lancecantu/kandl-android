package com.lancedcan.kandl_android

import android.content.Context
import android.util.Log
import android.widget.Toast
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.credentials.Credential
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialCancellationException
import androidx.credentials.exceptions.GetCredentialException
import androidx.credentials.exceptions.NoCredentialException
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.google.android.libraries.identity.googleid.GetSignInWithGoogleOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential.Companion.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import kotlinx.coroutines.launch

class LoginViewModel() : ViewModel() {
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()

    var currentUser by mutableStateOf<FirebaseUser?>(auth.currentUser)
        private set

    var email by mutableStateOf("")
        private set

    var password by mutableStateOf("")
        private set

    var loading by mutableStateOf(false)
        private set

    var errorMessage by mutableStateOf<String?>(null)
        private set

    internal fun setEmailText(text: String) {
        email = text
        errorMessage = null;
    }

    internal fun setPasswordText(text: String) {
        password = text
        errorMessage = null;
    }

    internal fun updateUI(user: FirebaseUser?) {
        currentUser = user
        loading = false
    }

    internal fun onCreateAccountButtonClick (context: Context, onSuccess: () -> Unit) {
        if (email.isBlank() || password.isBlank()) {
            errorMessage = "Enter email and password"
            return
        }
        loading = true
        errorMessage = null
        createAccount(context, email, password) { user ->
            updateUI(user)
            if (user == null) {
                errorMessage = "Authentication failed."
            } else {
                onSuccess()
            }
        }
    }

    internal fun createAccount(context: Context, email: String, password: String, onResult: (FirebaseUser?) -> Unit) {
        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    Log.d(TAG, "createUserWithEmail:success")
                    onResult(auth.currentUser)
                } else {
                    Log.w(TAG, "createUserWithEmail:failure", task.exception)
                    Toast.makeText(context, "Authentication failed.", Toast.LENGTH_SHORT).show()
                    onResult(null)
                }
            }
    }

    internal fun onSignInButtonClick (context: Context, onSuccess: () -> Unit) {
        if (email.isBlank() || password.isBlank()) {
            errorMessage = "Enter email and password"
            return
        }
        loading = true
        errorMessage = null
        signIn(context, email, password) { user ->
            updateUI(user)

            if (user == null) {
                errorMessage = "Sign in failed."
            } else {
                onSuccess()
            }
        }
    }

    internal fun signIn(context: Context, email: String, password: String, onResult: (FirebaseUser?) -> Unit) {
        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    Log.d(TAG, "signInWithEmail:success")
                    onResult(auth.currentUser)
                } else {
                    Log.w(TAG, "signInWithEmail:failure", task.exception)
                    Toast.makeText(context, "Sign in failed.", Toast.LENGTH_SHORT).show()
                    onResult(null)
                }
            }
    }

//    internal fun signOut() {
//        auth.signOut()
//
//        viewModelScope.launch {
//            try {
//                val clearRequest = ClearCredentialStateRequest()
//                credentialManager.clearCredentialState(clearRequest)
//            } catch (e: ClearCredentialException) {
//                Log.e(TAG, "Couldn't clear user credentials: ${e.localizedMessage}")
//            }
//        }
//    }

//    private fun signOutAndFinish() {
//        auth.signOut()
//        lifecycleScope.launch {
//            try {
//                credentialManager.clearCredentialState(ClearCredentialStateRequest())
//            } catch (_: ClearCredentialException) { }
//        }
//        startActivity(Intent(this, LoginActivity::class.java).apply {
//            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
//        })
//        finish()
//    }

    internal fun onSignInWithGoogleButtonClick(context: Context, onSuccess: () -> Unit) {
        loading = true
        errorMessage = null
        viewModelScope.launch {
            try {
                val credentialManager = CredentialManager.create(context)

                val googleSignInOption = GetSignInWithGoogleOption(
                    serverClientId = context.getString(R.string.default_web_client_id)
                )
                val request = GetCredentialRequest.Builder()
                    .addCredentialOption(googleSignInOption)
                    .build()
                val result = credentialManager.getCredential(
                    context = context,
                    request = request
                )
                handleGoogleSignIn(context, result.credential) { user ->
                    updateUI(user)

                    if (user == null) {
                        errorMessage = "Sign in failed."
                    } else {
                        onSuccess()
                    }
                }
            } catch (e: GetCredentialException) {
                loading = false
                when (e) {
                    is NoCredentialException -> {
                        errorMessage = "No Google account selected. Add an account in Settings or try again."
                    }
                    is GetCredentialCancellationException -> {
                        if (e.message?.contains("reauth failed", ignoreCase = true) == true) {
                            Log.e(TAG, "Account reauth failed - check SHA-1 in Firebase Console", e)
                            errorMessage = "Google sign-in failed. Ensure your app's SHA-1 fingerprint is added in Firebase Console."
                        } else {
                            // User cancelled the account picker
                            errorMessage = null
                        }
                    }
                    else -> {
                        Log.e(TAG, "Google sign-in failed", e)
                        errorMessage = "Google sign-in failed."
                    }
                }
            }
        }
    }

    internal fun handleGoogleSignIn(context: Context, credential: Credential, onResult: (FirebaseUser?) -> Unit) {
        if (credential is CustomCredential && credential.type == TYPE_GOOGLE_ID_TOKEN_CREDENTIAL) {
            val googleIdTokenCredential = GoogleIdTokenCredential.createFrom(credential.data)
            val firebaseCredential = GoogleAuthProvider.getCredential(googleIdTokenCredential.idToken, null)
            auth.signInWithCredential(firebaseCredential)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        Log.d(TAG, "signInWithGoogle:success")
                        onResult(auth.currentUser)
                    } else {
                        Log.w(TAG, "signInWithGoogle:failure", task.exception)
                        Toast.makeText(context, "Google sign-in failed.", Toast.LENGTH_SHORT).show()
                        onResult(null)
                    }
                }
        } else {
            Log.w(TAG, "Credential is not Google ID")
            onResult(null)
        }
    }

    companion object {
        private const val TAG = "Login"
    }
}

class LoginViewModelFactory() : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(LoginViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return LoginViewModel() as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}