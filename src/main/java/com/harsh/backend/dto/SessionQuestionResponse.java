package com.harsh.backend.dto;

public class SessionQuestionResponse {

    private Long sessionQuestionId;
    private Long questionId;
    private String questionText;
    private String optionA;
    private String optionB;
    private String optionC;
    private String optionD;

    public SessionQuestionResponse(Long sessionQuestionId, Long questionId,
                                   String questionText,
                                   String optionA, String optionB, String optionC, String optionD) {
        this.sessionQuestionId = sessionQuestionId;
        this.questionId = questionId;
        this.questionText = questionText;
        this.optionA = optionA;
        this.optionB = optionB;
        this.optionC = optionC;
        this.optionD = optionD;
    }

    public Long getSessionQuestionId() { return sessionQuestionId; }
    public Long getQuestionId() { return questionId; }
    public String getQuestionText() { return questionText; }
    public String getOptionA() { return optionA; }
    public String getOptionB() { return optionB; }
    public String getOptionC() { return optionC; }
    public String getOptionD() { return optionD; }
}