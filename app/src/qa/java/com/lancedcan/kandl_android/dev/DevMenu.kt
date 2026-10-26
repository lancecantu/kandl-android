package com.lancedcan.kandl_android

import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.credentials.ClearCredentialStateRequest
import androidx.credentials.CredentialManager
import androidx.credentials.exceptions.ClearCredentialException
import com.google.firebase.auth.FirebaseAuth
import com.lancedcan.kandl_android.designsystem.KandlShapes
import com.lancedcan.kandl_android.designsystem.KandlTertiaryYellow
import com.lancedcan.kandl_android.designsystem.OnSurface
import com.lancedcan.kandl_android.designsystem.Spacing
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

private const val TAG = "DevMenu"

@Composable
fun DevMenuOverlay(modifier: Modifier = Modifier) {
    var menuOpen by remember { mutableStateOf(false) }

    DevMenuLauncher(
        onClick = { menuOpen = true },
        modifier = modifier
    )

    if (menuOpen) {
        DevMenuPage(onDismiss = { menuOpen = false })
    }
}

@Composable
private fun DevMenuLauncher(onClick: () -> Unit, modifier: Modifier = Modifier) {
    Text(
        text = "DEV",
        style = MaterialTheme.typography.labelSmall,
        fontWeight = FontWeight.Bold,
        color = OnSurface,
        modifier = modifier
            .statusBarsPadding()
            .padding(start = Spacing.s8, top = Spacing.s2)
            .clip(KandlShapes.badge)
            .background(KandlTertiaryYellow.copy(alpha = 0.55f))
            .clickable(onClick = onClick)
            .padding(horizontal = Spacing.s3, vertical = Spacing.s1)
    )
}

@Composable
private fun DevMenuPage(onDismiss: () -> Unit) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    val actions = remember(context, scope) {
        listOf(
            DevAction("Force logout") { forceLogout(context, scope) }
        )
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .statusBarsPadding()
                    .padding(Spacing.s5)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    ScreenTitleHeader(
                        title = "Dev Menu",
                        modifier = Modifier.weight(1f)
                    )
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Close dev menu")
                    }
                }

                LazyColumn(verticalArrangement = Arrangement.spacedBy(Spacing.s3)) {
                    items(actions) { action ->
                        DevMenuActionButton(action = action)
                    }
                }
            }
        }
    }
}

@Composable
private fun DevMenuActionButton(action: DevAction) {
    OutlinedButton(
        onClick = action.onClick,
        modifier = Modifier.fillMaxWidth()
    ) {
        Text(
            text = action.label,
            style = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.Start,
            modifier = Modifier.fillMaxWidth()
        )
    }
}

private data class DevAction(
    val label: String,
    val onClick: () -> Unit
)

private fun forceLogout(context: Context, scope: CoroutineScope) {
    FirebaseAuth.getInstance().signOut()

    scope.launch {
        try {
            CredentialManager.create(context).clearCredentialState(ClearCredentialStateRequest())
        } catch (e: ClearCredentialException) {
            Log.e(TAG, "Couldn't clear credential state: ${e.localizedMessage}")
        }

        context.startActivity(
            Intent(context, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            }
        )
    }
}
