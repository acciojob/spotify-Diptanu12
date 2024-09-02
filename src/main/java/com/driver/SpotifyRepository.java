package com.driver;

import java.util.*;

import org.springframework.stereotype.Repository;

@Repository
public class SpotifyRepository {
    private HashMap<Artist, List<Album>> artistAlbumMap;
    private HashMap<Album, List<Song>> albumSongMap;
    private HashMap<Playlist, List<Song>> playlistSongMap;
    private HashMap<Playlist, List<User>> playlistListenerMap;
    private HashMap<User, Playlist> creatorPlaylistMap;
    private HashMap<User, List<Playlist>> userPlaylistMap;
    private HashMap<Song, List<User>> songLikeMap;

    private List<User> users;
    private List<Song> songs;
    private List<Playlist> playlists;
    private List<Album> albums;
    private List<Artist> artists;

    public SpotifyRepository() {
        artistAlbumMap = new HashMap<>();
        albumSongMap = new HashMap<>();
        playlistSongMap = new HashMap<>();
        playlistListenerMap = new HashMap<>();
        creatorPlaylistMap = new HashMap<>();
        userPlaylistMap = new HashMap<>();
        songLikeMap = new HashMap<>();

        users = new ArrayList<>();
        songs = new ArrayList<>();
        playlists = new ArrayList<>();
        albums = new ArrayList<>();
        artists = new ArrayList<>();
    }

    public User createUser(String name, String mobile) {
        User user = new User(name, mobile);
        users.add(user);
        return user;
    }

    public Artist createArtist(String name) {
        Artist artist = new Artist(name);
        artists.add(artist);
        artistAlbumMap.put(artist, new ArrayList<>());
        return artist;
    }

    public Album createAlbum(String title, String artistName) {
        Artist artist = findArtist(artistName);
        if (artist == null) {
            artist = createArtist(artistName);
        }
        Album album = new Album(title);
        albums.add(album);
        albumSongMap.put(album, new ArrayList<>());
        artistAlbumMap.get(artist).add(album);
        return album;
    }

    public Song createSong(String title, String albumName, int length) throws Exception {
        Album album = findAlbum(albumName);
        if (album == null) {
            throw new Exception("Album does not exist");
        }
        Song song = new Song(title, length);
        songs.add(song);
        albumSongMap.get(album).add(song);
        songLikeMap.put(song, new ArrayList<>());
        return song;
    }

    public Playlist createPlaylistOnLength(String mobile, String title, int length) throws Exception {
        User user = findUser(mobile);
        if (user == null) {
            throw new Exception("User does not exist");
        }
        Playlist playlist = new Playlist(title);
        playlists.add(playlist);
        List<Song> songsByLength = findSongsByLength(length);
        playlistSongMap.put(playlist, songsByLength);
        playlistListenerMap.put(playlist, new ArrayList<>(Collections.singletonList(user)));
        creatorPlaylistMap.put(user, playlist);
        userPlaylistMap.computeIfAbsent(user, k -> new ArrayList<>()).add(playlist);
        return playlist;
    }

    public Playlist createPlaylistOnName(String mobile, String title, List<String> songTitles) throws Exception {
        User user = findUser(mobile);
        if (user == null) {
            throw new Exception("User does not exist");
        }
        Playlist playlist = new Playlist(title);
        playlists.add(playlist);
        List<Song> songsByTitles = findSongsByTitles(songTitles);
        playlistSongMap.put(playlist, songsByTitles);
        playlistListenerMap.put(playlist, new ArrayList<>(Collections.singletonList(user)));
        creatorPlaylistMap.put(user, playlist);
        userPlaylistMap.computeIfAbsent(user, k -> new ArrayList<>()).add(playlist);
        return playlist;
    }

    public Playlist findPlaylist(String mobile, String playlistTitle) throws Exception {
        User user = findUser(mobile);
        if (user == null) {
            throw new Exception("User does not exist");
        }
        Playlist playlist = findPlaylistByTitle(playlistTitle);
        if (playlist == null) {
            throw new Exception("Playlist does not exist");
        }
        List<User> listeners = playlistListenerMap.get(playlist);
        if (listeners == null) {
            listeners = new ArrayList<>();
        }
        if (!listeners.contains(user) && !creatorPlaylistMap.containsKey(user)) {
            listeners.add(user);
            playlistListenerMap.put(playlist, listeners);
        }
        return playlist;
    }

    public Song likeSong(String mobile, String songTitle) throws Exception {
        User user = findUser(mobile);
        if (user == null) {
            throw new Exception("User does not exist");
        }
        Song song = findSong(songTitle);
        if (song == null) {
            throw new Exception("Song does not exist");
        }
        List<User> likedBy = songLikeMap.get(song);
        if (!likedBy.contains(user)) {
            likedBy.add(user);
            songLikeMap.put(song, likedBy);
            // Also like the artist
            Artist artist = findArtistBySong(song);
            if (artist != null) {
                List<User> artistLikes = songLikeMap.getOrDefault(artist, new ArrayList<>());
                if (!artistLikes.contains(user)) {
                    artistLikes.add(user);
                    songLikeMap.put(artist, artistLikes);
                }
            }
        }
        return song;
    }

    public String mostPopularArtist() {
        return artistLikesCount().entrySet().stream()
                .max(Map.Entry.comparingByValue())
                .map(Map.Entry::getKey)
                .orElse(null);
    }

    public String mostPopularSong() {
        return songLikesCount().entrySet().stream()
                .max(Map.Entry.comparingByValue())
                .map(Map.Entry::getKey)
                .orElse(null);
    }

    private User findUser(String mobile) {
        return users.stream()
                .filter(u -> u.getMobile().equals(mobile))
                .findFirst()
                .orElse(null);
    }

    private Artist findArtist(String name) {
        return artists.stream()
                .filter(a -> a.getName().equals(name))
                .findFirst()
                .orElse(null);
    }

    private Album findAlbum(String title) {
        return albums.stream()
                .filter(a -> a.getTitle().equals(title))
                .findFirst()
                .orElse(null);
    }

    private Song findSong(String title) {
        return songs.stream()
                .filter(s -> s.getTitle().equals(title))
                .findFirst()
                .orElse(null);
    }

    private Playlist findPlaylistByTitle(String title) {
        return playlists.stream()
                .filter(p -> p.getTitle().equals(title))
                .findFirst()
                .orElse(null);
    }

    private List<Song> findSongsByLength(int length) {
        return songs.stream()
                .filter(s -> s.getLength() == length)
                .toList();
    }

    private List<Song> findSongsByTitles(List<String> titles) {
        return songs.stream()
                .filter(s -> titles.contains(s.getTitle()))
                .toList();
    }

    private Artist findArtistBySong(Song song) {
        return artistAlbumMap.entrySet().stream()
                .filter(entry -> entry.getValue().stream()
                        .anyMatch(album -> albumSongMap.get(album).contains(song)))
                .map(Map.Entry::getKey)
                .findFirst()
                .orElse(null);
    }

    private Map<Artist, Integer> artistLikesCount() {
        Map<Artist, Integer> artistLikes = new HashMap<>();
        for (Song song : songs) {
            Artist artist = findArtistBySong(song);
            if (artist != null) {
                artistLikes.put(artist, artistLikes.getOrDefault(artist, 0) + songLikeMap.getOrDefault(song, new ArrayList<>()).size());
            }
        }
        return artistLikes;
    }

    private Map<Song, Integer> songLikesCount() {
        Map<Song, Integer> songLikes = new HashMap<>();
        for (Song song : songs) {
            songLikes.put(song, songLikeMap.getOrDefault(song, new ArrayList<>()).size());
        }
        return songLikes;
    }
}
