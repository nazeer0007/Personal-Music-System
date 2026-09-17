import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.util.Duration;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

/**
 * Handles audio playback state, queue, and simulation.
 * Uses JavaFX MediaPlayer for real audio files (MP3/WAV) when available,
 * and a standard background timer for simulated audio playback.
 * Demonstrates ENCAPSULATION and STATE MANAGEMENT.
 */
public class MusicPlayer {
    public enum PlayerState {
        STOPPED,
        PLAYING,
        PAUSED
    }

    public interface PlaybackListener {
        void onSongChanged(Song song);
        void onStateChanged(PlayerState state);
        void onProgressUpdate(int currentSeconds, int totalSeconds);
    }

    private Song currentSong;
    private PlayerState state = PlayerState.STOPPED;
    private double volume = 0.8; // 0.0 to 1.0
    private int currentPositionSeconds = 0;

    private List<Song> queue = new ArrayList<>();
    private int queueIndex = -1;

    // Real JavaFX MediaPlayer
    private MediaPlayer fxPlayer;

    // Standard Java executor for simulation (safe in console and GUI)
    private final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor(r -> {
        Thread t = new Thread(r, "TuneFlow-Playback-Timer");
        t.setDaemon(true);
        return t;
    });
    private ScheduledFuture<?> simulationTask;

    private final List<PlaybackListener> listeners = new ArrayList<>();

    public MusicPlayer() {
    }

    public void addListener(PlaybackListener listener) {
        if (listener != null && !listeners.contains(listener)) {
            listeners.add(listener);
        }
    }

    public void removeListener(PlaybackListener listener) {
        listeners.remove(listener);
    }

    public void setQueue(List<Song> songs, int startIndex) {
        this.queue = new ArrayList<>(songs);
        this.queueIndex = (startIndex >= 0 && startIndex < songs.size()) ? startIndex : 0;
        if (!queue.isEmpty()) {
            play(queue.get(queueIndex));
        }
    }

    public void play(Song song) {
        if (song == null) return;

        stop();

        this.currentSong = song;
        this.currentPositionSeconds = 0;
        this.state = PlayerState.PLAYING;
        song.play();

        boolean hasLocalFile = false;
        if (song.getFilePath() != null && !song.getFilePath().trim().isEmpty()) {
            File audioFile = new File(song.getFilePath());
            if (audioFile.exists() && audioFile.isFile()) {
                try {
                    Media media = new Media(audioFile.toURI().toString());
                    fxPlayer = new MediaPlayer(media);
                    fxPlayer.setVolume(volume);

                    fxPlayer.currentTimeProperty().addListener((obs, oldTime, newTime) -> {
                        currentPositionSeconds = (int) newTime.toSeconds();
                        notifyProgress();
                    });

                    fxPlayer.setOnEndOfMedia(this::next);
                    fxPlayer.play();
                    hasLocalFile = true;
                } catch (Throwable e) {
                    hasLocalFile = false;
                }
            }
        }

        if (!hasLocalFile) {
            startSimulation();
        }

        notifySongChanged();
        notifyStateChanged();
    }

    private void startSimulation() {
        stopSimulation();
        simulationTask = scheduler.scheduleAtFixedRate(() -> {
            if (state == PlayerState.PLAYING && currentSong != null) {
                currentPositionSeconds++;
                notifyProgress();
                if (currentPositionSeconds >= currentSong.getDurationSeconds()) {
                    next();
                }
            }
        }, 1, 1, TimeUnit.SECONDS);
    }

    private void stopSimulation() {
        if (simulationTask != null) {
            simulationTask.cancel(true);
            simulationTask = null;
        }
    }

    public void togglePlayPause() {
        if (state == PlayerState.PLAYING) {
            pause();
        } else if (state == PlayerState.PAUSED) {
            resume();
        } else if (currentSong != null) {
            play(currentSong);
        } else if (!queue.isEmpty()) {
            play(queue.get(0));
        }
    }

    public void pause() {
        if (state == PlayerState.PLAYING) {
            state = PlayerState.PAUSED;
            if (currentSong != null) currentSong.pause();

            if (fxPlayer != null) {
                try { fxPlayer.pause(); } catch (Throwable ignored) {}
            }
            notifyStateChanged();
        }
    }

    public void resume() {
        if (state == PlayerState.PAUSED) {
            state = PlayerState.PLAYING;
            if (currentSong != null) currentSong.play();

            if (fxPlayer != null) {
                try { fxPlayer.play(); } catch (Throwable ignored) {}
            }
            notifyStateChanged();
        }
    }

    public void stop() {
        if (fxPlayer != null) {
            try {
                fxPlayer.stop();
                fxPlayer.dispose();
            } catch (Throwable ignored) {}
            fxPlayer = null;
        }

        stopSimulation();

        if (currentSong != null && state != PlayerState.STOPPED) {
            currentSong.stop();
        }

        state = PlayerState.STOPPED;
        currentPositionSeconds = 0;
        notifyStateChanged();
        notifyProgress();
    }

    public void next() {
        if (queue.isEmpty()) {
            stop();
            return;
        }
        queueIndex++;
        if (queueIndex >= queue.size()) {
            queueIndex = 0;
        }
        play(queue.get(queueIndex));
    }

    public void previous() {
        if (queue.isEmpty()) {
            stop();
            return;
        }
        queueIndex--;
        if (queueIndex < 0) {
            queueIndex = queue.size() - 1;
        }
        play(queue.get(queueIndex));
    }

    public void seek(int seconds) {
        if (currentSong == null) return;
        this.currentPositionSeconds = Math.max(0, Math.min(seconds, currentSong.getDurationSeconds()));
        if (fxPlayer != null) {
            try {
                fxPlayer.seek(Duration.seconds(this.currentPositionSeconds));
            } catch (Throwable ignored) {}
        }
        notifyProgress();
    }

    public void setVolume(double vol) {
        this.volume = Math.max(0.0, Math.min(1.0, vol));
        if (fxPlayer != null) {
            try {
                fxPlayer.setVolume(this.volume);
            } catch (Throwable ignored) {}
        }
    }

    public double getVolume() {
        return volume;
    }

    public Song getCurrentSong() {
        return currentSong;
    }

    public PlayerState getState() {
        return state;
    }

    public int getCurrentPositionSeconds() {
        return currentPositionSeconds;
    }

    public String getFormattedCurrentTime() {
        int mins = currentPositionSeconds / 60;
        int secs = currentPositionSeconds % 60;
        return String.format("%d:%02d", mins, secs);
    }

    private void notifySongChanged() {
        for (PlaybackListener l : listeners) {
            try {
                l.onSongChanged(currentSong);
            } catch (Throwable ignored) {}
        }
    }

    private void notifyStateChanged() {
        for (PlaybackListener l : listeners) {
            try {
                l.onStateChanged(state);
            } catch (Throwable ignored) {}
        }
    }

    private void notifyProgress() {
        int total = (currentSong != null) ? currentSong.getDurationSeconds() : 0;
        for (PlaybackListener l : listeners) {
            try {
                l.onProgressUpdate(currentPositionSeconds, total);
            } catch (Throwable ignored) {}
        }
    }
}
