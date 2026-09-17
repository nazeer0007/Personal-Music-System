import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Manages database persistence via MySQL and JDBC.
 * Implements the Singleton pattern and Data Access Object (DAO) principles.
 */
public class DatabaseManager {
    private static DatabaseManager instance;

    // Database connection credentials
    private static final String DB_URL = "jdbc:mysql://localhost:3306/tuneflow?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC&createDatabaseIfNotExist=true";
    private static final String USER = "root";
    private static final String PASSWORD = "";

    private Connection connection;
    private boolean isConnected = false;

    private DatabaseManager() {
        connect();
        if (isConnected) {
            initSchema();
            seedDefaultDataIfEmpty();
        }
    }

    public static synchronized DatabaseManager getInstance() {
        if (instance == null) {
            instance = new DatabaseManager();
        }
        return instance;
    }

    private void connect() {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            this.connection = DriverManager.getConnection(DB_URL, USER, PASSWORD);
            this.isConnected = true;
            System.out.println("✅ [DatabaseManager] Connected to MySQL database 'tuneflow' successfully!");
        } catch (ClassNotFoundException e) {
            System.err.println("⚠️ [DatabaseManager] MySQL JDBC Driver not found in classpath. Ensure mysql-connector-j is included.");
            this.isConnected = false;
        } catch (SQLException e) {
            System.err.println("⚠️ [DatabaseManager] Could not connect to MySQL server at localhost:3306: " + e.getMessage());
            this.isConnected = false;
        }
    }

    public Connection getConnection() {
        try {
            if (connection == null || connection.isClosed()) {
                connect();
            }
        } catch (SQLException e) {
            System.err.println("Error verifying DB connection: " + e.getMessage());
        }
        return connection;
    }

    public boolean isConnected() {
        return isConnected;
    }

    /**
     * Initializes necessary tables if they don't exist.
     */
    private void initSchema() {
        String createArtists = "CREATE TABLE IF NOT EXISTS artists (" +
                "id INT AUTO_INCREMENT PRIMARY KEY, " +
                "name VARCHAR(255) NOT NULL UNIQUE, " +
                "bio TEXT);";

        String createAlbums = "CREATE TABLE IF NOT EXISTS albums (" +
                "id INT AUTO_INCREMENT PRIMARY KEY, " +
                "title VARCHAR(255) NOT NULL, " +
                "artist_id INT, " +
                "release_year INT, " +
                "FOREIGN KEY (artist_id) REFERENCES artists(id) ON DELETE SET NULL);";

        String createSongs = "CREATE TABLE IF NOT EXISTS songs (" +
                "id INT AUTO_INCREMENT PRIMARY KEY, " +
                "title VARCHAR(255) NOT NULL, " +
                "artist_id INT, " +
                "album_id INT, " +
                "genre VARCHAR(100) DEFAULT 'General', " +
                "duration_seconds INT DEFAULT 180, " +
                "file_path VARCHAR(500), " +
                "is_favorite BOOLEAN DEFAULT FALSE, " +
                "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP, " +
                "FOREIGN KEY (artist_id) REFERENCES artists(id) ON DELETE SET NULL, " +
                "FOREIGN KEY (album_id) REFERENCES albums(id) ON DELETE SET NULL);";

        String createPlaylists = "CREATE TABLE IF NOT EXISTS playlists (" +
                "id INT AUTO_INCREMENT PRIMARY KEY, " +
                "name VARCHAR(255) NOT NULL, " +
                "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP);";

        String createPlaylistSongs = "CREATE TABLE IF NOT EXISTS playlist_songs (" +
                "playlist_id INT, " +
                "song_id INT, " +
                "added_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP, " +
                "PRIMARY KEY (playlist_id, song_id), " +
                "FOREIGN KEY (playlist_id) REFERENCES playlists(id) ON DELETE CASCADE, " +
                "FOREIGN KEY (song_id) REFERENCES songs(id) ON DELETE CASCADE);";

        String createUsers = "CREATE TABLE IF NOT EXISTS users (" +
                "id INT AUTO_INCREMENT PRIMARY KEY, " +
                "username VARCHAR(100) NOT NULL UNIQUE, " +
                "email VARCHAR(255), " +
                "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP);";

        try (Statement stmt = getConnection().createStatement()) {
            stmt.execute(createArtists);
            stmt.execute(createAlbums);
            stmt.execute(createSongs);
            stmt.execute(createPlaylists);
            stmt.execute(createPlaylistSongs);
            stmt.execute(createUsers);
        } catch (SQLException e) {
            System.err.println("Error initializing schema: " + e.getMessage());
        }
    }

    /**
     * Pre-populates default songs and playlists if database is empty.
     */
    private void seedDefaultDataIfEmpty() {
        try (Statement stmt = getConnection().createStatement();
             ResultSet rs = stmt.executeQuery("SELECT COUNT(*) AS total FROM songs")) {
            if (rs.next() && rs.getInt("total") == 0) {
                System.out.println("🎵 [DatabaseManager] Seeding initial music collection into MySQL...");

                // Seed Artists
                insertArtistIfNotExists("The Weeknd", "Canadian singer, songwriter, and record producer.");
                insertArtistIfNotExists("Ed Sheeran", "English singer-songwriter known for pop and acoustic ballads.");
                insertArtistIfNotExists("Imagine Dragons", "American pop rock band from Las Vegas, Nevada.");
                insertArtistIfNotExists("Dua Lipa", "English and Albanian singer and songwriter.");
                insertArtistIfNotExists("Coldplay", "British rock band formed in London in 1997.");

                // Seed Albums
                insertAlbumIfNotExists("After Hours", "The Weeknd", 2020);
                insertAlbumIfNotExists("Starboy", "The Weeknd", 2016);
                insertAlbumIfNotExists("Divide", "Ed Sheeran", 2017);
                insertAlbumIfNotExists("Evolve", "Imagine Dragons", 2017);
                insertAlbumIfNotExists("Future Nostalgia", "Dua Lipa", 2020);
                insertAlbumIfNotExists("Parachutes", "Coldplay", 2000);

                // Seed Songs
                addSong("Blinding Lights", "The Weeknd", "After Hours", "Synthpop", 200, null);
                addSong("Save Your Tears", "The Weeknd", "After Hours", "Synthpop", 215, null);
                addSong("Starboy", "The Weeknd", "Starboy", "R&B / Pop", 230, null);
                addSong("Shape of You", "Ed Sheeran", "Divide", "Pop", 233, null);
                addSong("Perfect", "Ed Sheeran", "Divide", "Acoustic Pop", 263, null);
                addSong("Believer", "Imagine Dragons", "Evolve", "Alt Rock", 204, null);
                addSong("Levitating", "Dua Lipa", "Future Nostalgia", "Dance Pop", 203, null);
                addSong("Yellow", "Coldplay", "Parachutes", "Alt Rock", 269, null);

                // Seed Sample Playlist
                Playlist favHits = createPlaylist("Today's Top Hits");
                if (favHits != null) {
                    addSongToPlaylist(favHits.getId(), 1);
                    addSongToPlaylist(favHits.getId(), 4);
                    addSongToPlaylist(favHits.getId(), 6);
                }

                Playlist chillVibes = createPlaylist("Chill Acoustic");
                if (chillVibes != null) {
                    addSongToPlaylist(chillVibes.getId(), 5);
                    addSongToPlaylist(chillVibes.getId(), 8);
                }

                System.out.println("✅ [DatabaseManager] Successfully seeded 8 initial songs and 2 playlists!");
            }
        } catch (SQLException e) {
            System.err.println("Error checking/seeding data: " + e.getMessage());
        }
    }

    private int insertArtistIfNotExists(String name, String bio) {
        String query = "SELECT id FROM artists WHERE name = ?";
        try (PreparedStatement ps = getConnection().prepareStatement(query)) {
            ps.setString(1, name);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getInt("id");
            }
        } catch (SQLException ignored) {}

        String insert = "INSERT INTO artists (name, bio) VALUES (?, ?)";
        try (PreparedStatement ps = getConnection().prepareStatement(insert, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, name);
            ps.setString(2, bio);
            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) return rs.getInt(1);
            }
        } catch (SQLException e) {
            System.err.println("Error inserting artist: " + e.getMessage());
        }
        return -1;
    }

    private int insertAlbumIfNotExists(String title, String artistName, int year) {
        int artistId = insertArtistIfNotExists(artistName, "");
        String query = "SELECT id FROM albums WHERE title = ?";
        try (PreparedStatement ps = getConnection().prepareStatement(query)) {
            ps.setString(1, title);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getInt("id");
            }
        } catch (SQLException ignored) {}

        String insert = "INSERT INTO albums (title, artist_id, release_year) VALUES (?, ?, ?)";
        try (PreparedStatement ps = getConnection().prepareStatement(insert, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, title);
            if (artistId > 0) ps.setInt(2, artistId);
            else ps.setNull(2, Types.INTEGER);
            ps.setInt(3, year);
            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) return rs.getInt(1);
            }
        } catch (SQLException e) {
            System.err.println("Error inserting album: " + e.getMessage());
        }
        return -1;
    }

    public List<Song> getAllSongs() {
        List<Song> songs = new ArrayList<>();
        String sql = "SELECT s.id, s.title, a.name AS artist_name, al.title AS album_title, " +
                "s.genre, s.duration_seconds, s.file_path, s.is_favorite " +
                "FROM songs s " +
                "LEFT JOIN artists a ON s.artist_id = a.id " +
                "LEFT JOIN albums al ON s.album_id = al.id " +
                "ORDER BY s.id ASC";

        try (Statement stmt = getConnection().createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                Song song = mapRowToSong(rs);
                songs.add(song);
            }
        } catch (SQLException e) {
            System.err.println("Error retrieving songs: " + e.getMessage());
        }
        return songs;
    }

    public List<Song> searchSongs(String query) {
        List<Song> songs = new ArrayList<>();
        String sql = "SELECT s.id, s.title, a.name AS artist_name, al.title AS album_title, " +
                "s.genre, s.duration_seconds, s.file_path, s.is_favorite " +
                "FROM songs s " +
                "LEFT JOIN artists a ON s.artist_id = a.id " +
                "LEFT JOIN albums al ON s.album_id = al.id " +
                "WHERE LOWER(s.title) LIKE ? OR LOWER(a.name) LIKE ? OR LOWER(al.title) LIKE ? OR LOWER(s.genre) LIKE ? " +
                "ORDER BY s.id ASC";

        try (PreparedStatement ps = getConnection().prepareStatement(sql)) {
            String wildcard = "%" + query.toLowerCase().trim() + "%";
            ps.setString(1, wildcard);
            ps.setString(2, wildcard);
            ps.setString(3, wildcard);
            ps.setString(4, wildcard);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    songs.add(mapRowToSong(rs));
                }
            }
        } catch (SQLException e) {
            System.err.println("Error searching songs: " + e.getMessage());
        }
        return songs;
    }

    public Song getSongById(int songId) {
        String sql = "SELECT s.id, s.title, a.name AS artist_name, al.title AS album_title, " +
                "s.genre, s.duration_seconds, s.file_path, s.is_favorite " +
                "FROM songs s " +
                "LEFT JOIN artists a ON s.artist_id = a.id " +
                "LEFT JOIN albums al ON s.album_id = al.id " +
                "WHERE s.id = ?";
        try (PreparedStatement ps = getConnection().prepareStatement(sql)) {
            ps.setInt(1, songId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return mapRowToSong(rs);
            }
        } catch (SQLException e) {
            System.err.println("Error fetching song by ID: " + e.getMessage());
        }
        return null;
    }

    public int addSong(String title, String artist, String album, String genre, int durationSeconds, String filePath) {
        int artistId = insertArtistIfNotExists(artist, "");
        int albumId = insertAlbumIfNotExists(album, artist, 2024);

        String sql = "INSERT INTO songs (title, artist_id, album_id, genre, duration_seconds, file_path, is_favorite) " +
                "VALUES (?, ?, ?, ?, ?, ?, FALSE)";
        try (PreparedStatement ps = getConnection().prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, title);
            ps.setInt(2, artistId);
            ps.setInt(3, albumId);
            ps.setString(4, (genre != null && !genre.isEmpty()) ? genre : "General");
            ps.setInt(5, durationSeconds > 0 ? durationSeconds : 180);
            ps.setString(6, filePath);
            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) return rs.getInt(1);
            }
        } catch (SQLException e) {
            System.err.println("Error adding song: " + e.getMessage());
        }
        return -1;
    }

    public boolean deleteSong(int songId) {
        String sql = "DELETE FROM songs WHERE id = ?";
        try (PreparedStatement ps = getConnection().prepareStatement(sql)) {
            ps.setInt(1, songId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error deleting song: " + e.getMessage());
            return false;
        }
    }

    public boolean toggleFavorite(int songId) {
        String check = "SELECT is_favorite FROM songs WHERE id = ?";
        try (PreparedStatement ps = getConnection().prepareStatement(check)) {
            ps.setInt(1, songId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    boolean current = rs.getBoolean("is_favorite");
                    boolean updated = !current;
                    String update = "UPDATE songs SET is_favorite = ? WHERE id = ?";
                    try (PreparedStatement updatePs = getConnection().prepareStatement(update)) {
                        updatePs.setBoolean(1, updated);
                        updatePs.setInt(2, songId);
                        updatePs.executeUpdate();
                        return updated;
                    }
                }
            }
        } catch (SQLException e) {
            System.err.println("Error toggling favorite: " + e.getMessage());
        }
        return false;
    }

    public List<Song> getFavoriteSongs() {
        List<Song> songs = new ArrayList<>();
        String sql = "SELECT s.id, s.title, a.name AS artist_name, al.title AS album_title, " +
                "s.genre, s.duration_seconds, s.file_path, s.is_favorite " +
                "FROM songs s " +
                "LEFT JOIN artists a ON s.artist_id = a.id " +
                "LEFT JOIN albums al ON s.album_id = al.id " +
                "WHERE s.is_favorite = TRUE " +
                "ORDER BY s.id ASC";

        try (Statement stmt = getConnection().createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                songs.add(mapRowToSong(rs));
            }
        } catch (SQLException e) {
            System.err.println("Error retrieving favorite songs: " + e.getMessage());
        }
        return songs;
    }

    public Playlist createPlaylist(String name) {
        String sql = "INSERT INTO playlists (name) VALUES (?)";
        try (PreparedStatement ps = getConnection().prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, name);
            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    return new Playlist(rs.getInt(1), name);
                }
            }
        } catch (SQLException e) {
            System.err.println("Error creating playlist: " + e.getMessage());
        }
        return null;
    }

    public List<Playlist> getAllPlaylists() {
        List<Playlist> playlists = new ArrayList<>();
        String sql = "SELECT id, name FROM playlists ORDER BY id ASC";
        try (Statement stmt = getConnection().createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                int id = rs.getInt("id");
                String name = rs.getString("name");
                Playlist p = new Playlist(id, name);
                p.setSongs(getSongsInPlaylist(id));
                playlists.add(p);
            }
        } catch (SQLException e) {
            System.err.println("Error retrieving playlists: " + e.getMessage());
        }
        return playlists;
    }

    public boolean addSongToPlaylist(int playlistId, int songId) {
        String sql = "INSERT IGNORE INTO playlist_songs (playlist_id, song_id) VALUES (?, ?)";
        try (PreparedStatement ps = getConnection().prepareStatement(sql)) {
            ps.setInt(1, playlistId);
            ps.setInt(2, songId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error adding song to playlist: " + e.getMessage());
            return false;
        }
    }

    public boolean removeSongFromPlaylist(int playlistId, int songId) {
        String sql = "DELETE FROM playlist_songs WHERE playlist_id = ? AND song_id = ?";
        try (PreparedStatement ps = getConnection().prepareStatement(sql)) {
            ps.setInt(1, playlistId);
            ps.setInt(2, songId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error removing song from playlist: " + e.getMessage());
            return false;
        }
    }

    public List<Song> getSongsInPlaylist(int playlistId) {
        List<Song> songs = new ArrayList<>();
        String sql = "SELECT s.id, s.title, a.name AS artist_name, al.title AS album_title, " +
                "s.genre, s.duration_seconds, s.file_path, s.is_favorite " +
                "FROM playlist_songs ps " +
                "JOIN songs s ON ps.song_id = s.id " +
                "LEFT JOIN artists a ON s.artist_id = a.id " +
                "LEFT JOIN albums al ON s.album_id = al.id " +
                "WHERE ps.playlist_id = ? " +
                "ORDER BY ps.added_at ASC";

        try (PreparedStatement ps = getConnection().prepareStatement(sql)) {
            ps.setInt(1, playlistId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    songs.add(mapRowToSong(rs));
                }
            }
        } catch (SQLException e) {
            System.err.println("Error retrieving songs in playlist: " + e.getMessage());
        }
        return songs;
    }

    public List<Artist> getAllArtists() {
        List<Artist> artists = new ArrayList<>();
        String sql = "SELECT id, name, bio FROM artists ORDER BY name ASC";
        try (Statement stmt = getConnection().createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                Artist artist = new Artist(rs.getInt("id"), rs.getString("name"), rs.getString("bio"));
                // Load songs for this artist
                String songSql = "SELECT s.id, s.title, a.name AS artist_name, al.title AS album_title, " +
                        "s.genre, s.duration_seconds, s.file_path, s.is_favorite " +
                        "FROM songs s " +
                        "LEFT JOIN artists a ON s.artist_id = a.id " +
                        "LEFT JOIN albums al ON s.album_id = al.id " +
                        "WHERE s.artist_id = ?";
                try (PreparedStatement ps = getConnection().prepareStatement(songSql)) {
                    ps.setInt(1, artist.getId());
                    try (ResultSet songRs = ps.executeQuery()) {
                        while (songRs.next()) {
                            artist.addSong(mapRowToSong(songRs));
                        }
                    }
                }
                artists.add(artist);
            }
        } catch (SQLException e) {
            System.err.println("Error fetching artists: " + e.getMessage());
        }
        return artists;
    }

    public List<Album> getAllAlbums() {
        List<Album> albums = new ArrayList<>();
        String sql = "SELECT al.id, al.title, a.name AS artist_name, al.release_year " +
                "FROM albums al " +
                "LEFT JOIN artists a ON al.artist_id = a.id " +
                "ORDER BY al.title ASC";
        try (Statement stmt = getConnection().createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                Album album = new Album(rs.getInt("id"), rs.getString("title"), rs.getString("artist_name"), rs.getInt("release_year"));
                // Load tracks
                String trackSql = "SELECT s.id, s.title, a.name AS artist_name, al.title AS album_title, " +
                        "s.genre, s.duration_seconds, s.file_path, s.is_favorite " +
                        "FROM songs s " +
                        "LEFT JOIN artists a ON s.artist_id = a.id " +
                        "LEFT JOIN albums al ON s.album_id = al.id " +
                        "WHERE s.album_id = ?";
                try (PreparedStatement ps = getConnection().prepareStatement(trackSql)) {
                    ps.setInt(1, album.getId());
                    try (ResultSet trackRs = ps.executeQuery()) {
                        while (trackRs.next()) {
                            album.addTrack(mapRowToSong(trackRs));
                        }
                    }
                }
                albums.add(album);
            }
        } catch (SQLException e) {
            System.err.println("Error fetching albums: " + e.getMessage());
        }
        return albums;
    }

    private Song mapRowToSong(ResultSet rs) throws SQLException {
        int id = rs.getInt("id");
        String title = rs.getString("title");
        String artist = rs.getString("artist_name");
        String album = rs.getString("album_title");
        String genre = rs.getString("genre");
        int duration = rs.getInt("duration_seconds");
        String path = rs.getString("file_path");
        boolean fav = rs.getBoolean("is_favorite");
        return new Song(id, title, artist, album, genre, duration, path, fav);
    }
}
