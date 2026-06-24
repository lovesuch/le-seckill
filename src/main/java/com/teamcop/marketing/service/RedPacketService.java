package com.teamcop.marketing.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.teamcop.marketing.entity.RedPacket;
import org.springframework.stereotype.Service;

@Service
public interface RedPacketService extends IService<RedPacket> {
    RedPacketVO receiveRedPacket(String activityId, String userId);
    RedPacketVO getRedPacketDetail(Long redPacketId, String userId);
}
