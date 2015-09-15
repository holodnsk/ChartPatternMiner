

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.math.BigDecimal;
import java.util.*;
import java.util.concurrent.TimeUnit;

/**
 * Created by 1 on 09.03.2015.
 */

// читает и хранит свечи поступающие из источников исторических данных, и сообщает наболюдателям о наличии обновлений
// TODO должен работать в своей нити
public class HistoryStorage {
    private static HistoryStorage instance;

    // указывает какую долю от максимальной цены счиатать минимальной целью сделки, 200 равно пол процента, 100 равно 1 процент
    public static final BigDecimal PERCENT_TO_MINIMUM_GOAL_OF_DEAL = new BigDecimal(0.5f);
    // минимальная цель для совершения сделки, вычисляется лениво
    private BigDecimal minimumGoalOfDeal;

    synchronized public BigDecimal getMinimumGoalOfDeal() {
        if (minimumGoalOfDeal==null) setMinimumGoalOfDeal();
        return minimumGoalOfDeal;
    }
    private void setMinimumGoalOfDeal() {
        BigDecimal ONEHUNDERT = new BigDecimal(100);
        minimumGoalOfDeal =bars.get(bars.size()-1).CLOSE.multiply(PERCENT_TO_MINIMUM_GOAL_OF_DEAL).divide(ONEHUNDERT);
    }

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

    public void startAutoRefreshHistory() {
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                // берем таймфрейм истории минус 5 секунд
                int startPause = 1;
//                int startPause = getTimeFrameSeconds()-5;
//                ConsoleHelper.getInstance().writeMessage("HistoryStorage: время ожидания после первого обновения "+startPause+"с");


                int timePause = 1;
                while (true){
                    try {
                        // до первого обновления пауза будет 1с, затем она станет равной таймфрейму минус 5с
                        TimeUnit.SECONDS.sleep(timePause);
                        timePause = 1;
                    } catch (InterruptedException e) {
                        ConsoleHelper.getInstance().writeMessage("HistoryStorage: ошибка при ожидании");
                    }
                    if (tryRefreshData()) {
//                        ConsoleHelper.getInstance().writeMessage("HistoryStorage: есть обновление");

                        // делаем паузу между обновлениями равной таймфрейму минус 5с
                        timePause = startPause;
                        Advisor.getInstance().runAdvice();
                    }
                }
            }
        });
        thread.setDaemon(true);
        thread.start();
    }

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
        String result = new String();
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
            Iterator<BigDecimal> bigDecimalIterator = bigDecimals.iterator();
            while (bigDecimalIterator.hasNext()) {
                BigDecimal temp = bigDecimalIterator.next();
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

    public List<ExampleOfBarChart> getLastExamples() {
        List<ExampleOfBarChart> result = new ArrayList<>();
        for (int length = 1; length <= ApplicationExecutor.storagePattern.getMaxLenght(); length++) {

            // создаем и добавляем в результат пример, начиная с адреса размерИстории-length и длинной length
            result.add(new ExampleOfBarChart(this,HistoryStorage.getInstance().bars.size()-length-1,length));
        }
        return result;
    }


}
