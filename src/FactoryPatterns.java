import java.util.Date;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by 1 on 10.03.2015.
 */
// PatternStorage наблюдает за PatternFactory и по результатам работы сохраняет паттерны в файл
public class FactoryPatterns {



    public static AtomicInteger MINIMUM_PERCENT_DIFF_INTEREST = new AtomicInteger(51);
    public static int MIN_LENGTH_PATTERN = 1; // TODO вывести в консоль
    public static int MAX_LENGTH_PATTERN = 10; // TODO вывести в консоль

    private static final int N_THREADS = 74;

    private static FactoryPatterns instance;
    private boolean isStop = false;

    public static FactoryPatterns getInstance() {
        if (instance==null) instance = new FactoryPatterns();
        return instance;
    }

    private FactoryPatterns(){}



    // метод перебирает все исторические данные и создает фреймы длинной от 1 и  более, на основе которых
    // создает протоПаттерны, у протоПаттернов запрашиваются списки ВВЕРХ ВНИЗ ФЛЭТ примеров,
    // и на основе этих списков создаются паттерны
      /* конструктор из полученного примера делает паттерн,
    расширяет свечные значения паттерна так чтоб к нему подходило MINIMUM_NUM_OF_MAIN_EXAMPLES примеров
    ищет после каждого примера изменение цены на величину BarStorage.getMinimumGoalOfDeal
    сохраняет шаг на котором это произошло и записывает каждый пример к одному из списков ВВЕРХ ВНИЗ ФЛЭТ,
    примеры после которых на одном шаге был и ВВЕРХ и ВНИЗ BarStorage.getMinimumGoalOfDeal сохраняются как ФЛЭТ

    считает колличество шума для каждого списка отдельно:
    считает сколько в списках типа ВВЕРХ попало эксемплов типа ФЛЭТ и ВНИЗ,
    считает сколько в списках типа ФЛЭТ попало эксемплов типа ВВЕРХ и ВНИЗ
    считает сколько в списках типа ВНИЗ попало эксемплов типа ФЛЭТ и ВВЕРХ

    рабочим считаем тот шаблон у которого меньше всего шума по отношению к общему количеству эксемплов.
    для геттеров считаем диапазон шагов достижения BarStorage.getMinimumGoalOfDeal с суммарной частотностью MINIMUM_FREQ_VALUES_OF_DEAL
    для геттеров считаем диапазон цен ВХОДА с суммарной частотностью MINIMUM_FREQ_VALUES_OF_DEAL
    для геттеров считаем диапазон цен ВЫХОДА с суммарной частотностью MINIMUM_FREQ_VALUES_OF_DEAL

    */
    public void generatePatterns() {

        isStop =false;

        ConsoleHelper.getInstance().writeMessage("PatternFactory:"+"Считаю паттерны длинной от "+ MAX_LENGTH_PATTERN +" до "+ MIN_LENGTH_PATTERN);

        Date startDate = new Date();

        // цикл определяет длинну будующего паттерна от MIN_LENGTH_PATTERN до MAX_LENGTH_PATTERN включительно
        for (int lengthPattern = MAX_LENGTH_PATTERN; lengthPattern >= MIN_LENGTH_PATTERN; lengthPattern--) {
            if (isStop) break;
            // пересоздается на каждой итерации заново, TODO перенести перед циклом, а контроль исполнения за цикл
            ExecutorService executor = Executors.newFixedThreadPool(N_THREADS);

            final int finalLengthPattern = lengthPattern;
            // цикл определяет адрес начала нового Ексемпла,
            // адрес не может быть ближе к концу чем на длинну генерируемого эксемпла
            for (int startAddressOfExample = 0; startAddressOfExample< HistoryStorage.getInstance().getBars().size()- finalLengthPattern; startAddressOfExample++) {
                if (isStop) break;

                // Создаем ексемпл
                final ExampleOfBarChart exampleOfBarChart = new ExampleOfBarChart(HistoryStorage.getInstance(),startAddressOfExample, finalLengthPattern);

                Runnable runnable = new Runnable() {
                    public void run() {

                        // вначале делаем протопаттерн, который  ищет под себя примеры и делит на 3 сета LONG SHORT VOLATILITY
                        //  на его основе создаем паттерны которые пишем в список готовых паттернов
                        PatternBars patternBars = new PatternBars(HistoryStorage.getInstance(), exampleOfBarChart);


                        // проверяем колличество примеров каждого типа у протопаттерна и если их достаточно создаем соответствующий паттерн
                        if (patternBars.getExamplesToLong().size() > patternBars.MINIMUM_NUM_OF_MAIN_EXAMPLES) {
                            PatternFittedBars patternFittedBarsLong = new PatternFittedBars(
                                    patternBars.getExamplesToLong(),
                                    patternBars.getExamplesToShort(),
                                    patternBars.getExamplesHighVolatility(),
                                    TypePattern.LONG);

                            int percentPositiveExamples = patternFittedBarsLong.getPercentPositiveExamples();
                            if (percentPositiveExamples >=MINIMUM_PERCENT_DIFF_INTEREST.get()) {

                                ApplicationExecutor.storagePattern.addPattern(patternFittedBarsLong);

                            }
                        }

                        if (patternBars.getExamplesToShort().size() > patternBars.MINIMUM_NUM_OF_MAIN_EXAMPLES) {
                            PatternFittedBars patternFittedBarsShort = new PatternFittedBars(
                                    patternBars.getExamplesToShort(),
                                    patternBars.getExamplesToLong(),
                                    patternBars.getExamplesHighVolatility(),
                                    TypePattern.SHORT);

                            int percentPositiveExamples = patternFittedBarsShort.getPercentPositiveExamples();
                            if (percentPositiveExamples >=MINIMUM_PERCENT_DIFF_INTEREST.get()) {

                                ApplicationExecutor.storagePattern.addPattern(patternFittedBarsShort);

                            }
                        }


                        if (patternBars.getExamplesHighVolatility().size() > patternBars.MINIMUM_NUM_OF_MAIN_EXAMPLES) {
                            PatternFittedBars patternFittedBarsVolatility = new PatternFittedBars(
                                    patternBars.getExamplesHighVolatility(),
                                    patternBars.getExamplesToLong(),
                                    patternBars.getExamplesToShort(),
                                    TypePattern.VOLATILITY);

                            int percentPositiveExamples = patternFittedBarsVolatility.getPercentPositiveExamples();
                            if (percentPositiveExamples >=MINIMUM_PERCENT_DIFF_INTEREST.get()) {

                                ApplicationExecutor.storagePattern.addPattern(patternFittedBarsVolatility);

                            }
                        }

                    }
                };

                executor.submit(runnable);
            }


            synchronized (ApplicationExecutor.storagePattern) {
                ConsoleHelper.getInstance().writeMessage("PatternFactory:"+"Founded " + ApplicationExecutor.storagePattern.getShortStatistics());
            }
            ConsoleHelper.getInstance().writeMessage("PatternFactory:"+"prepared examples of length " + lengthPattern);

            executor.shutdown();

            ThreadPoolExecutor poolExecutor = (ThreadPoolExecutor) executor;


            while (true) {
                try {
                    ConsoleHelper.getInstance().writeMessage("PatternFactory:"+"length:" + lengthPattern + " Executor status:" + poolExecutor.toString().substring(poolExecutor.toString().indexOf("[")));
                    executor.awaitTermination(60, TimeUnit.SECONDS);
                    if (poolExecutor.getActiveCount()==0) break;
                } catch (InterruptedException e) {
                    ConsoleHelper.getInstance().writeMessage("PatternFactory:"+"во время ожидания ошибка");
                }
            }
        }


        ConsoleHelper.getInstance().writeMessage("PatternFactory:"+"pattern match is ended. Report:");

        Date endDate = new Date();
        long diff = endDate.getTime()-startDate.getTime();

        synchronized (ApplicationExecutor.storagePattern) {
            ConsoleHelper.getInstance().writeMessage("PatternFactory:"+diff / 1000 + " seconds of matching to found " + ApplicationExecutor.storagePattern.size() + " patterns");
        }

//        ConsoleHelper.getInstance().writeMessage(Director.patternStorage.getShortStatistics());
        // если пользователь прервал расчет выходим без сохранения
        if (isStop) return;
        // сохраняем PatternStorage в файл
        ConsoleHelper.getInstance().writeMessage("PatternFactory:"+"передал управление Director.savePatternStorage();");
        ApplicationExecutor.savePatternStorage();
    }



    public void setMaxLengthToSearchPatterns(Integer maxLengthToSearchPatterns) {
        this.MAX_LENGTH_PATTERN = maxLengthToSearchPatterns;
    }

    public void stopMatching() {
        isStop =true;
    }

    public void setMinLengthToSearchPatterns(Integer minLengthToSearchPatterns) {
        this.MIN_LENGTH_PATTERN = minLengthToSearchPatterns;
    }
}
