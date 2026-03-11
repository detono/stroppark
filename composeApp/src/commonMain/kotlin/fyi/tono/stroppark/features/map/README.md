# Purpose

This template provides a standard folder structure for implementing a new feature.
It promotes clean architecture with clear separation between domain, data, and UI layers.

# Feature Structure

```text
features/
└── feature/
    ├── domain/         # Immutable data models and business logic
    ├── data/           # Repository implementations and data sources
    └── ui/
        ├── FeatureViewModel.kt  # ViewModel for feature state management
        ├── components/         # Reusable UI components (e.g., FeatureCard)
        └── screens/            # Screens (e.g., FeatureListScreen, FeatureDetailScreen)