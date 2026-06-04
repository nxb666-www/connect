package com.social.search.service;

import com.social.search.entity.SearchHistory;
import com.social.search.vo.SearchResultVO;

import java.util.List;

public interface SearchService {

    SearchResultVO search(String keyword, Long userId);

    List<SearchHistory> getSearchHistory(Long userId);

    void clearSearchHistory(Long userId);
}
