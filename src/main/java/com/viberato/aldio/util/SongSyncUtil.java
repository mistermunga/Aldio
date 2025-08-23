package com.viberato.aldio.util;

import com.viberato.aldio.entity.Song;
import com.viberato.aldio.repository.SongRepository;
import com.viberato.aldio.service.SongService;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;

@Component
public class SongSyncUtil {

    private SongRepository songRepository;
    private SongService songService;

    private static final String MUSIC_DIR = "/music/";

    @Autowired
    public SongSyncUtil(SongRepository songRepository, SongService songService) {
        this.songRepository = songRepository;
        this.songService = songService;
    }

    @PostConstruct
    public void syncMusicFolder() {
        try {
        File musicDir = new ClassPathResource(MUSIC_DIR).getFile();
        if (!musicDir.exists() || !musicDir.isDirectory()) {
            throw new IllegalStateException("Music directory does not exist or is not a directory");
        }

        Arrays.stream(musicDir.listFiles((dir, name) -> name.endsWith(".mp3")))
                .forEach(this::processFile);

        } catch (IOException e) {
        throw new RuntimeException("Failed to load music directory " + e );
        }
    }

    private void processFile(File file) {
        try {
            String fileName = file.getName();

            if (songRepository.findByFilename(fileName).isPresent()) {return;}

            Song song = new Song();

            song.setFilename(fileName);

            songService.computeAndInputDuration(song);
            songService.extractSongNameAndArtistNameFromFilename(song);

            songService.addSong(song);

            System.out.println("Added " + song.getSongName() + " - " + song.getArtistName());
        } catch (Exception e) {
            System.err.println("Failed to process " + file.getName() + ": " + e.getMessage());
        }
    }

}
