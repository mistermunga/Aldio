package com.viberato.aldio.service;

import com.viberato.aldio.repository.SongRepository;
import com.viberato.aldio.entity.Song;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Service;

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


}
