package com.viberato.aldio.service;

import com.viberato.aldio.entity.Song;
import com.viberato.aldio.repository.SongRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;
import org.tritonus.share.sampled.file.TAudioFileFormat;

import javax.sound.sampled.AudioFileFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.UnsupportedAudioFileException;
import java.io.File;
import java.io.IOException;
import java.util.Map;

@Service
public class SongService {

    private SongRepository songRepository;
    private UserService userService;

    private static final String MUSIC_DIR = "music/";

    @Autowired
    public SongService(SongRepository songRepository, UserService userService) {
        this.songRepository = songRepository;
        this.userService = userService;
    }

    public Song addSong(Song song) {
        return songRepository.save(song);
    }

    // ==== UTILITY FUNCTIONS ====
    public double getDuration(File file) throws IOException, UnsupportedAudioFileException {

        AudioFileFormat fileFormat = AudioSystem.getAudioFileFormat(file);

        if (fileFormat instanceof TAudioFileFormat) {
            Map<?, ?> properties = ((TAudioFileFormat) fileFormat).properties();
            Long microseconds = (Long) properties.get("duration");
            double seconds = microseconds / 1_000_000.0;

            return seconds;
        } else {
            throw new UnsupportedAudioFileException("File is not an mp3");
        }
    }

    public Song computeAndInputDuration(Song song) throws UnsupportedAudioFileException, IOException {

        if (songFilepathIsEmpty(song)){ throw new IllegalStateException("Filename is empty"); }

        String resourcePath = MUSIC_DIR + song.getFilename();
        File file = new ClassPathResource(resourcePath).getFile();
        int duration = (int) getDuration(file);

        song.setDurationSeconds(duration);
        return song;
    }

    public Song extractSongNameAndArtistNameFromFilename(Song song) {

        if (songFilepathIsEmpty(song)){ throw new IllegalStateException("Filename is empty"); }

        String filename = song.getFilename();
        String filenameWithoutExt = filename.substring(0, filename.lastIndexOf('.'));

        String[] atoms = filenameWithoutExt.split(" - ");

        if (atoms.length != 2) {
            throw new IllegalStateException("filename should follow \"song - artist.mp3\" format");
        }

        song.setSongName(atoms[0]);
        song.setArtistName(atoms[1]);

        return song;

    }

    private boolean songFilepathIsEmpty(Song song) {
        String filename = song.getFilename();
        return filename == null || filename.isBlank();
    }

    public boolean songExists(String filename) {
        return songRepository.findByFilename(filename) != null;
    }

}
