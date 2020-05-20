package ffmpeg_project;
import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.File;
import java.io.IOException;
import java.net.DatagramPacket;

import org.bytedeco.javacv.CanvasFrame;
import org.bytedeco.javacv.FFmpegFrameRecorder;
import org.bytedeco.javacv.Frame;
import org.bytedeco.javacv.OpenCVFrameGrabber;

import net.bramp.ffmpeg.FFmpeg;
import net.bramp.ffmpeg.FFmpegExecutor;
import net.bramp.ffmpeg.FFprobe;
import net.bramp.ffmpeg.builder.FFmpegBuilder;
public class RealTimeBroadcast {

	private static final File FILENAME = null;
	private static String AUDIO_MP3_CODEC = "libmp3lame";

	public static void main(String[] args) throws IOException {
		byte[] buffer = new byte[2048];
	    DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
		DataInputStream dis = new DataInputStream(new ByteArrayInputStream(packet.getData(), packet.getOffset(), packet.getLength()));

	}
	
	public static File convertWaveToMp3(final File wavFile, final String mp3Filename) throws IOException {
        // will read out path to executables from environment variables FFMPEG and FFPROBE
        // take care of those variables being set in your system
        final FFmpeg ffmpeg = new FFmpeg();
        final FFprobe ffprobe = new FFprobe();
        final FFmpegBuilder builder = new FFmpegBuilder()
                .setInput(wavFile.getAbsolutePath())
                .overrideOutputFiles(true)
                .addOutput(mp3Filename)
                .setAudioCodec(AUDIO_MP3_CODEC)
                .setAudioChannels(FFmpeg.AUDIO_MONO)
                .setAudioBitRate(FFmpeg.AUDIO_SAMPLE_48000)
                .setAudioSampleRate(FFmpeg.AUDIO_SAMPLE_16000)
                .done();
        final FFmpegExecutor executor = new FFmpegExecutor(ffmpeg, ffprobe);
        // Run a one-pass encode
        executor.createJob(builder).run();
        return new File(mp3Filename);
    }

}
