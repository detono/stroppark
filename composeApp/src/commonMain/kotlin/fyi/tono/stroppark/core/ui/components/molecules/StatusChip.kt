package fyi.tono.stroppark.core.ui.components.molecules

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

@Composable
fun StatusChip(
  label: String,
  dotColor: Color,
  containerColor: Color,
  labelColor: Color,
) {
  StropChip(
    label = label,
    containerColor = containerColor,
    contentColor = labelColor,
    leading = {
      Box(
        modifier = Modifier
          .size(7.dp)
          .background(dotColor, CircleShape),
      )
    }
  )
}