package eu.kireobat.choicer_voicer_dub_exporter.interfaces

import java.nio.file.Path

data class PackInfo(
    var title: String,
    var icon: Path,
    var authors: List<String>,
    var readme: String,
    var lines: List<Placement>
)
