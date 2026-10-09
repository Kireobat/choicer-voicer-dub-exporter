package eu.kireobat.choicer_voicer_dub_exporter.utils

import java.util.Locale

enum class OS {
    WINDOWS, LINUX, MACOS
}


class OsUtil {

    fun detectOperatingSystem(): OS {
        val osName = System.getProperty("os.name").lowercase(Locale.ROOT)
        return when {
            osName.contains("mac") -> OS.MACOS
            osName.startsWith("windows") -> OS.WINDOWS
            osName.contains("nix") || osName.contains("nux") || osName.contains("aix") -> OS.LINUX
            else -> throw IllegalArgumentException("OS not supported")
        }
    }
}