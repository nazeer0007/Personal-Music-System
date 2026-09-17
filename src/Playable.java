/**
 * Interface representing any playable audio item in TuneFlow.
 * Demonstrates the OOP concept of INTERFACES and POLYMORPHISM.
 */
public interface Playable {
    void play();
    void pause();
    void stop();
    boolean isPlaying();
}
