package com.social.search.controller;

import com.social.common.core.result.Result;
import com.social.common.security.context.LoginUserContext;
import com.social.search.entity.SearchHistory;
import com.social.search.service.SearchService;
import com.social.search.vo.SearchResultVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "搜索管理", description = "搜索、搜索历史等接口")
@RestController
@RequestMapping("/api/search")
@RequiredArgsConstructor
public class SearchController {

    private final SearchService searchService;

    @Operation(summary = "综合搜索")
    @GetMapping
    public Result<SearchResultVO> search(@RequestParam String keyword) {
        Long userId = LoginUserContext.getUserId();
        SearchResultVO result = searchService.search(keyword, userId);
        return Result.success(result);
    }

    @Operation(summary = "获取搜索历史")
    @GetMapping("/history")
    public Result<List<SearchHistory>> getSearchHistory() {
        Long userId = LoginUserContext.getUserId();
        return Result.success(searchService.getSearchHistory(userId));
    }

    @Operation(summary = "清空搜索历史")
    @DeleteMapping("/history")
    public Result<Void> clearSearchHistory() {
        Long userId = LoginUserContext.getUserId();
        searchService.clearSearchHistory(userId);
        return Result.success();
    }
}
