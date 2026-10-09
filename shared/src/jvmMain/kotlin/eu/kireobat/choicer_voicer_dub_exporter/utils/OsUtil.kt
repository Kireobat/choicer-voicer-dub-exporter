package eu.kireobat.choicer_voicer_dub_exporter.utils

import androidx.compose.ui.text.toLowerCase
import java.util.Locale
import java.util.Locale.getDefault

enum class OS {
    WINDOWS, LINUX, MACOS
}


class OsUtil {

    fun detectOperatingSystem(): OS {
        val osName = System.getProperty("os.name").lowercase(getDefault())
        return when {
            osName.contains("win") -> OS.WINDOWS
            osName.contains("nix") || osName.contains("nux") || osName.contains("aix") -> OS.LINUX
            osName.contains("mac") -> OS.MACOS
            else -> throw IllegalArgumentException("OS not supported")
        }
    }
}