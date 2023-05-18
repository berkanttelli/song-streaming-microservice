package com.dirtify.songstreamingmicroservice.web.controller;

import com.dirtify.songstreamingmicroservice.service.MusicStorageService;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpRange;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.List;

@RestController
@RequestMapping("/songs")
public class MusicController {
    private final MusicStorageService musicStorageService;

    public MusicController(MusicStorageService musicStorageService1) {
        this.musicStorageService = musicStorageService1;
    }

    @GetMapping("/{fileName}/stream")
    public ResponseEntity<Resource> streamMusic(@PathVariable String fileName,
                                                @RequestHeader HttpHeaders headers) {
        try {
            Resource musicFile = musicStorageService.loadMusicFile(fileName);
            File file = musicFile.getFile();
            RandomAccessFile randomAccessFile = new RandomAccessFile(file, "r");

            long fileLength = randomAccessFile.length();
            long start =0;
            long end = fileLength - 1;



            if(!headers.getRange().isEmpty()) {
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

            InputStreamResource inputStreamResource = new InputStreamResource(new ByteArrayInputStream(bytes));

            return new ResponseEntity<>(inputStreamResource, responseHeaders, HttpStatus.PARTIAL_CONTENT);

        } catch (IOException e) {
            throw new RuntimeException("Could not stream music file", e);
        }
    }

}
