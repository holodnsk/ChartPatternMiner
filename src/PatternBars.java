import java.math.BigDecimal;
import java.util.*;

/*
этот класс описывает паттерн учитывающий возможные диапазоны значений OHLC образцов графика, не учитывает
value и время,
НЕ оптимизирует диапазоны возможных значений OHLC по эксемплам типа LONG SHORT или VOLATILITY, потому что
    оптимизация происходит  в другом классе специализирующемся именно на на этом
он считает сколько эксемплов типа LONG SHORT VOLATILITY
он сохраняет у каждого эксемпла шаг на котором после эксемпла произошло достаточное измнение цены относительно
оптимального входа в сделку
*/

public class PatternBars {

// указывает какую долю от максимальной цены счиатать минимальной целью сделки, 200 равно пол процента, 100 равно 1 процент
    public static final BigDecimal PERCENT_TO_MINIMUM_GOAL_OF_DEAL = new BigDecimal(0.5f);

    // не создаются эксемплы из участков графика если до конца графика осталось столько свечей
    private static final int LAST_BARS = 2;
    public static final int MAX_EXPAND_FACTOR = 50;
    private static final int STEP_TO_OPEN_AFTER_EXAMPLE = 1;

    private static final int STEP_TO_CLOSE_AFTER_EXAMPLE = 10;



    @Override
    public String toString() {
        return
                "ToLong=" + examplesToLong.size() +
                        " ToShort=" + examplesToShort.size() +
                        " examplesHighVolatility=" + examplesHighVolatility.size()
                ;
    }
    HistoryStorage historyStorage;

    // определяет колличество примеров паттерна которое должно встречаться быть на графике
    public int HISTORY_SIZE;

    // максимально необходимое количество примеров для статистической обработки, используется если свечей очень много
    public int MAX_NUM_OF_EXAMPLES = 2000; // надо не меньше 1000 TODO вывести настройку в консоль пользователя
    // чтобы считать паттерн статистически ценным надо определить минимальное количество эксемплов на основе количества
    // свечей стораджа, и если их достаточно много, то установить минимумом MAX_NUM_OF_MAIN_EXAMPLES
    public int MINIMUM_NUM_OF_EXAMPLES;

//        ConsoleHelper.getInstance().writeMessage("ProtoPattern: ищу минимум по "+String.valueOf(MINIMUM_NUM_OF_EXAMPLES)+" примеров");}

    // Open первой свечи равен нулю, остальные значения это диапазоны
    private List<BarPatterned> protoBarPatternedList = new ArrayList<>();
    // сдесь хранятся оригинальные образцы графика подходящие под patternedBarList
    private Set<ExampleOfBarChart> examples = new HashSet<>();
    private Set<ExampleOfBarChart> examplesToLong = new HashSet<>();
    private Set<ExampleOfBarChart> examplesToShort = new HashSet<>();
    private Set<ExampleOfBarChart> examplesHighVolatility = new HashSet<>();

    public BigDecimal MINIMUM_STEP_COST;


    public int percentDiffBetweenLongVsShort(){
        float onePercent = (float)examples.size()/(float)100;
        int percentLong = (int) ((float)examplesToLong.size()/onePercent);
        int percentShort = (int) ((float)examplesToShort.size()/onePercent);
        return Math.abs(percentLong-percentShort);
    }

    /* конструктор из полученного примера создает протоПаттерн, расширяя свечные значения так чтоб
    к нему подходило MINIMUM_NUM_OF_MAIN_EXAMPLES примеров
    ищет после каждого примера изменение цены на величину BarStorage.getMinimumGoalOfDeal
    сохраняет шаг на котором это произошло и записывает каждый пример к один из списков ВВЕРХ ВНИЗ ФЛЭТ
    примеры после которых на одном шаге был и ВВЕРХ и ВНИЗ BarStorage.getMinimumGoalOfDeal сохраняются как ФЛЭТ
    */


    public PatternBars(HistoryStorage historyStorage, ExampleOfBarChart exampleOfBarChart) {
//        ConsoleHelper.getInstance().writeLog("ProtoPatern: конструктор с отрезком графика");
//        for (Bar bar : exampleOfBarChart.getBars()) ConsoleHelper.getInstance().writeLog(bar.getText());

        MINIMUM_STEP_COST = historyStorage.getMinimumStepCost();
        HISTORY_SIZE = historyStorage.getBars().size();
//        MINIMUM_NUM_OF_MAIN_EXAMPLES =
//                historyStorage.getBars().size()/20/*5% of size*/> MAX_NUM_OF_MAIN_EXAMPLES ?
//                        MAX_NUM_OF_MAIN_EXAMPLES :
//                        historyStorage.getBars().size()/20;
        MINIMUM_NUM_OF_EXAMPLES =
                historyStorage.getBars().size()/50/*5% of size*/>MAX_NUM_OF_EXAMPLES?
                        MAX_NUM_OF_EXAMPLES:
                        historyStorage.getBars().size()/50;
        this.historyStorage=historyStorage;
        // первая свеча будет помечена как первая
        boolean isFirstBar  = true;

        // последовательно перебираем все свечи эксемпла, и записываем их в паттерн в нормализованном виде
        for (Bar bar : exampleOfBarChart.getBars()) {
            BarPatterned protoBarPatterned = new BarPatterned(isFirstBar, bar, exampleOfBarChart.getBars().get(0).OPEN);
            protoBarPatternedList.add(protoBarPatterned);
            // все остальные свечи НЕ будут помечены как первые
            isFirstBar = false;
        }

        // паттерн экспандится и сохраняется список эксемплов экспанднутого паттерна
        expandNewPatternToMinimumNumOfExamples();


//        setStatsOfChartAfterExamles();
//        раскладывает примеры по трем спискам ВВЕРХ ВНИЗ ФЛЭТ
        divideExamples();
    }



    // расширяет новый паттерн  до MINIMUM_NUM_OF_MAIN_EXAMPLES
    private void expandNewPatternToMinimumNumOfExamples() {
        // расширяем паттерн для получения минимально необходимого колличества примеров.
        // цикл завершится когда колличетсво примеров достигнет MINIMUM_NUM_OF_MAIN_EXAMPLES
//        ConsoleHelper.getInstance().writeLog("ProtoPattern start expand pattern to "+MINIMUM_NUM_OF_EXAMPLES);
        int count = 1;
        while (examples.size()<MINIMUM_NUM_OF_EXAMPLES) {

            expandPattern();
            count++;
            if (count> MAX_EXPAND_FACTOR) {
//                ConsoleHelper.getInstance().writeLog("ProtoPattern BREAK expand, Counter is "+count);
                break;
            }
            reSearchExamplesForPattern();
        }
//        ConsoleHelper.getInstance().writeLog("PatternBars finish expand pattern and found"+examples.size());


    }

    // расширяет паттерн на 1 шаг цены
    private void expandPattern() {

        for (BarPatterned protoBarPatterned : protoBarPatternedList) {
            protoBarPatterned.expand(MINIMUM_STEP_COST);

        }
    }

    // метод ищет в BarsStorage куски которые подходят под паттерн
    // TODO оптимизировать, этот метод является самым длительным, методы к которым он обращается делаются быстро
    public void reSearchExamplesForPattern() {

        for (int indexBarStorage = 0; indexBarStorage < HISTORY_SIZE - protoBarPatternedList.size()-LAST_BARS; indexBarStorage++) {

//            // если от индекса и до конца хранилища не поместится ексемпл размера паттерна то выходим из цикла
//            if (HISTORY_SIZE<indexBarStorage+ protoPatternedBarList.size()) break;
            ExampleOfBarChart testExample = new ExampleOfBarChart(historyStorage,indexBarStorage, protoBarPatternedList.size());
            tryAddExampleToPattern(testExample);


        }

    }

    private void divideExamples(){
        for (ExampleOfBarChart example : examples) {
            int step = 1;
            BigDecimal openToLong = new BigDecimal(0);
            int stepOpenLong=0;
            BigDecimal closeToLong = new BigDecimal(0);
            int stepCloseLong;
            BigDecimal openToShort = new BigDecimal(0);
            int stepOpenShort=0;
            BigDecimal closeToShort = new BigDecimal(0);
            int stepCloseShort;
            Set<Deal> dealsLong = new HashSet<>();
            Set<Deal> dealsShort = new HashSet<>();
            for (
                    int index = example.getEndAdress()+1; // индекс поиска сделки равен свече сразу за эксемплом
                    index < example.getEndAdress()+1 + STEP_TO_OPEN_AFTER_EXAMPLE //
                            &&
                            index<historyStorage.getBars().size()- STEP_TO_CLOSE_AFTER_EXAMPLE-1;//
                    index++
                    ) {
                Bar bar= historyStorage.getBars().get(index);
                // определяем вход в лонг и его шаг
                if (openToLong.compareTo(new BigDecimal(0))==0 || openToLong.compareTo(bar.LOW)>0) {
                    openToLong=bar.LOW;
                    stepOpenLong = step;
                }
//                else System.out.println();

                // определяем вход в шорт и его шаг
                if (bar.HIGH.compareTo(openToShort)>0) {
                    openToShort=bar.HIGH;
                    stepOpenShort = step;
                }
//                else System.out.println();

                // выходим всегда не последующих шагах
                step++;

                // определяем выход из лонга и его шаг
                if (
                        bar.HIGH.compareTo(openToLong)>0
                                && bar.HIGH.compareTo(closeToLong)>0) {
                    closeToLong=bar.HIGH;
                    stepCloseLong = step;
                    Deal deal = new Deal(Deal.Type.LONG,openToLong,closeToLong,stepOpenLong,stepCloseLong);
                    dealsLong.add(deal);
                }
//                else System.out.println();

                // определяем выход из ШОРТА и его ШАГ
                if (
                        bar.LOW.compareTo(openToShort)<0
                                && (bar.LOW.compareTo(closeToShort)<0 || closeToShort.compareTo(new BigDecimal(0))==0)) {
                    closeToShort=bar.LOW;
                    stepCloseShort = step;
                    Deal deal = new Deal(Deal.Type.SHORT,openToShort,closeToShort,stepOpenShort,stepCloseShort);
                    dealsShort.add(deal);
                }
//                else System.out.println();
            }
            Deal bestLongDeal = new Deal(Deal.Type.LONG,new BigDecimal(0),new BigDecimal(0),0,0);

            // определим лучшую лонг сделку
            for (Deal deal : dealsLong) {
                if (deal.profit.compareTo(bestLongDeal.profit)>0)
                    bestLongDeal=deal;
            }
            // если лучшая лонг сделка дает профита больше чем допустимо минимально
            if (bestLongDeal.profit.compareTo(getMinimumGoalOfDeal())>=0) {
                example.addDeal(bestLongDeal);
                examplesToLong.add(example);

                // вычисляем относительные вход и выход из сделки и сохраняем их в соответствующие списки
                relativeLongsOpens.add(bestLongDeal.open.subtract(example.getCloseValue()));
                relativeLongsCloses.add(bestLongDeal.close.subtract(bestLongDeal.open));
            }

            Deal bestShortDeal = new Deal(Deal.Type.SHORT,new BigDecimal(0),new BigDecimal(0),0,0);

            // определим лучшую ШОРТ сделку
            for (Deal deal : dealsShort) {
                if (deal.profit.compareTo(bestLongDeal.profit)>0)
                    bestShortDeal=deal;
            }
            // если лучшая ШОРТ сделка дает профита больше минимальго дпустимого
            if (bestShortDeal.profit.compareTo(getMinimumGoalOfDeal())>=0) {
                example.addDeal(bestShortDeal);
                examplesToShort.add(example);
                // вычисляем относительные вход и выход из сделки и сохраняем их в соответствующие списки
                relativeShortOpens.add(bestShortDeal.open.subtract(example.getCloseValue()));
                relativeShortCloses.add(bestShortDeal.close.subtract(bestShortDeal.open));
            }

        }
        for (ExampleOfBarChart example : examples) {
            if (example.getDealLong()!=null && example.getDealLong().stepOpen==example.getDealLong().stepClose)
                ConsoleHelper.getInstance().writeLog("PatternBars адреса open и close long сделки совпадают");
            if (example.getDealShort()!=null && example.getDealShort().stepOpen==example.getDealShort().stepClose)
                ConsoleHelper.getInstance().writeLog("PatternBars адреса open и close short сделки совпадают");

        }
    }
    // минимальная цель для совершения сделки, вычисляется лениво
    private BigDecimal minimumGoalOfDeal;

    synchronized public BigDecimal getMinimumGoalOfDeal() {
        if (minimumGoalOfDeal==null) setMinimumGoalOfDeal();
        return minimumGoalOfDeal;
    }
    private void setMinimumGoalOfDeal() {
        BigDecimal ONEHUNDERT = new BigDecimal(100);
        minimumGoalOfDeal =historyStorage.getBars().get(historyStorage.getBars().size()-1).CLOSE.multiply(PERCENT_TO_MINIMUM_GOAL_OF_DEAL).divide(ONEHUNDERT);
    }

    // если длинна фрейма не равна длинне паттерна то сразу false.
    // Иначе проверяет подходит ли фрейм к этому паттерну сразу, если подходит то добавляет фрейм в examples
    // пересчитывает статистику
    // если не подходит то создает новый expansionFactor так чтобы новый фрейм подходил к шаблону, затем проверяет,
    // улучшилась ли статтистика паттерна, при улучшении переписывает старый expansionFactor новым,
    // добавляет фрейм в примеры и возвращает true. Если статистика ухудшилась то возвращает false
    public boolean tryAddExampleToPattern(ExampleOfBarChart exampleOfBarChart) {

        if (exampleOfBarChart.getBars().size()!= protoBarPatternedList.size()) return false;

        BigDecimal openOfExample = exampleOfBarChart.getBars().get(0).OPEN;
        int indexOfExample = 0;

        for (BarPatterned protoBarPatterned : protoBarPatternedList) {
            if (!protoBarPatterned.thisPatternedBarIsFitsForBar(exampleOfBarChart.getBars().get(indexOfExample++), openOfExample)) return false;
        }
        examples.add(exampleOfBarChart);
        return true;

    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof PatternBars)) return false;

        PatternBars patternBars = (PatternBars) o;

        return protoBarPatternedList.equals(patternBars.protoBarPatternedList);

    }

    @Override
    public int hashCode() {
        return protoBarPatternedList.hashCode();
    }

    public Set<ExampleOfBarChart> getExamplesToLong() {
        return examplesToLong;
    }

    public Set<ExampleOfBarChart> getExamplesToShort() {return examplesToShort; }


    public String getStatsLongOrShort() {
        System.out.println("PatternBars: S:"+protoBarPatternedList.size()+" L:"+getExamplesToLong().size()+" S:"+getExamplesToShort().size());
        String stats = "";
        if (examplesToLong.size()>examplesToShort.size()) stats+= "S:"+protoBarPatternedList.size()+" "+"%"+percentDiffBetweenLongVsShort()+" LONG:"+examplesToLong.size()+" SHORT:"+examplesToShort.size();
        if (examplesToShort.size()>examplesToLong.size()) stats+= "S:"+protoBarPatternedList.size()+" "+"%"+percentDiffBetweenLongVsShort()+" SHORT:"+examplesToShort.size()+" LONG:"+examplesToLong.size();
        if (examplesToShort.size()==examplesToLong.size()) stats+= "S:"+protoBarPatternedList.size()+" "+" SHORT:"+examplesToShort.size()+" LONG:"+examplesToLong.size();
//        stats+= "LONG and SHORT is equals";
        return stats;
    }

    // отрицательные значения означают что вход в ЛОНГ меньше последнего close
    private List<BigDecimal> relativeLongsOpens = new ArrayList<>();
    public Collection<? extends BigDecimal> getRelativeLongsOpens() {
        return relativeLongsOpens;
    }

    // отрицательных значений быть не должно, все является приростом от входов в сделку
    private List<BigDecimal> relativeLongsCloses = new ArrayList<>();
    public Collection<? extends BigDecimal> geRelativetLongsCloses() {
        return relativeLongsCloses;
    }

    // отрицательные значения означают что вход в ШОРТ меньше последнего close
    private List<BigDecimal> relativeShortOpens = new ArrayList<>();
    public Collection<? extends BigDecimal> getRelativeShortOpens() {
        return relativeShortOpens;
    }
    // положительных значений быть не должно. отрицательные значение является профитами сделок
    private List<BigDecimal> relativeShortCloses = new ArrayList<>();
    public Collection<? extends BigDecimal> getRelativeShortCloses() {
        return relativeShortCloses;
    }
}

