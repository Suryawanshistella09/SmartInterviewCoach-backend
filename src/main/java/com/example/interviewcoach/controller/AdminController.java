package com.example.interviewcoach.controller;

import com.example.interviewcoach.service.AdminStatsService;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/admin")
@CrossOrigin(origins = "http://localhost:5173")
public class AdminController {

    private final AdminStatsService adminStatsService;

    public AdminController(AdminStatsService adminStatsService) {
        this.adminStatsService = adminStatsService;
    }


    // =====================================================
    // GET ADMIN DASHBOARD STATISTICS
    // =====================================================

    @GetMapping("/stats")
    public ResponseEntity<Map<String, Object>> getAdminStats() {

        Map<String, Object> stats = adminStatsService.getAdminStats();

        return ResponseEntity.ok(stats);
    }
}
