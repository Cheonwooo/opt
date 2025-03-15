package com.opt.ssafy.optback.domain.trainer_detail.dto;

import com.opt.ssafy.optback.domain.menu.dto.MenuResponse;
import com.opt.ssafy.optback.domain.trainer_detail.entity.TrainerDetail;
import java.util.List;
import lombok.Getter;

@Getter
public class TrainerDetailResponse {

    private int trainerId;
    private String trainerNickname;
    private boolean isOneDayAvailable;
    private String intro;
    private int experienceYears;
    private String availableHours;
    private List<String> keywords;
    private String trainerProfileImage;
    private String gymName;
    private String gymAddress;
    private double averageRating;
    private Integer reviewCount;
    private List<MenuResponse> menus;

    public TrainerDetailResponse(TrainerDetail trainerDetail, List<String> keywords, double averageRating,
                                 int reviewCount, List<MenuResponse> menus) {
        this.trainerId = trainerDetail.getTrainerId();
        this.trainerNickname = trainerDetail.getMember().getNickname();
        this.isOneDayAvailable = trainerDetail.getIsOneDayAvailable();
        this.intro = trainerDetail.getIntro();
        this.experienceYears = trainerDetail.getExperienceYears();
        this.availableHours = trainerDetail.getAvailableHours();
        this.keywords = keywords;
        this.trainerProfileImage = trainerDetail.getMember().getImagePath();
        this.gymName = trainerDetail.getGym().getGymName();
        this.gymAddress = trainerDetail.getGym().getFullAddress();
        this.averageRating = averageRating;
        this.reviewCount = reviewCount;
        this.menus = menus;
    }
}
