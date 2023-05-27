package com.dirtify.songstreamingmicroservice.service;

import com.dirtify.songstreamingmicroservice.model.MusicAvailableModel;
import com.dirtify.songstreamingmicroservice.web.model.response.MusicAvailableResponseModel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

public interface SongCheckService {
    Boolean isSongInDatabase(Long id);
    MusicAvailableResponseModel availableMusicList(List<Long> musicIds);
}
