import javax.swing.*;
import java.io.IOException;

public class ApplicationExecutor {

    public static void main(String[] args) throws InterruptedException {

        // создаем главное окно
        new Thread(() -> {
            ConsoleHelper.getInstance().setTitle("Candle pattern miner");
            ConsoleHelper.getInstance().setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
            ConsoleHelper.getInstance().setSize(600, 200);
            ConsoleHelper.getInstance().setExtendedState(JFrame.MAXIMIZED_BOTH);
            ConsoleHelper.getInstance().setVisible(true);
        }).start();

        // TODO исправить костыль ожидания загрузки главного окна
        // TODO исправить костыль ожидания загрузки главного окна
        while(ConsoleHelper.getInstance()==null){
            Thread.sleep(100);
        }

        //TODO исправить костыль обновления списка кодов интсрументов
        new Thread(() -> {
            try {
                ChartsCodes.getInstance();
            } catch (IOException e) {
                ConsoleHelper.getInstance().writeLog("Director: ошибка в ChartsCodes"+e.toString());
            } catch (ClassNotFoundException e) {
                ConsoleHelper.getInstance().writeLog("Director: при вызове ChartsCodes"+e.toString());
                e.printStackTrace();
            }
        }).start();
    }
}
