package com.social.statistics.vo;

import lombok.Data;

import java.io.Serializable;
import java.time.LocalDate;
import java.util.List;

@Data
public class StatisticsVO implements Serializable {

    private Long totalUsers;

    private Long totalPosts;

    private Long todayNewUsers;

    private Long todayNewPosts;

    private List<DailyStats> dailyStats;

    @Data
    public static class DailyStats implements Serializable {
        private LocalDate date;
        private Integer newUsers;
        private Integer activeUsers;
        private Integer newPosts;
        private Integer totalLikes;
        private Integer totalComments;
    }
}
