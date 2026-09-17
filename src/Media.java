/**
 * Abstract class representing a general media item.
 * Demonstrates the OOP concepts of ABSTRACTION and INHERITANCE.
 */
public abstract class Media implements Playable {
    protected int id;
    protected String title;
    protected int durationSeconds;

    public Media(int id, String title, int durationSeconds) {
        this.id = id;
        this.title = title;
        this.durationSeconds = durationSeconds > 0 ? durationSeconds : 180;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public int getDurationSeconds() {
        return durationSeconds;
    }

    public void setDurationSeconds(int durationSeconds) {
        this.durationSeconds = durationSeconds;
    }

    public String getFormattedDuration() {
        int minutes = durationSeconds / 60;
        int seconds = durationSeconds % 60;
        return String.format("%d:%02d", minutes, seconds);
    }

    /**
     * Abstract method forcing subclasses to define their own display formatting.
     */
    public abstract void displayDetails();

    @Override
    public String toString() {
        return String.format("[%d] %s (%s)", id, title, getFormattedDuration());
    }
}
