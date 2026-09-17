import java.util.List;

/**
 * High-level Music Library coordinating business logic and database persistence.
 * Connects the UI/Console with DatabaseManager and OOP models.
 */
public class MusicLibrary {
    private DatabaseManager db;

    public MusicLibrary() {
        this.db = DatabaseManager.getInstance();
    }

    public List<Song> getAllSongs() {
        return db.getAllSongs();
    }

    public List<Song> searchSongs(String query) {
        if (query == null || query.trim().isEmpty()) {
            return getAllSongs();
        }
        return db.searchSongs(query);
    }

    public Song getSongById(int id) {
        return db.getSongById(id);
    }

    public int addSong(String title, String artist, String album, String genre, int durationSeconds, String filePath) {
        return db.addSong(title, artist, album, genre, durationSeconds, filePath);
    }

    public boolean deleteSong(int songId) {
        return db.deleteSong(songId);
    }

    public boolean toggleFavorite(int songId) {
        return db.toggleFavorite(songId);
    }

    public List<Song> getFavorites() {
        return db.getFavoriteSongs();
    }

    public List<Playlist> getAllPlaylists() {
        return db.getAllPlaylists();
    }

    public Playlist createPlaylist(String name) {
        if (name == null || name.trim().isEmpty()) return null;
        return db.createPlaylist(name.trim());
    }

    public boolean addSongToPlaylist(int playlistId, int songId) {
        return db.addSongToPlaylist(playlistId, songId);
    }

    public boolean removeSongFromPlaylist(int playlistId, int songId) {
        return db.removeSongFromPlaylist(playlistId, songId);
    }

    public List<Song> getSongsInPlaylist(int playlistId) {
        return db.getSongsInPlaylist(playlistId);
    }

    public List<Artist> getAllArtists() {
        return db.getAllArtists();
    }

    public List<Album> getAllAlbums() {
        return db.getAllAlbums();
    }

    /**
     * Nicely formats and prints all songs to standard console output.
     */
    public void printSongTable(List<Song> songs, String headerTitle) {
        System.out.println("=================================================================================================");
        System.out.println("  " + headerTitle + " (" + songs.size() + " songs)");
        System.out.println("=================================================================================================");
        if (songs.isEmpty()) {
            System.out.println("  No songs found.");
        } else {
            System.out.printf("%-4s | %-24s | %-18s | %-18s | %-10s | %-6s | %s%n",
                    "ID", "Title", "Artist", "Album", "Genre", "Time", "Status");
            System.out.println("-------------------------------------------------------------------------------------------------");
            for (Song s : songs) {
                s.displayDetails();
            }
        }
        System.out.println("=================================================================================================");
    }
}
