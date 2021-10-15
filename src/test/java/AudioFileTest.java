import id.ac.president.choreco.component.AudioFile;
import id.ac.president.choreco.component.exception.SoundException;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.net.URL;

public class AudioFileTest {
    @Test
    public void createAudioFile() throws SoundException, InterruptedException {
        URL url = getClass().getResource("layer_toggle.wav");

        assert url != null;
        File file = new File(url.getPath());
        AudioFile audioFile = new AudioFile(file);
        audioFile.play();

    }
}
