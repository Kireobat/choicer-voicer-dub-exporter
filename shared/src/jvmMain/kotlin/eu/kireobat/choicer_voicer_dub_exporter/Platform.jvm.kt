package eu.kireobat.choicer_voicer_dub_exporter

import androidx.compose.ui.graphics.ImageBitmap
import eu.kireobat.choicer_voicer_dub_exporter.interfaces.PackInfo
import eu.kireobat.choicer_voicer_dub_exporter.interfaces.Placement
import eu.kireobat.choicer_voicer_dub_exporter.interfaces.Recording
import eu.kireobat.choicer_voicer_dub_exporter.utils.IniUtil
import eu.kireobat.choicer_voicer_dub_exporter.utils.RenderUtil
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import net.bramp.ffmpeg.FFmpegExecutor
import org.jetbrains.compose.resources.decodeToImageBitmap
import java.io.File
import java.io.InputStream
import java.nio.file.Files
import java.nio.file.Path
import kotlin.io.path.Path

actual suspend fun discoverRecordings(): List<String> =
    withContext(Dispatchers.IO) {
        val cvPaths = PathHandler().getChoicerVoicerPaths()
        Files.walk(cvPaths.basePath.resolve(cvPaths.recordings)).use { paths ->
            paths
                .filter { Files.isRegularFile(it)  || Files.isDirectory(it) }
                .map { it.toString() }
                .toList()

        }
    }
actual suspend fun getVoicePacks(): List<PackInfo> =

    withContext(Dispatchers.IO) {
        val cvPaths = PathHandler().getChoicerVoicerPaths()
        Files.list(cvPaths.basePath.resolve(cvPaths.packsVoice)).use { paths ->
            paths
                .filter { Files.isDirectory(it) && !it.toString().contains("The Choicer Voicer Tutorial Pack") }
                .map { IniUtil().readVoicePackIni(it) }
                .toList()
        }
    }

actual suspend fun getRecordings(voicePackTitle: String): List<Recording> =
    withContext(Dispatchers.IO) {
        val cvPaths = PathHandler().getChoicerVoicerPaths()
        val recordingsRoot = cvPaths.basePath.resolve(cvPaths.recordings)
        val normalizedTitle = voicePackTitle.filter(Char::isLetterOrDigit).lowercase()
        val matchingPackDirs = Files.list(recordingsRoot).use { dirs ->
            dirs
                .filter(Files::isDirectory)
                .filter {
                    it.fileName.toString().filter(Char::isLetterOrDigit).lowercase() == normalizedTitle
                }
                .toList()
        }
        val voicePackPath = when (matchingPackDirs.size) {
            1 -> matchingPackDirs.single()
            0 -> return@withContext emptyList()
            else -> throw IllegalArgumentException(
                "Multiple recording directories match voice pack '$voicePackTitle'"
            )
        }

        Files.list(voicePackPath).use { dirs ->
            dirs
                .filter { Files.isDirectory(it) }
                .map { directory ->
                    Recording(
                        directory.fileName.toString(),
                        Files.list(directory).use { files ->
                            files
                                .filter { Files.isRegularFile(it) }
                                .toList()
                        }
                    )
                }
                .toList()
        }
    }

actual suspend fun render(
    packInfo: PackInfo,
    recording: Recording
): Path {
    val recordingPathsByLine = recording.paths.groupBy { path ->
        path.fileName.toString()
            .substringBeforeLast('.', path.fileName.toString())
            .lowercase()
            .removePrefix("_dubrecord_")
    }
    val recordingPlacements = packInfo.lines.mapNotNull { placement ->
        val lineName = Path.of(placement.wav).fileName.toString()
            .substringBeforeLast('.', Path.of(placement.wav).fileName.toString())
            .lowercase()
        val matchingPaths = recordingPathsByLine[lineName].orEmpty()
        require(matchingPaths.size <= 1) {
            "Multiple recorded audio files match line '${placement.wav}'"
        }
        matchingPaths.singleOrNull()?.let { path ->
            Placement(path.toString(), placement.timestampSeconds)
        }
    }
    require(recordingPlacements.isNotEmpty()) {
        "No recording files match the selected voice pack lines"
    }
    val recordingDirectory = recording.paths.first().parent
        ?: throw IllegalArgumentException("Recording files must be inside a directory")
    val outputWav = recordingDirectory.resolve("rendered.wav")
    val outputVideo = recordingDirectory.resolve("rendered.mp4")

    val packDirectory = packInfo.icon.parent
    val dubVideo = findFileByBaseName(packDirectory, "dub_video")
    val backingTrack = findFileByBaseName(packDirectory, "_backing_track")

    return withContext(Dispatchers.IO) {
        val ffmpeg = FFmpegExecutor()
        RenderUtil().renderVoiceBatch(
            ffmpeg,
            recordingPlacements,
            outputWav)
        RenderUtil().renderFinalMp4(
            ffmpeg,
            dubVideo.toString(),
            backingTrack.toString(),
            listOf(outputWav.toString()),
            outputVideo.toString()
        )
        outputVideo
    }
}

private fun findFileByBaseName(directory: Path, baseName: String): Path {
    val matches = Files.list(directory).use { files ->
        files
            .filter(Files::isRegularFile)
            .filter {
                val fileName = it.fileName.toString()
                fileName.substringBeforeLast('.', fileName) == baseName
            }
            .toList()
    }
    require(matches.size == 1) {
        when (matches.size) {
            0 -> "No file with base name '$baseName' found in '$directory'"
            else -> "Multiple files with base name '$baseName' found in '$directory'"
        }
    }
    return matches.single()
}

actual suspend fun loadImageFromDisk(path: Path): ImageBitmap =
    withContext(Dispatchers.IO) {
        Files.readAllBytes(path).inputStream().use { input ->
            input.readAllBytes().decodeToImageBitmap()
        }
    }