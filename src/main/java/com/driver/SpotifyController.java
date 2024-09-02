package com.driver;

import java.util.List;

import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("spotify")
public class SpotifyController {

    // Autowire will not work in this case, no need to change this and add autowire
    SpotifyService spotifyService = new SpotifyService();

    @PostMapping("/add-user")
    public String createUser(@RequestParam(name = "name") String name, @RequestParam(name = "mobile") String mobile) {
        User user = spotifyService.createUser(name, mobile);
        return user != null ? "Success" : "Failure";
    }

    @PostMapping("/add-artist")
    public String createArtist(@RequestParam(name = "name") String name) {
        Artist artist = spotifyService.createArtist(name);
        return artist != null ? "Success" : "Failure";
    }

    @PostMapping("/add-album")
    public String createAlbum(@RequestParam(name = "title") String title, @RequestParam(name = "artistName") String artistName) {
        try {
            Album album = spotifyService.createAlbum(title, artistName);
            return album != null ? "Success" : "Failure";
        } catch (Exception e) {
            return "Failure: " + e.getMessage();
        }
    }

    @PostMapping("/add-song")
    public String createSong(@RequestParam(name = "title") String title, @RequestParam(name = "albumName") String albumName, @RequestParam(name = "length") int length) {
        try {
            Song song = spotifyService.createSong(title, albumName, length);
            return song != null ? "Success" : "Failure";
        } catch (Exception e) {
            return "Failure: " + e.getMessage();
        }
    }

    @PostMapping("/add-playlist-on-length")
    public String createPlaylistOnLength(@RequestParam(name = "mobile") String mobile, @RequestParam(name = "title") String title, @RequestParam(name = "length") int length) {
        try {
            Playlist playlist = spotifyService.createPlaylistOnLength(mobile, title, length);
            return playlist != null ? "Success" : "Failure";
        } catch (Exception e) {
            return "Failure: " + e.getMessage();
        }
    }

    @PostMapping("/add-playlist-on-name")
    public String createPlaylistOnName(@RequestParam(name = "mobile") String mobile, @RequestParam(name = "title") String title, @RequestParam(name = "songTitles") List<String> songTitles) {
        try {
            Playlist playlist = spotifyService.createPlaylistOnName(mobile, title, songTitles);
            return playlist != null ? "Success" : "Failure";
        } catch (Exception e) {
            return "Failure: " + e.getMessage();
        }
    }

    @PutMapping("/find-playlist")
    public String findPlaylist(@RequestParam(name = "mobile") String mobile, @RequestParam(name = "playlistTitle") String playlistTitle) {
        try {
            Playlist playlist = spotifyService.findPlaylist(mobile, playlistTitle);
            return playlist != null ? "Success" : "Failure";
        } catch (Exception e) {
            return "Failure: " + e.getMessage();
        }
    }

    @PutMapping("/like-song")
    public String likeSong(@RequestParam(name = "mobile") String mobile, @RequestParam(name = "songTitle") String songTitle) {
        try {
            Song song = spotifyService.likeSong(mobile, songTitle);
            return song != null ? "Success" : "Failure";
        } catch (Exception e) {
            return "Failure: " + e.getMessage();
        }
    }

    @GetMapping("/popular-artist")
    public String mostPopularArtist() {
        return spotifyService.mostPopularArtist();
    }

    @GetMapping("/popular-song")
    public String mostPopularSong() {
        return spotifyService.mostPopularSong();
    }
}
