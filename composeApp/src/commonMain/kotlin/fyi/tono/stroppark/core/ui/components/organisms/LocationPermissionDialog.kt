package fyi.tono.stroppark.core.ui.components.organisms

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.stringResource

@Composable
fun LocationPermissionDialog(
  title: StringResource,
  text: StringResource,
  confirmText: StringResource,
  dismissText: StringResource,
  onConfirm: () -> Unit,
  onDismiss: () -> Unit,
) {
  AlertDialog(
    onDismissRequest = onDismiss,
    title = { Text(stringResource(title)) },
    text = { Text(stringResource(text)) },
    confirmButton = {
      TextButton(onClick = onConfirm) { Text(stringResource(confirmText)) }
    },
    dismissButton = {
      TextButton(onClick = onDismiss) { Text(stringResource(dismissText)) }
    }
  )
}