package id.ac.president.choreco.component.sound;

import id.ac.president.choreco.component.exception.SoundException;

import javax.sound.sampled.AudioInputStream;
import java.io.File;

public interface Playable {
    int load(File file) throws SoundException;

    void play();
    void stop();
    void loop(int t);

    void waitUntilStop();
}
