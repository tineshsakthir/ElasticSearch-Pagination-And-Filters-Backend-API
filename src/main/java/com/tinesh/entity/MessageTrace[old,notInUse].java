//package com.tinesh.entity;
//
//import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
//import com.fasterxml.jackson.annotation.JsonProperty ;
//@JsonIgnoreProperties(ignoreUnknown = true)
//public class MessageTrace {
//    @JsonProperty("STATUS")
//    private String recipientType;
//
//    @JsonProperty("SUBJECT")
//    private String messageSubject;
//
//    @JsonProperty("FROM_IP")
//    private String clientIp;
//
//    @JsonProperty("SENDER")
//    private String senderAddress;
//
//    @JsonProperty("RECEIVED")
//    private String datetime;
//
//    @JsonProperty("RECIPIENT")
//    private String recipientAddress;
//
//    @JsonProperty("SIZE")
//    private String messageSize;
//
//
//    @JsonProperty("TO_IP")
//    private String serverIp;
//
//    @JsonProperty("MESSAGE_TRACE_ID")
//    private String messageId;
//
//
//    public String getRecipientType() {
//        return recipientType;
//    }
//
//    public void setRecipientType(String recipientType) {
//        this.recipientType = recipientType;
//    }
//
//    public String getMessageSubject() {
//        return messageSubject;
//    }
//
//    public void setMessageSubject(String messageSubject) {
//        this.messageSubject = messageSubject;
//    }
//
//    public String getClientIp() {
//        return clientIp;
//    }
//
//    public void setClientIp(String clientIp) {
//        this.clientIp = clientIp;
//    }
//
//    public String getSenderAddress() {
//        return senderAddress;
//    }
//
//    public void setSenderAddress(String senderAddress) {
//        this.senderAddress = senderAddress;
//    }
//
//    public String getDatetime() {
//        return datetime;
//    }
//
//    public void setDatetime(String datetime) {
//        this.datetime = datetime;
//    }
//
//    public String getRecipientAddress() {
//        return recipientAddress;
//    }
//
//    public void setRecipientAddress(String recipientAddress) {
//        this.recipientAddress = recipientAddress;
//    }
//
//    public String getMessageSize() {
//        return messageSize;
//    }
//
//    public void setMessageSize(String messageSize) {
//        this.messageSize = messageSize;
//    }
//
//    public String getServerIp() {
//        return serverIp;
//    }
//
//    public void setServerIp(String serverIp) {
//        this.serverIp = serverIp;
//    }
//
//    public String getMessageId() {
//        return messageId;
//    }
//
//    public void setMessageId(String messageId) {
//        this.messageId = messageId;
//    }
//
//    @Override
//    public String toString() {
//        return "MessageTrace{" +
//                ", recipientType='" + recipientType + '\'' +
//                ", messageSubject='" + messageSubject + '\'' +
//                ", clientIp='" + clientIp + '\'' +
//                ", senderAddress='" + senderAddress + '\'' +
//                ", datetime='" + datetime + '\'' +
//                ", recipientAddress='" + recipientAddress + '\'' +
//                ", messageSize='" + messageSize + '\'' +
//                ", serverIp='" + serverIp + '\'' +
//                ", messageId='" + messageId + '\'' +
//                '}';
//    }
//
//}
