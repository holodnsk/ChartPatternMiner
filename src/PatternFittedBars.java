import java.io.Serializable;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Created by 1 on 23.03.2015.
 */
public class PatternFittedBars implements Serializable, Comparable<PatternFittedBars>{
    /*
    экземпляры класса создаются на основе PatternBars путем оптимизации возможных значений OLHC всех свечей базового паттерна
    таким образом чтобы количество
     */

    private List<BarPatterned> barPatternedList = new ArrayList<>();
    Set<ExampleOfBarChart> examplesMain;
    Set<ExampleOfBarChart> examplesOther = new HashSet<>();
    TypePattern typePattern;
    private int expandFactor;

    public PatternFittedBars(Set<ExampleOfBarChart> examplesMain, Set<ExampleOfBarChart> examplesSecond, Set<ExampleOfBarChart> examplesThird, TypePattern typePattern) {

        this.typePattern=typePattern;
        this.examplesMain=new HashSet<>(examplesMain);
        this.expandFactor=expandFactor;
        boolean isFirstBar = true;
        int maxIndex = 0;
        // посчитаем длинну будущего паттерна взяв длинну одного эксемпла
        for (ExampleOfBarChart exampleOfBarChart : examplesMain) {
            maxIndex = exampleOfBarChart.getBars().size();
            break;
        }

        // создаем список ОПЕНОВ всех примеров
        List<BigDecimal> opensOfExamples = new ArrayList<>();
        for (ExampleOfBarChart exampleOfBarChart : examplesMain) {
            opensOfExamples.add(exampleOfBarChart.getBars().get(0).OPEN);
        }

        // перебираем последовательно номера свечей в примерах
        for (int index = 0; index < maxIndex; index++) {

            // создаем список свечей из всех примеров под одним номером
            List<Bar> bars = new ArrayList<>();
            for (ExampleOfBarChart exampleOfBarChart : examplesMain) {
                bars.add(exampleOfBarChart.getBars().get(index));
            }

            // создаем свечу на основе списка свечей одного номера из всех примеров
            BarPatterned barPatterned = new BarPatterned(isFirstBar,bars,opensOfExamples);

            // добавляем эту свечу в описание паттерна
            barPatternedList.add(barPatterned);

            // и для следующиех итераций сообщаем что первая свеча уже была
            isFirstBar=false;
        }
        for (ExampleOfBarChart exampleOfBarChart : examplesSecond) {
            tryAddExampleToPattern(exampleOfBarChart);
        }
        for (ExampleOfBarChart exampleOfBarChart : examplesThird) {
            tryAddExampleToPattern(exampleOfBarChart);
        }

    }

    // если длинна фрейма не равна длинне паттерна то сразу false.
    // Иначе проверяет подходит ли фрейм к этому паттерну сразу, если подходит то добавляет фрейм в examples
    // пересчитывает статистику
    // если не подходит то создает новый expansionFactor так чтобы новый фрейм подходил к шаблону, затем проверяет,
    // улучшилась ли статтистика паттерна, при улучшении переписывает старый expansionFactor новым,
    // добавляет фрейм в примеры и возвращает true. Если статистика ухудшилась то возвращает false
    public boolean tryAddExampleToPattern(ExampleOfBarChart exampleOfBarChart) {

        if (!isExampleFitToPattern(exampleOfBarChart)) return true;
        examplesOther.add(exampleOfBarChart);
        return true;

    }

    public boolean isExampleFitToPattern(ExampleOfBarChart exampleOfBarChart) {
        if (exampleOfBarChart.getBars().size()!= barPatternedList.size()) return false;

        BigDecimal openOfExample = exampleOfBarChart.getBars().get(0).OPEN;
        int indexFrame = 0;

        for (BarPatterned protoBarPatterned : barPatternedList) {
            if (!protoBarPatterned.thisPatternedBarIsFitsForBar(exampleOfBarChart.getBars().get(indexFrame++), openOfExample))
                return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return typePattern + " " + barPatternedList + " examples"+ examplesMain.size() +" antiExamples"+ examplesOther.size();
    }

    public int size() {
        return barPatternedList.size();
    }

    public int getExpandFactor() {
        return expandFactor;
    }
    public int getMainExapmlesCount(){
        return examplesMain.size();
    }
    public int getOtherExapmlesCount(){
        return examplesOther.size();
    }


    @Override
    public int compareTo(PatternFittedBars patternFittedBars) {

        return patternFittedBars.getPercentPositiveExamples()-this.getPercentPositiveExamples();
    }

    public int getPercentPositiveExamples() {

        int result = getMainExapmlesCount() * 100 / (this.getMainExapmlesCount() + this.getOtherExapmlesCount());
        return result;
    }
}
