package eu.kireobat.choicer_voicer_dub_exporter.utils

import eu.kireobat.choicer_voicer_dub_exporter.interfaces.PackInfo
import eu.kireobat.choicer_voicer_dub_exporter.interfaces.Placement
import java.nio.file.Files
import java.nio.file.Path
import kotlin.streams.toList

class IniUtil {
    fun readVoicePackIni(path: Path): PackInfo {

        val packInfo = PackInfo("", Path.of(""), emptyList(), "", emptyList())

        Files.readString(path.resolve("_pack_info.ini")).split("\n").let { string ->
            string
                .filter { it.isNotEmpty() && it.contains("=")}
                .forEach {

                    if (it.startsWith("title=")) {
                        packInfo.title = it.substringAfter("title=").trim().trim('"')
                    } else if (it.startsWith("authors=")) {
                        packInfo.authors = it.substringAfter("authors=").trim().trim('"','[',']').split(",")
                    } else if (it.startsWith("icon=")) {
                        val iconPath = it.substringAfter("icon=").trim().trim('"').replace('\\', '/')
                        packInfo.icon = path.resolve(iconPath) // Some packs use Windows separators in their INI files.
                    } else if (it.startsWith("readme=")) {
                        packInfo.readme = it.substringAfter("readme=").trim().trim('"')
                    }
                }
        }

        packInfo.lines = Files.list(path).use { files ->
            files
                .filter { Files.isRegularFile(it) && it.fileName.toString().matches(Regex("\\d+_.+\\.(ini|txt)")) }
                .map {readLineIni(it)}
                .toList()

        }

        return packInfo
    }

    fun readLineIni(path: Path): Placement =
        Placement(
            path.fileName.toString(),
            Files.readString(path)
                .split("\n")
                .first { it.isNotEmpty() && it.startsWith("dub_timestamps=") }
                .substringAfter("dub_timestamps=").trim().trim('[',']').toDouble()
        )
}