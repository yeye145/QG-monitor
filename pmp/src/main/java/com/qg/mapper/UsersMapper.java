package com.qg.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.qg.domain.Users;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Update;
import org.springframework.data.repository.query.Param;

@Mapper
public interface UsersMapper extends BaseMapper<Users> {
    /**
     * 更新用户头像
     *
     * @param userId
     * @param avatarUrl
     * @return
     */
    @Update("UPDATE users SET avatar = #{avatarUrl} WHERE id = #{userId}")
    Integer updateAvatar(@Param("userId") Long userId, @Param("avatarUrl") String avatarUrl);
}
