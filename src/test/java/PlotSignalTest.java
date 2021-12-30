import com.github.zukarusan.choreco.component.LogFrequencyVector;
import com.github.zukarusan.choreco.component.SignalFFT;
import com.github.zukarusan.choreco.component.spectrum.chroma.CRPSpectrum;
import com.github.zukarusan.choreco.component.spectrum.chroma.ChromaSpectrum;
import com.github.zukarusan.choreco.component.spectrum.FrequencySpectrum;
import com.github.zukarusan.choreco.component.sound.MP3File;
import com.github.zukarusan.choreco.component.sound.SoundFile;
import com.github.zukarusan.choreco.component.sound.WAVFile;
import com.github.zukarusan.choreco.component.spectrum.LogFrequencySpectrum;
import com.github.zukarusan.choreco.system.STFT;
import com.github.zukarusan.choreco.component.Signal;
import com.github.zukarusan.choreco.system.exception.STFTException;
import com.github.zukarusan.choreco.system.CommonProcessor;
import com.github.zukarusan.choreco.util.PlotManager;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.net.URL;

public class PlotSignalTest {

    URL url1 = getClass().getResource("Voice_058.wav");
    URL url2 = getClass().getResource("major.mp3");
    URL url3 = getClass().getResource("layer_toggle.wav");
    SoundFile guitar_c;
    SoundFile piano_major;
    SoundFile effect_toggle;


    PlotSignalTest() {
        assert url1 != null;
        assert url2 != null;
        File file1 = new File(url1.getPath());
        File file2 = new File(url2.getPath());
        File file3 = new File(url3.getPath());
        guitar_c = new WAVFile(file1);
        piano_major = new MP3File(file2);
        effect_toggle = new WAVFile(file3);
    }
    @Test @Disabled
    public void testPlot() {
        Signal WAV_signal = guitar_c.getSamples(0);
        Signal MP3_signal = piano_major.getSamples(0);
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
        Signal signal = piano_major.getSamples(0);
        signal.setName("testFFTPlot");

//        STFT stft = new STFT(1024, 512);

        Signal signalFFT = STFT.fftPower(signal, signal.getSampleRate());
        int length = signal.getData().length;

//        SignalProcessor.powerToDb(signalFFT);
        CommonProcessor.normalizeZeroOne(signalFFT.getData());
        signalFFT = CommonProcessor.trimOfRange(signalFFT, 0, 5000);
        int[] peaks = CommonProcessor.findPeaksByAverage(signalFFT.getData(), 4.3f);
        float[] freq_peaks = CommonProcessor.idxToFreq(peaks, signalFFT.getFrequencyResolution());

        PlotManager plotManager= PlotManager.getInstance();
        plotManager.createPlot(
            "TestFFT", "Test", freq_peaks
        );
        plotManager.createPlot(
                "TestFFT", "Test signalFFT", signalFFT.getData()
        );
        plotManager.waitForClose();
    }

    @Test //@Disabled
    public void testSpectrogram() throws STFTException {
        assert url1 != null;

        Signal signal = guitar_c.getSamples(0);
        signal.setName("Test Spectrogram");

        Signal mp3Signal = piano_major.getSamples(0);

        STFT stft = new STFT(4096, 2048);
        PlotManager plotManager= PlotManager.getInstance();

        FrequencySpectrum spectrum = stft.process(mp3Signal, mp3Signal.getSampleRate());
        CommonProcessor.harmonicPeakSubtract(spectrum.getDataBuffer(), spectrum.getFrequencyResolution(), 6);
        CommonProcessor.logCompress(spectrum, 10000);
        CommonProcessor.powerToDb(spectrum);
        CommonProcessor.normalizeZeroOne(spectrum);
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
        Signal signal = guitar_c.getSamples(0);
        STFT stft = new STFT(4096, 2048);
        LogFrequencySpectrum logFSpectrum = new LogFrequencySpectrum(
                stft.process(signal, signal.getSampleRate())
        );
        logFSpectrum.plot();
        PlotManager.getInstance().waitForClose();
    }

    @Test @Disabled
    public void testLogVector() throws STFTException {
        Signal signal = piano_major.getSamples(0);
        SignalFFT signalFFT = STFT.fftPower(signal, signal.getSampleRate());
        LogFrequencyVector vector = new LogFrequencyVector(signalFFT);
        vector.plot();
        PlotManager.getInstance().waitForClose();
    }

    @Test @Disabled
    public void testSignalSpectrum() throws STFTException {
        Signal signal = piano_major.getSamples(0);
        STFT stft = new STFT(1024, 512);
        SignalFFT signalFFT = stft.process(signal, signal.getSampleRate()).getSignalAt(0.3f);
        signalFFT.plot();
        PlotManager.getInstance().waitForClose();
    }

    @Test //@Disabled
    public void testChroma() throws STFTException {
        Signal signal = guitar_c.getSamples(0);
        STFT stft = new STFT(16384, 8192);
        FrequencySpectrum spectrum = stft.process(signal, signal.getSampleRate());
        CommonProcessor.logCompress(spectrum, 1000);
//        CommonProcessor.powerToDb(spectrum);
//        CommonProcessor.harmonicPeakSubtract(spectrum.getDataBuffer(), spectrum.getFrequencyResolution(), 10);
        ChromaSpectrum chroma = new CRPSpectrum(
                new LogFrequencySpectrum(spectrum),
                100);
        chroma.plot();
        PlotManager.getInstance().waitForClose();
    }
}
