package com.github.zukarusan.jchoreco.component.sound;

import com.github.zukarusan.jchoreco.component.Signal;
import com.github.zukarusan.jchoreco.component.exception.SoundException;

import javax.sound.sampled.AudioFormat;

public interface Sampleable {
    void extractSamples(byte[] rawSamples, AudioFormat format);
    Signal getSamples(int channel) throws SoundException;
    Signal getSamplesOfRange(int channel, float second, float lengthSecond) throws SoundException;

    void plotSamples();
}
