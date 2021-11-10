package id.ac.president.choreco.component.sound;

import id.ac.president.choreco.component.exception.SoundException;
import lombok.Getter;

import javax.sound.sampled.*;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.concurrent.CountDownLatch;

@Getter
public class WAVFile extends SoundFile {
    public final static int NOT_SPECIFIED = AudioSystem.NOT_SPECIFIED;
    protected Clip clip;
    protected boolean canPlay;
    protected CountDownLatch syncLatch;

    public WAVFile(File file) throws IllegalArgumentException {
        super(file.getName());
        try {
            syncLatch = new CountDownLatch(0);
            dataSize = this.load(file);
        } catch (SoundException e) {
            throw new IllegalArgumentException(e.getCode()+": File argument mismatches the format", e);
        }
    }

    @Override
    public int load(File file) throws SoundException {
        AudioInputStream audioInputStream;
        try{
            audioInputStream = AudioSystem.getAudioInputStream(file);
        } catch (UnsupportedAudioFileException | IOException e) {
            throw new SoundException("UNABLE_LOAD", "File may be corrupted or unsupported format", e);
        }
        BufferedInputStream bufferedInputStream = new BufferedInputStream(audioInputStream);
        audioFormat = audioInputStream.getFormat();

        long frameTotal = audioInputStream.getFrameLength();
        long frameSize = audioFormat.getFrameSize();
        long dataSize = frameSize * frameTotal;
        
        byte[] data = new byte[(int) dataSize];
        int totalRead;
        try {
            totalRead = bufferedInputStream.read(data);
            extractSamples(data, audioFormat);
            audioInputStream.close();
        } catch (IllegalStateException | IOException e) {
            throw new SoundException("UNABLE_READ", "Cannot read audio stream data", e);
        }
        AudioInputStream aisForPlay;
        try {
            aisForPlay = AudioSystem.getAudioInputStream(file);
        } catch (UnsupportedAudioFileException | IOException e) {
            throw new SoundException("STREAM_FAIL", "Stream audio load failed", e);
        }
        initPlayer(aisForPlay);

        return totalRead;
    }

    protected void initPlayer(InputStream inputStream) throws SoundException {
        if (!(inputStream instanceof AudioInputStream)) {
            throw new IllegalArgumentException("Must be an AudioInputStream for the inputStream");
        }
        AudioInputStream aisForPlay = (AudioInputStream) inputStream;
        try {
            DataLine.Info info = new DataLine.Info(Clip.class, audioFormat);
            clip = (Clip) AudioSystem.getLine(info);
            clip.open(aisForPlay);

            syncLatch = new CountDownLatch(0);
            clip.addLineListener(e -> {
                if (e.getType() == LineEvent.Type.STOP) {
                    syncLatch.countDown();
                }
            });

            clip.setFramePosition(0);
            canPlay = true;
        } catch (LineUnavailableException | IOException e) {
            canPlay = false;
            throw new SoundException("UNABLE_PLAY", "Error creating sound player", e);
        }
    }


    @Override
    public void play() {
        if (canPlay && syncLatch.getCount() != 1) {
            syncLatch = new CountDownLatch(1);
            clip.start();
        }
        else throw new IllegalCallerException("Clip not initialized or has been played");
    }

    @Override
    public void stop() {
        if (canPlay && syncLatch.getCount() == 1) {
            clip.stop();
        } else
            throw new IllegalCallerException("Clip not initialized or not played");
    }

    @Override
    public void loop(int t) {
        if (canPlay && syncLatch.getCount() == 1) {
            clip.loop(t);
        } else
            throw new IllegalCallerException("Clip not initialized or not played");
    }

    @Override
    public void waitUntilStop() {
        if (syncLatch.getCount() != 1 && !canPlay)
            throw new IllegalCallerException("Clip not initialized or not played");
        try {
            syncLatch.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }


}
