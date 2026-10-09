package eu.kireobat.choicer_voicer_dub_exporter

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.visible
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.min
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.Button
import kotlinx.coroutines.launch

import eu.kireobat.choicer_voicer_dub_exporter.interfaces.PackInfo
import eu.kireobat.choicer_voicer_dub_exporter.interfaces.Recording

@Composable
@Preview
fun App(version: String) {
    var voicePacks by remember { mutableStateOf<List<PackInfo>>(emptyList()) }
    var loading by remember { mutableStateOf(true) }
    var error by remember { mutableStateOf<String?>(null) }
    var recordings by remember { mutableStateOf<List<Recording>>(emptyList()) }
    var selectedVoicePack by remember {mutableStateOf<PackInfo?>(null)}

    LaunchedEffect(Unit) {
        try {
            voicePacks = getVoicePacks()
        } catch (e: Exception) {
            error = e.message ?: "Could not load voice packs"
        } finally {
            loading = false
        }
    }

    MaterialTheme {
        val coroutineScope = rememberCoroutineScope()
        val robotoFontFamily = rememberRobotoFontFamily()
        Column {
            Column {
                Text(modifier = Modifier.padding(4.dp),text = "Choicer Voicer Dub Exporter - v$version", fontFamily = robotoFontFamily, fontWeight = FontWeight.Light)

            }
            Column {

                if (loading) {
                    Text("Loading voice packs...")
                } else if (error != null) {
                    Text("Error: $error")
                } else if (voicePacks.isEmpty()) {
                    Text("No voice packs found")
                } else {
                    LazyVerticalGrid(
                        columns = GridCells.Adaptive(minSize = 200.dp)
                    )
                    {
                        items(voicePacks) { pack ->
                            Card(
                                modifier = Modifier.size(250.dp).padding(4.dp),
                                onClick = {
                                    coroutineScope.launch {
                                        recordings = getRecordings(pack.title)
                                        selectedVoicePack = pack
                                    }
                                }
                            ) {
                                val image = produceState<ImageBitmap?>(initialValue = null, key1 = pack.icon) {
                                    value = loadImageFromDisk(pack.icon)
                                }.value

                                image?.let {
                                    Image(
                                        bitmap = it,
                                        contentDescription = "${pack.title} icon",
                                        modifier = Modifier.aspectRatio(1.3f)
                                    )
                                }

                                Column(
                                    modifier = Modifier.padding(2.dp)
                                ) {
                                    Text(
                                        text = pack.title,
                                        fontSize = 18.sp,
                                        fontWeight = FontWeight.Normal,
                                        fontFamily = robotoFontFamily
                                    )
                                    Text(
                                        text = "By: ${pack.authors.joinToString().replace("\"", "")}",
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Light,
                                        fontStyle = FontStyle.Italic,
                                        fontFamily = robotoFontFamily
                                    )
                                }
                            }
                        }
                    }
                }

                Column(
                    modifier = Modifier.visible(selectedVoicePack != null)
                )
                {
                    for (recording in recordings) {
                        Text(text = "Recording ${recording.datetime}", fontSize = 14.sp)
                        Button( onClick = {
                            coroutineScope.launch {
                                selectedVoicePack?.let { pack ->
                                    println("Rendered WAV: ${render(pack, recording)}")
                                }
                            }
                        }
                        ) {
                            Text(text = "Export")
                        }

                    }
                }
            }
        }

    }
}