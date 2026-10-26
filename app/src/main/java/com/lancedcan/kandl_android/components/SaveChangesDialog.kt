package com.lancedcan.kandl_android

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.text.font.FontWeight
import com.lancedcan.kandl_android.designsystem.components.KandlContrastButton
import com.lancedcan.kandl_android.designsystem.components.KandlTextButton

/**
 * Back-out prompt for the notepad screens, where users read the back arrow as "done". Both
 * ways out are offered rather than a warning. [reason] explains why saving is unavailable
 * whenever [saveEnabled] is false.
 */
@Composable
fun SaveChangesDialog(
    onExit: () -> Unit,
    onSaveAndExit: () -> Unit,
    onDismiss: () -> Unit,
    saveEnabled: Boolean = true,
    reason: String? = null
) {
    val body: (@Composable () -> Unit)? = if (reason == null) {
        null
    } else {
        { Text(reason, style = MaterialTheme.typography.bodyMedium) }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                "Save changes?",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
            )
        },
        text = body,
        confirmButton = {
            KandlContrastButton(
                text = "Save & Exit",
                onClick = onSaveAndExit,
                enabled = saveEnabled
            )
        },
        dismissButton = {
            KandlTextButton(
                text = "Exit",
                onClick = onExit,
                contentColor = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    )
}
