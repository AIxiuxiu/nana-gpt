package com.website.websocket.support;

import lombok.Data;

import javax.websocket.Session;
import java.util.ArrayList;
import java.util.List;

/**
 * @author ahl
 * @desc
 * @create 2023/1/11 09:26
 */
@Data
public class CustomSession {
    private Session session;
    /**
     * 多页面情况
     */
    private List<Session> sessionList;
    private String userId;

    public CustomSession(Session session, String userId) {
        this.session = session;
        this.userId = userId;
        this.sessionList = new ArrayList<>();
        this.sessionList.add(session);
    }

}
