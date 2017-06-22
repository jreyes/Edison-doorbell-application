package org.vaporwarecorp.doorbell.process;

import com.zaxxer.nuprocess.NuAbstractProcessHandler;
import com.zaxxer.nuprocess.NuProcess;
import com.zaxxer.nuprocess.NuProcessBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.vaporwarecorp.doorbell.config.WebConfigurer;

import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.concurrent.TimeUnit;

public class YoutubeEncoderProcess extends NuAbstractProcessHandler {
// ------------------------------ FIELDS ------------------------------

    private static final String COMMAND = "/usr/bin/ffmpeg " +
            "-f v4l2 -s 640x360 -r 5 -input_format h264 -i /dev/video1 " +
            "-f s16le -ac 2 -ar 44100 -i /dev/zero " +
            "-c:v libx264 -crf 26 -preset ultrafast -b:v 500k -g 7 " +
            "-x264-params keyint=5 -bufsize 1000k " +
            "-c:a aac -b:a 128k " +
            "-f flv rtmp://x.rtmp.youtube.com/live2/";

    private final Logger log = LoggerFactory.getLogger(WebConfigurer.class);

    private NuProcess nuProcess;
    private NuProcessBuilder pb;

// ------------------------ INTERFACE METHODS ------------------------


// --------------------- Interface NuProcessHandler ---------------------

    @Override
    public void onStdout(ByteBuffer buffer, boolean closed) {
        if (!closed) {
            byte[] bytes = new byte[buffer.remaining()];
            buffer.get(bytes);
            log.info(new String(bytes));
        }
    }

    @Override
    public void onStderr(ByteBuffer buffer, boolean closed) {
        if (!closed) {
            byte[] bytes = new byte[buffer.remaining()];
            buffer.get(bytes);
            log.error(new String(bytes));
        }
    }

// -------------------------- OTHER METHODS --------------------------

    public String getName() {
        return "Youtube Encoder";
    }

    public void start(String streamUrl) throws InterruptedException {
        this.pb = new NuProcessBuilder(Arrays.asList(
                "/usr/bin/ffmpeg", "-f", "v4l2", "-s", "640x360", "-r", "5", "-input_format", "h264",
                "-i", "/dev/video1", "-f", "s16le", "-ac", "2", "-ar", "44100", "-i", "/dev/zero", "-c:v", "libx264",
                "-crf", "26", "-preset", "ultrafast", "-b:v", "500k", "-g", "7", "-x264-params", "keyint=5",
                "-bufsize", "1000k", "-c:a", "aac", "-b:a", "128k", "-f", "flv", streamUrl
        ));
        this.pb.setProcessListener(this);
        this.nuProcess = this.pb.start();
        try {
            this.nuProcess.waitFor(0, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            log.error(e.getMessage(), e);
        }
    }

    public void stop() {
        if (this.nuProcess != null) {
            this.nuProcess.destroy(true);
        }
        this.pb = null;
    }
}
