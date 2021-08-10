package mod.quizbot.me.Calculator;

public class CalculatorSettings {

    private boolean isRad = false;

    private String prevAns = null;

    public CalculatorSettings(boolean r, String pa){
        this.isRad = r;
        this.prevAns = pa;
    }

    public CalculatorSettings(){}

    public boolean isRad() {
        return isRad;
    }

    public void setRad(boolean rad) {
        isRad = rad;
    }

    public String getPrevAns() {
        return prevAns;
    }

    public void setPrevAns(String prevAns) {
        this.prevAns = prevAns;
    }

}
