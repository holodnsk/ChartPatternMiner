import java.io.Serializable;
import java.util.Iterator;
import java.util.Set;
import java.util.TreeSet;

/**
 * Created by 1 on 10.03.2015.
 */
public class StoragePattern implements  Serializable{
    private static final long serialVersionUID = 0;
    private static int maxNumPatterns = 100;

    private Set<PatternFittedBars> patternFittedBarses = new TreeSet<>();

    public static int getMexNumPatterns() {
        return maxNumPatterns;
    }

    public static void setMaxNufPatterns(int n_PATTERNS) {
        maxNumPatterns = n_PATTERNS;
    }

    synchronized public int getMaxLenght() {
        int result = 0;
        for (PatternFittedBars patternFittedBars : patternFittedBarses) {
            if (patternFittedBars.size()>result) result = patternFittedBars.size();
        }
        return result;
    }

    public void addPattern(PatternFittedBars patternFittedBars) {

        // если паттернов еще мало то просто добавить и выйти
        if (patternFittedBarses.size()<maxNumPatterns) {
            synchronized (patternFittedBarses) {
                patternFittedBarses.add(patternFittedBars);
            }
            return;
        }

        // если у нового паттерна процентность лучше чем минимальная в сете
        if (patternFittedBars.getPercentPositiveExamples()>=getMinimumPercentage()){
            synchronized (patternFittedBarses) {
                patternFittedBarses.add(patternFittedBars);
            }
            cutBadlesPatterns();
            ConsoleHelper.getInstance().writeMessage("PatternStorage:"+"Было минимум "+getMinimumPercentage()+"% позитивных примеров");
            FactoryPatterns.getInstance().MINIMUM_PERCENT_DIFF_INTEREST.set(getMinimumPercentage());
            ConsoleHelper.getInstance().writeMessage("PatternStorage:"+"Стало минимум "+getMinimumPercentage()+ "% позитивных примеров");
        }
        // в иных случаях паттерн добавлять не надо
    }


    private void cutBadlesPatterns() {
        // если количество паттернов меньше чем можно иметь то ничего резать не надо
        if (patternFittedBarses.size()<maxNumPatterns) return;

        int minimumPercentage= getMinimumPercentage();

        Iterator<PatternFittedBars> iterator = patternFittedBarses.iterator();
        while (iterator.hasNext()){
            PatternFittedBars patternFittedBars = iterator.next();
            if (patternFittedBars.getPercentPositiveExamples()<minimumPercentage) patternFittedBarses.remove(patternFittedBars);
        }
    }

    public int getMinimumPercentage() {

        // бедем перебирать весь сет и считать от 0 до максимально нужного количества паттернов
        int min=0;
        int count = 0;
        // здесь запомним значение процентности паттерна имеющего номер равный максимально допустимому
        for (PatternFittedBars patternFittedBars : patternFittedBarses) {
            // если мы добрались до максимально допустимого количества паттернов то текущая запись является последней нужной
            if (count++>maxNumPatterns) {
                // сохраним процентность
                min= patternFittedBars.getPercentPositiveExamples();
                break;
            }
        }
        return min;
    }

    public int size() {
        return patternFittedBarses.size();
    }

    synchronized public String getShortStatistics() {

        String result = "";
        for (int lenghtPattern = 0; lenghtPattern <= getMaxLenght(); lenghtPattern++) {
            int count = 0;

            for (PatternFittedBars patternFittedBars : patternFittedBarses) {
                if (patternFittedBars.size()==lenghtPattern) {
                    count++;
                }
            }
            result+="Patterns of Length " + lenghtPattern + ":" + count+"\n";
        }
        return result;
    }

    public String getFullStatistics() {
        String result = "";
        for (PatternFittedBars patternFittedBars : patternFittedBarses) {
            result+="L:"+ patternFittedBars.size()+" "+ patternFittedBars.typePattern.toString()+" "+ patternFittedBars.getPercentPositiveExamples()+"%\n";
        }
        return result;
    }

    public Set<PatternFittedBars> getPatternFittedBarses() {
        return patternFittedBarses;
    }
}
