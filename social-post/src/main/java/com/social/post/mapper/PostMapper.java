package com.social.post.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.social.post.entity.Post;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface PostMapper extends BaseMapper<Post> {
}
