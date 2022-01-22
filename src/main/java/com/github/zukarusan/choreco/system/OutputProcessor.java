package com.github.zukarusan.choreco.system;

import be.tarsos.dsp.AudioEvent;
import be.tarsos.dsp.AudioProcessor;
import lombok.Getter;

public class OutputProcessor implements AudioProcessor {
    @Getter private boolean headerSet;
    @Override
    public boolean process(AudioEvent audioEvent) {
        return false;
    }

    @Override
    public void processingFinished() {

    }
}
