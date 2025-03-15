package com.opt.ssafy.optback.domain.menu.service;

import com.opt.ssafy.optback.domain.auth.application.UserDetailsServiceImpl;
import com.opt.ssafy.optback.domain.member.entity.Member;
import com.opt.ssafy.optback.domain.menu.entity.Menu;
import com.opt.ssafy.optback.domain.menu.exception.MenuNotFoundException;
import com.opt.ssafy.optback.domain.menu.exception.MenuNotSaveException;
import com.opt.ssafy.optback.domain.menu.repository.MenuRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MenuService {

    private final MenuRepository menuRepository;
    private final UserDetailsServiceImpl userDetailsService;

    // 트레이너의 메뉴 조회
    public List<Menu> findByTrainerId(int trainerId) {
        return menuRepository.findByTrainerId(trainerId);
    }

    // 메뉴 조회
    public Menu findMenuById(int id) {
        Menu m = menuRepository.findById(id).orElseThrow(() -> new MenuNotFoundException("메뉴 조회 실패"));
        return m;
    }
    
    // 메뉴 등록
    @Transactional
    public Menu saveMenu(Menu menu) {
        Member member = userDetailsService.getMemberByContextHolder();
        int memberId = member.getId();

        Menu newMenu = Menu.builder()
                .name(menu.getName())
                .trainerId(memberId)
                .price(menu.getPrice())
                .totalSessions(menu.getTotalSessions())
                .build();

        return menuRepository.save(newMenu);
    }

    // 메뉴 삭제
    @Transactional
    public void deleteMenu(int id) {
        Menu findMenu = findMenuById(id);
        checkTrainerAuthority(findMenu);

        try {
            menuRepository.deleteById(id);
        } catch (Exception e) {
            throw new MenuNotSaveException("메뉴 삭제 실패");
        }

    }

    // 메뉴 수정
    @Transactional
    public Menu updateMenu(int id, Menu updatedMenu) {
        Menu findMenu = findMenuById(id);
        checkTrainerAuthority(findMenu);
        try {
            findMenu.setNewRecord(updatedMenu);
        } catch (Exception e) {
            throw new MenuNotSaveException("메뉴 수정 실패");
        }
        return menuRepository.save(findMenu);
    }

    // 로그인한 유저 확인
    private void checkTrainerAuthority(Menu menu) {
        Member member = userDetailsService.getMemberByContextHolder();
        int memberId = member.getId();

        if (memberId != menu.getTrainerId()) {
            throw new MenuNotSaveException("권한이 없습니다");
        }
    }
}
