package eu.kireobat.choicer_voicer_dub_exporter.utils

import eu.kireobat.choicer_voicer_dub_exporter.interfaces.PackInfo
import eu.kireobat.choicer_voicer_dub_exporter.interfaces.Placement
import java.nio.file.Files
import java.nio.file.Path
import kotlin.collections.filter
import kotlin.io.path.Path
import kotlin.streams.toList

class IniUtil {
    fun readVoicePackIni(path: Path): PackInfo {

        val packInfo = PackInfo("",Path(""),emptyList(),"", emptyList())

        Files.readString(Path("$path\\_pack_info.ini")).split("\n").let { string ->
            string
                .filter { it.isNotEmpty() && it.contains("=")}
                .forEach {

                    if (it.startsWith("title=")) {
                        packInfo.title = it.substringAfter("title=").trim().trim('"')
                    } else if (it.startsWith("authors=")) {
                        packInfo.authors = it.substringAfter("authors=").trim().trim('"','[',']').split(",")
                    } else if (it.startsWith("icon=")) {
                        packInfo.icon = path.resolve(it.substringAfter("icon=").trim().trim('"')) // unable to find the image in some cases i.e. ini says Icon.png file says _icon.png
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