package com.lancedcan.kandl_android

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import com.lancedcan.kandl_android.designsystem.Spacing

@Composable
fun LoginScreen(
    viewModel: LoginViewModel,
    onLoginSuccess: () -> Unit
) {
    val context = LocalContext.current

    val currentUser = viewModel.currentUser
    val email = viewModel.email
    val password = viewModel.password
    val loading = viewModel.loading
    val errorMessage = viewModel.errorMessage

    Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(Spacing.s6),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            when {
                currentUser == null -> {
                    Text(
                        text = "Sign in or create account",
                        style = MaterialTheme.typography.headlineSmall,
                    )
                    Spacer(modifier = Modifier.height(Spacing.s6))
                    OutlinedTextField(
                        value = email,
                        onValueChange = { viewModel.setEmailText(it) },
                        label = { Text("Email") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                    )
                    Spacer(modifier = Modifier.height(Spacing.s4))
                    OutlinedTextField(
                        value = password,
                        onValueChange = { viewModel.setPasswordText(it) },
                        label = { Text("Password") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        visualTransformation = PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                    )
                    errorMessage?.let { msg ->
                        Spacer(modifier = Modifier.height(Spacing.s3))
                        Text(
                            text = msg,
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodySmall,
                        )
                    }
                    Spacer(modifier = Modifier.height(Spacing.s5))
                    if (loading) {
                        CircularProgressIndicator()
                    } else {
                        Button(
                            onClick = {
                                viewModel.onCreateAccountButtonClick(context, onLoginSuccess)
                            },
                            modifier = Modifier.fillMaxWidth(),
                        ) {
                            Text("Create account")
                        }
                        Spacer(modifier = Modifier.height(Spacing.s3))
                        Button(
                            onClick = {
                                viewModel.onSignInButtonClick(context, onLoginSuccess)
                            },
                            modifier = Modifier.fillMaxWidth(),
                        ) {
                            Text("Sign in")
                        }
                    }
                    Spacer(modifier = Modifier.height(Spacing.s6))
                    Text(
                        text = "or sign in with",
                        style = MaterialTheme.typography.bodyMedium,
                    )
                    Spacer(modifier = Modifier.height(Spacing.s4))
                    Button(
                        onClick = {
                            viewModel.onSignInWithGoogleButtonClick(context, onLoginSuccess)
                        },
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Text("Sign in with Google")
                    }
                }
            }
        }
    }
}