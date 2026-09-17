import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Encapsulates Artist details and their collection of tracks.
 * Demonstrates the OOP concept of ENCAPSULATION and COMPOSITION.
 */
public class Artist {
    private int id;
    private String name;
    private String bio;
    private List<Song> songs;

    public Artist(int id, String name, String bio) {
        this.id = id;
        this.name = name;
        this.bio = (bio != null) ? bio : "";
        this.songs = new ArrayList<>();
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getBio() {
        return bio;
    }

    public void setBio(String bio) {
        this.bio = bio;
    }

    public List<Song> getSongs() {
        return Collections.unmodifiableList(songs);
    }

    public void addSong(Song song) {
        if (song != null && !songs.contains(song)) {
            songs.add(song);
        }
    }

    public int getSongCount() {
        return songs.size();
    }

    @Override
    public String toString() {
        return String.format("%s (%d songs)", name, songs.size());
    }
}
