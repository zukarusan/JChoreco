package com.github.zukarusan.choreco.component.sound;

import com.github.zukarusan.choreco.component.Signal;
import com.github.zukarusan.choreco.component.exception.SoundException;

import javax.sound.sampled.AudioFormat;

public interface Sampleable {
    void extractSamples(byte[] rawSamples, AudioFormat format);
    Signal getSamples(int channel) throws SoundException;
    Signal getSamplesOfRange(int channel, float second, float lengthSecond) throws SoundException;

    void plotSamples();
}
