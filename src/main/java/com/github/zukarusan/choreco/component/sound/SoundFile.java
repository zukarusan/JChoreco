package com.github.zukarusan.choreco.component.sound;

import com.github.zukarusan.choreco.component.Signal;
import com.github.zukarusan.choreco.component.exception.SoundException;
import com.github.zukarusan.choreco.util.PlotManager;
import lombok.Getter;

import javax.sound.sampled.*;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Getter
public abstract class SoundFile implements Sampleable, Playable {

    protected AudioFormat audioFormat;

    protected final String name;
    protected List<Signal> samples;

    protected long dataSize;
    protected int totalSamples; // including all channel
    protected float totalSecond;

    public SoundFile(String name) {
        this.name = name;
    }

    @Override
    public void extractSamples(byte[] rawSamples, AudioFormat format)  {
        int sampleSizeInBits = format.getSampleSizeInBits();
        int channelNums = format.getChannels();
        boolean isBigEndian = format.isBigEndian();

        if (rawSamples == null) {
            throw new IllegalArgumentException("Sound does not have data");
        }
        ByteOrder byteOrder = isBigEndian ? ByteOrder.BIG_ENDIAN : ByteOrder.LITTLE_ENDIAN;

        int dataSize = rawSamples.length;
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
                        sampleByte[3] = rawSamples[kIndex + jIndex];
                        break;
                    case 2:
                        sampleByte[2] = rawSamples[kIndex + jIndex];
                        sampleByte[3] = rawSamples[kIndex + jIndex + 1];
                        break;
                    default:
                        throw new IllegalStateException("Sound only support 8-bit or 16-bit depth");
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

    @Override
    public Signal getSamples(int channel) {
        if (channel >= this.samples.size())
            throw new IllegalArgumentException("Channel does not exist, channel size: "+samples.size());
        return this.samples.get(channel);
    }

    @Override
    public Signal getSamplesOfRange(int channel, float second, float lengthSecond) throws SoundException {
        try {
            float fs = this.audioFormat.getSampleRate();
            int begin = (int) (fs * second);
            int length = (int) (fs * lengthSecond);
            int channelSamples = this.totalSamples / 2;
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

    abstract void initPlayer(InputStream inputStream) throws SoundException;

    @Override
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
