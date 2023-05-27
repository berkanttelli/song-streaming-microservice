package com.dirtify.songstreamingmicroservice.service;

import org.springframework.core.io.Resource;

public interface MusicStorageService {
    Resource loadMusicFile(Long id);
}
