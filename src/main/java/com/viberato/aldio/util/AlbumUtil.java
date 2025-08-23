package com.viberato.aldio.util;

import com.viberato.aldio.entity.Song;
import com.viberato.aldio.service.SongService;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.DependsOn;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.util.*;

@Component
@DependsOn("songSyncUtil")
public class AlbumUtil {

    private static final Logger log = LoggerFactory.getLogger(AlbumUtil.class);

    private static final String MUSICBRAINZ_URL = "https://musicbrainz.org/ws/2/recording";
    private static final String COVERART_URL = "https://coverartarchive.org/release/";
    private static final String MUSIC_DIR = "music/";

    private final RestTemplate restTemplate = new RestTemplate();
    private final SongService songService;

    @Autowired
    public AlbumUtil(SongService songService) {
        this.songService = songService;
    }

    public Song addAlbumDetails(Song song) {
        try {
            URI uri = UriComponentsBuilder.fromUriString(MUSICBRAINZ_URL)
                    .queryParam("query", "artist:\"" + song.getArtistName() + "\" AND recording:\"" + song.getSongName() + "\"")
                    .queryParam("fmt", "json")
                    .build().toUri();

            Map<String, Object> response = restTemplate.getForObject(uri, Map.class);
            if (response == null || !response.containsKey("recordings")) {
                return song;
            }

            List<Map<String, Object>> recordings = (List<Map<String, Object>>) response.get("recordings");
            if (recordings == null || recordings.isEmpty()) {
                return song;
            }

            Map<String, Object> firstRecording = recordings.get(0);
            List<Map<String, Object>> releases = (List<Map<String, Object>>) firstRecording.get("releases");
            if (releases == null || releases.isEmpty()) {
                return song;
            }

            // Choose the earliest "Official" release
            Optional<Map<String, Object>> chosen = releases.stream()
                    .filter(r -> "Official".equalsIgnoreCase((String) r.get("status")))
                    .min(Comparator.comparing(r -> (String) r.get("date"), Comparator.nullsLast(String::compareTo)));

            if (chosen.isPresent()) {
                Map<String, Object> release = chosen.get();

                String albumName = (String) release.get("title");
                String releaseDate = (String) release.get("date");
                String mbid = (String) release.get("id");

                song.setAlbumName(albumName);

                if (releaseDate != null && releaseDate.length() >= 4) {
                    try {
                        song.setReleaseYear(Integer.parseInt(releaseDate.substring(0, 4)));
                    } catch (NumberFormatException ignored) {
                        log.warn("Invalid release year format: {}", releaseDate);
                    }
                }

                if (mbid != null) {
                    song.setAlbumArtUrl(COVERART_URL + mbid + "/front");
                }

                log.info("Enriched song [{} - {}] with album {}", song.getArtistName(), song.getSongName(), albumName);
            }

        } catch (Exception e) {
            log.error("Lookup failed for {}: {}", song.getFilename(), e.getMessage());
        }

        return song;
    }

    @PostConstruct
    public void syncAlbumDetails() {
        try {
            File musicDir = new ClassPathResource(MUSIC_DIR).getFile();
            if (!musicDir.exists() || !musicDir.isDirectory()) {
                throw new IllegalStateException("Music directory not found: " + MUSIC_DIR);
            }

            File[] files = musicDir.listFiles();
            if (files == null) {
                log.warn("No files found in music directory {}", MUSIC_DIR);
                return;
            }

            for (File file : files) {
                if (!songService.songExists(file.getName())) {
                    log.warn("File {} has no matching DB entry", file.getName());
                    continue;
                }

                Song song = songService.fetchSongByFilename(file.getName());

                if (song.getAlbumArtUrl() != null && !song.getAlbumArtUrl().isBlank()) {
                    continue; // already has album art
                }

                addAlbumDetails(song);

                songService.addSong(song);

                // Rate limiting
                Thread.sleep(1000);
            }

        } catch (IOException e) {
            throw new RuntimeException("Failed to access music directory", e);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("Album sync interrupted", e);
        }
    }
}
