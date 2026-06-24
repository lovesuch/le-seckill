package com.teamcop.marketing.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.teamcop.marketing.entity.RedPacket;
import com.teamcop.marketing.service.RedPacketService;
import com.teamcop.marketing.vo.RedPacketVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/red-packets")
public class RedPacketController {
    @Autowired
    private RedPacketService redPacketService;

    @GetMapping("/{activityId}")
    public RedPacketVO receiveRedPacket(@PathVariable String activityId) {
        // 调用服务层方法
        return null;
    }

    @GetMapping("/detail/{redPacketId}")
    public RedPacketVO getRedPacketDetail(@PathVariable Long redPacketId) {
        // 调用服务层方法
        return null;
    }
}
