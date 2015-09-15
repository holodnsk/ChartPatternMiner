import java.io.Serializable;
import java.math.BigDecimal;
import java.util.List;

/**
 * Created by 1 on 24.03.2015.
 */
public class BarPatterned implements Serializable{

    boolean isFirstBar;

    // указывает смещение относительно входа эксемпла
    BigDecimal OpenL,OpenH,
            LowL, LowH,
            HighL, HighH,
            CloseL, CloseH;

    // указвает диапазон возможных значений
//        Integer     VolL, VolH;

    // у всех значений OHLC отнимаем OPEN первой свечи, таким образом OPEN первой свечи окажется равен нулю,
    // а все остальные значения будут указаны относительно OPEN первой свечи

    public BarPatterned(boolean isFirstBar, List<Bar> bars, List<BigDecimal> opensOfExamples) {
        this.isFirstBar=isFirstBar;
        OpenL = new BigDecimal(0);
        OpenH = new BigDecimal(0);
        LowL = new BigDecimal(0);
        LowH = new BigDecimal(0);
        HighL = new BigDecimal(0);
        HighH = new BigDecimal(0);
        CloseL = new BigDecimal(0);
        CloseH = new BigDecimal(0);
//            VolL = Integer.MAX_VALUE;
//            VolH = Integer.MIN_VALUE;


        // расширяем все значения свечи до максимумов и минимумов отношений ко входу паттерна этих же значений свечи от каждого эксемпла
        for (int index = 0; index < bars.size(); index++) {

            BigDecimal openSubtractOpenOfExample = bars.get(index).OPEN.subtract(opensOfExamples.get(index));
            if (OpenL.compareTo(openSubtractOpenOfExample)==1) OpenL=openSubtractOpenOfExample;
            if (OpenH.compareTo(openSubtractOpenOfExample)==-1) OpenH=openSubtractOpenOfExample;

            BigDecimal highSubtractOpenOfExample = bars.get(index).HIGH.subtract(opensOfExamples.get(index));
            if (HighL.compareTo(highSubtractOpenOfExample)==1) HighL=highSubtractOpenOfExample;
            if (HighH.compareTo(highSubtractOpenOfExample)==-1) HighH=highSubtractOpenOfExample;

            BigDecimal lowSubtractOpenOfExample = bars.get(index).LOW.subtract(opensOfExamples.get(index));
            if (LowL.compareTo(lowSubtractOpenOfExample)==1) LowL=lowSubtractOpenOfExample;
            if (LowH.compareTo(lowSubtractOpenOfExample)==-1) LowH=lowSubtractOpenOfExample;

            BigDecimal closeSubtractOpenOfExample = bars.get(index).CLOSE.subtract(opensOfExamples.get(index));
            if (CloseL.compareTo(closeSubtractOpenOfExample)==1) CloseL=closeSubtractOpenOfExample;
            if (CloseH.compareTo(closeSubtractOpenOfExample)==-1) CloseH=closeSubtractOpenOfExample;

            // объем интересует не как отношение ко входу, а абсолютный
//                if (VolL> bars.get(index).VOL) VolL=bars.get(index).VOL;
//                if (VolH<bars.get(index).VOL) VolH=bars.get(index).VOL;
        }
    }

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

    public void dexpand(BigDecimal step) {
        if (!isFirstBar) dexpandOpen(step);
        dexpandHigh(step);
        dexpandLow(step);
        dexpandClose(step);
    }

    private void dexpandOpen(BigDecimal step) {
        OpenL = OpenL.add(step);
        OpenH = OpenH.subtract(step);
    }

    private void dexpandClose(BigDecimal step) {
        CloseL = CloseL.add(step);
        CloseH = CloseH.subtract(step);
    }

    private void dexpandHigh(BigDecimal step) {
        HighL = HighL.add(step);
        HighH = HighH.subtract(step);
    }

    private void dexpandLow(BigDecimal step) {
        LowL = LowL.add(step);
        LowH = LowH.subtract(step);
    }

    // возвращает true если bar(из стораджа) подходит под patternedBar паттерна
    public boolean thisPatternedBarIsFitsForBar(Bar bar, BigDecimal openOfExample) {

        // JAVADOC a.compareTo(b); // возвращает (-1 если a < b), (0 если a == b), (1 если a > b)

        // если хоть одно значение окажется за пределами допустимого диапазона, сразу вернет FALSE
        if (bar.OPEN.subtract(openOfExample).compareTo(OpenL)==-1 || bar.OPEN.subtract(openOfExample).compareTo(OpenH)==1) return false;
        if (bar.HIGH.subtract(openOfExample).compareTo(HighL)==-1 || bar.HIGH.subtract(openOfExample).compareTo(HighH)==1) return false;
        if (bar.LOW.subtract(openOfExample).compareTo(LowL)==-1 || bar.LOW.subtract(openOfExample).compareTo(LowH)==1) return false;
        if (bar.CLOSE.subtract(openOfExample).compareTo(CloseL)==-1 || bar.CLOSE.subtract(openOfExample).compareTo(CloseH)==1) return false;
//            if (bar.VOL<VolL || bar.VOL>VolH) return false;

        // если уж до сюда добралось то
        return true;
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
        if (OpenL.compareTo(that.OpenL)!=0) return false;
//            if (!VolH.equals(that.VolH)) return false;
//            if (!VolL.equals(that.VolL)) return false;

        return true;
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
//            result = 31 * result + VolL.hashCode();
//            result = 31 * result + VolH.hashCode();
        return result;
    }


}
