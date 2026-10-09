package eu.kireobat.choicer_voicer_dub_exporter

import eu.kireobat.choicer_voicer_dub_exporter.utils.OS
import eu.kireobat.choicer_voicer_dub_exporter.utils.OsUtil
import java.nio.file.Files
import java.nio.file.Path

data class ChoicerVoicerDataPaths(
    val basePath: Path,
    val temp: Path = Path.of(".temp"),
    val packsChatter: Path = Path.of("packs_chatter"),
    val packsHost: Path = Path.of("packs_host"),
    val packsJudges: Path = Path.of("packs_judges"),
    val packsMenu: Path = Path.of("packs_menu"),
    val packsPlayer: Path = Path.of("packs_player"),
    val packsStudio: Path = Path.of("packs_studio"),
    val packsVoice: Path = Path.of("packs_voice"),
    val recordings: Path = Path.of("recordings", "dub_recordings"),
    val saves: Path = Path.of("saves")
)

class PathHandler {

    fun getChoicerVoicerPaths(): ChoicerVoicerDataPaths {
        val basePath = when (OsUtil().detectOperatingSystem()) {
            OS.WINDOWS -> Path.of(
                System.getenv("APPDATA")
                    ?: throw IllegalStateException("APPDATA environment variable is not set")
            ).resolve("YeahMaybe").resolve("ChoicerVoicer").resolve("game")
            OS.LINUX -> {
                val dataHome = System.getenv("XDG_DATA_HOME")
                    ?.takeIf(String::isNotBlank)
                    ?.let(Path::of)
                    ?.takeIf(Path::isAbsolute)
                    ?: Path.of(System.getProperty("user.home")).resolve(".local").resolve("share")
                val gameDataRoot = dataHome.resolve("YeahMaybe").resolve("ChoicerVoicer")
                listOf(gameDataRoot, gameDataRoot.resolve("game"))
                    .maxByOrNull(::existingDataDirectoryCount)
                    ?: gameDataRoot
            }
            OS.MACOS -> Path.of(System.getProperty("user.home"))
                .resolve("Library")
                .resolve("Application Support")
                .resolve("YeahMaybe")
                .resolve("ChoicerVoicer")
                .resolve("game")
        }
        return ChoicerVoicerDataPaths(basePath)
    }

    private fun existingDataDirectoryCount(basePath: Path): Int =
        listOf(Path.of("packs_voice"), Path.of("recordings", "dub_recordings"))
            .count { Files.isDirectory(basePath.resolve(it)) }
}