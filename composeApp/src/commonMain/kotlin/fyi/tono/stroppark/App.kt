package fyi.tono.stroppark

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.tooling.preview.Preview
import dev.icerock.moko.permissions.PermissionsController
import dev.icerock.moko.permissions.compose.BindEffect
import fyi.tono.stroppark.core.ui.navigation.AppNavigation
import fyi.tono.stroppark.core.ui.theme.StropParkTheme
import org.koin.compose.koinInject

@Composable
@Preview
fun App() {
    val permissionsController = koinInject<PermissionsController>()
    BindEffect(permissionsController)

    StropParkTheme {
        AppNavigation()
    }
}