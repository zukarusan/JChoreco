import com.github.zukarusan.choreco.component.sound.MP3File;
import com.github.zukarusan.choreco.component.sound.WAVFile;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.net.URL;

public class SoundFIleTest {
    @Test @Disabled
    public void createWavFile()  {
        URL url = getClass().getResource("ui_layer.wav");

        assert url != null;
        File file = new File(url.getPath());
        WAVFile WAVFile = new WAVFile(file);
        WAVFile.play();
    }

    @Test @Disabled
    public void createMp3File() {
        URL url = getClass().getResource("major.mp3");

        assert url != null;
        File file = new File(url.getPath());
        MP3File mp3File = new MP3File(file);
        mp3File.loop(2);
        mp3File.waitUntilStop();
    }
}
