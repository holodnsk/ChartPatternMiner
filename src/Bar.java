import java.io.Serializable;
import java.math.BigDecimal;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

// описывает свечу в том виде в котором она хранится в исторических данных
public class Bar implements Serializable,Comparable<Bar>{
    String DATE;
    String TIME;
    BigDecimal OPEN;
    BigDecimal HIGH;
    BigDecimal LOW;
    BigDecimal CLOSE;
    long VOL;

    public Bar(String lineDateTimeOpenHighLowCloseVol) {
        String[] line = lineDateTimeOpenHighLowCloseVol.split(",");


        this.DATE = line[0];
        this.TIME = line[1];
        this.OPEN = BigDecimal.valueOf(Double.valueOf(line[2]));
        this.HIGH = BigDecimal.valueOf(Double.valueOf(line[3]));
        this.LOW = BigDecimal.valueOf(Double.valueOf(line[4]));
        this.CLOSE = BigDecimal.valueOf(Double.valueOf(line[5]));
        this.VOL = Long.valueOf(line[6]);
    }

    public Bar(BigDecimal OPEN, BigDecimal HIGH, BigDecimal LOW, BigDecimal CLOSE, Integer VOL) {
        this.OPEN = OPEN;
        this.HIGH = HIGH;
        this.LOW = LOW;
        this.CLOSE = CLOSE;
        this.VOL = VOL;
    }

    @Override
    public String toString() {
        // 20000101,000000,168.5600000,204.6200000,167.0300000,172.3100000,482342889
        return DATE+","+TIME+","+OPEN+","+HIGH+","+LOW+","+CLOSE+","+VOL;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Bar)) return false;

        Bar bar = (Bar) o;

        if (VOL != bar.VOL) return false;
        if (!CLOSE.equals(bar.CLOSE)) return false;
        if (!DATE.equals(bar.DATE)) return false;
        if (!HIGH.equals(bar.HIGH)) return false;
        if (!LOW.equals(bar.LOW)) return false;
        if (!OPEN.equals(bar.OPEN)) return false;
        return TIME.equals(bar.TIME);

    }

    @Override
    public int hashCode() {
        int result = DATE.hashCode();
        result = 31 * result + TIME.hashCode();
        result = 31 * result + OPEN.hashCode();
        result = 31 * result + HIGH.hashCode();
        result = 31 * result + LOW.hashCode();
        result = 31 * result + CLOSE.hashCode();
        result = 31 * result + (int) (VOL ^ (VOL >>> 32));
        return result;
    }

    public boolean equalsTime(Bar bar){
        if (this == bar) return true;
        if (!(bar instanceof Bar)) return false;
        return DATE.equals(bar.DATE) && TIME.equals(bar.TIME);
    }

    @Override
    public int compareTo(Bar o) {
        return (int) (Long.parseLong(this.DATE)+Long.parseLong(this.TIME)-Long.parseLong(o.DATE)+Long.parseLong(o.TIME));
    }

    public Date getDate() {
        DateFormat df = new SimpleDateFormat("yyyyMMddhhmmss");
        Date result = null;
        try {
            result = df.parse(DATE+TIME);
        } catch (ParseException e) {
            ConsoleHelper.getInstance().writeLog("Bar: не удалось спарсить дату и время бара в методе getDate");
            for (StackTraceElement stackTraceElement : e.getStackTrace()) ConsoleHelper.getInstance().writeLog(stackTraceElement.toString());
        }
        return result;
    }
}
