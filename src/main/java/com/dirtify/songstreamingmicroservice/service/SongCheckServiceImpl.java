package com.dirtify.songstreamingmicroservice.service;


import com.dirtify.songstreamingmicroservice.model.MusicAvailableModel;
import com.dirtify.songstreamingmicroservice.repository.SongRepository;
import com.dirtify.songstreamingmicroservice.web.model.response.MusicAvailableResponseModel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
public class SongCheckServiceImpl implements  SongCheckService {
    final SongRepository songRepository;

    public SongCheckServiceImpl(SongRepository songRepository) {
        this.songRepository = songRepository;
    }

    @Override
    public Boolean isSongInDatabase(Long id) {
        return songRepository.existsById(id);
    }

    @Override
    public MusicAvailableResponseModel availableMusicList(List<Long> musicIds) {
        MusicAvailableResponseModel responseModel = new MusicAvailableResponseModel();
        List<MusicAvailableModel> list = new ArrayList<>();
        for(Long id:musicIds) {
            final MusicAvailableModel _data = new MusicAvailableModel();
            _data.setId(id);
            _data.setIsAvailable(isSongInDatabase(id));
            list.add(_data);
        }
        responseModel.setMusicAvailableModelList(list);
        return responseModel;
    }
}
