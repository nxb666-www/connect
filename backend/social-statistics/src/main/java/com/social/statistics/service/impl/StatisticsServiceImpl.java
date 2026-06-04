package com.social.statistics.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.social.statistics.entity.Statistics;
import com.social.statistics.feign.PostFeignClient;
import com.social.statistics.feign.UserFeignClient;
import com.social.statistics.mapper.StatisticsMapper;
import com.social.statistics.service.StatisticsService;
import com.social.statistics.vo.StatisticsVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class StatisticsServiceImpl implements StatisticsService {

    private final StatisticsMapper statisticsMapper;
    private final UserFeignClient userFeignClient;
    private final PostFeignClient postFeignClient;

    @Scheduled(cron = "0 0 2 * * ?")
    public void generateDailyStats() {
        String yesterday = LocalDate.now().minusDays(1).format(DateTimeFormatter.ISO_LOCAL_DATE);
        log.info("开始生成 {} 统计数据", yesterday);

        try {
            Statistics stats = new Statistics();
            stats.setDate(LocalDate.parse(yesterday));

            try {
                Map<String, Object> userStats = userFeignClient.getUserStats(yesterday);
                stats.setNewUsers(userStats.get("newUsers") != null ? ((Number) userStats.get("newUsers")).intValue() : 0);
            } catch (Exception e) {
                log.warn("获取用户统计失败: {}", e.getMessage());
            }

            try {
                Map<String, Object> postStats = postFeignClient.getPostStats(yesterday);
                stats.setNewPosts(postStats.get("newPosts") != null ? ((Number) postStats.get("newPosts")).intValue() : 0);
                stats.setTotalLikes(postStats.get("totalLikes") != null ? ((Number) postStats.get("totalLikes")).intValue() : 0);
                stats.setTotalComments(postStats.get("totalComments") != null ? ((Number) postStats.get("totalComments")).intValue() : 0);
            } catch (Exception e) {
                log.warn("获取动态统计失败: {}", e.getMessage());
            }

            // check if already exists
            LambdaQueryWrapper<Statistics> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(Statistics::getDate, stats.getDate());
            Statistics existing = statisticsMapper.selectOne(wrapper);
            if (existing != null) {
                stats.setId(existing.getId());
                statisticsMapper.updateById(stats);
            } else {
                statisticsMapper.insert(stats);
            }
            log.info("统计完成: date={}, newUsers={}, newPosts={}", stats.getDate(), stats.getNewUsers(), stats.getNewPosts());
        } catch (Exception e) {
            log.error("统计生成失败", e);
        }
    }

    @Override
    public StatisticsVO getPlatformStatistics() {
        StatisticsVO vo = new StatisticsVO();

        LambdaQueryWrapper<Statistics> wrapper = new LambdaQueryWrapper<>();
        wrapper.orderByDesc(Statistics::getDate).last("LIMIT 30");
        List<Statistics> stats = statisticsMapper.selectList(wrapper);

        vo.setTotalUsers(stats.stream().mapToLong(s -> s.getNewUsers().longValue()).sum());
        vo.setTotalPosts(stats.stream().mapToLong(s -> s.getNewPosts().longValue()).sum());
        vo.setTodayNewUsers(stats.isEmpty() ? 0L : stats.get(0).getNewUsers().longValue());
        vo.setTodayNewPosts(stats.isEmpty() ? 0L : stats.get(0).getNewPosts().longValue());

        List<StatisticsVO.DailyStats> dailyStats = new ArrayList<>();
        for (Statistics stat : stats) {
            StatisticsVO.DailyStats daily = new StatisticsVO.DailyStats();
            daily.setDate(stat.getDate());
            daily.setNewUsers(stat.getNewUsers());
            daily.setActiveUsers(stat.getActiveUsers());
            daily.setNewPosts(stat.getNewPosts());
            daily.setTotalLikes(stat.getTotalLikes());
            daily.setTotalComments(stat.getTotalComments());
            dailyStats.add(daily);
        }
        vo.setDailyStats(dailyStats);

        return vo;
    }

    @Override
    public StatisticsVO getStatisticsByDateRange(String startDate, String endDate) {
        StatisticsVO vo = new StatisticsVO();

        LambdaQueryWrapper<Statistics> wrapper = new LambdaQueryWrapper<>();
        wrapper.ge(Statistics::getDate, LocalDate.parse(startDate))
                .le(Statistics::getDate, LocalDate.parse(endDate))
                .orderByAsc(Statistics::getDate);
        List<Statistics> stats = statisticsMapper.selectList(wrapper);

        vo.setTotalUsers(stats.stream().mapToLong(s -> s.getNewUsers().longValue()).sum());
        vo.setTotalPosts(stats.stream().mapToLong(s -> s.getNewPosts().longValue()).sum());

        List<StatisticsVO.DailyStats> dailyStats = new ArrayList<>();
        for (Statistics stat : stats) {
            StatisticsVO.DailyStats daily = new StatisticsVO.DailyStats();
            daily.setDate(stat.getDate());
            daily.setNewUsers(stat.getNewUsers());
            daily.setActiveUsers(stat.getActiveUsers());
            daily.setNewPosts(stat.getNewPosts());
            daily.setTotalLikes(stat.getTotalLikes());
            daily.setTotalComments(stat.getTotalComments());
            dailyStats.add(daily);
        }
        vo.setDailyStats(dailyStats);

        return vo;
    }
}
