import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Represents a user-curated collection of songs.
 * Demonstrates the OOP concept of COMPOSITION (Playlist has-a List of Songs).
 */
public class Playlist {
    private int id;
    private String name;
    private List<Song> songs;

    public Playlist(int id, String name) {
        this.id = id;
        this.name = name;
        this.songs = new ArrayList<>();
    }

    public Playlist(String name) {
        this(0, name);
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

    public List<Song> getSongs() {
        return Collections.unmodifiableList(songs);
    }

    public void setSongs(List<Song> songList) {
        this.songs = new ArrayList<>(songList);
    }

    public boolean addSong(Song song) {
        if (song != null && !songs.contains(song)) {
            songs.add(song);
            return true;
        }
        return false;
    }

    public boolean removeSong(Song song) {
        return songs.remove(song);
    }

    public int getSongCount() {
        return songs.size();
    }

    public int getTotalDurationSeconds() {
        int total = 0;
        for (Song s : songs) {
            total += s.getDurationSeconds();
        }
        return total;
    }

    public String getFormattedTotalDuration() {
        int totalSeconds = getTotalDurationSeconds();
        int mins = totalSeconds / 60;
        int secs = totalSeconds % 60;
        return String.format("%d mins %d secs", mins, secs);
    }

    public void displayPlaylist() {
        System.out.println("=========================================================================================");
        System.out.printf("📁 PLAYLIST: %s (ID: %d) | %d Songs | Total Time: %s%n",
                name, id, songs.size(), getFormattedTotalDuration());
        System.out.println("=========================================================================================");
        if (songs.isEmpty()) {
            System.out.println("  (This playlist is currently empty. Add songs using option 4!)");
        } else {
            System.out.printf("%-4s | %-24s | %-18s | %-18s | %-10s | %-6s%n",
                    "No.", "Title", "Artist", "Album", "Genre", "Time");
            System.out.println("-----------------------------------------------------------------------------------------");
            for (int i = 0; i < songs.size(); i++) {
                Song s = songs.get(i);
                System.out.printf("%-4d | %-24s | %-18s | %-18s | %-10s | %-6s%n",
                        (i + 1), s.getTitle(), s.getArtist(), s.getAlbum(), s.getGenre(), s.getFormattedDuration());
            }
        }
        System.out.println("=========================================================================================");
    }

    @Override
    public String toString() {
        return String.format("%s (%d songs)", name, songs.size());
    }
}
