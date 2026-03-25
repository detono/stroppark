package fyi.tono.stroppark.core.utils

sealed interface PermissionDialog {
  data object Rationale : PermissionDialog
  data object Settings : PermissionDialog
  data object None : PermissionDialog
}