package com.tinesh.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;


// The @JsonProperty mapping is mandatory, otherwise the field is set to null
@JsonIgnoreProperties(ignoreUnknown = true)
public class MailTrace {

    @JsonProperty("SENDER")
    private String SENDER;
    @JsonProperty("RECIPIENT")
    private String RECIPIENT;
    @JsonProperty("MESSAGE_TRACE_ID")
    private String MESSAGE_TRACE_ID;
    @JsonProperty("SUBJECT")
    private String SUBJECT;
    @JsonProperty("FROM_IP")
    private String FROM_IP;
    @JsonProperty("TO_IP")
    private String TO_IP;
    @JsonProperty("SIZE")
    private String SIZE;
    @JsonProperty("RECEIVED")
    private String RECEIVED;

    public String getSENDER() {
        return SENDER;
    }

    public void setSENDER(String SENDER) {
        this.SENDER = SENDER;
    }

    public String getRECIPIENT() {
        return RECIPIENT;
    }

    public void setRECIPIENT(String RECIPIENT) {
        this.RECIPIENT = RECIPIENT;
    }

    public String getMESSAGE_TRACE_ID() {
        return MESSAGE_TRACE_ID;
    }

    public void setMESSAGE_TRACE_ID(String MESSAGE_TRACE_ID) {
        this.MESSAGE_TRACE_ID = MESSAGE_TRACE_ID;
    }

    public String getSUBJECT() {
        return SUBJECT;
    }

    public void setSUBJECT(String SUBJECT) {
        this.SUBJECT = SUBJECT;
    }

    public String getFROM_IP() {
        return FROM_IP;
    }

    public void setFROM_IP(String FROM_IP) {
        this.FROM_IP = FROM_IP;
    }

    public String getTO_IP() {
        return TO_IP;
    }

    public void setTO_IP(String TO_IP) {
        this.TO_IP = TO_IP;
    }

    public String getSIZE() {
        return SIZE;
    }

    public void setSIZE(String SIZE) {
        this.SIZE = SIZE;
    }

    public String getRECEIVED() {
        return RECEIVED;
    }

    public void setRECEIVED(String RECEIVED) {
        this.RECEIVED = RECEIVED;
    }

    @Override
    public String toString() {
        return "MailTrace{" +
                "SENDER='" + SENDER + '\'' +
                ", RECIPIENT='" + RECIPIENT + '\'' +
                ", MESSAGE_TRACE_ID='" + MESSAGE_TRACE_ID + '\'' +
                ", SUBJECT='" + SUBJECT + '\'' +
                ", FROM_IP='" + FROM_IP + '\'' +
                ", TO_IP='" + TO_IP + '\'' +
                ", SIZE='" + SIZE + '\'' +
                ", RECEIVED='" + RECEIVED + '\'' +
                '}';
    }
}
