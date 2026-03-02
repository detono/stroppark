package fyi.tono.stroppark.core.ui.components.molecules

import androidx.compose.foundation.layout.size
import androidx.compose.material.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp

@Composable
fun InfoChip(
  label: String,
  icon: ImageVector,
  containerColor: Color,
  contentColor: Color,
) {
  StropChip(
    label = label,
    containerColor = containerColor,
    contentColor = contentColor,
    leading = {
      Icon(
        imageVector = icon,
        contentDescription = null,
        modifier = Modifier.size(12.dp),
      )
    }
  )
}