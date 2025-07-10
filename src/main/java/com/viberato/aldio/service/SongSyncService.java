package com.viberato.aldio.service;

import com.viberato.aldio.repository.SongRepository;
import com.viberato.aldio.entity.Song;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Service;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashSet;
import java.util.List;
import java.util.Scanner;
import java.util.Set;
import java.util.stream.Stream;

@Service
public class SongSyncService implements CommandLineRunner {

    @Autowired
    private SongRepository songRepository;

    @Autowired
    private ResourceLoader resourceLoader;

    @Value("${app.music.directory:music}")
    private String musicDirectory;

    private final Scanner scanner = new Scanner(System.in);

    @Override
    public void run(String... args) throws Exception {
        System.out.println("Beginning song synchronisation ...");
        synchroniseSongs();
        System.out.println("=== song synchronisation complete!");
    }

    public void synchroniseSongs() {
        try {

            // get the path to the music directory
            Resource resource = resourceLoader.getResource("classpath:" + musicDirectory);
            Path musicPath;

            if (resource.exists()) {
                musicPath = Paths.get(resource.getURI());
            } else {
                System.err.println("Resource not found: " + musicDirectory);
                return;
            }

            // get all mp3 files and database entries
            Set<String> mp3Files = getMp3Files(musicPath);

            List<Song> dbSongs = songRepository.findAll();
            Set<String> dbFilePaths = new HashSet<>();

            for (Song song : dbSongs) {
                dbFilePaths.add(song.getFilepath());
            }

            // Check for database entries without corresponding files
            checkOrphanedDatabaseEntries(dbSongs, musicPath);

            // Check for MP3 files without database entries
            checkOrphanedMp3Files(mp3Files, dbFilePaths, musicPath);

        } catch (IOException e) {
            System.err.println("Error during synchronisation: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private Set<String> getMp3Files(Path musicPath) throws IOException{
        Set<String> mp3Files = new HashSet<>();

        try (Stream<Path> paths = Files.walk(musicPath)) {
            paths.filter(Files::isRegularFile)
                    .filter(path -> path.toString().toLowerCase().endsWith(".mp3"))
                    .forEach(path -> {
                        // store as relative path
                        String relativePath = musicPath.relativize(path).toString();
                        mp3Files.add(relativePath);
                    });
        }

        return mp3Files;
    }

    private void checkOrphanedDatabaseEntries(List<Song> dbSongs, Path musicPath) {
        System.out.println("\nChecking orphaned database entries...");

        for (Song song : dbSongs) {
            Path fullPath = musicPath.resolve(song.getFilepath());

            if (!Files.exists(fullPath)) {

                System.out.println("**Database entry found without MP3 file!**");
                System.out.println("Song: " + song.getSongName() + " by " + song.getArtistName());
                System.out.println("Expected path: " + fullPath);
                System.out.println("\nDelete Entry? [Y/n]");

                String response = scanner.nextLine().trim().toLowerCase();
                if (response.equals("y") || response.equals("yes") || response.isEmpty()) {
                    songRepository.delete(song);
                    System.out.println("**Database entry deleted!**");
                } else {
                    System.out.println("**Skipped...**");
                }
            }
        }
    }

    private void checkOrphanedMp3Files(Set<String> mp3Files, Set<String> dbFilePaths, Path musicPath) {
        System.out.println("Checking for songs without Database entries...");

        for (String file : mp3Files) {
            if (!dbFilePaths.contains(file)) {
                System.out.println("**Mp3 file found without Database entry!**");
                System.out.println("Create DB entry? [Y/n]");

                String response = scanner.nextLine().trim().toLowerCase();
                if (response.equals("y") || response.equals("yes") || response.isEmpty()) {
                    createSongEntry(file, musicPath);
                    System.out.println("**Entry created!**");
                } else {
                    System.out.println("**Skipped...**");
                }
            }
        }
    }

    private void createSongEntry(String filePath, Path musicPath) {
        System.out.println("\nCreating entry for file: " + filePath);

        Song song = new Song();
        song.setFilepath(filePath);

        // get the filename
        String filename = Paths.get(filePath).getFileName().toString();
        String slicedFilename = filename.substring(0, filename.lastIndexOf("."));

        // Parse Song - Artist format
        String[] atoms = slicedFilename.split(" - ", 2);
        song.setArtistName(atoms[1].trim());
        song.setSongName(atoms[0].trim());

        // Key in metadata
        System.out.print("Album Name (optional): ");
        String albumName = scanner.nextLine().trim();
        if (!albumName.isEmpty()) {
            song.setAlbumName(albumName);
        }

        System.out.print("Genre Tags (optional): ");
        String genreTags = scanner.nextLine().trim();
        if (!genreTags.isEmpty()) {
            song.setGenreTags(genreTags);
        }

        System.out.print("Release Year (optional): ");
        String yearStr = scanner.nextLine().trim();
        if (!yearStr.isEmpty()) {
            try {
                song.setReleaseYear(Integer.parseInt(yearStr));
            } catch (NumberFormatException e) {
                System.out.println("Invalid year format, skipping.");
            }
        }

        System.out.print("Album Art URL (optional): ");
        String albumArtUrl = scanner.nextLine().trim();
        if (!albumArtUrl.isEmpty()) {
            song.setAlbumArtUrl(albumArtUrl);
        }

        // Calculate length
        try {
            Path fullpath = musicPath.resolve(song.getFilepath());
            int duration = calculateDuration(fullpath.toFile());
            song.setDurationSeconds(duration);
            System.out.println("Song length: " + duration +" seconds\n"+ formatDuration(duration));
        } catch(Exception e) {
            System.err.println("Could not calculate duration ");
            System.out.print("   Enter duration in seconds: ");
            String durationStr = scanner.nextLine().trim();
            try {
                song.setDurationSeconds(Integer.parseInt(durationStr));
            } catch (NumberFormatException ex) {
                song.setDurationSeconds(0);
            }
        }

        // save to database
        try {
            songRepository.save(song);
        } catch (Exception ex) {
            System.err.println("Error saving to database; " + ex.getMessage());
        }
    }

    private int calculateDuration(File mp3) throws Exception {
        try (AudioInputStream ais = AudioSystem.getAudioInputStream(mp3)) {
            AudioFormat format = ais.getFormat();
            long frames = ais.getFrameLength();
            double durationInSeconds = (frames + 0.0) / format.getFrameRate();
            return (int) Math.round(durationInSeconds);
        }
    }

    private String formatDuration(int seconds) {
        int minutes = seconds / 60;
        int remainder = seconds % 60;

        return String.format("%d:%02d", minutes, remainder);
    }
}
