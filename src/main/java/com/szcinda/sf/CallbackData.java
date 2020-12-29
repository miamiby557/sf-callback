package com.szcinda.sf;

import lombok.Data;

import java.io.Serializable;

@Data
public class CallbackData implements Serializable {
    private String notify;
    private CallbackData.Info info;
    private CallbackData.Subject subject;
    private String data;
    private String timestamp;

    @Data
    class Subject implements Serializable {
        private String caller;
        private String called;
        private Integer business;
        private Integer ttsCount;
        private Integer ttsLength;
        private Integer ivrCount;
        private Integer ivrTime;
        private Integer duration;
        private Double cost;
        private String recordFilename;
        private Integer recordSize;
        private String createTime;
        private String answerTime;
        private String releaseTime;
        private String dtmf;
        private Integer direction;
        private Integer callout;
        private Integer softCause;

        public Subject() {
        }
    }

    @Data
    class Info implements Serializable {
        private String appID;
        private String callID;
        private String sessionID;

        public Info() {
        }
    }
}
