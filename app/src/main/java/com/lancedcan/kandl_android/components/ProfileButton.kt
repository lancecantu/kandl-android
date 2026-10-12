package com.lancedcan.kandl_android

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.lancedcan.kandl_android.designsystem.components.KandlIconButton

/** Opens the profile panel. Only shown on the tabs' own landing screens. */
@Composable
fun ProfileButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    KandlIconButton(
        icon = Icons.Default.Person,
        contentDescription = "Profile",
        onClick = onClick,
        modifier = modifier
    )
}
