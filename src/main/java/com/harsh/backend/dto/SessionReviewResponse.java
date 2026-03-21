package com.harsh.backend.dto;

public class SessionReviewResponse {

    private Long questionId;
    private String questionText;

    private String optionA;
    private String optionB;
    private String optionC;
    private String optionD;

    private String correctAnswer;
    private String selectedAnswer;

    private Boolean correct;
    private Integer marksAwarded;

    public SessionReviewResponse(Long questionId, String questionText,
                                 String optionA, String optionB, String optionC, String optionD,
                                 String correctAnswer, String selectedAnswer,
                                 Boolean correct, Integer marksAwarded) {
        this.questionId = questionId;
        this.questionText = questionText;
        this.optionA = optionA;
        this.optionB = optionB;
        this.optionC = optionC;
        this.optionD = optionD;
        this.correctAnswer = correctAnswer;
        this.selectedAnswer = selectedAnswer;
        this.correct = correct;
        this.marksAwarded = marksAwarded;
    }

    public Long getQuestionId() { return questionId; }
    public String getQuestionText() { return questionText; }
    public String getOptionA() { return optionA; }
    public String getOptionB() { return optionB; }
    public String getOptionC() { return optionC; }
    public String getOptionD() { return optionD; }
    public String getCorrectAnswer() { return correctAnswer; }
    public String getSelectedAnswer() { return selectedAnswer; }
    public Boolean getCorrect() { return correct; }
    public Integer getMarksAwarded() { return marksAwarded; }
}