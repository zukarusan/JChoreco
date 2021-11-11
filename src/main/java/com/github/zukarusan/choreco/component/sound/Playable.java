package com.github.zukarusan.choreco.component.sound;

import com.github.zukarusan.choreco.component.exception.SoundException;

import java.io.File;

public interface Playable {
    int load(File file) throws SoundException;

    void play();
    void stop();
    void loop(int t);

    void waitUntilStop();
}
