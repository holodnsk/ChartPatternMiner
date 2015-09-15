import java.io.Serializable;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by 1 on 16.03.2015.
 */
public class ExampleOfBarChart implements Serializable{

    private HistoryStorage historyStorage;
    private int startAddress;
    private int endAdress;
    private int stepAfterExampleToGetProfit;

    private List<Bar> bars = new ArrayList<>();

    public Deal getDealLong() {
        return dealLong;
    }

    private Deal dealLong;

    public Deal getDealShort() {
        return dealShort;
    }

    private Deal dealShort;
    private int size;



    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ExampleOfBarChart)) return false;

        ExampleOfBarChart that = (ExampleOfBarChart) o;

        if (toString().equals(that.toString())) return true;

        return false;
    }

    @Override
    public int hashCode() {

        return toString().hashCode();
    }

    public List<Bar> getBars() {
        return bars;
    }

    public ExampleOfBarChart(HistoryStorage historyStorage,int startBarOfExample, int lenghtFrame) {
        this.historyStorage = historyStorage;
        this.startAddress=startBarOfExample;
        this.endAdress=startAddress+lenghtFrame;
        this.size = lenghtFrame;
        // адрес каждой свечи равен startBarOfExample + shift который может быть от нуля и не больше длинны
        for (int shift = 0; shift < lenghtFrame; shift++) {
            this.bars.add(this.historyStorage.getBars().get(startBarOfExample + shift));
        }
    }

    public int getEndAdress() {
        return endAdress;
    }

    public BigDecimal getCloseValue() {

        return historyStorage.getBars().get(endAdress).CLOSE;
    }

    @Override
    public String toString() {
        String result = new String();
        for (Bar bar : bars) result+=bar;
        return "Start:"+String.valueOf(startAddress)+" End:"+String.valueOf(endAdress)+" "+result;
    }

    public void setStepToProfit(int stepAfterExample) {
        stepAfterExampleToGetProfit = stepAfterExample;
    }

    public int getSize() {
        return size;
    }

    public void addDeal(Deal deal) {
        if (deal.type.equals(Deal.Type.LONG)) this.dealLong=deal;
        else if (deal.type.equals(Deal.Type.SHORT)) this.dealShort=deal;
        else ConsoleHelper.getInstance().writeLog("ExampleOfChartBar попытка добавить сделку не LONG  и не SHORT");
    }

}
