package engine.quiz;

public class Answer {

    private int[] answer;

    Answer() {};
    Answer(int[] answer) {
        this.answer = answer;
    }
    Answer(Answer a) {
        this.answer = a.answer;
    }

    public int[] getAnswer() {
        return answer;
    }

    public void setAnswer(int[] answer) {
        this.answer = answer;
    }
}