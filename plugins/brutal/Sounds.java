package brutal;

import javax.sound.sampled.*;
import java.io.File;
import java.io.IOException;

public class Sounds {

    static Sound start;
    static Sound kick;

    static {
        try {
            start = new Sound("dstelept.wav");
        } catch (IOException e) {
            e.printStackTrace();
        } catch (UnsupportedAudioFileException e) {
            e.printStackTrace();
        }
    }

    static class Sound {
        AudioInputStream stream;
        public Sound(String path) throws IOException, UnsupportedAudioFileException {
            stream = AudioSystem.getAudioInputStream(new File(path));
        }

        public void play() {
            try {
                Clip clip = AudioSystem.getClip();
                clip.open(stream);
                clip.start();
            } catch (LineUnavailableException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
