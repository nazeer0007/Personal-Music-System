import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Encapsulates User details, their custom playlists, and favorite songs.
 * Demonstrates ENCAPSULATION and COMPOSITION.
 */
public class User {
    private int id;
    private String username;
    private String email;
    private List<Playlist> playlists;
    private List<Song> favorites;

    public User(int id, String username, String email) {
        this.id = id;
        this.username = username;
        this.email = email;
        this.playlists = new ArrayList<>();
        this.favorites = new ArrayList<>();
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public List<Playlist> getPlaylists() {
        return Collections.unmodifiableList(playlists);
    }

    public void addPlaylist(Playlist playlist) {
        if (playlist != null && !playlists.contains(playlist)) {
            playlists.add(playlist);
        }
    }

    public List<Song> getFavorites() {
        return Collections.unmodifiableList(favorites);
    }

    public void addFavorite(Song song) {
        if (song != null && !favorites.contains(song)) {
            favorites.add(song);
            song.setFavorite(true);
        }
    }

    public void removeFavorite(Song song) {
        if (song != null) {
            favorites.remove(song);
            song.setFavorite(false);
        }
    }

    @Override
    public String toString() {
        return String.format("User: %s (%s)", username, email);
    }
}
