package com.viberato.aldio;
import com.viberato.aldio.service.SongService;
import org.springframework.core.io.ClassPathResource;

import java.io.File;

public class DurationTest {
    public static void main(String[] args) throws Exception {
        SongService service = new SongService(null, null); // we don't need repos for this test

        File file = new ClassPathResource("music/Country Girl - Boy Harsher.mp3").getFile();
        double seconds = service.getDuration(file);

        System.out.println("Duration = " + seconds + "s");
    }
}

