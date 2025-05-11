package com.pose.server.core.member.api;

import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class HomeController {

    @GetMapping("/")
    public String root() {
        return "index";  // 또는 별도 루트 페이지
    }

    @GetMapping("/loginhome")
    public String loginhome(HttpSession session, Model model) {
        String userId = (String) session.getAttribute("user");
        String role = session.getAttribute("role").toString();

        if (userId != null) {
            model.addAttribute("user", userId); // 세션에 있는 사용자 정보 전달
            model.addAttribute("role", role);
        }

        return "indexG";
    }
}