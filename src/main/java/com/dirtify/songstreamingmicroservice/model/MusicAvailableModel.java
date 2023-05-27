package com.dirtify.songstreamingmicroservice.model;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class MusicAvailableModel {
    private Long id;
    private Boolean isAvailable;
}
