package com.driver;

import java.util.*;
import org.springframework.stereotype.Repository;

@Repository
public class SpotifyRepository {
    public HashMap<Artist, List<Album>> artistAlbumMap;
    public HashMap<Album, List<Song>> albumSongMap;
    public HashMap<Playlist, List<Song>> playlistSongMap;
    public HashMap<Playlist, List<User>> playlistListenerMap;
    public HashMap<User, Playlist> creatorPlaylistMap;
    public HashMap<User, List<Playlist>> userPlaylistMap;
    public HashMap<Song, List<User>> songLikeMap;

    public List<User> users;
    public List<Song> songs;
    public List<Playlist> playlists;
    public List<Album> albums;
    public List<Artist> artists;

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
        return artist;
    }

    public Album createAlbum(String title, String artistName) {
        Artist artist = artists.stream()
                               .filter(a -> a.getName().equals(artistName))
                               .findFirst()
                               .orElseGet(() -> createArtist(artistName));

        Album album = new Album(title);
        albums.add(album);
        artistAlbumMap.computeIfAbsent(artist, k -> new ArrayList<>()).add(album);
        return album;
    }

    public Song createSong(String title, String albumName, int length) throws Exception {
        Album album = albums.stream()
                            .filter(a -> a.getTitle().equals(albumName))
                            .findFirst()
                            .orElseThrow(() -> new Exception("Album does not exist"));

        Song song = new Song(title, length);
        songs.add(song);
        albumSongMap.computeIfAbsent(album, k -> new ArrayList<>()).add(song);
        return song;
    }

    public Playlist createPlaylistOnLength(String mobile, String title, int length) throws Exception {
        User user = users.stream()
                         .filter(u -> u.getMobile().equals(mobile))
                         .findFirst()
                         .orElseThrow(() -> new Exception("User does not exist"));

        Playlist playlist = new Playlist(title);
        playlists.add(playlist);
        List<Song> songsOfLength = songs.stream()
                                        .filter(s -> s.getLength() == length)
                                        .toList();
        playlistSongMap.put(playlist, songsOfLength);
        playlistListenerMap.put(playlist, new ArrayList<>(List.of(user)));
        userPlaylistMap.computeIfAbsent(user, k -> new ArrayList<>()).add(playlist);
        return playlist;
    }

    public Playlist createPlaylistOnName(String mobile, String title, List<String> songTitles) throws Exception {
        User user = users.stream()
                         .filter(u -> u.getMobile().equals(mobile))
                         .findFirst()
                         .orElseThrow(() -> new Exception("User does not exist"));

        Playlist playlist = new Playlist(title);
        playlists.add(playlist);
        List<Song> songsToAdd = songs.stream()
                                     .filter(s -> songTitles.contains(s.getTitle()))
                                     .toList();
        playlistSongMap.put(playlist, songsToAdd);
        playlistListenerMap.put(playlist, new ArrayList<>(List.of(user)));
        userPlaylistMap.computeIfAbsent(user, k -> new ArrayList<>()).add(playlist);
        return playlist;
    }

    public Playlist findPlaylist(String mobile, String playlistTitle) throws Exception {
        User user = users.stream()
                         .filter(u -> u.getMobile().equals(mobile))
                         .findFirst()
                         .orElseThrow(() -> new Exception("User does not exist"));

        Playlist playlist = playlists.stream()
                                     .filter(p -> p.getTitle().equals(playlistTitle))
                                     .findFirst()
                                     .orElseThrow(() -> new Exception("Playlist does not exist"));

        if (!playlistListenerMap.get(playlist).contains(user)) {
            playlistListenerMap.get(playlist).add(user);
            userPlaylistMap.computeIfAbsent(user, k -> new ArrayList<>()).add(playlist);
        }
        return playlist;
    }

    public Song likeSong(String mobile, String songTitle) throws Exception {
        User user = users.stream()
                         .filter(u -> u.getMobile().equals(mobile))
                         .findFirst()
                         .orElseThrow(() -> new Exception("User does not exist"));

        Song song = songs.stream()
                         .filter(s -> s.getTitle().equals(songTitle))
                         .findFirst()
                         .orElseThrow(() -> new Exception("Song does not exist"));

        List<User> likedUsers = songLikeMap.computeIfAbsent(song, k -> new ArrayList<>());
        if (!likedUsers.contains(user)) {
            likedUsers.add(user);
            song.setLikes(song.getLikes() + 1);
        }

        Artist artist = artistAlbumMap.entrySet()
                                      .stream()
                                      .filter(entry -> entry.getValue()
                                                             .stream()
                                                             .anyMatch(a -> albumSongMap.get(a)
                                                                                        .contains(song)))
                                      .map(Map.Entry::getKey)
                                      .findFirst()
                                      .orElse(null);
        if (artist != null) {
            List<User> artistLikes = songLikeMap.computeIfAbsent(song, k -> new ArrayList<>());
            if (!artistLikes.contains(user)) {
                artistLikes.add(user);
            }
        }
        return song;
    }

    public String mostPopularArtist() {
        return artistAlbumMap.keySet()
                             .stream()
                             .max(Comparator.comparing(a -> songLikeMap.values()
                                                                      .stream()
                                                                      .flatMap(List::stream)
                                                                      .filter(u -> songLikeMap.get(a).contains(u))
                                                                      .count()))
                             .map(Artist::getName)
                             .orElse(null);
    }

    public String mostPopularSong() {
        return songs.stream()
                    .max(Comparator.comparing(Song::getLikes))
                    .map(Song::getTitle)
                    .orElse(null);
    }
}
