/**
 * Represents a musical track.
 * Extends Media (Inheritance) and implements Playable (Polymorphism & Interfaces).
 * Uses private fields with accessors (Encapsulation).
 */
public class Song extends Media {
    private String artist;
    private String album;
    private String genre;
    private String filePath;
    private boolean isFavorite;
    private boolean isPlaying;

    public Song(int id, String title, String artist, String album, String genre, int durationSeconds, String filePath, boolean isFavorite) {
        super(id, title, durationSeconds);
        this.artist = (artist != null && !artist.trim().isEmpty()) ? artist.trim() : "Unknown Artist";
        this.album = (album != null && !album.trim().isEmpty()) ? album.trim() : "Unknown Album";
        this.genre = (genre != null && !genre.trim().isEmpty()) ? genre.trim() : "General";
        this.filePath = filePath;
        this.isFavorite = isFavorite;
        this.isPlaying = false;
    }

    public Song(int id, String title, String artist, String album, String genre) {
        this(id, title, artist, album, genre, 200, null, false);
    }

    public String getArtist() {
        return artist;
    }

    public void setArtist(String artist) {
        this.artist = artist;
    }

    public String getAlbum() {
        return album;
    }

    public void setAlbum(String album) {
        this.album = album;
    }

    public String getGenre() {
        return genre;
    }

    public void setGenre(String genre) {
        this.genre = genre;
    }

    public String getFilePath() {
        return filePath;
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }

    public boolean isFavorite() {
        return isFavorite;
    }

    public void setFavorite(boolean favorite) {
        isFavorite = favorite;
    }

    // Playable implementation (Polymorphism)
    @Override
    public void play() {
        this.isPlaying = true;
        System.out.printf("▶ [NOW PLAYING] \"%s\" by %s (Album: %s, Genre: %s)%n", title, artist, album, genre);
    }

    @Override
    public void pause() {
        this.isPlaying = false;
        System.out.printf("⏸ [PAUSED] \"%s\" by %s%n", title, artist);
    }

    @Override
    public void stop() {
        this.isPlaying = false;
        System.out.printf("⏹ [STOPPED] \"%s\"%n", title);
    }

    @Override
    public boolean isPlaying() {
        return isPlaying;
    }

    @Override
    public void displayDetails() {
        String favBadge = isFavorite ? "❤️ Liked" : "  ";
        System.out.printf("%-4d | %-24s | %-18s | %-18s | %-10s | %-6s | %s%n",
                id,
                truncate(title, 24),
                truncate(artist, 18),
                truncate(album, 18),
                truncate(genre, 10),
                getFormattedDuration(),
                favBadge);
    }

    private String truncate(String text, int max) {
        if (text == null) return "";
        if (text.length() <= max) return text;
        return text.substring(0, max - 2) + "..";
    }

    @Override
    public String toString() {
        return String.format("%s - %s (%s)", title, artist, getFormattedDuration());
    }
}
