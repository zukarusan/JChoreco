package id.ac.president.choreco.component;

import id.ac.president.choreco.component.exception.SoundException;
import id.ac.president.choreco.util.PlotManager;
import lombok.Getter;
import java.io.FilterInputStream;
import javax.sound.sampled.*;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CountDownLatch;

@Getter
public class WAVFile {
    public final static int NOT_SPECIFIED = AudioSystem.NOT_SPECIFIED;

    private final String name;
    private List<Signal> samples;
    private int totalSamples; // including all channel

    private final int dataSize;
    private float totalSecond;

    private CountDownLatch syncLatch;
    private AudioInputStream audioInputStream;
    private AudioFormat audioFormat;

    Clip clip;
    private boolean canPlay;

    public WAVFile(File file) throws SoundException {
        try {
            dataSize = this.load(file);
            name = file.getName();
        } catch (UnsupportedAudioFileException | IOException e) {
            throw new SoundException("UNABLE_OPEN", "Data may be corrupted or unsupported format", e);
        }
    }


    public int load(File file) throws UnsupportedAudioFileException, IOException, SoundException {

        audioInputStream = AudioSystem.getAudioInputStream(file);
        audioFormat = audioInputStream.getFormat();

        long frameTotal = audioInputStream.getFrameLength();
        long frameSize = audioFormat.getFrameSize();
        long dataSize = frameSize * frameTotal;
        
        byte[] data = new byte[(int) dataSize];
        int totalRead = audioInputStream.read(data);

        extractSample(
                data,
                audioFormat.getSampleSizeInBits(),
                audioFormat.getChannels(),
                audioFormat.isBigEndian());

        audioInputStream.close();
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

    public void extractSample(
            byte[] rawData,
            int sampleSizeInBits,
            int channelNums,
            boolean isBigEndian
    ) throws SoundException {
        if (rawData == null) {
            throw new SoundException("SOUND_NO_DATA", "Sound does not have data");
        }
        ByteOrder byteOrder = isBigEndian ? ByteOrder.BIG_ENDIAN : ByteOrder.LITTLE_ENDIAN;

        int dataSize = rawData.length;
        if (dataSize % channelNums != 0) {
            throw new IllegalArgumentException("Number of channels does not match or data may be corrupted");
        }
        if (sampleSizeInBits % 8 != 0) {
            throw new IllegalArgumentException("Does not support non-multiples of 8-bit sample");
        }

        int channelSize = sampleSizeInBits >> 3; // * 8
        int chunkSize = channelSize * channelNums;
        int totalChunks = dataSize / chunkSize;

        this.totalSamples = totalChunks * channelNums;
        this.totalSecond =  (float)(totalSamples) / audioFormat.getSampleRate() / channelNums;

        float[] channelsOfSamples = new float[totalSamples];

        for (int k = 0; k < totalChunks; k++) {
            int kIndex = k * chunkSize;

            for (int j = 0, indexOut = k; j < channelNums; j++, indexOut += totalChunks) {
                int jIndex = j * channelSize;
                byte[] sampleByte = new byte[4]; // float bytes

                switch (channelSize) {
                    case 1:
                        sampleByte[3] = rawData[kIndex + jIndex];
                        break;
                    case 2:
                        sampleByte[2] = rawData[kIndex + jIndex];
                        sampleByte[3] = rawData[kIndex + jIndex + 1];
                        break;
                    default:
                        throw new SoundException("SOUND_BIT_DEPTH", "Sound only support 8-bit or 16-bit depth");
                }
                channelsOfSamples[indexOut] = (float) ByteBuffer
                        .wrap(sampleByte)
                        .order(byteOrder)
                        .getInt();
            }
        }

        samples = new ArrayList<>();
        for(int i = 0; i < channelNums; ++i) {
            float[] sampleChannel = new float[totalChunks];
            System.arraycopy(
                    channelsOfSamples,i * totalChunks,
                    sampleChannel, 0,
                    totalChunks);
            samples.add(new Signal(
                    ""+i,
                    sampleChannel,
                    audioFormat.getSampleRate(),
                    Signal.Domain.TIME_DOMAIN));
        }

    }

    public Signal getSamples(int channel) {
        return samples.get(channel);
    }

    public Signal getSamplesOfRange(int channel, float second, int length) throws SoundException {
        try {
            int begin = (int) (audioFormat.getSampleRate() * second);
            int channelSamples = totalSamples / 2;
            int trail = (begin + length) % channelSamples;
            int end = (Math.floorDiv((begin + length) , channelSamples) != 1)
                    ? (begin+length) : (begin+length)%trail;
            return new Signal(
                    ""+channel,
                    Arrays.copyOfRange(samples.get(channel).getData(), begin, end),
                    audioFormat.getSampleRate(),
                    Signal.Domain.TIME_DOMAIN
                    );
        } catch (IllegalArgumentException | ArrayIndexOutOfBoundsException e) {
            throw new SoundException("SOUND_SAMPLES_GET", "Second or length is out of samples");
        }
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

    public void plotSamples() {
        PlotManager plotManager = PlotManager.getInstance();
        int i = 0;
        for (Signal samp: samples) {
            plotManager.createPlot(
                    "\""+name+"\" - Channel: "+ (1 + i++),
                    "Frequencies",
                    samp.getData());
        }
    }
}
