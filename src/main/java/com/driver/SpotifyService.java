package com.driver;

import java.util.*;

import org.springframework.stereotype.Service;

@Service
public class SpotifyService {

    private SpotifyRepository spotifyRepository = new SpotifyRepository();

    public User createUser(String name, String mobile) {
        User user = new User(name, mobile);
        spotifyRepository.getUserMap().put(mobile, user);
        return user;
    }

    public Artist createArtist(String name) {
        Artist artist = new Artist(name);
        spotifyRepository.getArtistMap().put(name, artist);
        return artist;
    }

    public Album createAlbum(String title, String artistName) {
        Artist artist = spotifyRepository.getArtistMap().get(artistName);
        if (artist == null) {
            artist = createArtist(artistName);
        }
        Album album = new Album(title);
        spotifyRepository.getAlbumMap().put(title, album);
        return album;
    }

    public Song createSong(String title, String albumName, int length) throws Exception {
        Album album = spotifyRepository.getAlbumMap().get(albumName);
        if (album == null) {
            throw new Exception("Album does not exist");
        }
        Song song = new Song(title, length);
        spotifyRepository.getSongMap().put(title, song);
        return song;
    }

    public Playlist createPlaylistOnLength(String mobile, String title, int length) throws Exception {
        User user = spotifyRepository.getUserMap().get(mobile);
        if (user == null) {
            throw new Exception("User does not exist");
        }
        Playlist playlist = new Playlist(title);
        List<Song> songs = new ArrayList<>();
        for (Song song : spotifyRepository.getSongMap().values()) {
            if (song.getLength() == length) {
                songs.add(song);
            }
        }
        spotifyRepository.getPlaylistMap().put(title, playlist);
        spotifyRepository.getPlaylistSongsMap().put(title, songs);
        spotifyRepository.getPlaylistListenersMap().put(title, Collections.singletonList(user));
        return playlist;
    }

    public Playlist createPlaylistOnName(String mobile, String title, List<String> songTitles) throws Exception {
        User user = spotifyRepository.getUserMap().get(mobile);
        if (user == null) {
            throw new Exception("User does not exist");
        }
        Playlist playlist = new Playlist(title);
        List<Song> songs = new ArrayList<>();
        for (String songTitle : songTitles) {
            Song song = spotifyRepository.getSongMap().get(songTitle);
            if (song != null) {
                songs.add(song);
            }
        }
        spotifyRepository.getPlaylistMap().put(title, playlist);
        spotifyRepository.getPlaylistSongsMap().put(title, songs);
        spotifyRepository.getPlaylistListenersMap().put(title, Collections.singletonList(user));
        return playlist;
    }

    public Playlist findPlaylist(String mobile, String playlistTitle) throws Exception {
        User user = spotifyRepository.getUserMap().get(mobile);
        if (user == null) {
            throw new Exception("User does not exist");
        }
        Playlist playlist = spotifyRepository.getPlaylistMap().get(playlistTitle);
        if (playlist == null) {
            throw new Exception("Playlist does not exist");
        }
        List<User> listeners = spotifyRepository.getPlaylistListenersMap().get(playlistTitle);
        if (!listeners.contains(user) && !user.equals(spotifyRepository.getPlaylistMap().get(playlistTitle))) {
            listeners.add(user);
        }
        return playlist;
    }

    public Song likeSong(String mobile, String songTitle) throws Exception {
        User user = spotifyRepository.getUserMap().get(mobile);
        if (user == null) {
            throw new Exception("User does not exist");
        }
        Song song = spotifyRepository.getSongMap().get(songTitle);
        if (song == null) {
            throw new Exception("Song does not exist");
        }
        Set<User> likedUsers = spotifyRepository.getSongLikesMap().getOrDefault(songTitle, new HashSet<>());
        if (!likedUsers.contains(user)) {
            likedUsers.add(user);
            spotifyRepository.getSongLikesMap().put(songTitle, likedUsers);
            // Auto-like the artist
            String artistName = findArtistBySong(songTitle); // Method to find the artist by song
            if (artistName != null) {
                Artist artist = spotifyRepository.getArtistMap().get(artistName);
                // You may need to handle artist likes if needed
            }
        }
        return song;
    }

    public String mostPopularArtist() {
        String mostPopular = "";
        int maxLikes = 0;
        for (Artist artist : spotifyRepository.getArtistMap().values()) {
            int likeCount = countLikesForArtist(artist.getName());
            if (likeCount > maxLikes) {
                maxLikes = likeCount;
                mostPopular = artist.getName();
            }
        }
        return mostPopular;
    }

    public String mostPopularSong() {
        String mostPopular = "";
        int maxLikes = 0;
        for (Song song : spotifyRepository.getSongMap().values()) {
            int likeCount = spotifyRepository.getSongLikesMap().getOrDefault(song.getTitle(), new HashSet<>()).size();
            if (likeCount > maxLikes) {
                maxLikes = likeCount;
                mostPopular = song.getTitle();
            }
        }
        return mostPopular;
    }

    private String findArtistBySong(String songTitle) {
        for (Map.Entry<String, Album> entry : spotifyRepository.getAlbumMap().entrySet()) {
            Album album = entry.getValue();
            for (Song song : spotifyRepository.getSongMap().values()) {
                if (song.getTitle().equals(songTitle)) {
                    return spotifyRepository.getArtistMap().entrySet().stream()
                        .filter(e -> e.getValue().equals(album)).map(Map.Entry::getKey)
                        .findFirst().orElse(null);
                }
            }
        }
        return null;
    }

    private int countLikesForArtist(String artistName) {
        int count = 0;
        for (Song song : spotifyRepository.getSongMap().values()) {
            if (song.getTitle().equals(artistName)) {
                count += spotifyRepository.getSongLikesMap().getOrDefault(song.getTitle(), new HashSet<>()).size();
            }
        }
        return count;
    }
}
