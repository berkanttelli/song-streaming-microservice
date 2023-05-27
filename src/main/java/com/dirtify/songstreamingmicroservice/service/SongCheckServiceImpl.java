package com.dirtify.songstreamingmicroservice.service;


import com.dirtify.songstreamingmicroservice.model.MusicAvailableModel;
import com.dirtify.songstreamingmicroservice.repository.SongModel;
import com.dirtify.songstreamingmicroservice.repository.SongRepository;
import com.dirtify.songstreamingmicroservice.web.model.response.MusicAvailableResponseModel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.FileSystemUtils;
import org.springframework.web.multipart.MultipartFile;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Slf4j
@Service
public class SongCheckServiceImpl implements SongCheckService {
    final SongRepository songRepository;
    final String basePath;
    final String uglySlash;
    final String mp3Extension;

    public SongCheckServiceImpl(SongRepository songRepository, @Value("${app.music.storage.path}") String basePath) {
        this.songRepository = songRepository;
        this.basePath = basePath;
        this.uglySlash = "/";
        this.mp3Extension = ".mp3";
    }

    @Override
    public Boolean isSongInDatabase(Long id) {
        return songRepository.existsById(id);
    }

    @Override
    public MusicAvailableResponseModel availableMusicList(List<Long> musicIds) {
        MusicAvailableResponseModel responseModel = new MusicAvailableResponseModel();
        List<MusicAvailableModel> list = new ArrayList<>();
        for (Long id : musicIds) {
            final MusicAvailableModel _data = new MusicAvailableModel();
            _data.setId(id);
            _data.setIsAvailable(isSongInDatabase(id));
            list.add(_data);
        }
        responseModel.setMusicAvailableModelList(list);
        return responseModel;
    }

    @Override
    public Boolean saveMusicPathAndFile(MultipartFile file, Long id, String artist, String album) throws IOException {
        // Save relative path and id to db
        final SongModel songModel = new SongModel();
        artist = specialCharacterChecker(artist);
        album = specialCharacterChecker(album);
        String musicName = specialCharacterChecker(Objects.requireNonNull(file.getOriginalFilename()));
        String musicRelativePath = artist + uglySlash + album + uglySlash + musicName + mp3Extension;

        // Create folders and put music file into it
        String songFilePath = basePath + uglySlash + musicRelativePath;
        try {
            File createFilePath = new File(songFilePath);
            if(!createFilePath.exists()) {
                if (new File(songFilePath).mkdirs()) {
                    file.transferTo(createFilePath);
                }
            } else {
                return Boolean.FALSE;
            }
            songModel.setPath(musicRelativePath);
            songModel.setId(id);
            songRepository.saveAndFlush(songModel);
            return Boolean.TRUE;

        } catch (Exception e) {
            throw new IOException(e);
        }

    }

    @Override
    public Boolean deleteMusicPathAndFile(Long id) {

        try {
            Optional<SongModel> songModel = songRepository.findById(id);
            if(songModel.isPresent()) {
                String musicPath = basePath + uglySlash + songModel.get().getPath();
                FileSystemUtils.deleteRecursively(new File(musicPath));
                songRepository.deleteById(id);
                return Boolean.TRUE;
                }
            } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
        return Boolean.FALSE;
    }
    private String specialCharacterChecker(String value) {
        return value.replaceAll("[^a-zA-Z0-9]", "").replaceAll(" ", "").toLowerCase();
    }


}
