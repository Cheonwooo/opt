package com.opt.ssafy.optback.domain.ai_report.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.opt.ssafy.optback.domain.ai_report.dto.WeeklyDateRange;
import com.opt.ssafy.optback.domain.ai_report.dto.WeeklyExerciseSummary;
import com.opt.ssafy.optback.domain.ai_report.dto.WeeklyMealSummary;
import com.opt.ssafy.optback.domain.ai_report.entity.AiReport;
import com.opt.ssafy.optback.domain.ai_report.repository.AiReportRepository;
import com.opt.ssafy.optback.domain.auth.application.UserDetailsServiceImpl;
import com.opt.ssafy.optback.domain.exercise.entity.ExerciseRecord;
import com.opt.ssafy.optback.domain.exercise.repository.ExerciseRecordRepository;
import com.opt.ssafy.optback.domain.meal_record.entity.MealRecord;
import com.opt.ssafy.optback.domain.meal_record.repository.MealRecordRepository;
import com.opt.ssafy.optback.domain.member.entity.Member;
import com.opt.ssafy.optback.domain.member.repository.MemberRepository;
import com.opt.ssafy.optback.global.application.GPTService;
import com.opt.ssafy.optback.global.exception.GPTException;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.temporal.WeekFields;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AiReportService {

    private final AiReportRepository aiReportRepository;
    private final MealRecordRepository mealRecordRepository;
    private final ExerciseRecordRepository exerciseRecordRepository;
    private final MemberRepository memberRepository;
    private final GPTService gptService;
    private final UserDetailsServiceImpl userDetailsService;

    public AiReport getAiReportContent(int year, int month, int weekNumber) {
        Member member = userDetailsService.getMemberByContextHolder();
        return aiReportRepository.findByMemberIdAndYearAndMonthAndWeekNumber(member.getId(), year, month, weekNumber)
                .orElseGet(() -> createEmptyReport(member.getId(), year, month, weekNumber));
    }

    private AiReport createEmptyReport(int memberId, int year, int month, int weekNumber) {
        return AiReport.builder()
                .member(memberRepository.findById(memberId).orElse(null))
                .content("해당 기간의 AI 리포트가 존재하지 않습니다.")
                .year(year)
                .month(month)
                .weekNumber(weekNumber)
                .build();
    }

    @Scheduled(cron = "0 0 3 * * MON")
    public void generateWeeklyReport() {
        WeeklyDateRange dateRange = getReponrtDateRange();
        List<Member> members = memberRepository.findAll();
        for (Member member : members) {
            generateReportForMember(member, dateRange);
        }
    }

    // 날짜 지정
    private WeeklyDateRange getReponrtDateRange() {
        LocalDate today = LocalDate.now();
        LocalDate startDate = today.with(DayOfWeek.MONDAY).minusWeeks(1);
        LocalDate endDate = startDate.plusDays(6);
        int year = startDate.getYear();
        int month = startDate.getMonthValue();
        int weekNumber = startDate.get(WeekFields.of(Locale.getDefault()).weekOfMonth());

        return new WeeklyDateRange(startDate, endDate, year, month, weekNumber);
    }

    // 주간 리포트 생성
    @Transactional
    public void generateReportForMember(Member member, WeeklyDateRange dateRange) {

        WeeklyExerciseSummary exerciseSummary = getWeeklyExerciseSummary(member.getId(), dateRange.getStartDate(),
                dateRange.getEndDate());
        WeeklyMealSummary mealSummary = getWeeklyMealSummary(member.getId(), dateRange.getStartDate(),
                dateRange.getEndDate());

        String aiContent = generateReportForMember(member, exerciseSummary, mealSummary);

        if (exerciseSummary.getTotalWorkouts() == 0 && mealSummary.getTotalCalories() == 0) {
            return;
        }

        AiReport aiReport = AiReport.builder()
                .member(member)
                .content(aiContent)
                .year(dateRange.getYear())
                .month(dateRange.getMonth())
                .weekNumber(dateRange.getWeekNumber())
                .build();

        aiReportRepository.save(aiReport);
    }

    private WeeklyMealSummary getWeeklyMealSummary(int memberId, LocalDate startDate, LocalDate endDate) {
        List<MealRecord> meals = mealRecordRepository.findByMemberIdAndCreatedDateBetween(memberId, startDate, endDate);

        int totalCalories = meals.stream().mapToInt(MealRecord::getCalorie).sum();
        float totalProtein = (float) meals.stream().mapToDouble(MealRecord::getProtein).sum();
        float totalCarb = (float) meals.stream().mapToDouble(MealRecord::getCarb).sum();
        float totalFat = (float) meals.stream().mapToDouble(MealRecord::getFat).sum();

        return WeeklyMealSummary.builder()
                .totalCalories(totalCalories)
                .totalProtein(totalProtein)
                .totalCarb(totalCarb)
                .totalFat(totalFat)
                .build();
    }

    private WeeklyExerciseSummary getWeeklyExerciseSummary(int memberId, LocalDate startDate, LocalDate endDate) {
        List<ExerciseRecord> records = exerciseRecordRepository.findByMemberIdAndCreatedAtBetween(memberId, startDate,
                endDate);
        int totalWorkouts = records.size();
        int totalDuration = records.stream().mapToInt(e -> e.getDuration() != null ? e.getDuration() : 0).sum();
        int totalDistance = records.stream().mapToInt(e -> e.getDistance() != null ? e.getDistance() : 0).sum();
        Map<String, Long> exerciseFrequency = records.stream()
                .collect(Collectors.groupingBy(e -> e.getExercise().getName(), Collectors.counting()));

        return WeeklyExerciseSummary.builder()
                .totalWorkouts(totalWorkouts)
                .totalDuration(totalDuration)
                .totalDistance(totalDistance)
                .exerciseFrequency(exerciseFrequency)
                .build();
    }

    // AI 리포트 생성
    private String generateReportForMember(Member member, WeeklyExerciseSummary exerciseSummary,
                                           WeeklyMealSummary mealSummary) {
        // 관심사
        String interest = getMemberInterest(member);

        //프롬프트 생성
        String prompt = generatePrompt(interest, exerciseSummary, mealSummary);

        //GPT응답
        Object response = gptService.requestGPT(prompt);

        // 마크다운 형식 그대로 반환
        if (response instanceof String) {
            return (String) response;
            // JSON 응답이 반환될 경우
        } else if (response instanceof Map) {
            return response.toString();
        } else {
            throw new GPTException("AI 보고서 생성 중 GPT 응답이 실패했습니다");
        }
    }

    // 사용자 목표 조회
    private String getMemberInterest(Member member) {
        return "건강 개선";
    }

    // GPT 응답 중 필요한 내용(content)만 추출
    private String contentResponse(List<Map<String, Object>> gptResponse) {
        if (gptResponse.isEmpty()) {
            throw new GPTException("AI보고서 생성 중 GPT응답이 실패했습니다");
        }
        return gptResponse.get(0).get("content").toString();
    }

    // GPT에게 보내기 전에 프롬프트 내용을 JSON형태로 변환
    private String convertToJson(Object object) {
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            String jsonString = objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(object);
            return jsonString.equals("{}") ? "해당하는 기록이 없습니다" : jsonString;
        } catch (Exception e) {
            return "";
        }
    }

    // GPT 프롬프트 생성
    private String generatePrompt(String goal, WeeklyExerciseSummary exerciseSummary, WeeklyMealSummary mealSummary) {
        return String.format("""
                        당신은 스포츠 과학 전문가이자 영양학자입니다.
                        사용자의 지난 7일 동안의 운동 및 식단 데이터를 바탕으로 주간 건강 보고서를 작성해 주세요.
                        사용자는 건강한 삶을 위해 운동과 식단을 관리하고 있으며, AI 리포트가 이를 도와줍니다.
                        보고서는 저장 용량이 65,535자 (64KB)를 넘지 않도록 작성하세요.
                        
                        목표: %s
                        
                        ## 🏋️ 운동 분석
                        - 총 운동 횟수: %d회
                        - 주간 총 운동 시간: %d분
                        - 총 이동 거리 (유산소): %dkm
                        - 운동별 수행 횟수:
                        
                        ```json
                        %s
                        ```
                        
                        AI는 위 데이터를 바탕으로 **운동 강도 분석**, **운동 패턴 평가**, **개선 방향**을 제시하세요.
                        
                        ## 🍽 식단 분석
                        - 총 섭취 칼로리: %dkcal
                        - 총 단백질 섭취량: %.1fg
                        - 총 탄수화물 섭취량: %.1fg
                        - 총 지방 섭취량: %.1fg
                        
                        AI는 위 데이터를 바탕으로 **영양 균형 분석**을 수행하고, 사용자의 목표에 맞는 최적의 식단 조정을 제안하세요.
                        
                        ## 📈 개선 방향
                        - 사용자의 목표 대비 현재 운동 및 식단의 강점과 약점 분석
                        - 목표 달성을 위해 다음 주에 적용할 수 있는 맞춤형 조언 제공
                        
                        **반드시 리포트를 Markdown 형식으로 작성하세요.**
                        """,
                goal,
                exerciseSummary.getTotalWorkouts(),
                exerciseSummary.getTotalDuration(),
                exerciseSummary.getTotalDistance(),
                convertToJson(exerciseSummary.getExerciseFrequency()),
                mealSummary.getTotalCalories(),
                mealSummary.getTotalProtein(),
                mealSummary.getTotalCarb(),
                mealSummary.getTotalFat()
        );
    }

}
