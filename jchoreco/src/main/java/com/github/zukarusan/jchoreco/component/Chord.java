package com.github.zukarusan.jchoreco.component;

public final class Chord {
    public static String[] Labels = {
            "A#maj", "A#min", "Amaj", "Amin", "Bmaj", "Bmin", "C#maj", "C#min",
            "Cmaj", "Cmin", "D#maj", "D#min", "Dmaj", "Dmin", "Emaj", "Emin",
            "F#maj", "F#min", "Fmaj", "Fmin", "G#maj", "G#min", "Gmaj", "Gmin"};
    public static final int Total = Labels.length;
    public static String get(int idxLabel) {
        return Labels[idxLabel];
    }
}
