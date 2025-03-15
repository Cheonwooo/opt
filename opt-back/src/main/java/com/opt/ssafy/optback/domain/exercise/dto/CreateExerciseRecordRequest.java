package com.opt.ssafy.optback.domain.exercise.dto;

import java.util.List;
import lombok.Builder;
import lombok.Getter;
import org.springframework.web.multipart.MultipartFile;

@Getter
@Builder
public class CreateExerciseRecordRequest {
    private Integer exerciseId;
    private Integer set;
    private Integer rep;
    private Integer weight;
    private List<MultipartFile> medias;
}
