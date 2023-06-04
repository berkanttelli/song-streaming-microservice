package com.dirtify.songstreamingmicroservice.web.controller;

import com.dirtify.songstreamingmicroservice.service.MusicStorageService;
import com.dirtify.songstreamingmicroservice.service.SongCheckService;
import com.dirtify.songstreamingmicroservice.web.model.response.MusicAvailableResponseModel;
import jakarta.ws.rs.Path;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpRange;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.List;

@RestController
@RequestMapping("/songs")
public class MusicController {

    private static final Logger logger = LoggerFactory.getLogger(MusicController.class);
    private final MusicStorageService musicStorageService;
    private final SongCheckService songCheckService;

    public MusicController(MusicStorageService musicStorageService1, SongCheckService songCheckService) {
        this.musicStorageService = musicStorageService1;
        this.songCheckService = songCheckService;
    }

    @GetMapping("/{id}/stream")
    public ResponseEntity<Resource> streamMusic(@PathVariable Long id,
                                                @RequestHeader HttpHeaders headers) {
        try {
            Resource musicFile = musicStorageService.loadMusicFile(id);
            File file = musicFile.getFile();
            RandomAccessFile randomAccessFile = new RandomAccessFile(file, "r");

            long fileLength = randomAccessFile.length();
            long start = 0;
            long end = fileLength - 1;


            if (!headers.getRange().isEmpty()) {
                List<HttpRange> httpRanges = headers.getRange();
                HttpRange range = httpRanges.get(0);
                start = range.getRangeStart(fileLength);
                end = range.getRangeEnd(fileLength);
            }

            randomAccessFile.seek(start);
            long rangeLength = end - start + 1;
            byte[] bytes = new byte[(int) rangeLength];
            randomAccessFile.readFully(bytes);

            HttpHeaders responseHeaders = new HttpHeaders();
            responseHeaders.add(HttpHeaders.CONTENT_TYPE, "audio/mpeg");
            responseHeaders.add(HttpHeaders.ACCEPT_RANGES, "bytes");
            responseHeaders.add("ETag", "fileName");
            responseHeaders.add(HttpHeaders.CONTENT_RANGE, "bytes " + start + "-" + end + "/" + fileLength);
            responseHeaders.add(HttpHeaders.CONTENT_LENGTH, String.valueOf(bytes.length));
            logger.info("Id: " + id.toString() + " Range: " + headers.get("Range"));

            InputStreamResource inputStreamResource = new InputStreamResource(new ByteArrayInputStream(bytes));

            return new ResponseEntity<>(inputStreamResource, responseHeaders, HttpStatus.PARTIAL_CONTENT);

        } catch (IOException e) {
            throw new RuntimeException("Could not stream music file", e);
        }

    }


    @PostMapping("isAvailable")
    public ResponseEntity<MusicAvailableResponseModel> checkMusicAvailability(@RequestBody List<Long> musicIds) {
        MusicAvailableResponseModel musicAvailableResponseModel = songCheckService.availableMusicList(musicIds);

        return new ResponseEntity<>(musicAvailableResponseModel, HttpStatus.OK);
    }

    // TODO upload music to the folder and save the path to the db
    @PostMapping("/create/{artist}/{album}/{id}")
    public ResponseEntity<String> uploadMusicFile(@RequestParam("file") MultipartFile file,
                                          @PathVariable Long id,
                                          @PathVariable String artist,
                                          @PathVariable String album) throws IOException {
        if(songCheckService.saveMusicPathAndFile(file, id, artist, album)) {
            return ResponseEntity.ok().body("Music is added to the db");
        } else {
            return ResponseEntity.status(HttpStatus.I_AM_A_TEAPOT).body("Everyone Love Teapot");
        }
    }

    @DeleteMapping("/delete/{id}")
    public ResponseEntity<String> deleteMusic(@PathVariable Long id) throws IOException{
        if(songCheckService.deleteMusicPathAndFile(id)) {
            return ResponseEntity.ok().body("Delete Music is Successful");
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Music not found.");
        }
    }

}
