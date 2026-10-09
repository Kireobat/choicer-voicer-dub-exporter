package eu.kireobat.choicer_voicer_dub_exporter.utils

import eu.kireobat.choicer_voicer_dub_exporter.interfaces.Placement
import net.bramp.ffmpeg.FFmpegExecutor
import net.bramp.ffmpeg.builder.FFmpegBuilder
import java.nio.file.Path
import kotlin.math.roundToLong



class RenderUtil {

    fun renderVoiceBatch(
        executor: FFmpegExecutor,
        placements: List<Placement>,
        outputWav: Path,
    ) {
        require(placements.isNotEmpty())

        val builder = FFmpegBuilder().overrideOutputFiles(true)
        val filters = StringBuilder()
        val labels = mutableListOf<String>()

        placements.forEachIndexed { index, placement ->
            builder.addInput(placement.wav).done()

            val delayMs = (placement.timestampSeconds * 1_000.0).roundToLong()
            val label = "v$index"
            filters.append(
                "[$index:a]aresample=48000," +
                        "aformat=sample_fmts=fltp:channel_layouts=stereo," +
                        "adelay=$delayMs:all=1[$label];\n"
            )
            labels += "[$label]"
        }

        filters.append(
            labels.joinToString("") +
                    "amix=inputs=${placements.size}:duration=longest:" +
                    "dropout_transition=0:normalize=0," +
                    "alimiter=limit=0.95[batch]\n"
        )

        builder.addOutput(outputWav.toString())
            .setComplexFilter(filters.toString())
            .disableVideo()
            .disableSubtitle()
            .setFormat("wav")
            .setAudioCodec("pcm_s16le")
            .setAudioSampleRate(48_000)
            .addExtraArgs("-map", "[batch]")

        executor.createJob(builder).run()

        check(java.io.File(outputWav.toString()).isFile) {
            "FFmpeg completed but did not create the output WAV: $outputWav"
        }
    }

    fun renderFinalMp4(
        executor: FFmpegExecutor,
        videoPath: String,
        backingTrackPath: String?, // null if there is no backing track
        batchWavPaths: List<String>,
        outputMp4Path: String,
    ) {
        val builder = FFmpegBuilder()
            .overrideOutputFiles(true)

        // Input 0 must be the video, because the filter graph and map refer to 0:v:0.
        builder.addInput(videoPath).done()

        val filter = StringBuilder()
        val audioLabels = mutableListOf("[bg]")
        var nextInput = 1

        if (backingTrackPath != null) {
            builder.addInput(backingTrackPath).done()
            filter.append(
                "[$nextInput:a]aresample=48000," +
                        "aformat=sample_fmts=fltp:channel_layouts=stereo[bg];\n"
            )
            nextInput++
        } else {
            filter.append("anullsrc=r=48000:cl=stereo[bg];\n")
        }

        batchWavPaths.forEachIndexed { index, wavPath ->
            builder.addInput(wavPath).done()

            val label = "b$index"
            filter.append(
                "[$nextInput:a]aresample=48000," +
                        "aformat=sample_fmts=fltp:channel_layouts=stereo[$label];\n"
            )
            audioLabels += "[$label]"
            nextInput++
        }

        filter.append(
            audioLabels.joinToString("") +
                    "amix=inputs=${audioLabels.size}:duration=longest:" +
                    "dropout_transition=0:normalize=0," +
                    "alimiter=limit=0.95,apad[mixed]\n"
        )

        builder.addOutput(outputMp4Path)
            .setComplexFilter(filter.toString())
            .setVideoCodec("libx264")
            .setPreset("medium")
            .setConstantRateFactor(18.0)
            .setVideoPixelFormat("yuv420p")
            .setAudioCodec("aac")
            .setAudioBitRate(320_000)
            .setAudioSampleRate(48_000)
            .setVideoMovFlags("+faststart")
            .disableSubtitle()
            .addExtraArgs("-map", "0:v:0", "-map", "[mixed]", "-shortest")

        executor.createJob(builder).run()

        check(java.io.File(outputMp4Path).isFile) {
            "FFmpeg completed but did not create the output MP4: $outputMp4Path"
        }
    }
}