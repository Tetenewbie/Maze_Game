package game;

import javax.sound.sampled.*;
import java.net.URL; // IMPORTANT: URL is used instead of File for loading from JAR >>>>UPDATED

public class SoundManager {
    // Singleton instance of SoundManager
    private static SoundManager instance;

    // Clip for background music
    private Clip backgroundClip;

    // Control for changing volume
    private FloatControl volumeControl;

    // Tracks whether the sound is muted
    private boolean isMuted = false;

    // Stores the current (unmuted) volume level
    private float currentVolume = -10.0f;

    // Returns the global singleton instance of SoundManager
    public static SoundManager getInstance() {
        if (instance == null) {
            instance = new SoundManager();
        }
        return instance;
    }

    /**
     * Plays background music from a resource path inside the JAR or classpath.
     * Example path: "/resources/sound.wav"
     */
    public void playMusic(String resourcePath) {
        try {
            // Load audio file as a resource (works inside JAR too)
            URL soundURL = getClass().getResource(resourcePath);

            if (soundURL == null) {
                System.err.println("Error: Sound file not found inside JAR at: " + resourcePath);
                return;
            }

            // Create audio stream from the resource
            AudioInputStream audioStream = AudioSystem.getAudioInputStream(soundURL);

            // Prepare a Clip to play the audio
            backgroundClip = AudioSystem.getClip();
            backgroundClip.open(audioStream);

            // If volume control is supported, get it and apply current volume
            if (backgroundClip.isControlSupported(FloatControl.Type.MASTER_GAIN)) {
                volumeControl = (FloatControl) backgroundClip.getControl(FloatControl.Type.MASTER_GAIN);
                setVolume(currentVolume);
            }

            // Loop music forever
            backgroundClip.loop(Clip.LOOP_CONTINUOUSLY);

            // Start playback
            backgroundClip.start();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Mutes or unmutes the audio by setting the volume to minimum or restoring previous volume.
     */
    public void toggleMute() {
        if (volumeControl == null) return;

        // Flip state
        isMuted = !isMuted;

        if (isMuted) {
            // Set volume to minimum possible value (full mute)
            volumeControl.setValue(volumeControl.getMinimum());
        } else {
            // Restore previous volume setting
            volumeControl.setValue(currentVolume);
        }
    }

    /**
     * Sets the volume (in decibels).
     * Note: Allowed volume range varies depending on the audio system.
     */
    public void setVolume(float value) {
        if (volumeControl == null) return;

        currentVolume = value;

        if (!isMuted) {
            // Clamp volume within allowed range
            if (value > volumeControl.getMaximum()) value = volumeControl.getMaximum();
            if (value < volumeControl.getMinimum()) value = volumeControl.getMinimum();

            // Apply volume
            volumeControl.setValue(value);
        }
    }

    // Returns whether the audio is ccrrently muted
    public boolean isMuted() {
        return isMuted;
    }
}
