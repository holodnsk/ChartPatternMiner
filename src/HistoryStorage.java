import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.math.BigDecimal;
import java.util.*;

// читает и хранит свечи поступающие из источников исторических данных, и сообщает наболюдателям о наличии обновлений
// TODO должен работать в своей нити
public class HistoryStorage {
    private static HistoryStorage instance;

    // указывает какую долю от максимальной цены счиатать минимальной целью сделки, 200 равно пол процента, 100 равно 1 процент
    public static final BigDecimal PERCENT_TO_MINIMUM_GOAL_OF_DEAL = new BigDecimal(0.5f);
    // минимальная цель для совершения сделки, вычисляется лениво
    private BigDecimal minimumGoalOfDeal;

    // минимальный шаг цены, вычисляется автоматически лениво
    private BigDecimal minimumStepCost;
    private List<Bar> bars;

    String importFile;

    private String dir="./history";

    public HistoryStorage(String nameContract, String idContract, String FILE_EXT, String timeframe) {

        String nameImportFile = nameContract+"-"+idContract+"-"+HistoryDownloader.getNumberOfTimeframe(timeframe)+FILE_EXT;
        ConsoleHelper.getInstance().writeLog("HistoryStorage создан "+nameImportFile);
        setImportFile(nameImportFile);
    }



    public static HistoryStorage getInstance() {
        if (instance==null) instance = new HistoryStorage();
        return instance;
    }

    private HistoryStorage() {

    }

    // устанавливает имя файла из которого надо прочитать исторические даные
    public void setImportFile(String importFile ) {

        this.importFile = importFile;
        readData();

    }

    // читает исторические данные из всего файла и заполняет ими пустой candles
    private void readData() {
        bars = new ArrayList<>();
        ConsoleHelper.getInstance().writeLog("HistoryStorage: try to read:"+dir+"\\"+importFile);
        try (Scanner scanner = new Scanner(new FileInputStream(dir+"\\"+importFile))){
            while (scanner.hasNextLine()) {
                bars.add(new Bar(scanner.nextLine()));
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            ConsoleHelper.getInstance().writeMessage("HistoryStorage:"+"ошибка при чтении файла:" + e);
        }

        ConsoleHelper.getInstance().writeLog("HistoryStorage: finish of read:"+dir+"\\"+importFile);

    }

    // в отдельной нити проверяем обновления файла истории


    public boolean tryRefreshData() {

        // полностью прочитаем файл истории
        List<Bar> newBars = new ArrayList<>();
        try (Scanner scanner = new Scanner(new FileInputStream(dir+"\\"+importFile))){
            while (scanner.hasNextLine()) {
                newBars.add(new Bar(scanner.nextLine()));
            }
        } catch (FileNotFoundException e) {
            //e.printStackTrace();
            ConsoleHelper.getInstance().writeMessage("HistoryStorage:" + "ошибка при чтении файла:" + e);
            return false;
        }

        // если разер новой истории больше старой - записывам новую историю вместо старой и возвращаем true
        if (newBars.size()>bars.size()){
            bars=newBars;

            return true;
        }

        // если время последних свечей равны но в остальное разное - записывам новую историю вместо старой и возвращаем true
        if ( newBars.get(newBars.size()-1).equalsTime(bars.get(bars.size()-1)) &&
                !(newBars.get(newBars.size()-1).equals(bars.get(bars.size()-1))) ) {
            bars=newBars;

            return true;
        }
        return false;
    }

    public String lastBarsToString(Integer nBars) {
        String result = "";
        for (int i = nBars; i > 0; i--) {
            result+=bars.get(bars.size()-1-i).toString()+"\n";
        }
        result = result.substring(0,result.length()-1);
        return result;
    }

    private int getTimeFrameSeconds() {
        int result = Integer.MAX_VALUE;
        int prevTime = 0;
        int count = 10;
        for (Bar bar : bars) {
            int time = Integer.parseInt(bar.TIME.substring(0,bar.TIME.length()-2));
            int diff = time - prevTime;
            if (diff <result) result= diff;
            prevTime=time;
            if (count--==0) break;
        }
        return result*60;
    }

    // метод определяет шаг цены
    public BigDecimal getMinimumStepCost() {

        // делаем сет всех возможных значений цены
        Set<BigDecimal> bigDecimals = getAllValuesOfCost();

        // надо вместо этого куска взять два любых значения из bigDecimals и их модуль разности присвоить minimumStepCost
        ArrayList<BigDecimal> tempBD = new ArrayList<>();
        tempBD.addAll(bigDecimals);

        // если еще не вычисляли то вычислим
        if (minimumStepCost ==null) {

            minimumStepCost = tempBD.get(tempBD.size() - 1).subtract(tempBD.get(0));

            BigDecimal prev = new BigDecimal(0); // чемуто долджно быть равно предыдущее значение

            // здесь можно и сокращенным итератором, но поначалу хотелось брать по два значения сразу
            for (BigDecimal temp : bigDecimals) {
                BigDecimal tempMinimum = temp.subtract(prev);

                // если существует положительная разница между временным минимумом и тем который уже был ранее известен
                boolean tempMinimusIsRealMinimum = (minimumStepCost.compareTo(tempMinimum)) > 0;
                minimumStepCost = tempMinimusIsRealMinimum ? tempMinimum : minimumStepCost;

                // заменяем предыдущее значение для будущего шага итератора
                prev = temp;
            }
        }
        return minimumStepCost;
    }

    private Set<BigDecimal> getAllValuesOfCost() {
        Set<BigDecimal> bigDecimals = new TreeSet<>();
        for (Bar bar : bars) {
            bigDecimals.add(bar.OPEN);
            bigDecimals.add(bar.HIGH);
            bigDecimals.add(bar.LOW);
            bigDecimals.add(bar.CLOSE);
        }
        return bigDecimals;
    }

    public List<Bar> getBars() {
        return bars;
    }

    public String getFileName() {
        return importFile;
    }

    public void setDir(String dir) {
        this.dir = dir;
    }

    public String getDir() {
        return dir;
    }



}
