package com.dirtify.songstreamingmicroservice.service;

import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;

import java.io.FileNotFoundException;
import java.net.MalformedURLException;
import java.nio.file.Path;
import java.nio.file.Paths;

@Slf4j
@Service
public class MusicStorageServiceImpl implements MusicStorageService {
    private final Path musicStorageLocation;
    private static final Logger logger = LoggerFactory.getLogger(MusicStorageServiceImpl.class);

    public MusicStorageServiceImpl(@Value("${app.music.storage.path}") String musicStoragePath) {
        this.musicStorageLocation = Paths.get(musicStoragePath).toAbsolutePath().normalize();
    }

    @Override
    public Resource loadMusicFile(String fileName) {
        try {
            Path filePath = this.musicStorageLocation.resolve(fileName).normalize();
            logger.debug("Loading music file: {}", filePath);
            Resource resource = new UrlResource(filePath.toUri());
            if(resource.exists()) {
                return resource;
            } else {
                throw new FileNotFoundException("File not found: " + fileName);
            }
        } catch (MalformedURLException | FileNotFoundException e) {
            throw new RuntimeException(e);
        }
    }
}
