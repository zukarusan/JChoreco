package id.ac.president.choreco.component.sound;

import id.ac.president.choreco.component.Signal;
import id.ac.president.choreco.component.exception.SoundException;

import javax.sound.sampled.AudioFormat;
import java.io.File;

public interface Sampleable {
    void extractSamples(byte[] rawSamples, AudioFormat format);
    Signal getSamples(int channel) throws SoundException;
    Signal getSamplesOfRange(int channel, float second, float lengthSecond) throws SoundException;

    void plotSamples();
}
