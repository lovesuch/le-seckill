package com.teamcop.marketing.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.teamcop.marketing.entity.RedPacket;
import com.teamcop.marketing.mapper.RedPacketMapper;
import com.teamcop.marketing.service.RedPacketService;
import com.teamcop.marketing.vo.RedPacketVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class RedPacketServiceImpl extends ServiceImpl<RedPacketMapper, RedPacket> implements RedPacketService {
    @Override
    @Transactional(rollbackFor = Exception.class)
    public RedPacketVO receiveRedPacket(String activityId, String userId) {
        // 校验活动是否存在且进行中
        // 创建红包记录
        // 返回红包详情
        return null;
    }

    @Override
    public RedPacketVO getRedPacketDetail(Long redPacketId, String userId) {
        // 查询红包详情
        // 权限校验
        // 返回红包详情
        return null;
    }
}
