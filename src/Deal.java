import java.math.BigDecimal;

/**
 * Created by 1 on 19.06.2015.
 */
public class Deal {

    enum Type{LONG,SHORT}
    public Type type;
    public BigDecimal open;
    public BigDecimal close;
    public int stepOpen;
    public int stepClose;
    public BigDecimal profit;

    public Deal(Type type, BigDecimal open, BigDecimal close, int stepOpen, int stepClose) {
        this.type = type;
        this.open = open;
        this.close = close;
        this.stepOpen = stepOpen;
        this.stepClose = stepClose;
        if (type.equals(Type.SHORT)){profit = open.subtract(close);}
        else profit = close.subtract(open);
    }

    @Override
    public String toString() {
        return "profit="+ profit + " type=" + type +" open=" + open + " close=" + close + " stepOpen=" + stepOpen+ " stepClose="+stepClose;
    }

}
