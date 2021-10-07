package com.sanri.tools.modules.tcp.controller;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

/**
 * 服务端对客户端分组数据发送
 */
@Data
public class GroupSendMessage {
    private List<String> hostAndPorts = new ArrayList<>();
    private String ascii;
    private String hex;
}
