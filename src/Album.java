import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Encapsulates Album details and its tracklist.
 * Demonstrates the OOP concept of ENCAPSULATION and COMPOSITION.
 */
public class Album {
    private int id;
    private String title;
    private String artist;
    private int releaseYear;
    private List<Song> tracks;

    public Album(int id, String title, String artist, int releaseYear) {
        this.id = id;
        this.title = title;
        this.artist = artist;
        this.releaseYear = releaseYear;
        this.tracks = new ArrayList<>();
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

    public String getArtist() {
        return artist;
    }

    public void setArtist(String artist) {
        this.artist = artist;
    }

    public int getReleaseYear() {
        return releaseYear;
    }

    public void setReleaseYear(int releaseYear) {
        this.releaseYear = releaseYear;
    }

    public List<Song> getTracks() {
        return Collections.unmodifiableList(tracks);
    }

    public void addTrack(Song song) {
        if (song != null && !tracks.contains(song)) {
            tracks.add(song);
        }
    }

    public int getTrackCount() {
        return tracks.size();
    }

    @Override
    public String toString() {
        return String.format("%s by %s (%d) - %d tracks", title, artist, releaseYear, tracks.size());
    }
}
