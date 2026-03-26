package fyi.tono.stroppark.features.chargers.ui

import fyi.tono.stroppark.core.utils.SyncProgress
import fyi.tono.stroppark.features.chargers.domain.ChargerFilter

data class ChargerUiState (
  val isLoading: Boolean = false,
  val chargers: List<ChargerUiModel> = emptyList(),
  val errorMessage: String? = null,
  val activeFilters: Set<ChargerFilter> = setOf(),
  val syncProgress: SyncProgress? = null
) {
  val availableFilters: Set<ChargerFilter> = buildSet {
    ChargerFilter.entries.forEach {
      add(it)
    }
  }
}

