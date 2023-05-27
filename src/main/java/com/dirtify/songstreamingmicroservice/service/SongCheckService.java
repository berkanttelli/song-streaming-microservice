package com.dirtify.songstreamingmicroservice.service;

import com.dirtify.songstreamingmicroservice.model.MusicAvailableModel;
import com.dirtify.songstreamingmicroservice.web.model.response.MusicAvailableResponseModel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

public interface SongCheckService {
    Boolean isSongInDatabase(Long id);
    MusicAvailableResponseModel availableMusicList(List<Long> musicIds);

    Boolean saveMusicPathAndFile(MultipartFile file, Long id, String artist, String album) throws IOException;

    Boolean deleteMusicPathAndFile(Long id) throws IOException;
}
