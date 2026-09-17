import java.util.List;
import java.util.Scanner;

/**
 * Console CLI runner for TuneFlow.
 * Implements the 7-option main menu specified in the requirements.
 */
public class Main {
    private static final Scanner scanner = new Scanner(System.in);
    private static final MusicLibrary library = new MusicLibrary();
    private static final MusicPlayer player = new MusicPlayer();

    public static void main(String[] args) {
        printBanner();

        boolean running = true;
        while (running) {
            printMenu();
            System.out.print("👉 Enter your choice (1-7): ");
            String input = scanner.nextLine().trim();

            switch (input) {
                case "1":
                    handleViewAllSongs();
                    break;
                case "2":
                    handleSearchSong();
                    break;
                case "3":
                    handleCreatePlaylist();
                    break;
                case "4":
                    handleAddSongToPlaylist();
                    break;
                case "5":
                    handleViewPlaylist();
                    break;
                case "6":
                    handlePlaySong();
                    break;
                case "7":
                    running = false;
                    player.stop();
                    System.out.println("\n🎶 Thank you for using TuneFlow! Keep the rhythm going. Goodbye! 👋\n");
                    break;
                default:
                    System.out.println("❌ Invalid choice. Please enter a number between 1 and 7.\n");
            }
        }
    }

    private static void printBanner() {
        System.out.println("\n" +
                "  ████████╗██╗   ██╗███╗   ██╗███████╗███████╗██╗      ██████╗ ██╗    ██╗\n" +
                "  ╚══██╔══╝██║   ██║████╗  ██║██╔════╝██╔════╝██║     ██╔═══██╗██║    ██║\n" +
                "     ██║   ██║   ██║██╔██╗ ██║█████╗  █████╗  ██║     ██║   ██║██║ █╗ ██║\n" +
                "     ██║   ██║   ██║██║╚██╗██║██╔══╝  ██╔══╝  ██║     ██║   ██║██║███╗██║\n" +
                "     ██║   ╚██████╔╝██║ ╚████║███████╗██║     ███████╗╚██████╔╝╚███╔███╔╝\n" +
                "     ╚═╝    ╚═════╝ ╚═╝  ╚═══╝╚══════╝╚═╝     ╚══════╝ ╚═════╝  ╚══╝╚══╝ \n" +
                "                  🎵 PERSONAL MUSIC SYSTEM (JAVA & MYSQL) 🎵\n");
    }

    private static void printMenu() {
        System.out.println("\n===========================================");
        System.out.println("               TUNEFLOW 🎵                 ");
        System.out.println("===========================================");
        System.out.println("1. View all songs");
        System.out.println("2. Search song");
        System.out.println("3. Create playlist");
        System.out.println("4. Add song to playlist");
        System.out.println("5. View playlist");
        System.out.println("6. Play song");
        System.out.println("7. Exit");
        System.out.println("===========================================");
    }

    // 1. View all songs
    private static void handleViewAllSongs() {
        List<Song> songs = library.getAllSongs();
        library.printSongTable(songs, "ALL TRACKS IN LIBRARY");
    }

    // 2. Search song
    private static void handleSearchSong() {
        System.out.print("\n🔍 Enter search keyword (title, artist, album, genre): ");
        String query = scanner.nextLine().trim();
        if (query.isEmpty()) {
            System.out.println("⚠️ Search query cannot be empty.");
            return;
        }

        List<Song> results = library.searchSongs(query);
        library.printSongTable(results, "SEARCH RESULTS FOR: \"" + query + "\"");
    }

    // 3. Create playlist
    private static void handleCreatePlaylist() {
        System.out.print("\n📁 Enter new playlist name: ");
        String name = scanner.nextLine().trim();
        if (name.isEmpty()) {
            System.out.println("⚠️ Playlist name cannot be empty.");
            return;
        }

        Playlist playlist = library.createPlaylist(name);
        if (playlist != null) {
            System.out.println("✅ Playlist \"" + playlist.getName() + "\" created successfully (ID: " + playlist.getId() + ")!");
        } else {
            System.out.println("❌ Failed to create playlist.");
        }
    }

    // 4. Add song to playlist
    private static void handleAddSongToPlaylist() {
        List<Playlist> playlists = library.getAllPlaylists();
        if (playlists.isEmpty()) {
            System.out.println("\n⚠️ No playlists found. Please create a playlist first (Option 3)!");
            return;
        }

        System.out.println("\n--- Available Playlists ---");
        for (Playlist p : playlists) {
            System.out.printf("  [%d] %s (%d songs)%n", p.getId(), p.getName(), p.getSongCount());
        }

        System.out.print("👉 Enter Playlist ID to add song to: ");
        int playlistId = readInt();
        if (playlistId == -1) return;

        // Display songs
        List<Song> songs = library.getAllSongs();
        library.printSongTable(songs, "SELECT A SONG TO ADD");

        System.out.print("👉 Enter Song ID to add: ");
        int songId = readInt();
        if (songId == -1) return;

        boolean success = library.addSongToPlaylist(playlistId, songId);
        if (success) {
            Song added = library.getSongById(songId);
            System.out.println("✅ Successfully added \"" + (added != null ? added.getTitle() : "Song #" + songId) + "\" to playlist!");
        } else {
            System.out.println("⚠️ Could not add song. (It may already be in the playlist or invalid IDs were provided).");
        }
    }

    // 5. View playlist
    private static void handleViewPlaylist() {
        List<Playlist> playlists = library.getAllPlaylists();
        if (playlists.isEmpty()) {
            System.out.println("\n⚠️ No playlists available. Create one using option 3!");
            return;
        }

        System.out.println("\n--- Your Playlists ---");
        for (Playlist p : playlists) {
            System.out.printf("  [%d] %s (%d songs)%n", p.getId(), p.getName(), p.getSongCount());
        }

        System.out.print("👉 Enter Playlist ID to view: ");
        int playlistId = readInt();
        if (playlistId == -1) return;

        for (Playlist p : playlists) {
            if (p.getId() == playlistId) {
                p.displayPlaylist();
                return;
            }
        }
        System.out.println("❌ Playlist ID not found.");
    }

    // 6. Play song
    private static void handlePlaySong() {
        List<Song> songs = library.getAllSongs();
        if (songs.isEmpty()) {
            System.out.println("\n⚠️ No songs in the library to play.");
            return;
        }

        library.printSongTable(songs, "SELECT A TRACK TO PLAY");
        System.out.print("👉 Enter Song ID to play: ");
        int songId = readInt();
        if (songId == -1) return;

        Song selected = library.getSongById(songId);
        if (selected == null) {
            System.out.println("❌ Song with ID " + songId + " not found.");
            return;
        }

        // Start playback
        player.play(selected);

        // Interactive Playback submenu
        boolean inPlayback = true;
        while (inPlayback) {
            System.out.println("\n-------------------------------------------");
            System.out.printf("🎧 NOW PLAYING: %s - %s [%s]%n",
                    selected.getTitle(), selected.getArtist(), selected.getFormattedDuration());
            System.out.printf("   Status: %s | Favorite: %s%n",
                    player.getState(), (selected.isFavorite() ? "❤️ Liked" : "No"));
            System.out.println("-------------------------------------------");
            System.out.println("  [P] Pause / Resume");
            System.out.println("  [F] Toggle Favorite (❤️)");
            System.out.println("  [S] Stop Playback");
            System.out.println("  [B] Back to Main Menu");
            System.out.print("👉 Choose action: ");
            String cmd = scanner.nextLine().trim().toUpperCase();

            switch (cmd) {
                case "P":
                    player.togglePlayPause();
                    break;
                case "F":
                    boolean nowFav = library.toggleFavorite(selected.getId());
                    selected.setFavorite(nowFav);
                    System.out.println(nowFav ? "❤️ Marked as favorite!" : "🤍 Removed from favorites.");
                    break;
                case "S":
                    player.stop();
                    inPlayback = false;
                    break;
                case "B":
                    inPlayback = false;
                    break;
                default:
                    System.out.println("Invalid option.");
            }
        }
    }

    private static int readInt() {
        try {
            String line = scanner.nextLine().trim();
            return Integer.parseInt(line);
        } catch (NumberFormatException e) {
            System.out.println("❌ Invalid number entered.");
            return -1;
        }
    }
}
