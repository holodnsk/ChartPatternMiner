import java.io.*;
import java.net.URL;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;


/**
 * Created by 1 on 12.06.2015.
 */
public class HistoryDownloader {
    // http://195.128.78.52/RSTI_150601_150617.txt?market=1&em=20971&code=RSTI&df=1&mf=5&yf=2015&from=01.06.2015&dt=17&mt=5&yt=2015&to=17.06.2015&p=7&f=RSTI_150601_150617&e=.txt&cn=RSTI&dtf=1&tmf=1&MSOR=1&mstime=on&mstimever=1&sep=1&sep2=1&datf=5
    // с финама
    // RSTI
    // 1 час
    // RSTI_130101_150331.txt
    // формат даты ггггммдд
    // формат времени ччммсс
    // указано московское время окончания свечи
    // разделитель полей ,
    // разделитель разрядов нет
    // формат DATE, TIME, OPEN, HIGH, LOW, CLOSE, VOL
    // заголовок файла не добавлять
    // не заполнять периоды без сделок
    // с 01.01.2013 по на день в будущее: вернет последние имеющиеся данные

    // финам дает таймфреймы: тикер 1м 5м 10м 15м 30м 1час 1день 1неделя 1месяц
    private static boolean skipRefresh = false;


    String  nameContract;
    String  idContract;
    public static final String FILE_EXT = ".txt";
    public static final Integer START_YEAR = 2000;
    private boolean isInterrupt = false;

    public HistoryDownloader(String nameContractId) {
        ConsoleHelper.getInstance().writeLog("HistoryDownloader получил код и id инструмента " + nameContractId);
        String args[] = nameContractId.split(" ");
        this.nameContract = args[0];
        this.idContract=args[1];

        Thread refresher = new Thread(new Runnable() {
            @Override
            public void run() {
                boolean firstIter = true;
                // вечный цикл
                while(!isInterrupt) {
                    ConsoleHelper.getInstance().writeLog("HistoryDownloader: новая итерация вечного цикла");
                    // блок ожидания начала каждой новой пятимунутки
                    waitor:
                    while (!firstIter) { // не спим если это первая итераци за сессию
                            try {
                                // TODO вычислять сколько секунд осталось до следующей пятиминутки и спать именно это время

                                Thread.currentThread().sleep(9000); //
                            } catch (InterruptedException e) {
                                for (StackTraceElement stackTraceElement : e.getStackTrace())
                                    ConsoleHelper.getInstance().writeLog(stackTraceElement.toString());
                            }

                        Date date = new Date();
                        if (
                                (date.getMinutes()%5==5 ||date.getMinutes()%5==0)
                                        && (date.getSeconds() > 0 && date.getSeconds() <= 10)
                                )
                            break waitor;
                    }
                    firstIter=false;

                    refresher:
                    while (true) {
                        if (download(3)/*5м*/|| skipRefresh) break refresher;

                        try {
                            Thread.currentThread().sleep(250);
                            ConsoleHelper.getInstance().writeLog("HistoryDownloader: итерация ожадания попыток обновить свечу короткого таймфрейма");

                        } catch (InterruptedException e) {
                            for (StackTraceElement stackTraceElement : e.getStackTrace())
                                ConsoleHelper.getInstance().writeLog(stackTraceElement.toString());
                        }

                    }
                    skipRefresh=false;

                    // после успешной загрузки свежих данных выводим пользователю рекомендации
                    ConsoleHelper.getInstance().clearMessageArea();
                    ConsoleHelper.getInstance().writeSignals(nameContract,idContract,FILE_EXT,timeframes.get(3));
//                    остальные таймфреймы пытаемся обновить каждый раз после обновления самого короткого таймфрейма
                        download(3); //5м

//                    download(4); //10м
//                    ConsoleHelper.getInstance().writeSignals(nameContract,idContract,FILE_EXT,timeframes.get(4));
//                    download(5); //15м
//                    ConsoleHelper.getInstance().writeSignals(nameContract,idContract,FILE_EXT,timeframes.get(5));
//                    download(6); //30м
//                    ConsoleHelper.getInstance().writeSignals(nameContract,idContract,FILE_EXT,timeframes.get(6));
//                    download(7); //1час
//                    ConsoleHelper.getInstance().writeSignals(nameContract,idContract,FILE_EXT,timeframes.get(7));
//                    download(8); //1день
//                    ConsoleHelper.getInstance().writeSignals(nameContract,idContract,FILE_EXT,timeframes.get(8));
//                    download(9); //1неделя
//                    ConsoleHelper.getInstance().writeSignals(nameContract,idContract,FILE_EXT,timeframes.get(9));
//                    download(10);//1месяц
//                    ConsoleHelper.getInstance().writeSignals(nameContract,idContract,FILE_EXT,timeframes.get(10));

                }

            }
        });

        refresher.setDaemon(true);// чтоб нить прибилась при выходе из программы
        refresher.start();




    }

    static public void skipRefreshOneTime(){
        skipRefresh=true;
    }

    // загружает историю в HistoryStorage
    private boolean download(final int timeframe) {
        boolean downloaded = false;
        final String filename = nameContract+"-"+idContract+"-"+timeframe;

        if (!fileIsFound(filename)){
            ConsoleHelper.getInstance().writeMessage("HistoryDownloader файл истории не обнаружен: пытаюсь загрузить историю инструмента " + nameContract);


            // если не нашли файла то заказываем качать от 1995г по сейчас+день на этот таймфрем и этот инстурмент
            Date startDate = new Date();
            startDate.setSeconds(0);
            startDate.setMinutes(0);
            startDate.setDate(1);
            startDate.setMonth(0);
            startDate.setYear(START_YEAR-1900);
            Date endDate = new Date();
            endDate.setTime(endDate.getTime()+1000*60*60*24); // + день к текущей дате чтобы финам нам дал свежайшие данные
            List<Bar> history = downloadData(filename, FILE_EXT, startDate, endDate, timeframe);

            String message = new String();
            if (history.size()>0)message =
                            "HistoryDownloader: загружена история инструмента:"+nameContract
                            +" ID:"+idContract
                            +" Таймфрейм:"+ timeframes.get(timeframe)
                            +" всего свечей:"+ history.size()+"\n"
                            +" первая свеча:"+history.get(0)+"\n"
                            +" последняя свеча:"+ history.get(history.size()-1)+"\n"
                    ;
            ConsoleHelper.getInstance().writeLog(message);
            ConsoleHelper.getInstance().writeMessage(message);

            downloaded = saveHistory(history,filename);


        }
//        ConsoleHelper.getInstance().writeMessage("HistoryDownloader обновляю данные " + nameContract);
        try {
            // здесь мы знаем что файл уже есть поэтому берем его последнюю строку
            Bar lastBarfromFile = getLastBarFromFileHistory(filename);
            Date nowDatePlusDay = new Date();
            nowDatePlusDay.setDate(nowDatePlusDay.getDate()+1);

            //скачиваем историю начиная с даты последнего бара
            List<Bar> freshHistory = downloadData(filename,FILE_EXT,lastBarfromFile.getDate(),nowDatePlusDay,timeframe);
            // и сохраняем полученные данные
            downloaded= saveHistory(freshHistory,filename);
        } catch (FileNotFoundException e) {
            ConsoleHelper.getInstance().writeLog("HistoryDownloader при загрузке последнего бара файл не найден " + filename + FILE_EXT);
            for (StackTraceElement stackTraceElement : e.getStackTrace()) ConsoleHelper.getInstance().writeLog(stackTraceElement.toString());
        }

        return downloaded;
    }

    private boolean fileIsFound(String filename) {
        File dir = new File(".");
        File[] filesList = dir.listFiles();

        for (File file : filesList) {
            if (file.isFile()) {
                if (file.getName().equals(filename+ FILE_EXT))  return true;
            }
        }
        return false;
    }

    private boolean saveHistory(List<Bar> history, String filename) {
        boolean savedFreshHistory = false;
        ConsoleHelper.getInstance().writeLog("HistoryDownloader: сохраняю" + filename + FILE_EXT);

        try {
            if (fileIsFound(filename)) {
                Bar lastBarInFile = getLastBarFromFileHistory(filename);

                // если время свечей в конце файла и в начале истории равны но параметры свечей разные, то удаляем последнюю свечу в файле
                if (lastBarInFile.equalsTime(history.get(0)) && !lastBarInFile.equals(history.get(0))){
                    RandomAccessFile randomAccessFile = new RandomAccessFile(filename + FILE_EXT,"rw");
                    long length = randomAccessFile .length() - 1;
                    byte b;
                    do {
                        length -= 1;
                        randomAccessFile .seek(length);
                        b = randomAccessFile .readByte();
                    } while(b != 10);
                    randomAccessFile .setLength(length + 1);
                    randomAccessFile .close();

                }
                if (history.contains(lastBarInFile)) {
                    cutExtraHistory(history, lastBarInFile);
                }
            }
            // аппендим в файл остаток history если в нем чтото есть
            if (history.size()>0) {
                FileWriter fileWriter = new FileWriter(filename + FILE_EXT, true);
                for (Bar bar : history) {
                    fileWriter.append(bar.toString() + "\n");
                    savedFreshHistory = true;
                }
                fileWriter.close();
            }

        } catch (FileNotFoundException e) {
            ConsoleHelper.getInstance().writeLog("HistoryDownloader файл не найден "+filename+FILE_EXT);
            for (StackTraceElement stackTraceElement : e.getStackTrace()) ConsoleHelper.getInstance().writeLog(stackTraceElement.toString());
        } catch (IOException e) {
            ConsoleHelper.getInstance().writeLog("HistoryDownloader ошибка при записи файла" + filename + FILE_EXT);
            for (StackTraceElement stackTraceElement : e.getStackTrace()) ConsoleHelper.getInstance().writeLog(stackTraceElement.toString());
        }
        return savedFreshHistory;
    }

    private void cutExtraHistory(List<Bar> history, Bar lastBarInFile) {
        Iterator<Bar> irerator = history.iterator();
        while (irerator.hasNext()) {
            Bar bar = irerator.next();
            if (bar.equals(lastBarInFile)) {
                // как только нашли нужный Бар, удаляем его
                irerator.remove();
                // и выходим из цикла чтобы остальные Бары остались
                break;
            }
            // удаляем все Бары пока не выйдем из цикла
            irerator.remove();
        }
    }

    private Bar getLastBarFromFileHistory(String filename) throws FileNotFoundException {
        Scanner scanner = new Scanner(new FileReader(filename + FILE_EXT));
        String lastLine = new String();
        while (scanner.hasNextLine()) lastLine = scanner.nextLine();
        scanner.close();
        return new Bar(lastLine);
    }

    private static List<String> timeframes = Arrays.asList("","тикер","1минута", "5минут", "10минут","15минут","30минут","1час","1день","1неделя","1месяц");


    public static int getNumberOfTimeframe(String nameTimeFrame){
        return timeframes.indexOf(nameTimeFrame);

    }

    private List<Bar> downloadData(String filename, String fileExt, Date startDate, Date endDate, int timeframe) {
        Set<Bar> barSet = new TreeSet<>();
        if (endDate.getYear()-startDate.getYear()>1){
            ConsoleHelper.getInstance().writeLog("HistoryDownloader: диапазон дат длинней допустимого, делим на диапазоны по одному году");
            while (startDate.getYear() <endDate.getYear()) {
                long time = startDate.getTime();
                Date newEndDate = new Date(time);
                newEndDate.setYear(newEndDate.getYear()+1);// последовательно прибавляем год
                newEndDate.setDate(newEndDate.getDate()-1);// отнимаем один день чтобы даты не перекрывались
                List<Bar> temp = downloadData(filename, fileExt, startDate, newEndDate, timeframe);
                barSet.addAll(temp);
                startDate.setYear(startDate.getYear()+1); // после загрузки данных прибавляем к начальной дате один год для следующей итерации
            }
        }
        ConsoleHelper.getInstance().writeLog("HistoryDownloader: загружаю "+filename+fileExt);
        DateFormat dateFormat= new SimpleDateFormat("dd.MM.yyyy");
        String dateString = dateFormat.format(endDate);
        //
        String url =
                "http://195.128.78.52/" // откуда качаются данные TODO прикрутить автоматическое определение IP адреса
                        + filename+fileExt // имя файла, в моем кнтексте может быть любым, указываю для порядка
                        +"?market=1"  // Номер рынка, видимо не работает
                        +"&em=" +idContract // Номер инструмента
                        +"&code=" + nameContract// Тикер инструмента
                        +"&df="+startDate.getDate()  // Начальный день (1-31)
                        +"&mf="+startDate.getMonth()  // Начальный месяц (0-11)
                        +"&yf=" +(startDate.getYear()+1900) // Начальный год
                        +"&from="+ dateFormat.format(startDate) // полная начальная дата в формате 20.12.2014
                        +"&dt=" +endDate.getDate() // Конечный день начинаются – с 1.
                        +"&mt=" + endDate.getMonth()//Конечный номер месяца начиная с 0;
                        +"&yt=" + (endDate.getYear()+1900)// Конечный год
                        +"&to=" + dateString  // Конечная дата	в формате 20.12.2014
                        +"&p=" +timeframe // Таймфрейм: для тиковых 1; 1м 2; 5м 3; 10м 4; 15м 5; 30м 6; 1час 7; 1день 8; 1неделя 9; 1месяц10.
                        +"&f=" + filename // Имя  файла
                        +"&e=" + fileExt // Расширение  файла
                        +"&cn=" + nameContract// Имя контракта	GBPUSD
                        +"&dtf=1"  // Номер формата дат
                        +"&tmf=1"  // Номер формата времени
                        +"&MSOR=0"  // Время свечи (0 - open; 1 - close)	1   в квике open
                        +"&mstime=on"  // Московское время on, если не московское этот параметр опускается
                        +"&mstimever=1"  // mstimever	Коррекция часового пояса	1
                        +"&sep=1"  // Разделитель полей
                        +"&sep2=1"  // Разделитель разрядов
                        +"&datf=5" //Формат записи в файл
                        +"&fsp=1" //заполнять периоды без сделок, если не нужны параметр опустить
                //at Нужны ли заголовки столбцов 1, если не нужны параметр опустить
                ;

        ConsoleHelper.getInstance().writeLog("загружаю адрес\n" + url);


        try {
            Scanner scanner = new Scanner(new InputStreamReader(new URL(url).openStream()));
            while (scanner.hasNext()) {
                String line = scanner.nextLine();
                Bar newBar = new Bar(line);


                barSet.add(newBar);


            }
        } catch (IOException e) {
            ConsoleHelper.getInstance().writeLog("HistoryDownloader: ошибка во время загрузки истрических данных из " + url + "\n" + e.getMessage());
        }
        List<Bar> result = new ArrayList<>(barSet);
        return result;
    }


    public void interrupt() {
        isInterrupt = true;
    }
}
