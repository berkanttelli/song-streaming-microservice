package com.dirtify.songstreamingmicroservice.service;

import com.dirtify.songstreamingmicroservice.repository.SongModel;
import com.dirtify.songstreamingmicroservice.repository.SongRepository;
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
import java.util.Optional;

@Slf4j
@Service
public class MusicStorageServiceImpl implements MusicStorageService {
    private final Path musicStorageLocation;
    private final SongRepository songRepository;
    private static final Logger logger = LoggerFactory.getLogger(MusicStorageServiceImpl.class);

    public MusicStorageServiceImpl(@Value("${app.music.storage.path}") String musicStoragePath, SongRepository songRepository) {
        this.musicStorageLocation = Paths.get(musicStoragePath).toAbsolutePath().normalize();
        this.songRepository = songRepository;
    }

    @Override
    public Resource loadMusicFile(Long id) {
        try {
            Optional<SongModel> songPath = songRepository.findById(id);
            if (songPath.isPresent()) {
                Path filePath = this.musicStorageLocation.resolve(songPath.get().getPath()).normalize();
                logger.debug("Loading music file: {}", filePath);
                Resource resource = new UrlResource(filePath.toUri());
                if (resource.exists()) {
                    return resource;
                } else {
                    throw new FileNotFoundException("File not found: " + songPath.get().getPath());
                }

            } else {
                throw new FileNotFoundException("Sound does not exist.");
            }

        } catch (MalformedURLException | FileNotFoundException e) {
            throw new RuntimeException(e);
        }
    }
}
