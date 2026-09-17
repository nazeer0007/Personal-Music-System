import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.File;
import java.util.List;

/**
 * TuneFlow JavaFX Desktop Application.
 * Full GUI for personal music management with MySQL persistence,
 * real audio playback, playlist management, and sleek dark modern design.
 */
public class TuneFlowApp extends Application implements MusicPlayer.PlaybackListener {

    private MusicLibrary library;
    private MusicPlayer player;

    private BorderPane rootLayout;
    private VBox contentArea;
    private TextField searchField;

    // Bottom Player Controls
    private Label nowTitleLabel;
    private Label nowArtistLabel;
    private Button playPauseBtn;
    private Slider progressBar;
    private Slider volumeSlider;
    private Label currentTimeLabel;
    private Label totalTimeLabel;

    private String currentSection = "home";

    @Override
    public void start(Stage primaryStage) {
        this.library = new MusicLibrary();
        this.player = new MusicPlayer();
        this.player.addListener(this);

        rootLayout = new BorderPane();
        rootLayout.setStyle("-fx-background-color: #080b18;");

        // 1. Sidebar (Left)
        VBox sidebar = createSidebar();
        rootLayout.setLeft(sidebar);

        // 2. Center Content Container
        VBox mainContainer = new VBox(20);
        mainContainer.setPadding(new Insets(25, 30, 25, 30));
        mainContainer.setStyle("-fx-background-color: transparent;");

        // Topbar (Search & Add Music)
        HBox topbar = createTopbar(primaryStage);
        mainContainer.getChildren().add(topbar);

        // Dynamic Content Area
        contentArea = new VBox(20);
        contentArea.setStyle("-fx-background-color: transparent;");
        ScrollPane scrollPane = new ScrollPane(contentArea);
        scrollPane.setFitToWidth(true);
        scrollPane.setStyle("-fx-background: transparent; -fx-background-color: transparent; -fx-border-color: transparent;");
        VBox.setVgrow(scrollPane, Priority.ALWAYS);
        mainContainer.getChildren().add(scrollPane);

        rootLayout.setCenter(mainContainer);

        // 3. Bottom Player Bar
        HBox playerBar = createPlayerBar();
        rootLayout.setBottom(playerBar);

        // Initial view
        showHomeView();

        Scene scene = new Scene(rootLayout, 1100, 750);
        applyGlobalStyles(scene);

        primaryStage.setTitle("TuneFlow 🎵 - Personal Music System");
        primaryStage.setScene(scene);
        primaryStage.setOnCloseRequest(e -> {
            player.stop();
            Platform.exit();
            System.exit(0);
        });
        primaryStage.show();
    }

    // ==========================================
    // UI BUILDERS
    // ==========================================

    private VBox createSidebar() {
        VBox sidebar = new VBox(10);
        sidebar.setPrefWidth(240);
        sidebar.setPadding(new Insets(25, 15, 25, 15));
        sidebar.setStyle("-fx-background-color: #0b0e20; -fx-border-color: rgba(255,255,255,0.08); -fx-border-width: 0 1 0 0;");

        // Logo
        HBox logoBox = new HBox(8);
        logoBox.setAlignment(Pos.CENTER_LEFT);
        Label logoIcon = new Label("🎵");
        logoIcon.setStyle("-fx-font-size: 26px;");
        Label logoText = new Label("TuneFlow");
        logoText.setFont(Font.font("Segoe UI", FontWeight.BOLD, 22));
        logoText.setStyle("-fx-text-fill: linear-gradient(to right, #c084fc, #22d3ee);");
        logoBox.getChildren().addAll(logoIcon, logoText);
        sidebar.getChildren().add(logoBox);

        // Discover Header
        Label discoverLabel = new Label("DISCOVER");
        discoverLabel.setStyle("-fx-text-fill: #777d9a; -fx-font-size: 11px; -fx-font-weight: bold; -fx-padding: 15 0 5 10;");
        sidebar.getChildren().add(discoverLabel);

        Button homeBtn = createNavButton("🏠  Home", () -> showHomeView());
        Button libraryBtn = createNavButton("🎵  Music Library", () -> showLibraryView());
        Button artistsBtn = createNavButton("👤  Artists", () -> showArtistsView());
        Button albumsBtn = createNavButton("💿  Albums", () -> showAlbumsView());

        sidebar.getChildren().addAll(homeBtn, libraryBtn, artistsBtn, albumsBtn);

        // Your Music Header
        Label yourMusicLabel = new Label("YOUR MUSIC");
        yourMusicLabel.setStyle("-fx-text-fill: #777d9a; -fx-font-size: 11px; -fx-font-weight: bold; -fx-padding: 15 0 5 10;");
        sidebar.getChildren().add(yourMusicLabel);

        Button favoritesBtn = createNavButton("❤️  Favorites", () -> showFavoritesView());
        Button playlistsBtn = createNavButton("📁  Playlists", () -> showPlaylistsView());

        sidebar.getChildren().addAll(favoritesBtn, playlistsBtn);

        // Spacer & Database Info Badge
        Region spacer = new Region();
        VBox.setVgrow(spacer, Priority.ALWAYS);
        sidebar.getChildren().add(spacer);

        Label dbBadge = new Label("⚡ MySQL Connected");
        dbBadge.setStyle("-fx-text-fill: #34d399; -fx-font-size: 11px; -fx-background-color: rgba(52, 211, 153, 0.1); -fx-padding: 6 12; -fx-background-radius: 12;");
        sidebar.getChildren().add(dbBadge);

        return sidebar;
    }

    private Button createNavButton(String text, Runnable action) {
        Button btn = new Button(text);
        btn.setMaxWidth(Double.MAX_VALUE);
        btn.setAlignment(Pos.CENTER_LEFT);
        btn.setPadding(new Insets(10, 15, 10, 15));
        btn.setStyle("-fx-background-color: transparent; -fx-text-fill: #aeb4d0; -fx-font-size: 13px; -fx-font-weight: 500; -fx-background-radius: 8;");

        btn.setOnMouseEntered(e -> btn.setStyle("-fx-background-color: rgba(139, 92, 246, 0.15); -fx-text-fill: #ffffff; -fx-font-size: 13px; -fx-font-weight: 500; -fx-background-radius: 8;"));
        btn.setOnMouseExited(e -> btn.setStyle("-fx-background-color: transparent; -fx-text-fill: #aeb4d0; -fx-font-size: 13px; -fx-font-weight: 500; -fx-background-radius: 8;"));

        btn.setOnAction(e -> action.run());
        return btn;
    }

    private HBox createTopbar(Stage stage) {
        HBox topbar = new HBox(15);
        topbar.setAlignment(Pos.CENTER_LEFT);

        searchField = new TextField();
        searchField.setPromptText("🔍 Search songs, artists, albums...");
        searchField.setPrefWidth(420);
        searchField.setStyle("-fx-background-color: rgba(255,255,255,0.06); -fx-text-fill: white; -fx-prompt-text-fill: #737993; -fx-background-radius: 20; -fx-border-color: rgba(255,255,255,0.1); -fx-border-radius: 20; -fx-padding: 10 18;");

        searchField.textProperty().addListener((obs, oldVal, newVal) -> {
            filterSongs(newVal);
        });

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Button addMusicBtn = new Button("＋ Add Music");
        addMusicBtn.setStyle("-fx-background-color: linear-gradient(to right, #7c3aed, #06b6d4); -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 20; -fx-padding: 10 20; -fx-cursor: hand;");
        addMusicBtn.setOnAction(e -> openAddMusicDialog(stage));

        topbar.getChildren().addAll(searchField, spacer, addMusicBtn);
        return topbar;
    }

    private HBox createPlayerBar() {
        HBox bar = new HBox(20);
        bar.setPrefHeight(90);
        bar.setAlignment(Pos.CENTER_LEFT);
        bar.setPadding(new Insets(10, 25, 10, 25));
        bar.setStyle("-fx-background-color: rgba(10, 14, 30, 0.95); -fx-border-color: rgba(255,255,255,0.08); -fx-border-width: 1 0 0 0;");

        // 1. Left: Track Info
        HBox infoBox = new HBox(12);
        infoBox.setPrefWidth(260);
        infoBox.setAlignment(Pos.CENTER_LEFT);

        StackPane coverArt = new StackPane();
        coverArt.setPrefSize(48, 48);
        coverArt.setStyle("-fx-background-color: linear-gradient(to bottom right, #7c3aed, #06b6d4); -fx-background-radius: 8;");
        Label icon = new Label("♪");
        icon.setStyle("-fx-font-size: 20px; -fx-text-fill: white;");
        coverArt.getChildren().add(icon);

        VBox metaBox = new VBox(3);
        metaBox.setAlignment(Pos.CENTER_LEFT);
        nowTitleLabel = new Label("Nothing Playing");
        nowTitleLabel.setFont(Font.font("Segoe UI", FontWeight.BOLD, 13));
        nowTitleLabel.setStyle("-fx-text-fill: white;");

        nowArtistLabel = new Label("Select a song to start");
        nowArtistLabel.setStyle("-fx-text-fill: #777f9e; -fx-font-size: 11px;");

        metaBox.getChildren().addAll(nowTitleLabel, nowArtistLabel);
        infoBox.getChildren().addAll(coverArt, metaBox);

        // 2. Center: Controls & Progress
        VBox controlsBox = new VBox(6);
        controlsBox.setAlignment(Pos.CENTER);
        HBox.setHgrow(controlsBox, Priority.ALWAYS);

        HBox btnRow = new HBox(15);
        btnRow.setAlignment(Pos.CENTER);

        Button prevBtn = new Button("⏮");
        styleIconButton(prevBtn);
        prevBtn.setOnAction(e -> player.previous());

        playPauseBtn = new Button("▶");
        playPauseBtn.setStyle("-fx-background-color: linear-gradient(to right, #a855f7, #06b6d4); -fx-text-fill: white; -fx-font-size: 16px; -fx-font-weight: bold; -fx-pref-width: 40; -fx-pref-height: 40; -fx-background-radius: 20; -fx-cursor: hand;");
        playPauseBtn.setOnAction(e -> player.togglePlayPause());

        Button nextBtn = new Button("⏭");
        styleIconButton(nextBtn);
        nextBtn.setOnAction(e -> player.next());

        btnRow.getChildren().addAll(prevBtn, playPauseBtn, nextBtn);

        HBox progressRow = new HBox(10);
        progressRow.setAlignment(Pos.CENTER);

        currentTimeLabel = new Label("0:00");
        currentTimeLabel.setStyle("-fx-text-fill: #737993; -fx-font-size: 11px;");

        progressBar = new Slider(0, 100, 0);
        progressBar.setPrefWidth(450);
        progressBar.setStyle("-fx-accent: #a855f7;");
        progressBar.valueProperty().addListener((obs, oldVal, newVal) -> {
            if (progressBar.isValueChanging() && player.getCurrentSong() != null) {
                int seekSeconds = (int) (newVal.doubleValue() / 100.0 * player.getCurrentSong().getDurationSeconds());
                player.seek(seekSeconds);
            }
        });

        totalTimeLabel = new Label("0:00");
        totalTimeLabel.setStyle("-fx-text-fill: #737993; -fx-font-size: 11px;");

        progressRow.getChildren().addAll(currentTimeLabel, progressBar, totalTimeLabel);
        controlsBox.getChildren().addAll(btnRow, progressRow);

        // 3. Right: Volume
        HBox volumeBox = new HBox(8);
        volumeBox.setPrefWidth(220);
        volumeBox.setAlignment(Pos.CENTER_RIGHT);

        Label volIcon = new Label("🔊");
        volIcon.setStyle("-fx-text-fill: #aeb4d0; -fx-font-size: 14px;");

        volumeSlider = new Slider(0, 1, 0.8);
        volumeSlider.setPrefWidth(90);
        volumeSlider.setStyle("-fx-accent: #22d3ee;");
        volumeSlider.valueProperty().addListener((obs, oldVal, newVal) -> player.setVolume(newVal.doubleValue()));

        volumeBox.getChildren().addAll(volIcon, volumeSlider);

        bar.getChildren().addAll(infoBox, controlsBox, volumeBox);
        return bar;
    }

    private void styleIconButton(Button btn) {
        btn.setStyle("-fx-background-color: rgba(255,255,255,0.06); -fx-text-fill: #c7cbe0; -fx-font-size: 14px; -fx-pref-width: 34; -fx-pref-height: 34; -fx-background-radius: 17; -fx-cursor: hand;");
        btn.setOnMouseEntered(e -> btn.setStyle("-fx-background-color: rgba(139,92,246,0.3); -fx-text-fill: white; -fx-font-size: 14px; -fx-pref-width: 34; -fx-pref-height: 34; -fx-background-radius: 17; -fx-cursor: hand;"));
        btn.setOnMouseExited(e -> btn.setStyle("-fx-background-color: rgba(255,255,255,0.06); -fx-text-fill: #c7cbe0; -fx-font-size: 14px; -fx-pref-width: 34; -fx-pref-height: 34; -fx-background-radius: 17; -fx-cursor: hand;"));
    }

    // ==========================================
    // VIEWS
    // ==========================================

    private void showHomeView() {
        currentSection = "home";
        contentArea.getChildren().clear();

        // Hero Banner
        VBox hero = new VBox(8);
        hero.setPadding(new Insets(30));
        hero.setStyle("-fx-background-color: linear-gradient(to bottom right, rgba(124,58,237,0.3), rgba(15,23,42,0.95)); -fx-background-radius: 20; -fx-border-color: rgba(255,255,255,0.08); -fx-border-radius: 20;");

        Label heroTitle = new Label("Welcome to TuneFlow 🎵");
        heroTitle.setFont(Font.font("Segoe UI", FontWeight.BOLD, 28));
        heroTitle.setStyle("-fx-text-fill: linear-gradient(to right, #c084fc, #22d3ee);");

        Label heroSubtitle = new Label("Your personal music library powered by Java OOP, JavaFX, and MySQL.\nBrowse songs, create custom playlists, mark favorites, and enjoy smooth playback.");
        heroSubtitle.setStyle("-fx-text-fill: #aeb4d0; -fx-font-size: 13px; -fx-line-spacing: 4;");

        hero.getChildren().addAll(heroTitle, heroSubtitle);
        contentArea.getChildren().add(hero);

        // Section Title
        HBox secHeader = new HBox();
        Label secTitle = new Label("Your Songs");
        secTitle.setFont(Font.font("Segoe UI", FontWeight.BOLD, 18));
        secTitle.setStyle("-fx-text-fill: white;");

        List<Song> songs = library.getAllSongs();
        Label countLabel = new Label(songs.size() + " songs");
        countLabel.setStyle("-fx-text-fill: #8c94b3; -fx-font-size: 13px; -fx-padding: 3 0 0 10;");

        secHeader.getChildren().addAll(secTitle, countLabel);
        contentArea.getChildren().add(secHeader);

        // Song List
        VBox songList = createSongList(songs);
        contentArea.getChildren().add(songList);
    }

    private void showLibraryView() {
        currentSection = "library";
        contentArea.getChildren().clear();

        HBox secHeader = new HBox();
        Label secTitle = new Label("🎵 Music Library");
        secTitle.setFont(Font.font("Segoe UI", FontWeight.BOLD, 22));
        secTitle.setStyle("-fx-text-fill: white;");

        List<Song> songs = library.getAllSongs();
        Label countLabel = new Label(songs.size() + " songs available in MySQL database");
        countLabel.setStyle("-fx-text-fill: #8c94b3; -fx-font-size: 13px; -fx-padding: 5 0 0 12;");

        secHeader.getChildren().addAll(secTitle, countLabel);
        contentArea.getChildren().addAll(secHeader, createSongList(songs));
    }

    private void showArtistsView() {
        currentSection = "artists";
        contentArea.getChildren().clear();

        Label secTitle = new Label("👤 Artists");
        secTitle.setFont(Font.font("Segoe UI", FontWeight.BOLD, 22));
        secTitle.setStyle("-fx-text-fill: white;");
        contentArea.getChildren().add(secTitle);

        FlowPane grid = new FlowPane();
        grid.setHgap(20);
        grid.setVgap(20);

        List<Artist> artists = library.getAllArtists();
        for (Artist a : artists) {
            VBox card = new VBox(10);
            card.setPrefSize(180, 180);
            card.setAlignment(Pos.CENTER);
            card.setStyle("-fx-background-color: rgba(255,255,255,0.04); -fx-background-radius: 16; -fx-border-color: rgba(255,255,255,0.06); -fx-border-radius: 16; -fx-cursor: hand;");

            Label icon = new Label("👤");
            icon.setStyle("-fx-font-size: 40px;");

            Label name = new Label(a.getName());
            name.setFont(Font.font("Segoe UI", FontWeight.BOLD, 14));
            name.setStyle("-fx-text-fill: white;");

            Label count = new Label(a.getSongCount() + " songs");
            count.setStyle("-fx-text-fill: #858ca8; -fx-font-size: 12px;");

            card.getChildren().addAll(icon, name, count);
            card.setOnMouseClicked(e -> {
                searchField.setText(a.getName());
                showLibraryView();
            });
            grid.getChildren().add(card);
        }

        contentArea.getChildren().add(grid);
    }

    private void showAlbumsView() {
        currentSection = "albums";
        contentArea.getChildren().clear();

        Label secTitle = new Label("💿 Albums");
        secTitle.setFont(Font.font("Segoe UI", FontWeight.BOLD, 22));
        secTitle.setStyle("-fx-text-fill: white;");
        contentArea.getChildren().add(secTitle);

        FlowPane grid = new FlowPane();
        grid.setHgap(20);
        grid.setVgap(20);

        List<Album> albums = library.getAllAlbums();
        for (Album a : albums) {
            VBox card = new VBox(10);
            card.setPrefSize(180, 190);
            card.setAlignment(Pos.CENTER);
            card.setStyle("-fx-background-color: rgba(255,255,255,0.04); -fx-background-radius: 16; -fx-border-color: rgba(255,255,255,0.06); -fx-border-radius: 16; -fx-cursor: hand;");

            Label icon = new Label("💿");
            icon.setStyle("-fx-font-size: 40px;");

            Label title = new Label(a.getTitle());
            title.setFont(Font.font("Segoe UI", FontWeight.BOLD, 14));
            title.setStyle("-fx-text-fill: white;");

            Label meta = new Label(a.getArtist() + "\n" + a.getTrackCount() + " tracks (" + a.getReleaseYear() + ")");
            meta.setAlignment(Pos.CENTER);
            meta.setStyle("-fx-text-fill: #858ca8; -fx-font-size: 11px; -fx-text-alignment: center;");

            card.getChildren().addAll(icon, title, meta);
            card.setOnMouseClicked(e -> {
                searchField.setText(a.getTitle());
                showLibraryView();
            });
            grid.getChildren().add(card);
        }

        contentArea.getChildren().add(grid);
    }

    private void showFavoritesView() {
        currentSection = "favorites";
        contentArea.getChildren().clear();

        Label secTitle = new Label("❤️ Favorite Tracks");
        secTitle.setFont(Font.font("Segoe UI", FontWeight.BOLD, 22));
        secTitle.setStyle("-fx-text-fill: white;");

        List<Song> favs = library.getFavorites();
        contentArea.getChildren().addAll(secTitle, createSongList(favs));
    }

    private void showPlaylistsView() {
        currentSection = "playlists";
        contentArea.getChildren().clear();

        HBox header = new HBox(15);
        header.setAlignment(Pos.CENTER_LEFT);

        Label secTitle = new Label("📁 Playlists");
        secTitle.setFont(Font.font("Segoe UI", FontWeight.BOLD, 22));
        secTitle.setStyle("-fx-text-fill: white;");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Button newPlBtn = new Button("＋ New Playlist");
        newPlBtn.setStyle("-fx-background-color: linear-gradient(to right, #7c3aed, #06b6d4); -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 18; -fx-padding: 8 16; -fx-cursor: hand;");
        newPlBtn.setOnAction(e -> openCreatePlaylistDialog());

        header.getChildren().addAll(secTitle, spacer, newPlBtn);
        contentArea.getChildren().add(header);

        FlowPane grid = new FlowPane();
        grid.setHgap(20);
        grid.setVgap(20);

        List<Playlist> playlists = library.getAllPlaylists();
        for (Playlist p : playlists) {
            VBox card = new VBox(10);
            card.setPrefSize(200, 210);
            card.setAlignment(Pos.CENTER);
            card.setPadding(new Insets(15));
            card.setStyle("-fx-background-color: rgba(255,255,255,0.04); -fx-background-radius: 16; -fx-border-color: rgba(255,255,255,0.06); -fx-border-radius: 16;");

            Label icon = new Label("🎶");
            icon.setStyle("-fx-font-size: 38px;");

            Label name = new Label(p.getName());
            name.setFont(Font.font("Segoe UI", FontWeight.BOLD, 14));
            name.setStyle("-fx-text-fill: white;");

            Label count = new Label(p.getSongCount() + " songs • " + p.getFormattedTotalDuration());
            count.setStyle("-fx-text-fill: #858ca8; -fx-font-size: 11px;");

            HBox cardBtns = new HBox(8);
            cardBtns.setAlignment(Pos.CENTER);

            Button playAll = new Button("▶ Play");
            playAll.setStyle("-fx-background-color: rgba(139,92,246,0.3); -fx-text-fill: white; -fx-background-radius: 12; -fx-font-size: 11px; -fx-padding: 5 12; -fx-cursor: hand;");
            playAll.setOnAction(e -> {
                if (!p.getSongs().isEmpty()) {
                    player.setQueue(p.getSongs(), 0);
                } else {
                    showAlert("Empty Playlist", "Add songs to this playlist before playing!");
                }
            });

            Button addSongBtn = new Button("＋ Song");
            addSongBtn.setStyle("-fx-background-color: rgba(255,255,255,0.08); -fx-text-fill: #cbd0e5; -fx-background-radius: 12; -fx-font-size: 11px; -fx-padding: 5 12; -fx-cursor: hand;");
            addSongBtn.setOnAction(e -> openAddToPlaylistDialog(p));

            cardBtns.getChildren().addAll(playAll, addSongBtn);
            card.getChildren().addAll(icon, name, count, cardBtns);
            grid.getChildren().add(card);
        }

        contentArea.getChildren().add(grid);
    }

    private void filterSongs(String query) {
        List<Song> filtered = library.searchSongs(query);
        contentArea.getChildren().clear();

        Label label = new Label("Search results for: \"" + query + "\" (" + filtered.size() + " matches)");
        label.setStyle("-fx-text-fill: #aeb4d0; -fx-font-size: 14px; -fx-padding: 5 0;");
        contentArea.getChildren().addAll(label, createSongList(filtered));
    }

    private VBox createSongList(List<Song> songs) {
        VBox list = new VBox(8);
        if (songs.isEmpty()) {
            VBox emptyBox = new VBox(10);
            emptyBox.setAlignment(Pos.CENTER);
            emptyBox.setPadding(new Insets(50));
            Label emptyIcon = new Label("🎵");
            emptyIcon.setStyle("-fx-font-size: 48px;");
            Label emptyText = new Label("No tracks found");
            emptyText.setStyle("-fx-text-fill: #cbd0e5; -fx-font-size: 16px; -fx-font-weight: bold;");
            Label emptySub = new Label("Add some music to your TuneFlow collection.");
            emptySub.setStyle("-fx-text-fill: #737993; -fx-font-size: 13px;");
            emptyBox.getChildren().addAll(emptyIcon, emptyText, emptySub);
            list.getChildren().add(emptyBox);
            return list;
        }

        for (int i = 0; i < songs.size(); i++) {
            final int index = i;
            Song song = songs.get(i);

            HBox row = new HBox(15);
            row.setAlignment(Pos.CENTER_LEFT);
            row.setPadding(new Insets(10, 15, 10, 15));
            row.setStyle("-fx-background-color: rgba(255,255,255,0.035); -fx-background-radius: 12; -fx-border-color: rgba(255,255,255,0.04); -fx-border-radius: 12;");

            row.setOnMouseEntered(e -> row.setStyle("-fx-background-color: rgba(139,92,246,0.12); -fx-background-radius: 12; -fx-border-color: rgba(139,92,246,0.25); -fx-border-radius: 12;"));
            row.setOnMouseExited(e -> row.setStyle("-fx-background-color: rgba(255,255,255,0.035); -fx-background-radius: 12; -fx-border-color: rgba(255,255,255,0.04); -fx-border-radius: 12;"));

            // Cover Box
            StackPane songArt = new StackPane();
            songArt.setPrefSize(44, 44);
            songArt.setStyle("-fx-background-color: linear-gradient(to bottom right, #7c3aed, #06b6d4); -fx-background-radius: 8;");
            Label songIcon = new Label("♪");
            songIcon.setStyle("-fx-text-fill: white; -fx-font-size: 18px;");
            songArt.getChildren().add(songIcon);

            // Title & Meta
            VBox info = new VBox(4);
            info.setAlignment(Pos.CENTER_LEFT);
            HBox.setHgrow(info, Priority.ALWAYS);

            Label titleLabel = new Label(song.getTitle());
            titleLabel.setFont(Font.font("Segoe UI", FontWeight.BOLD, 14));
            titleLabel.setStyle("-fx-text-fill: white;");

            Label metaLabel = new Label(song.getArtist() + " • " + song.getAlbum() + " • " + song.getGenre());
            metaLabel.setStyle("-fx-text-fill: #8188a5; -fx-font-size: 12px;");

            info.getChildren().addAll(titleLabel, metaLabel);

            // Duration
            Label durLabel = new Label(song.getFormattedDuration());
            durLabel.setStyle("-fx-text-fill: #8188a5; -fx-font-size: 12px; -fx-padding: 0 10;");

            // Actions (Heart, Play, Delete)
            Button favBtn = new Button(song.isFavorite() ? "❤️" : "🤍");
            favBtn.setStyle("-fx-background-color: transparent; -fx-font-size: 14px; -fx-cursor: hand;");
            favBtn.setOnAction(e -> {
                boolean newFav = library.toggleFavorite(song.getId());
                song.setFavorite(newFav);
                favBtn.setText(newFav ? "❤️" : "🤍");
                if ("favorites".equals(currentSection)) {
                    showFavoritesView();
                }
            });

            Button playBtn = new Button("▶");
            styleIconButton(playBtn);
            playBtn.setOnAction(e -> {
                player.setQueue(songs, index);
            });

            Button delBtn = new Button("🗑");
            styleIconButton(delBtn);
            delBtn.setOnAction(e -> {
                library.deleteSong(song.getId());
                refreshCurrentView();
            });

            row.getChildren().addAll(songArt, info, durLabel, favBtn, playBtn, delBtn);
            list.getChildren().add(row);
        }

        return list;
    }

    private void refreshCurrentView() {
        switch (currentSection) {
            case "home": showHomeView(); break;
            case "library": showLibraryView(); break;
            case "artists": showArtistsView(); break;
            case "albums": showAlbumsView(); break;
            case "favorites": showFavoritesView(); break;
            case "playlists": showPlaylistsView(); break;
            default: showHomeView();
        }
    }

    // ==========================================
    // DIALOGS
    // ==========================================

    private void openAddMusicDialog(Stage owner) {
        Stage dialog = new Stage();
        dialog.initModality(Modality.WINDOW_MODAL);
        dialog.initOwner(owner);
        dialog.setTitle("🎵 Add Music to TuneFlow");

        VBox form = new VBox(15);
        form.setPadding(new Insets(25));
        form.setStyle("-fx-background-color: #11152a;");

        Label heading = new Label("🎵 Add New Music");
        heading.setFont(Font.font("Segoe UI", FontWeight.BOLD, 18));
        heading.setStyle("-fx-text-fill: white;");

        // Audio File Chooser
        Label fileLabel = new Label("Audio File (Optional MP3/WAV)");
        fileLabel.setStyle("-fx-text-fill: #9da4c1; -fx-font-size: 12px;");

        HBox fileBox = new HBox(10);
        TextField filePathField = new TextField();
        filePathField.setPromptText("Browse or leave empty for virtual track");
        filePathField.setPrefWidth(300);
        styleInput(filePathField);

        Button browseBtn = new Button("Browse...");
        browseBtn.setStyle("-fx-background-color: rgba(255,255,255,0.1); -fx-text-fill: white; -fx-background-radius: 8; -fx-padding: 8 14; -fx-cursor: hand;");
        browseBtn.setOnAction(e -> {
            FileChooser chooser = new FileChooser();
            chooser.setTitle("Select Audio File");
            chooser.getExtensionFilters().addAll(
                    new FileChooser.ExtensionFilter("Audio Files (*.mp3, *.wav, *.m4a)", "*.mp3", "*.wav", "*.m4a"),
                    new FileChooser.ExtensionFilter("All Files", "*.*")
            );
            File selectedFile = chooser.showOpenDialog(dialog);
            if (selectedFile != null) {
                filePathField.setText(selectedFile.getAbsolutePath());
            }
        });
        fileBox.getChildren().addAll(filePathField, browseBtn);

        // Inputs
        TextField titleField = createFormField(form, "Song Title *", "e.g. Starboy");
        TextField artistField = createFormField(form, "Artist Name *", "e.g. The Weeknd");
        TextField albumField = createFormField(form, "Album Name *", "e.g. Starboy");
        TextField genreField = createFormField(form, "Genre", "e.g. Pop, Synthwave, Rock");

        HBox btnBox = new HBox(12);
        btnBox.setAlignment(Pos.CENTER_RIGHT);

        Button cancelBtn = new Button("Cancel");
        cancelBtn.setStyle("-fx-background-color: rgba(255,255,255,0.08); -fx-text-fill: #cbd0e5; -fx-background-radius: 8; -fx-padding: 8 16; -fx-cursor: hand;");
        cancelBtn.setOnAction(e -> dialog.close());

        Button saveBtn = new Button("Add Song");
        saveBtn.setStyle("-fx-background-color: linear-gradient(to right, #7c3aed, #06b6d4); -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 8; -fx-padding: 8 20; -fx-cursor: hand;");
        saveBtn.setOnAction(e -> {
            String title = titleField.getText().trim();
            String artist = artistField.getText().trim();
            String album = albumField.getText().trim();
            String genre = genreField.getText().trim();
            String path = filePathField.getText().trim();

            if (title.isEmpty() || artist.isEmpty() || album.isEmpty()) {
                showAlert("Missing Information", "Please provide Title, Artist, and Album.");
                return;
            }

            int id = library.addSong(title, artist, album, genre, 210, path.isEmpty() ? null : path);
            if (id > 0) {
                dialog.close();
                refreshCurrentView();
            } else {
                showAlert("Error", "Could not save song to MySQL database.");
            }
        });

        btnBox.getChildren().addAll(cancelBtn, saveBtn);
        form.getChildren().addAll(heading, fileLabel, fileBox, btnBox);

        Scene scene = new Scene(form, 450, 480);
        dialog.setScene(scene);
        dialog.show();
    }

    private void openCreatePlaylistDialog() {
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("New Playlist");
        dialog.setHeaderText("Create a New Playlist");
        dialog.setContentText("Playlist Name:");

        dialog.showAndWait().ifPresent(name -> {
            if (!name.trim().isEmpty()) {
                library.createPlaylist(name.trim());
                showPlaylistsView();
            }
        });
    }

    private void openAddToPlaylistDialog(Playlist playlist) {
        Stage dialog = new Stage();
        dialog.initModality(Modality.APPLICATION_MODAL);
        dialog.setTitle("Add Song to " + playlist.getName());

        VBox layout = new VBox(15);
        layout.setPadding(new Insets(20));
        layout.setStyle("-fx-background-color: #11152a;");

        Label label = new Label("Select song to add to \"" + playlist.getName() + "\":");
        label.setStyle("-fx-text-fill: white; -fx-font-size: 14px;");

        ListView<Song> listView = new ListView<>();
        listView.setStyle("-fx-background-color: #181d38; -fx-control-inner-background: #181d38; -fx-text-fill: white;");
        listView.getItems().addAll(library.getAllSongs());

        Button addBtn = new Button("Add to Playlist");
        addBtn.setStyle("-fx-background-color: linear-gradient(to right, #7c3aed, #06b6d4); -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 8; -fx-padding: 8 16; -fx-cursor: hand;");
        addBtn.setOnAction(e -> {
            Song selected = listView.getSelectionModel().getSelectedItem();
            if (selected != null) {
                library.addSongToPlaylist(playlist.getId(), selected.getId());
                dialog.close();
                showPlaylistsView();
            }
        });

        layout.getChildren().addAll(label, listView, addBtn);
        Scene scene = new Scene(layout, 400, 450);
        dialog.setScene(scene);
        dialog.show();
    }

    private TextField createFormField(VBox container, String labelText, String placeholder) {
        Label lbl = new Label(labelText);
        lbl.setStyle("-fx-text-fill: #9da4c1; -fx-font-size: 12px;");
        TextField tf = new TextField();
        tf.setPromptText(placeholder);
        styleInput(tf);
        container.getChildren().addAll(lbl, tf);
        return tf;
    }

    private void styleInput(TextField tf) {
        tf.setStyle("-fx-background-color: rgba(255,255,255,0.06); -fx-text-fill: white; -fx-prompt-text-fill: #616782; -fx-background-radius: 8; -fx-border-color: rgba(255,255,255,0.1); -fx-border-radius: 8; -fx-padding: 8 12;");
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    // ==========================================
    // MUSIC PLAYER LISTENERS
    // ==========================================

    @Override
    public void onSongChanged(Song song) {
        Platform.runLater(() -> {
            if (song != null) {
                nowTitleLabel.setText(song.getTitle());
                nowArtistLabel.setText(song.getArtist());
                totalTimeLabel.setText(song.getFormattedDuration());
                playPauseBtn.setText("⏸");
            } else {
                nowTitleLabel.setText("Nothing Playing");
                nowArtistLabel.setText("Select a song");
                playPauseBtn.setText("▶");
            }
        });
    }

    @Override
    public void onStateChanged(MusicPlayer.PlayerState state) {
        Platform.runLater(() -> {
            if (state == MusicPlayer.PlayerState.PLAYING) {
                playPauseBtn.setText("⏸");
            } else {
                playPauseBtn.setText("▶");
            }
        });
    }

    @Override
    public void onProgressUpdate(int currentSeconds, int totalSeconds) {
        Platform.runLater(() -> {
            currentTimeLabel.setText(String.format("%d:%02d", currentSeconds / 60, currentSeconds % 60));
            if (totalSeconds > 0) {
                double pct = ((double) currentSeconds / totalSeconds) * 100.0;
                progressBar.setValue(pct);
            }
        });
    }

    private void applyGlobalStyles(Scene scene) {
        scene.setFill(Color.web("#080b18"));
    }

    public static void main(String[] args) {
        launch(args);
    }
}
