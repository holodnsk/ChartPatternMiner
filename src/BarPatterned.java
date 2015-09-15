import java.io.Serializable;
import java.math.BigDecimal;

public class BarPatterned implements Serializable{

    private boolean isFirstBar;

    // указывает смещение относительно входа эксемпла
    private BigDecimal OpenL,OpenH,
            LowL, LowH,
            HighL, HighH,
            CloseL, CloseH;


    // конструктор у всех значений OHLC отнимает OPEN первой свечи, таким образом OPEN первой свечи окажется равен нулю,
    // а все остальные значения являются дельтой к OPEN первой свечи
    public BarPatterned(boolean isFirstBar, Bar bar, BigDecimal openValueOfFrame) {
        this.isFirstBar = isFirstBar;

        OpenL = bar.OPEN.subtract(openValueOfFrame);
        OpenH = bar.OPEN.subtract(openValueOfFrame);

        LowL = bar.LOW.subtract(openValueOfFrame);
        LowH = bar.LOW.subtract(openValueOfFrame);

        HighL = bar.HIGH.subtract(openValueOfFrame);
        HighH = bar.HIGH.subtract(openValueOfFrame);

        CloseL = bar.CLOSE.subtract(openValueOfFrame);
        CloseH = bar.CLOSE.subtract(openValueOfFrame);

    }

    public void expand(BigDecimal step) {
        if (!isFirstBar) expandOpen(step);
        expandHigh(step);
        expandLow(step);
        expandClose(step);
    }

    private void expandOpen(BigDecimal step) {
        OpenL = OpenL.subtract(step);
        OpenH = OpenH.add(step);
    }

    private void expandClose(BigDecimal step) {
        CloseL = CloseL.subtract(step);
        CloseH = CloseH.add(step);
    }

    private void expandHigh(BigDecimal step) {
        HighL = HighL.subtract(step);
        HighH = HighH.add(step);
    }

    private void expandLow(BigDecimal step) {
        LowL = LowL.subtract(step);
        LowH = LowH.add(step);
    }

    // возвращает true если bar(из стораджа) подходит под patternedBar паттерна
    public boolean thisPatternedBarIsFitsForBar(Bar bar, BigDecimal openOfExample) {

        // JAVADOC a.compareTo(b); // возвращает (-1 если a < b), (0 если a == b), (1 если a > b)
        if (bar.OPEN.subtract(openOfExample).compareTo(OpenL)==-1 || bar.OPEN.subtract(openOfExample).compareTo(OpenH)==1) return false;
        if (bar.HIGH.subtract(openOfExample).compareTo(HighL)==-1 || bar.HIGH.subtract(openOfExample).compareTo(HighH)==1) return false;
        if (bar.LOW.subtract(openOfExample).compareTo(LowL)==-1 || bar.LOW.subtract(openOfExample).compareTo(LowH)==1) return false;
        return !(bar.CLOSE.subtract(openOfExample).compareTo(CloseL) == -1 || bar.CLOSE.subtract(openOfExample).compareTo(CloseH) == 1);
    }



    @Override
    public String toString() {
        return "PatternBar:"+"OpenL:"+OpenL.toString()+" OpenH:"+OpenH+" LowL:"+LowL+" LowH:"+LowH+" HighL:"+HighL+" HighH:"+HighH+" CloseL:"+CloseL+" CloseH:"+CloseH/*+" VolL:"+VolL+" VolH:"+VolH+"\n"*/;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof BarPatterned)) return false;

        BarPatterned that = (BarPatterned) o;

        if (isFirstBar != that.isFirstBar) return false;
        if (CloseH.compareTo(that.CloseH)!=0) return false;
        if (CloseL.compareTo(that.CloseL)!=0) return false;
        if (HighH.compareTo(that.HighH)!=0) return false;
        if (HighL.compareTo(that.HighL)!=0) return false;
        if (LowH.compareTo(that.LowH)!=0) return false;
        if (LowL.compareTo(that.LowL)!=0) return false;
        if (OpenH.compareTo(that.OpenH)!=0) return false;
        return OpenL.compareTo(that.OpenL) == 0;

    }

    @Override
    public int hashCode() {
        int result = (isFirstBar ? 1 : 0);
        result = 31 * result + OpenL.hashCode();
        result = 31 * result + OpenH.hashCode();
        result = 31 * result + LowL.hashCode();
        result = 31 * result + LowH.hashCode();
        result = 31 * result + HighL.hashCode();
        result = 31 * result + HighH.hashCode();
        result = 31 * result + CloseL.hashCode();
        result = 31 * result + CloseH.hashCode();
        return result;
    }
}
