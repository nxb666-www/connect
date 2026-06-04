package com.social.statistics.controller;

import com.social.common.core.result.Result;
import com.social.statistics.service.StatisticsService;
import com.social.statistics.vo.StatisticsVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@Tag(name = "统计管理", description = "平台统计数据接口")
@RestController
@RequestMapping("/api/statistics")
@RequiredArgsConstructor
public class StatisticsController {

    private final StatisticsService statisticsService;

    @Operation(summary = "获取平台统计数据")
    @GetMapping("/platform")
    public Result<StatisticsVO> getPlatformStatistics() {
        return Result.success(statisticsService.getPlatformStatistics());
    }

    @Operation(summary = "按日期范围获取统计数据")
    @GetMapping("/range")
    public Result<StatisticsVO> getStatisticsByDateRange(
            @RequestParam String startDate,
            @RequestParam String endDate) {
        return Result.success(statisticsService.getStatisticsByDateRange(startDate, endDate));
    }
}
