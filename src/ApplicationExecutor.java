import javax.swing.*;
import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;

public class ApplicationExecutor {

    public static final String EXTENSION_FILE_PATTERNS_BAR = ".patternsBAR";
    static public StoragePattern storagePattern = new StoragePattern();

    public static void main(String[] args) {

        // создаем главное окно
        Thread mainWindow = new Thread(() -> {
            ConsoleHelper.getInstance().setTitle("Candle pattern miner");
            ConsoleHelper.getInstance().setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            ConsoleHelper.getInstance().setSize(600, 200);
            ConsoleHelper.getInstance().setExtendedState(JFrame.MAXIMIZED_BOTH);
            ConsoleHelper.getInstance().setVisible(true);
        });
        mainWindow.start();
        while(ConsoleHelper.getInstance()==null);


        //обновляем или получаем списки кодов интсрументов
        Thread chartCodes = new Thread(() -> {
            try {
                ChartsCodes.getInstance();
            } catch (IOException e) {
                ConsoleHelper.getInstance().writeLog("Director: ошибка в ChartsCodes"+e.toString());
            } catch (ClassNotFoundException e) {
                ConsoleHelper.getInstance().writeLog("Director: при вызове ChartsCodes"+e.toString());
                e.printStackTrace();
            }
        });
        chartCodes.start();
    }

    public static void checkReadedPatternStorage() {
        // проверяем наличие сохраненных паттернов к этому файлу, и если они есть то грузим и не ищем новые паттерны
        // TODO искать паттерны в той части стораджа которая новая
        // TODO искать паттерны такой длинны которой еще нет в PatternStorage
        // создаем список файлов в тойже директории из которой за
        File f = new File(HistoryStorage.getInstance().getDir());
        ArrayList<String> names = new ArrayList<String>(Arrays.asList(f.list()));
        for (String name : names) {
            if ((HistoryStorage.getInstance().getFileName() + EXTENSION_FILE_PATTERNS_BAR).equals(name)) {
                ConsoleHelper.getInstance().writeMessage("Director:"+"reading PatternStorage from file");
                try {
                    ObjectInputStream ois = new ObjectInputStream(new FileInputStream(HistoryStorage.getInstance().getDir()+"\\"+ HistoryStorage.getInstance().getFileName()+ EXTENSION_FILE_PATTERNS_BAR));

                    ApplicationExecutor.storagePattern = (StoragePattern) ois.readObject();

                } catch (IOException e) {
                    e.printStackTrace();
                    ConsoleHelper.getInstance().writeMessage("Director:"+"error recovering PatternStorage from file");
                } catch (ClassNotFoundException e) {
                    e.printStackTrace();
                    ConsoleHelper.getInstance().writeMessage("Director:"+"error recovering PatternStorage from file");
                }

                ConsoleHelper.getInstance().writeMessage("Director:"+"loaded PatternStorage");
                ConsoleHelper.getInstance().writeMessage("Director:\n"+ ApplicationExecutor.storagePattern.getShortStatistics());
                // проверяем какой максимальной длинны есть уже готовые паттерны, и если считать новые на надо то завершаем метод
                if (ApplicationExecutor.storagePattern.getMaxLenght()>= FactoryPatterns.MAX_LENGTH_PATTERN) return;
                // если считать надо то устанавливаем минимальную длинну которую будем считать больше максимальной длинны уже посчитанных паттернов
                FactoryPatterns.MIN_LENGTH_PATTERN = ApplicationExecutor.storagePattern.getMaxLenght()+1;

            }
        }
        ConsoleHelper.getInstance().writeMessage("Director:"+"min="+ FactoryPatterns.MIN_LENGTH_PATTERN +" max="+ FactoryPatterns.MAX_LENGTH_PATTERN);
    }

    public static void savePatternStorage() {
        // сохраняем файл
        String fullFileName = HistoryStorage.getInstance().getDir()+"\\"+ HistoryStorage.getInstance().getFileName()+ EXTENSION_FILE_PATTERNS_BAR;
        ConsoleHelper.getInstance().writeMessage("Director:"+"saving patterns to file " + fullFileName);
        try {
            ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(fullFileName));
            oos.writeObject(ApplicationExecutor.storagePattern);
            oos.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
