package eu.kireobat.choicer_voicer_dub_exporter

import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application


fun main() = application {
    Window(
        onCloseRequest = ::exitApplication,
        title = "choicer-voicer-dub-exporter",
    ) {
        App(APP_VERSION)
    }
}