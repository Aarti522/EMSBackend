package com.example.ems.dto.ai;

public class PerformanceAIResponse {

    private String prediction;

    private double score;

    private double confidence;

    public PerformanceAIResponse() {
    }

    public String getPrediction() {
        return prediction;
    }

    public void setPrediction(String prediction) {
        this.prediction = prediction;
    }

    public double getScore() {
        return score;
    }

    public void setScore(double score) {
        this.score = score;
    }

    public double getConfidence() {
        return confidence;
    }

    public void setConfidence(double confidence) {
        this.confidence = confidence;
    }
}