package engine.quiz;

public class Feedback {
    private boolean success;
    private String feedback;

    public Feedback(boolean s) {
        this.success = s;
        this.feedback = s ? "Congratulations, you're right!" : "Wrong answer! Please try again.";
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public void setFeedback(String feedback) {
        this.feedback = feedback;
    }

    public boolean isSuccess() {
        return success;
    }

    public String getFeedback() {
        return feedback;
    }
}