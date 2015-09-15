import java.util.List;

/**
 * Created by 1 on 01.04.2015.
 */
public class Advisor {
    public static Advisor getInstance() {
        if (instance==null) instance = new Advisor(HistoryStorage.getInstance());
        return instance;
    }
    static private Advisor instance ;
    HistoryStorage historyStorage;

    private Advisor(HistoryStorage historyStorage) {
        this.historyStorage = historyStorage;
    }

    public void runAdvice() {
        List<ExampleOfBarChart> lastExamples = historyStorage.getLastExamples();

        for (ExampleOfBarChart example : lastExamples) {
//            ConsoleHelper.getInstance().writeMessage("Advisor: проверяю "+example.toString());
            for (PatternFittedBars patternFittedBars : ApplicationExecutor.storagePattern.getPatternFittedBarses()) {
                if (patternFittedBars.isExampleFitToPattern(example)) ConsoleHelper.getInstance().writeMessage(patternFittedBars.typePattern.toString()+" "+ patternFittedBars.getPercentPositiveExamples());
            }
        }

    }
}
