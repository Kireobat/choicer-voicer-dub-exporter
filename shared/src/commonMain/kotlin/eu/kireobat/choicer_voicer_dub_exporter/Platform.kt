package eu.kireobat.choicer_voicer_dub_exporter

import androidx.compose.ui.graphics.ImageBitmap
import eu.kireobat.choicer_voicer_dub_exporter.interfaces.PackInfo
import eu.kireobat.choicer_voicer_dub_exporter.interfaces.Placement
import eu.kireobat.choicer_voicer_dub_exporter.interfaces.Recording
import java.nio.file.Path

expect suspend fun discoverRecordings(): List<String>

expect suspend fun getVoicePacks(): List<PackInfo>

expect suspend fun loadImageFromDisk(path: Path): ImageBitmap

expect suspend fun getRecordings(voicePackTitle: String): List<Recording>

expect suspend fun render(packInfo: PackInfo, recording: Recording): Path