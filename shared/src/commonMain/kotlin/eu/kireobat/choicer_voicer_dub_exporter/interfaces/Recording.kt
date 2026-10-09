package eu.kireobat.choicer_voicer_dub_exporter.interfaces

import java.nio.file.Path

data class Recording (
    val datetime: String,
    val paths: List<Path>
)