package com.social.statistics.service;

import com.social.statistics.vo.StatisticsVO;

public interface StatisticsService {

    StatisticsVO getPlatformStatistics();

    StatisticsVO getStatisticsByDateRange(String startDate, String endDate);
}
