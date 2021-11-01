import id.ac.president.choreco.component.AudioFile;
import id.ac.president.choreco.component.exception.SoundException;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.net.URL;

public class AudioFileTest {
    @Test @Disabled
    public void createAudioFile() throws SoundException, InterruptedException {
        URL url = getClass().getResource("Voice_058.wav");

        assert url != null;
        File file = new File(url.getPath());
        AudioFile audioFile = new AudioFile(file);
        audioFile.play();
    }
}
