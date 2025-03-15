package com.opt.ssafy.optback.domain.profile;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/profile")
public class ProfileController {

    private final ProfileService profileService;

//    @GetMapping("/my")
//    public ResponseEntity<ProfileResponse> getMyProfile() {
//        return ResponseEntity.ok(profileService.getMyProfile());
//    }

    @GetMapping("/{id}")
    public ResponseEntity<ProfileResponse> getProfile(@PathVariable Integer id) {
        return ResponseEntity.ok(profileService.getProfile(id));
    }
}
