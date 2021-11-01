package id.ac.president.choreco.component;

import id.ac.president.choreco.component.exception.SoundException;
import id.ac.president.choreco.util.PlotManager;
import lombok.Getter;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


@Getter
public class Sound {
    private final String name;
    private List<float[]> samples;
    private final int sampleRate;
    private int totalSamples; // including all channel

    private int channelNums;
    private int sampleSize; // size in Byte unit
    private final float Max_Second;

    public Sound(String labelName, AudioFile audioFile) throws SoundException {
        try {
            extractFrom(
                    audioFile.getData(),
                    audioFile.getSampleSizeInBits(),
                    audioFile.getAudioFormat().getChannels(),
                    audioFile.getAudioFormat().isBigEndian());
            this.sampleRate = audioFile.getSampleRate();
            this.Max_Second =  (float)(totalSamples) / sampleRate / channelNums;
            this.name = labelName;
        } catch (Exception e) {
            throw new SoundException("SAMPLE_ERROR_CONSTRUCT", "Error constructing", e);
        }
    }

    public void extractFrom(
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
        final float divFloat = 1.0f/32768.0f;

        this.sampleSize = channelSize;
        this.channelNums = channelNums;
        this.totalSamples = totalChunks * channelNums;

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
            samples.add(sampleChannel);
        }

    }

    public float[] getSamples(int channel) {
        return samples.get(channel);
    }

    public float[] getSamplesOfRange(int channel, float second, int length) throws SoundException {
        try {
            int begin = (int) (sampleRate * second);
            int channelSamples = totalSamples / 2;
            int trail = (begin + length) % channelSamples;
            int end = (Math.floorDiv((begin + length) , channelSamples) != 1)
                    ? (begin+length) : (begin+length)%trail;
            return Arrays.copyOfRange(samples.get(channel), begin, end);
        } catch (IllegalArgumentException | ArrayIndexOutOfBoundsException e) {
            throw new SoundException("SOUND_SAMPLES_GET", "Second or length is out of samples");
        }
    }

    public void plot(int channel, float second, int length, String name) throws SoundException {
        PlotManager plotManager = PlotManager.getInstance();

        plotManager.createPlot(
                name,
                "Frequencies",
                getSamplesOfRange(
                        channel, second, length
                ));
    }

    public void plot() {
        PlotManager plotManager = PlotManager.getInstance();
        int i = 0;
        for (float[] samp: samples) {
            plotManager.createPlot(
                    "\""+name +"\" - Channel: "+ (1 + i++),
                    "Frequencies",
                    samp);
        }
    }

}
