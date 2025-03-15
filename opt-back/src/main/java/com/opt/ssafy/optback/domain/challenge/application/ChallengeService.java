package com.opt.ssafy.optback.domain.challenge.application;

import com.opt.ssafy.optback.domain.auth.application.UserDetailsServiceImpl;
import com.opt.ssafy.optback.domain.challenge.dto.ChallengeRecordResponse;
import com.opt.ssafy.optback.domain.challenge.dto.ChallengeRecordWithRankResponse;
import com.opt.ssafy.optback.domain.challenge.dto.ChallengeResponse;
import com.opt.ssafy.optback.domain.challenge.dto.ContributionResponse;
import com.opt.ssafy.optback.domain.challenge.dto.CreateChallengeRequest;
import com.opt.ssafy.optback.domain.challenge.dto.JoinChallengeRequest;
import com.opt.ssafy.optback.domain.challenge.entity.Challenge;
import com.opt.ssafy.optback.domain.challenge.entity.ChallengeMember;
import com.opt.ssafy.optback.domain.challenge.entity.ChallengeRecord;
import com.opt.ssafy.optback.domain.challenge.exception.ChallengeCreationException;
import com.opt.ssafy.optback.domain.challenge.exception.ChallengeNotFoundException;
import com.opt.ssafy.optback.domain.challenge.exception.ChallengeRecordNotFoundException;
import com.opt.ssafy.optback.domain.challenge.exception.ChallengeTypeMismatchException;
import com.opt.ssafy.optback.domain.challenge.repository.ChallengeMemberRepository;
import com.opt.ssafy.optback.domain.challenge.repository.ChallengeRecordRepository;
import com.opt.ssafy.optback.domain.challenge.repository.ChallengeRepository;
import com.opt.ssafy.optback.domain.member.entity.Member;
import com.opt.ssafy.optback.domain.member.exception.MemberNotFoundException;
import com.opt.ssafy.optback.domain.member.repository.MemberRepository;
import com.opt.ssafy.optback.global.application.S3Service;
import jakarta.transaction.Transactional;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Slf4j
@Service
@RequiredArgsConstructor
public class ChallengeService {

    private final ChallengeRepository challengeRepository;
    private final ChallengeRecordRepository challengeRecordRepository;
    private final ChallengeMemberRepository challengeMemberRepository;
    private final UserDetailsServiceImpl userDetailsService;
    private final MemberRepository memberRepository;
    private final S3Service s3Service;

    @Value("${challenge.image.bucket.name}")
    private String bucketName;

    @Transactional
    public Page<ChallengeResponse> getChallenges(String status, Pageable pageable) {
        Page<Challenge> challenges = challengeRepository.findAllByStatusOrderByIdDesc(status, pageable);

        List<ChallengeResponse> challengeDtos = challenges.stream().map(challenge -> {
            Member host = memberRepository.findById(challenge.getHostId()).orElseThrow(MemberNotFoundException::new);
            // winnerId가 존재하면 winnerName 조회
            String winnerNickname = challenge.getWinnerId() == null ? null
                    : memberRepository.getMemberById(challenge.getWinnerId()).getNickname();
            return ChallengeResponse.from(challenge, host, winnerNickname);
        }).toList();
        return new PageImpl<>(challengeDtos, pageable, challenges.getTotalElements());
    }

    public ChallengeResponse getChallengeById(int id) {
        Challenge challenge = challengeRepository.findById(id)
                .orElseThrow(() -> new ChallengeNotFoundException("존재하지 않는 챌린지입니다. with id: " + id));
        Member host = memberRepository.findById(challenge.getHostId()).orElseThrow(MemberNotFoundException::new);
        String winnerNickname = challenge.getWinnerId() == null ? null
                : memberRepository.getMemberById(challenge.getWinnerId()).getNickname();
        return ChallengeResponse.from(challenge, host, winnerNickname);
    }

    public List<ContributionResponse> getChallengeContributions(int id) {
        Member currentUser = userDetailsService.getMemberByContextHolder();

        if (!getChallengeById(id).getType().equals("TEAM")) {
            throw new ChallengeTypeMismatchException("기여도를 계산할 수 없는 챌린지 유형입니다. TEAM 챌린지만 가능합니다.");
        }

        List<Object[]> contributionData = challengeRecordRepository.findAllContributionsByChallengeId(id);

        double totalContribution = 0.0;
        Map<Integer, ContributionResponse> contributionMap = new HashMap<>();

        for (Object[] record : contributionData) {
            int memberId = (int) record[0];
            String nickname = (String) record[1];

            // `count`, `duration`, `distance` 값을 가져오되, 타입 변환 안전하게 수행
            Double count = (record[2] instanceof Number) ? ((Number) record[2]).doubleValue() : null;
            Double duration = (record[3] instanceof Number) ? ((Number) record[3]).doubleValue() : null;
            Double distance = (record[4] instanceof Number) ? ((Number) record[4]).doubleValue() : null;

            // `count`, `duration`, `distance` 중 **NOT NULL**인 값을 찾아서 사용
            double validContribution =
                    (count != null) ? count : (duration != null) ? duration : (distance != null) ? distance : 0.0;

            totalContribution += validContribution;
            boolean isMyRecord = false;
            if(currentUser != null) {
                isMyRecord = (currentUser.getId() == memberId);
            }

            // 기존 memberId가 이미 존재하면 값 누적
            if (contributionMap.containsKey(memberId)) {
                ContributionResponse existing = contributionMap.get(memberId);
                existing.setMeasurement(existing.getMeasurement() + validContribution);
            } else {
                contributionMap.put(memberId, new ContributionResponse(memberId, nickname, validContribution, 0.0, isMyRecord));
            }
        }

        List<ContributionResponse> contributions = new ArrayList<>(contributionMap.values());

        // 총 기여도를 이용하여 각 사용자의 기여도(%) 재계산
        for (ContributionResponse contribution : contributions) {
            double contributionPercentage =
                    (totalContribution == 0) ? 0.0 : (contribution.getMeasurement() / totalContribution) * 100;
            contribution.setContributionPercentage(contributionPercentage);
        }

        return contributions;
    }


    @Transactional
    public void createChallenge(CreateChallengeRequest request) {
        if (request == null) {
            log.error("챌린지 생성 요청이 null입니다.");
            throw new ChallengeCreationException("챌린지 생성 요청이 유효하지 않습니다.");
        }

        try {
            Member host = userDetailsService.getMemberByContextHolder();
            MultipartFile image = request.getImage();
            String imgUrl = null;

            if (image != null && !image.isEmpty()) {
                try {
                    imgUrl = s3Service.uploadImageFile(image, bucketName);
                } catch (IOException e) {
                    log.error("S3 이미지 업로드 실패", e);
                    throw new ChallengeCreationException("S3 이미지 업로드 실패", e);
                }
            }

            request.setImagePath(imgUrl);

            Challenge challenge = Challenge.from(request, host);

            if (challenge == null) {
                log.error("Challenge 엔티티 변환 실패");
                throw new ChallengeCreationException("챌린지 엔티티 변환 중 오류 발생");
            }

            challengeRepository.save(challenge);
            log.info("챌린지 저장 성공: {}", challenge.getId());

        } catch (Exception e) {
            log.error("챌린지 생성 중 오류 발생", e);
            throw new ChallengeCreationException("챌린지 생성 실패", e);
        }
    }


    public void deleteChallenge(int id) {
        if (!challengeRepository.existsById(id)) {
            throw new ChallengeNotFoundException("존재하지 않는 챌린지 입니다. with id: " + id);
        }
        challengeRepository.deleteById(id);
    }

    // 챌린지 수행 기록
    public void recordChallenge(int memberId, int challengeId, Integer count, Integer duration, Integer distance) {
        // 챌린지 정보 조회
        Challenge challenge = challengeRepository.findById(challengeId)
                .orElseThrow(() -> new ChallengeNotFoundException("챌린지를 찾을 수 없습니다."));

        // 무조건 하나의 값만 NOT NULL이므로, 해당하는 기록 메서드만 호출
        if (challenge.getExerciseCount() != null) {
            recordCount(memberId, challengeId, count);
        } else if (challenge.getExerciseDistance() != null) {
            recordDistance(memberId, challengeId, distance);
        } else if (challenge.getExerciseDuration() != null) {
            recordDuration(memberId, challengeId, duration);
        }
        throw new IllegalStateException("count, duration, distance가 모두 null이면 안됩니다.");
    }

    @Transactional
    public void recordCount(int memberId, int challengeId, Integer count) {
        Challenge challenge = challengeRepository.findById(challengeId)
                .orElseThrow(() -> new ChallengeNotFoundException("존재하지 않는 챌린지 입니다. with id: " + challengeId));

        ChallengeMember challengeMember = challengeMemberRepository
                .findByChallengeIdAndMemberId(challengeId, memberId)
                .orElseThrow(() -> new IllegalStateException("아직 챌린지에 참여하지 않은 사용자 입니다. 먼저 챌린지에 참여해주세요."));

        Date today = new Date(); // 오늘 날짜

        Optional<ChallengeRecord> existingRecord = challengeRecordRepository.findByChallengeMemberAndCreatedAt(
                challengeMember, today);

        if (existingRecord.isPresent()) {
            ChallengeRecord record = existingRecord.get();
            if (count > record.getCount()) {
                record.setCount(count);
            }
            updateIsPassed(record, challenge);
            challengeRecordRepository.save(record);
        } else {
            boolean isPassed = checkIsPassed(count, null, null, challenge);
            ChallengeRecord newRecord = ChallengeRecord.builder()
                    .challenge(challenge)
                    .challengeMember(challengeMember)
                    .memberId(challengeMember.getMemberId())
                    .count(count)
                    .createdAt(today)
                    .isPassed(isPassed)
                    .build();
            challengeRecordRepository.save(newRecord);
        }
        if (challenge.getType().equals("TEAM")) {
            updateCountProgress(challenge);

        }
    }


    public void recordDistance(int memberId, int challengeId, Integer distance) {
        Challenge challenge = challengeRepository.findById(challengeId)
                .orElseThrow(() -> new ChallengeNotFoundException("존재하지 않는 챌린지 입니다. with id: " + challengeId));

        ChallengeMember challengeMember = challengeMemberRepository
                .findByChallengeIdAndMemberId(challengeId, memberId)
                .orElseThrow(() -> new IllegalStateException("아직 챌린지에 참여하지 않은 사용자 입니다. 먼저 챌린지에 참여해주세요."));

        Date today = new Date();

        Optional<ChallengeRecord> existingRecord = challengeRecordRepository.findByChallengeMemberAndCreatedAt(
                challengeMember, today);

        if (existingRecord.isPresent()) {
            ChallengeRecord record = existingRecord.get();
            Integer newDistance = record.getDistance() + distance;

            record.setDistance(newDistance);

            updateIsPassed(record, challenge);
            challengeRecordRepository.save(record);
        } else {
            boolean isPassed = checkIsPassed(null, null, distance, challenge);
            ChallengeRecord newRecord = ChallengeRecord.builder()
                    .challenge(challenge)
                    .challengeMember(challengeMember)
                    .memberId(challengeMember.getMemberId())
                    .distance(distance)
                    .createdAt(today)
                    .isPassed(isPassed)
                    .build();
            challengeRecordRepository.save(newRecord);
        }
        if (challenge.getType().equals("TEAM")) {
            updateDistanceProgress(challenge);
        }
    }


    public void recordDuration(int memberId, int challengeId, Integer duration) {
        Challenge challenge = challengeRepository.findById(challengeId)
                .orElseThrow(() -> new ChallengeNotFoundException("존재하지 않는 챌린지 입니다. with id: " + challengeId));

        ChallengeMember challengeMember = challengeMemberRepository
                .findByChallengeIdAndMemberId(challengeId, memberId)
                .orElseThrow(() -> new IllegalStateException("아직 챌린지에 참여하지 않은 사용자 입니다. 먼저 챌린지에 참여해주세요."));

        Date today = new Date(); // 오늘 날짜

        Optional<ChallengeRecord> existingRecord = challengeRecordRepository.findByChallengeMemberAndCreatedAt(
                challengeMember, today);

        if (existingRecord.isPresent()) {
            ChallengeRecord record = existingRecord.get();
            if (duration > record.getDuration()) {
                record.setDuration(duration);
            }
            updateIsPassed(record, challenge);
            challengeRecordRepository.save(record);
        } else {
            boolean isPassed = checkIsPassed(null, duration, null, challenge);
            ChallengeRecord newRecord = ChallengeRecord.builder()
                    .challenge(challenge)
                    .challengeMember(challengeMember)
                    .memberId(challengeMember.getMemberId())
                    .duration(duration)
                    .createdAt(today)
                    .isPassed(isPassed)
                    .build();
            challengeRecordRepository.save(newRecord);
        }
        if (challenge.getType().equals("TEAM")) {
            updateDurationProgress(challenge);
        }
    }


    // 기존의 챌린지 기록을 업데이트할 때, is_passed를 판정하는 함수
    private void updateIsPassed(ChallengeRecord record, Challenge challenge) {
        if ("TEAM".equals(challenge.getType()) && challenge.getProgress() >= 100F) {
            setAllTeamMembersPassed(challenge.getId());
        } else if (record.getCount() != null && challenge.getExerciseCount() != null
                && record.getCount() >= challenge.getExerciseCount()) {
            record.setIsPassed();
        } else if (record.getDuration() != null && challenge.getExerciseDuration() != null
                && record.getDuration() >= challenge.getExerciseDuration()) {
            record.setIsPassed();
        } else if (record.getDistance() != null && challenge.getExerciseDistance() != null
                && record.getDistance().compareTo(challenge.getExerciseDistance()) >= 0) {
            record.setIsPassed();
        }
    }


    // 챌린지 기록을 새로 생성할 때, is_passed를 판정하는 함수
    private boolean checkIsPassed(Integer count, Integer duration, Integer distance, Challenge challenge) {
        if ("TEAM".equals(challenge.getType())) {
            return challenge.getProgress() >= 100F;
        } else if (count != null && challenge.getExerciseCount() != null && count >= challenge.getExerciseCount()) {
            return true;
        } else if (duration != null && challenge.getExerciseDuration() != null
                && duration >= challenge.getExerciseDuration()) {
            return true;
        } else if (distance != null && challenge.getExerciseDistance() != null
                && distance >= challenge.getExerciseDistance()) {
            return true;
        }
        return false;
    }

    public void updateCountProgress(Challenge challenge) {
        List<ChallengeMember> members = challengeMemberRepository.findByChallengeId(challenge.getId());

        // 참여한 멤버들의 count 합산
        int totalCount = members.stream()
                .map(member -> challengeRecordRepository.sumCountByMemberIdAndChallengeId(member.getMemberId(),
                        challenge.getId()).orElse(0))
                .reduce(0, Integer::sum);

        // progress 계산
        float progress = (challenge.getExerciseCount() > 0)
                ? Math.round(((float) totalCount / challenge.getExerciseCount()) * 100)
                : 0.0f;

        // progress가 100을 초과하지 않도록 제한
        if (progress > 100) {
            progress = 100.0f;
        }

        challenge.setProgress(progress);
        challengeRepository.save(challenge);

        log.info("챌린지 {}의 progress가 {}로 업데이트됨.", challenge.getId(), progress);
    }

    public void updateDurationProgress(Challenge challenge) {
        List<ChallengeMember> members = challengeMemberRepository.findByChallengeId(challenge.getId());

        // 참여한 멤버들의 duration 합산
        int totalDuration = members.stream()
                .map(member -> challengeRecordRepository.sumDurationByMemberIdAndChallengeId(member.getMemberId(),
                        challenge.getId()).orElse(0))
                .reduce(0, Integer::sum);

        // progress 계산
        float progress = (challenge.getExerciseDuration() > 0)
                ? Math.round(((float) totalDuration / challenge.getExerciseDuration()) * 100)
                : 0.0f;

        // progress가 100을 초과하지 않도록 제한
        if (progress > 100) {
            progress = 100.0f;
        }

        challenge.setProgress(progress);
        challengeRepository.save(challenge);

        log.info("챌린지 {}의 progress (duration 기준) {}로 업데이트됨.", challenge.getId(), progress);
    }

    public void updateDistanceProgress(Challenge challenge) {
        List<ChallengeMember> members = challengeMemberRepository.findByChallengeId(challenge.getId());

        // 참여한 멤버들의 distance 합산
        int totalDistance = members.stream()
                .map(member -> challengeRecordRepository.sumDistanceByMemberIdAndChallengeId(member.getMemberId(),
                        challenge.getId()).orElse(0))
                .reduce(0, Integer::sum);

        // progress 계산
        float progress = (challenge.getExerciseDistance() > 0)
                ? Math.min(((float) totalDistance / challenge.getExerciseDistance()) * 100, 100.0f)
                : 0.0f;

        challenge.setProgress(progress);
        challengeRepository.save(challenge);

        log.info("챌린지 {}의 progress (distance 기준) {}로 업데이트됨.", challenge.getId(), progress);
    }


    private void setAllTeamMembersPassed(int challengeId) {
        List<ChallengeRecord> teamRecords = challengeRecordRepository.findByChallengeId(challengeId);

        for (ChallengeRecord record : teamRecords) {
            if (!record.isPassed()) { // 이미 true인 경우는 제외
                record.setIsPassed();
            }
        }

        challengeRecordRepository.saveAll(teamRecords);
        log.info("챌린지 {}의 모든 멤버 isPassed = true", challengeId);
    }

    //챌린지 기록 조회
    public List<ChallengeRecordResponse> getChallengeRecords(int memberId) {
        List<ChallengeRecord> records = challengeRecordRepository.findByMemberId(memberId);

        if (records.isEmpty()) {
            throw new ChallengeRecordNotFoundException("현재 유저의 챌린지 기록이 존재하지 않습니다.");
        }

        return records.stream()
                .map(ChallengeRecordResponse::fromEntity)
                .collect(Collectors.toList());
    }

//    public ChallengeRecordWithRankResponse getChallengeRecord(int memberId, int challengeId) {
//        ChallengeRecord record = challengeRecordRepository
//                .findByMemberIdAndChallengeId(memberId, challengeId)
//                .orElseThrow(() -> new ChallengeRecordNotFoundException(
//                        "challengeId: " + challengeId + "에 대한 챌린지 기록을 찾을 수 없습니다."));
//
//        return ChallengeRecordWithRankResponse.fromEntity(record);
//    }
public ChallengeRecordWithRankResponse getChallengeRecord(int memberId, int challengeId) {
    ChallengeRecord record = challengeRecordRepository
            .findByMemberIdAndChallengeId(memberId, challengeId)
            .orElseThrow(() -> new ChallengeRecordNotFoundException(
                    "challengeId: " + challengeId + "에 대한 챌린지 기록을 찾을 수 없습니다."));

    // 같은 챌린지에 속한 모든 참가자의 기록을 가져옴
    List<ChallengeRecord> challengeRecords = challengeRecordRepository.findByChallengeId(challengeId);

    // 랭킹 계산
    int rank = calculateRank(record, challengeRecords);

    // 기존 `fromEntity()` 메서드를 호출한 후, rank 값을 추가하여 반환
    ChallengeRecordWithRankResponse response = ChallengeRecordWithRankResponse.fromEntity(record);
    response.setRank(rank);

    return response;
}

    private int calculateRank(ChallengeRecord record, List<ChallengeRecord> challengeRecords) {
        // 챌린지 유형 가져오기
        String challengeType = record.getChallenge().getType();

        // 참가자의 기여도를 저장할 리스트
        Map<Integer, Double> memberContributionMap = new HashMap<>();

        for (ChallengeRecord cr : challengeRecords) {
            int memberId = cr.getMemberId();

            Double count = cr.getCount() != null ? cr.getCount().doubleValue() : null;
            Double duration = cr.getDuration() != null ? cr.getDuration().doubleValue() : null;
            Double distance = cr.getDistance() != null ? cr.getDistance().doubleValue() : null;

            double bestValue;
            if ("TEAM".equals(challengeType)) {
                // TEAM 챌린지: 모든 날의 측정치를 합산
                double totalValue = (count != null ? count : 0.0) +
                        (duration != null ? duration : 0.0) +
                        (distance != null ? distance : 0.0);
                memberContributionMap.put(memberId, memberContributionMap.getOrDefault(memberId, 0.0) + totalValue);
            } else {
                // NORMAL, SURVIVAL 챌린지: 가장 높은 측정치를 기준으로 랭킹 결정
                bestValue = (count != null) ? count : (duration != null) ? duration : (distance != null) ? distance : 0.0;
                if (!memberContributionMap.containsKey(memberId) || memberContributionMap.get(memberId) < bestValue) {
                    memberContributionMap.put(memberId, bestValue);
                }
            }
        }

        // 모든 참가자의 기여도를 리스트에 저장
        List<Double> contributions = new ArrayList<>(memberContributionMap.values());

        // 내 기여도 계산
        Double myBestValue = memberContributionMap.getOrDefault(record.getMemberId(), 0.0);

        // 내 기여도가 더 높은 순서대로 정렬 후, 내 순위 찾기
        contributions.sort(Collections.reverseOrder());
        int rank = contributions.indexOf(myBestValue) + 1; // 1등부터 시작하도록 설정

        return rank;
    }




    // 챌린지 참여
    public void joinChallenge(JoinChallengeRequest request) {
        Member member = userDetailsService.getMemberByContextHolder();
        Challenge challenge = challengeRepository.findById(request.getChallengeId())
                .orElseThrow(() -> new ChallengeNotFoundException(
                        "id: " + request.getChallengeId() + "인 챌린지를 찾을 수 없습니다."));

        boolean isAlreadyJoined = challengeMemberRepository.existsByChallengeIdAndMemberId(challenge.getId(),
                member.getId());

        if (isAlreadyJoined) {
            throw new IllegalStateException("이미 챌린지에 지원한 유저입니다.");
        }

        if (challenge.getStatus().equals("PROGRESS")) {
            throw new IllegalStateException("챌린지가 이미 진행 중입니다. 참여할 수 없습니다.");
        }
        if (challenge.getStatus().equals("END")) {
            throw new IllegalStateException("종료된 챌린지 입니다. 참여할 수 없습니다.");
        }

        increaseParticipants(request.getChallengeId());

        ChallengeMember challengeMember = ChallengeMember.builder()
                .challengeId(challenge.getId())
                .memberId(member.getId())
                .status("APPLIED")
                .joinAt(new Date())
                .build();
        challengeMemberRepository.save(challengeMember);
    }

    // 챌린지 탈퇴
    @Transactional
    public void leaveChallenge(int challengeId) {
        Member member = userDetailsService.getMemberByContextHolder();

        // 챌린지 멤버 확인
        ChallengeMember challengeMember = challengeMemberRepository
                .findByChallengeIdAndMemberId(challengeId, member.getId())
                .orElseThrow(() -> new IllegalStateException("챌린지에 참여하지 않은 사용자입니다."));

        // 챌린지 상태 확인
        Challenge challenge = challengeRepository.findById(challengeId)
                .orElseThrow(() -> new IllegalStateException("존재하지 않는 챌린지입니다."));

        if ("PROGRESS".equals(challenge.getStatus())) {
            throw new IllegalStateException("진행 중인 챌린지는 탈퇴할 수 없습니다.");
        }

        decreaseParticipants(challengeId);

        challengeMemberRepository.deleteByChallengeIdAndMemberId(challengeId, member.getId());
    }

    public void increaseParticipants(int challengeId) {
        Challenge challenge = challengeRepository.findById(challengeId)
                .orElseThrow(() -> new ChallengeNotFoundException("존재하지 않는 챌린지 입니다."));

        if (challenge.getCurrentParticipants() >= challenge.getMaxParticipants()) {
            throw new IllegalStateException("최대 인원 수에 도달했습니다.");
        }

        challenge.setCurrentParticipants(challenge.getCurrentParticipants() + 1);
        challengeRepository.save(challenge);
    }

    public void decreaseParticipants(int challengeId) {
        Challenge challenge = challengeRepository.findById(challengeId)
                .orElseThrow(() -> new ChallengeNotFoundException("존재하지 않는 챌린지 입니다."));

        if (challenge.getCurrentParticipants() <= 0) {
            throw new IllegalStateException("No participants to remove.");
        }

        challenge.setCurrentParticipants(challenge.getCurrentParticipants() - 1);
        challengeRepository.save(challenge);
    }

    // 내(트레이너)가 생성한 챌린지 목록
    public List<ChallengeResponse> getCreatedChallenges() {
        Member member = userDetailsService.getMemberByContextHolder();
        List<Challenge> challenges = challengeRepository.findByHostId(member.getId());
        return challenges.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    // 내가 참여중인 챌린지 목록: challenge_member.status == "JOINED"
    public List<ChallengeResponse> getParticipatingChallenges() {
        Member member = userDetailsService.getMemberByContextHolder();
        List<Integer> challengeIds = challengeMemberRepository.findChallengeIdsByMemberIdAndStatus(member.getId(),
                "JOINED");
        List<Challenge> challenges = challengeRepository.findByIdIn(challengeIds);
        return challenges.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    // 내가 신청한 챌린지 목록 (예: status가 "APPLIED")
    public List<ChallengeResponse> getAppliedChallenges() {
        Member member = userDetailsService.getMemberByContextHolder();
        List<Integer> challengeIds = challengeMemberRepository.findChallengeIdsByMemberIdAndStatus(member.getId(),
                "APPLIED");
        List<Challenge> challenges = challengeRepository.findByIdIn(challengeIds);
        return challenges.stream().map(this::mapToResponse).collect(Collectors.toList());
    }

    // 내가 참여했던 챌린지 목록: challenge_member.status == "END"
    public List<ChallengeResponse> getPastChallenges() {
        Member member = userDetailsService.getMemberByContextHolder();
        List<Integer> challengeIds = challengeMemberRepository.findChallengeIdsByMemberIdAndStatus(member.getId(),
                "END");
        List<Challenge> challenges = challengeRepository.findByIdIn(challengeIds);
        return challenges.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }


    // 진행중인 챌린지 (전체, 날짜 조건: start_date ≤ 오늘 ≤ end_date)
    public List<ChallengeResponse> getOngoingChallenges() {
        Date now = new Date();
        List<Challenge> challenges = challengeRepository.findByStartDateLessThanEqualAndEndDateGreaterThanEqual(now,
                now);
        return challenges.stream().map(this::mapToResponse).collect(Collectors.toList());
    }

    // 종료된 챌린지 (전체, 날짜 조건: end_date < 오늘)
    public List<ChallengeResponse> getEndedChallenges() {
        Date now = new Date();
        List<Challenge> challenges = challengeRepository.findByEndDateLessThan(now);
        return challenges.stream().map(this::mapToResponse).collect(Collectors.toList());
    }

    // 개최예정인 챌린지 (전체, 날짜 조건: start_date > 오늘)
    public List<ChallengeResponse> getUpcomingChallenges() {
        Date now = new Date();
        List<Challenge> challenges = challengeRepository.findByStartDateGreaterThan(now);
        return challenges.stream().map(this::mapToResponse).collect(Collectors.toList());
    }

    @Scheduled(cron = "10 0 0 * * *")
    @Transactional
    public void updateChallengeAndMember() {
        Calendar cal = Calendar.getInstance();

        // 하루 전 날짜 계산 (start_date 기준)
        cal.setTime(new Date());
        cal.add(Calendar.DATE, -1);
        Date startTargetDate = cal.getTime();

        // 하루 전 날짜 계산 (end_date 기준)
        cal.setTime(new Date());
        cal.add(Calendar.DATE, -1);
        Date endTargetDate = cal.getTime();

        log.info("챌린지 상태 변경 스케줄러 실행 (start_date 기준: {}, end_date 기준: {})", startTargetDate, endTargetDate);

        // start_date가 오늘보다 하루 전인 챌린지를 PROGRESS 상태로 변경
        List<Challenge> startingChallenges = challengeRepository.findByStartDateAndStatus(startTargetDate, "OPEN");

        for (Challenge challenge : startingChallenges) {
            log.info("진행 시작된 챌린지 ID: {}", challenge.getId());
            List<ChallengeMember> members = challengeMemberRepository.findByChallengeId(challenge.getId());
            challenge.setStatus("PROGRESS");

            if (members.isEmpty()) {
                log.info("챌린지 {}에 참가한 멤버가 없습니다.", challenge.getId());
                challengeRepository.save(challenge);
                continue;
            }

            for (ChallengeMember member : members) {
                member.setStatus("JOINED");
            }
            challengeMemberRepository.saveAll(members);
            challengeRepository.save(challenge);
        }

        // end_date가 오늘보다 하루 전인 챌린지를 ENDED 상태로 변경
        List<Challenge> endingChallenges = challengeRepository.findByEndDateAndStatus(endTargetDate, "PROGRESS");

        for (Challenge challenge : endingChallenges) {
            log.info("종료된 챌린지 ID: {}", challenge.getId());

            List<ChallengeMember> members = challengeMemberRepository.findByChallengeId(challenge.getId());
            challenge.setStatus("END");

            if (members.isEmpty()) {
                log.info("챌린지 {}에 참가한 멤버가 없습니다.", challenge.getId());
                challengeRepository.save(challenge);
                continue;
            }

            for (ChallengeMember member : members) {
                member.setStatus("ENDED");
            }
            challengeMemberRepository.saveAll(members);

            // 가장 높은 count, duration, distance를 기록한 멤버 찾기
            int winnerId = findWinner(challenge, members);

            if (winnerId != -1) {
                challenge.setWinner(winnerId);
                log.info("챌린지 {} 우승자: Member ID {}", challenge.getId(), winnerId);
            }
            challengeRepository.save(challenge);
        }
    }

    // 가장 높은 count, duration, distance를 기록한 멤버 찾기
    private int findWinner(Challenge challenge, List<ChallengeMember> members) {
        int winnerId = -1;

        // 각 기준의 최댓값 저장
        int maxCount = 0;
        int maxDuration = 0;
        int maxDistance = 0;

        for (ChallengeMember member : members) {
            int count = challengeRecordRepository.findCountByChallengeMemberId(member.getId()).orElse(0);
            int duration = challengeRecordRepository.findDurationByChallengeMemberId(member.getId()).orElse(0);
            int distance = challengeRecordRepository.findDistanceByChallengeMemberId(member.getId()).orElse(0);

            if (challenge.getExerciseCount() != null && count > maxCount) {
                maxCount = count;
                winnerId = member.getMemberId();
            }

            if (challenge.getExerciseDuration() != null && duration > maxDuration) {
                maxDuration = duration;
                winnerId = member.getMemberId();
            }

            if (challenge.getExerciseDistance() != null && distance > maxDistance) {
                maxDistance = distance;
                winnerId = member.getMemberId();
            }
        }
        return winnerId;
    }


    // 엔티티 → DTO 매핑
    private ChallengeResponse mapToResponse(Challenge challenge) {
        return ChallengeResponse.builder()
                .id(challenge.getId())
                .type(challenge.getType())
                .title(challenge.getTitle())
                .description(challenge.getDescription())
                .reward(challenge.getReward())
                .templateId(challenge.getTemplateId())
                .startDate(challenge.getStartDate())
                .endDate(challenge.getEndDate())
                .createdAt(challenge.getCreatedAt())
                .status(challenge.getStatus())
                .currentParticipants(challenge.getCurrentParticipants())
                .maxParticipants(challenge.getMaxParticipants())
                .frequency(challenge.getFrequency())
                .imagePath(challenge.getImagePath())
                .progress(challenge.getProgress())
                .exerciseType(challenge.getExerciseType())
                .exerciseCount(challenge.getExerciseCount())
                .exerciseDistance(challenge.getExerciseDistance())
                .exerciseDuration(challenge.getExerciseDuration())
                .build();
    }
}
