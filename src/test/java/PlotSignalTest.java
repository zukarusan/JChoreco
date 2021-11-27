import com.github.zukarusan.choreco.component.LogFrequencyVector;
import com.github.zukarusan.choreco.component.SignalFFT;
import com.github.zukarusan.choreco.component.chroma.CP;
import com.github.zukarusan.choreco.component.chroma.ChromaVector;
import com.github.zukarusan.choreco.component.spectrum.chroma.CLPSpectrum;
import com.github.zukarusan.choreco.component.spectrum.chroma.CPSpectrum;
import com.github.zukarusan.choreco.component.spectrum.chroma.ChromaSpectrum;
import com.github.zukarusan.choreco.component.spectrum.FrequencySpectrum;
import com.github.zukarusan.choreco.component.sound.MP3File;
import com.github.zukarusan.choreco.component.sound.SoundFile;
import com.github.zukarusan.choreco.component.sound.WAVFile;
import com.github.zukarusan.choreco.component.spectrum.LogFrequencySpectrum;
import com.github.zukarusan.choreco.system.STFT;
import com.github.zukarusan.choreco.component.Signal;
import com.github.zukarusan.choreco.system.exception.STFTException;
import com.github.zukarusan.choreco.system.SignalProcessor;
import com.github.zukarusan.choreco.util.PlotManager;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.net.URL;

public class PlotSignalTest {

    URL url1 = getClass().getResource("Voice_058.wav");
    URL url2 = getClass().getResource("major.mp3");
    SoundFile WAVFile;
    SoundFile MP3File;


    PlotSignalTest() {
        assert url1 != null;
        assert url2 != null;
        File file1 = new File(url1.getPath());
        File file2 = new File(url2.getPath());
        WAVFile = new WAVFile(file1);
        MP3File = new MP3File(file2);
    }
    @Test @Disabled
    public void testPlot() {
        Signal WAV_signal = WAVFile.getSamples(0);
        Signal MP3_signal = MP3File.getSamples(0);
        WAV_signal.setName("Test audio plot");
//        sound.plot(0, 0.0f * sound.getMax_Second(), 340, "Test1");
//        sound.plot(1, 0.2f * sound.getMax_Second(), 2000, "Test2");
//        sound.plotJavaFx(0, 0.0f * sound.getMax_Second(), 340);
//        sound.plotJavaFx(1, 0.2f * sound.getMax_Second(), 2000);
        WAV_signal.plot();
        MP3_signal.plot();
        PlotManager.getInstance().waitForClose();
    }

    @Test @Disabled
    public void testFFTPlot() throws STFTException {
        assert url1 != null;
        Signal signal = MP3File.getSamples(0);
        signal.setName("testFFTPlot");

        STFT stft = new STFT(1024, 512);

        Signal signalFFT = stft.fftPower(signal, signal.getSampleRate());
        int length = signal.getData().length;

//        SignalProcessor.powerToDb(signalFFT);
        SignalProcessor.normalizeZeroOne(signalFFT.getData());
        signalFFT = SignalProcessor.trimOfRange(signalFFT, 0, 5000);
        int[] peaks = SignalProcessor.peakDetection(signalFFT.getData(), 4.3f);
        float[] freq_peaks = SignalProcessor.idxToFreq(peaks, signalFFT.getFrequencyResolution());

        PlotManager plotManager= PlotManager.getInstance();
        plotManager.createPlot(
            "TestFFT", "Test", freq_peaks
        );
        plotManager.createPlot(
                "TestFFT", "Test signalFFT", signalFFT.getData()
        );
        plotManager.waitForClose();
    }

    @Test @Disabled
    public void testSpectrogram() throws STFTException {
        assert url1 != null;

        Signal signal = WAVFile.getSamples(0);
        signal.setName("Test Spectrogram");

        Signal mp3Signal = MP3File.getSamples(0);

        STFT stft = new STFT(1024, 512);
        PlotManager plotManager= PlotManager.getInstance();

        FrequencySpectrum spectrum = stft.process(mp3Signal, mp3Signal.getSampleRate());
        SignalProcessor.powerToDb(spectrum);
        SignalProcessor.normalizeZeroOne(spectrum);
//        spectrum = SignalProcessor.trimOfRange(spectrum, 100, 5000, sound.getSampleRate());

//        for (float[] d : spectrum) {
//            plotManager.createPlot(
//                    "TestFFT", "Test", d
//            );
//        }
        plotManager.createSpectrogram(signal.getName(), spectrum.getDataBuffer());
        plotManager.waitForClose();
    }

    @Test @Disabled
    public void testLogSpectrum() throws STFTException {
        Signal signal = MP3File.getSamples(0);
        STFT stft = new STFT(1024, 512);
        LogFrequencySpectrum logFSpectrum = new LogFrequencySpectrum(
                stft.process(signal, signal.getSampleRate())
        );
        logFSpectrum.plot();
        PlotManager.getInstance().waitForClose();
    }

    @Test @Disabled
    public void testLogVector() throws STFTException {
        Signal signal = MP3File.getSamples(0);
        STFT stft = new STFT(1024, 512);
        SignalFFT signalFFT = stft.fftPower(signal, signal.getSampleRate());
        LogFrequencyVector vector = new LogFrequencyVector(signalFFT);
        vector.plot();
        PlotManager.getInstance().waitForClose();
    }

    @Test @Disabled
    public void testSignalSpectrum() throws STFTException {
        Signal signal = MP3File.getSamples(0);
        STFT stft = new STFT(1024, 512);
        SignalFFT signalFFT = stft.process(signal, signal.getSampleRate()).getSignalAt(0.3f);
        signalFFT.plot();
        PlotManager.getInstance().waitForClose();
    }

    @Test
    public void testChroma() throws STFTException {
        Signal signal = MP3File.getSamples(0);
        STFT stft = new STFT(1024, 512);
        ChromaSpectrum chroma = new CLPSpectrum(
                new LogFrequencySpectrum(stft.process(signal, signal.getSampleRate())),
                50);
        chroma.plot();
        PlotManager.getInstance().waitForClose();
    }
}
