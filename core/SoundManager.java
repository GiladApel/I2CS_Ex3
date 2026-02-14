package assignments.Ex3.core;

import javax.sound.sampled.*;
import java.io.File;

/**
 * SoundManager - Simple audio handler.
 * Plays background music (looping) and sound effects (one-time).
 * Uses a separate thread to keep the game running smoothly.
 */
public class SoundManager {

    private Clip backgroundClip;

    public SoundManager() {}

    /**
     * Plays a sound file.
     * @param fileName - The name of the sound file (e.g., "intro.wav").
     * @param loop - true for background music, false for sound effects.
     */
    public void playSound(String fileName, boolean loop) {
        // Run in a separate thread to prevent game freezing
        new Thread(() -> {
            try {
                // 1. Look for the file in the "data" folder
                File soundFile = new File("data/" + fileName);

                // 2. If not found, look in the root folder
                if (!soundFile.exists()) {
                    soundFile = new File(fileName);
                }

                // 3. Check if file exists before playing
                if (soundFile.exists()) {
                    AudioInputStream audioIn = AudioSystem.getAudioInputStream(soundFile);
                    Clip clip = AudioSystem.getClip();
                    clip.open(audioIn);

                    if (loop) {
                        // --- Logic for Background Music ---
                        // Stop previous music if playing
                        if (backgroundClip != null && backgroundClip.isRunning()) {
                            backgroundClip.stop();
                        }
                        // Save reference and loop continuously
                        backgroundClip = clip;
                        clip.loop(Clip.LOOP_CONTINUOUSLY);
                    } else {
                        // --- Logic for Sound Effects ---
                        // Play once
                        clip.start();
                    }
                }
            } catch (Exception e) {
                // Catch errors safely without crashing the game
                System.out.println("Error playing sound: " + fileName);
            }
        }).start();
    }

    /**
     * Stops the background music if it is running.
     */
    public void stopBackground() {
        if (backgroundClip != null && backgroundClip.isRunning()) {
            backgroundClip.stop();
        }
    }
}