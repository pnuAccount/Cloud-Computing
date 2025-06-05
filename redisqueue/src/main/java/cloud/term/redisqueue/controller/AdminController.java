package cloud.term.redisqueue.controller;

import cloud.term.redisqueue.service.RedisSettingsService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@Controller
@RequiredArgsConstructor
public class AdminController {

    private final RedisSettingsService redisSettingsService;

    // 관리자 페이지 렌더링
    @GetMapping("/system")
    public String getMasterPage(Model model) {
        Map<String, String> settings = redisSettingsService.getAllSettings();
        model.addAttribute("settings", settings);
        return "system";
    }

    // 설정 업데이트 처리
    @PostMapping("/system")
    public String updateSettings(@RequestParam Map<String, String> params, Model model) {
        params.forEach((key, value) -> {
            if (value != null && !value.isBlank()) {
                redisSettingsService.updateSetting(key, value);
            }
        });

        model.addAttribute("settings", redisSettingsService.getAllSettings());
        model.addAttribute("message", "설정이 성공적으로 저장되었습니다. 약 30초 후에 모든 서버에 적용됩니다.");
        return "system";
    }
}