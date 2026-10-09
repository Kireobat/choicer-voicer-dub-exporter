package eu.kireobat.choicer_voicer_dub_exporter

import androidx.compose.runtime.Composable
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import choicer_voicer_dub_exporter.shared.generated.resources.Roboto_Regular
import choicer_voicer_dub_exporter.shared.generated.resources.Res
import choicer_voicer_dub_exporter.shared.generated.resources.Roboto_Light
import choicer_voicer_dub_exporter.shared.generated.resources.Roboto_LightItalic
import org.jetbrains.compose.resources.Font


@Composable
fun rememberRobotoFontFamily(): FontFamily =
    FontFamily(
        Font(Res.font.Roboto_Regular, FontWeight.Normal),
        Font(Res.font.Roboto_Light, FontWeight.Light),
        Font(Res.font.Roboto_LightItalic, FontWeight.Light, FontStyle.Italic),
    )