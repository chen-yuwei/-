package com.example.reading.controller.admin;

import com.example.reading.common.Result;
import com.example.reading.service.AdminStatisticsService;
import com.example.reading.vo.StatisticsVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "管理端-统计")
@RestController
@RequestMapping("/api/admin/statistics")
@RequiredArgsConstructor
public class AdminStatisticsController {

    private final AdminStatisticsService adminStatisticsService;

    @Operation(summary = "后台首页统计")
    @GetMapping
    public Result<StatisticsVO> getStatistics() {
        return Result.success(adminStatisticsService.getStatistics());
    }
}
