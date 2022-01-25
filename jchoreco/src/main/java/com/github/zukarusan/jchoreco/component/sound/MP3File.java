package com.github.zukarusan.jchoreco.component.sound;

import com.github.zukarusan.jchoreco.component.exception.SoundException;
import javazoom.jl.decoder.*;
import javazoom.jl.player.advanced.AdvancedPlayer;
import javazoom.jl.player.advanced.PlaybackEvent;
import javazoom.jl.player.advanced.PlaybackListener;

import javax.sound.sampled.AudioFormat;
import java.io.*;
import java.util.concurrent.CountDownLatch;

public class MP3File extends SoundFile {
    public static AudioFormat.Encoding MP3 = new AudioFormat.Encoding("MP3");

    private AdvancedPlayer mp3Player;
    private CountDownLatch playLatch, loopLatch;
    private final PlaybackListener mp3Playback = new PlaybackListener() {
        @Override
        public void playbackFinished(PlaybackEvent evt) {
            super.playbackFinished(evt);
            playLatch.countDown();
        }
    };

    public MP3File(File file) {
        super(file.getName());
        try {
            loopLatch = new CountDownLatch(0);
            playLatch = new CountDownLatch(0);
            dataSize = load(file);
        } catch(SoundException e) {
            throw new IllegalArgumentException(e.getCode()+": File is not supported or failed loading", e);
        }
    }

    public AudioFormat decodeAll(InputStream is, OutputStream os) throws SoundException {
        Decoder mp3Decoder = new Decoder();
        SampleBuffer buffer;
        AudioFormat af;
        short[] bufferSamples;
        int sampleSize;

        Bitstream mp3Stream = new Bitstream(new BufferedInputStream(is));

        Header mp3Header;
        try {
            mp3Header = mp3Stream.readFrame();

            if (mp3Header != null) {
                buffer = (SampleBuffer) mp3Decoder.decodeFrame(mp3Header, mp3Stream);
                af = new AudioFormat(
                        MP3File.MP3,
                        buffer.getSampleFrequency(),
                        16, // Default PCM bit from javazoom.jl.decoder package
                        buffer.getChannelCount(),
                        mp3Header.framesize,
                        1f / (mp3Header.ms_per_frame() * 1000),
                        false
                );
            }
            else {
                throw new SoundException("UNABLE_DECODE", "No mp3 header is read");
            }
        } catch (BitstreamException | DecoderException e) {
            throw new SoundException("UNABLE_DECODE", "Mp3 failed to decode", e);
        }

        byte[] sample = new byte[2];
        try {
            while (mp3Header != null) {
                buffer = (SampleBuffer) mp3Decoder.decodeFrame(mp3Header, mp3Stream);
                bufferSamples = buffer.getBuffer();
                sampleSize = buffer.getBufferLength();

                synchronized (this) {
                    for (int i = 0; i < sampleSize; i++) {
                        sample[0] = (byte)(bufferSamples[i] & 0xff);
                        sample[1] = (byte)((bufferSamples[i] >> 8) & 0xff);
                        os.write(sample);
                    }
                }

                mp3Stream.closeFrame();
                mp3Header = mp3Stream.readFrame();
            }
            mp3Stream.close();
        } catch (BitstreamException | DecoderException | IOException e) {
            throw new SoundException("UNABLE_EXTRACT", "Mp3 failed to decode", e);
        }

        return af;
    }

    @Override
    public int load(File file) throws SoundException {
        FileInputStream decodingInputStream, playingInputStream;

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        try{
            decodingInputStream = new FileInputStream(file);
            playingInputStream = new FileInputStream(file);
        } catch (IOException e) {
            throw new SoundException("UNABLE_READ", "File may be corrupted or unsupported format", e);
        }
        initPlayer(new BufferedInputStream(playingInputStream));

        audioFormat = decodeAll(decodingInputStream, outputStream);
        byte[] data = outputStream.toByteArray();
        extractSamples(data, audioFormat);

        return data.length;
    }

    @Override
    void initPlayer(InputStream inputStream) throws SoundException {
        loopLatch = new CountDownLatch(0);
        playLatch = new CountDownLatch(0);
        try {
            mp3Player = new AdvancedPlayer(new BufferedInputStream(inputStream));
        } catch (JavaLayerException e) {
            throw new SoundException("UNABLE_INIT_PLAYER", "Mp3Player cannot read the stream, check if it is an mp3 file", e);
        }
        mp3Player.setPlayBackListener(mp3Playback);
    }

    @Override
    public void play() {
        if (playLatch.getCount() == 1)
            throw new IllegalCallerException("Player has started before");
        playLatch = new CountDownLatch(1);
        Thread playing = new Thread(() -> {
            try {
                mp3Player.play();
            } catch (JavaLayerException e) {
                e.printStackTrace();
            }
        });

        playing.start();
    }

    @Override
    public void stop() {
        if (playLatch.getCount() != 1)
            throw new IllegalCallerException("Player has not started");
        mp3Player.stop();
    }

    @Override
    public void loop(int t) { // still no good
        if (playLatch.getCount() == 1)
            throw new IllegalCallerException("Player has started before");
        loopLatch = new CountDownLatch(t);
        Thread looping = new Thread(() -> {
            while (loopLatch.getCount() > 0) {
                try {
                    play(); // check closing stream error
                    playLatch.await();
                    loopLatch.countDown();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        });

        looping.start();
    }

    @Override
    public void waitUntilStop() {
        if (playLatch.getCount() == 1) {
            try {
                if (loopLatch.getCount() > 0) {
                    loopLatch.await();
                }
                else
                    playLatch.await();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

}
