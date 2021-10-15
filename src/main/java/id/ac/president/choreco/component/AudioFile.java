package id.ac.president.choreco.component;

import id.ac.president.choreco.component.exception.SoundException;
import lombok.Getter;

import javax.sound.sampled.*;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.concurrent.CountDownLatch;

@Getter
public class AudioFile {
    public final static int NOT_SPECIFIED = AudioSystem.NOT_SPECIFIED;

    private final int dataSize;

    private int sampleRate = NOT_SPECIFIED;
    private int sampleSizeInBits = NOT_SPECIFIED; // in bits
    private long frameTotal = NOT_SPECIFIED;
    private int frameSize = NOT_SPECIFIED;
    private float frameRate = NOT_SPECIFIED;

    private CountDownLatch syncLatch;
    private AudioInputStream audioInputStream;
    private AudioFormat audioFormat;
    private byte[] data;

    Clip clip;
    private boolean canPlay;

    public AudioFile(File file) throws SoundException {
        try {
            dataSize = this.load(file);
        } catch (UnsupportedAudioFileException | IOException e) {
            throw new SoundException("UNABLE_OPEN", "Data may be corrupted or unsupported format", e);
        }
    }


    public int load(File file) throws UnsupportedAudioFileException, IOException {
        audioInputStream = AudioSystem.getAudioInputStream(file);
        audioFormat = audioInputStream.getFormat();
        sampleRate = (int) audioFormat.getSampleRate();
        sampleSizeInBits = audioFormat.getSampleSizeInBits();
        frameTotal = audioInputStream.getFrameLength();
        frameSize = audioFormat.getFrameSize();
        frameRate = audioFormat.getFrameRate();

        long dataSize = frameSize * frameTotal;
        
        data = new byte[(int) dataSize];
        int totalRead = audioInputStream.read(data);

        AudioInputStream aisForPlay = AudioSystem.getAudioInputStream(file);
        try {
            DataLine.Info info = new DataLine.Info(Clip.class, audioFormat);
            clip = (Clip) AudioSystem.getLine(info);
            clip.open(aisForPlay);

            clip.addLineListener(e -> {
                if (e.getType() == LineEvent.Type.STOP) {
                    syncLatch.countDown();
                }
            });

            clip.setFramePosition(0);
            canPlay = true;
        } catch (LineUnavailableException e) {
            canPlay = false;
            e.printStackTrace();
        }
        return totalRead;
    }

    public void play() throws InterruptedException {
        syncLatch = new CountDownLatch(1);
        clip.start();
        syncLatch.await();
    }

    public void loop() {
        clip.loop(Clip.LOOP_CONTINUOUSLY);
    }

    public void stop() {
        clip.stop();
    }
}
