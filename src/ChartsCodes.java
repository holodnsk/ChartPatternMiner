import java.io.*;
import java.net.URL;
import java.util.*;

public class ChartsCodes {

    private static String CHART_CODES_FILE ="chartCodes.txt";
    private static String URL_OFCODES="http://www.finam.ru/cache/icharts/icharts.js";


    private Map<String, String> idsCodes;

    private static ChartsCodes instance;
    static {
        try {
            instance=new ChartsCodes();
        } catch (IOException | ClassNotFoundException e) {
            ConsoleHelper.getInstance().writeLog("ChartsCodes при статичной инициализации синглотона произошла ошибка"+ Arrays.toString(e.getStackTrace()));
            e.printStackTrace();
        }
    }

    // http://www.finam.ru/cache/icharts/icharts.js

    private ChartsCodes() throws IOException, ClassNotFoundException {
        File dir = new File(".");
        File[] filesList = dir.listFiles();

        boolean fileIsFound = false;

        for (File file : filesList) {
            if (file.isFile()) {
                if (file.getName().equals(CHART_CODES_FILE)) {
                    fileIsFound=true;
                    break;
                }
            }

        }
        if (!fileIsFound){
            // если не нашли файла то
            // качаем данные из URL_OFCODES в мапу idsCodes
            ConsoleHelper.getInstance().writeLog("ChartsCodes: загружаю список кодов инструментов");
            downloadCodes();
            // создаем файл CHART_CODES_FILE и сохраняем в него данные обоих мап
            ConsoleHelper.getInstance().writeLog("ChartsCodes: сохраняю список кодов инструментов");
            saveData();
        }

        // здесь мы знаем что файл CHART_CODES_FILE уже есть
        // поэтому грузим данные из файла CHART_CODES_FILE в мапы

        ObjectInputStream ois = new ObjectInputStream(new FileInputStream(CHART_CODES_FILE));
        if (ois.available()>0) idsCodes = (Map<String, String>) ois.readObject();
        ois.close();

        // грузим данные из URL_OFCODES и сравниваем их со старыми
        ConsoleHelper.getInstance().writeLog("ChartsCodes: обновляю список кодов инструментов");
        downloadCodes();

        // сохраняем данные в CHART_CODES_FILE
        ConsoleHelper.getInstance().writeLog("ChartsCodes: записываю список кодов инструментов");
        saveData();
        ConsoleHelper.getInstance().writeLog("ChartsCodes: знаю "+ idsCodes.size()+" кодов инструментов");

    }

    private void saveData() throws IOException {
        ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(CHART_CODES_FILE));
        oos.writeObject(idsCodes);
        oos.close();
    }


    private void downloadCodes() throws IOException {
        URL website = new URL(URL_OFCODES);

        Scanner scanner = new Scanner(new InputStreamReader(website.openStream(),"cp1251"));
        String stringedSite = "";
        while (scanner.hasNext()) stringedSite+= scanner.nextLine();

        stringedSite=stringedSite.substring(0,stringedSite.indexOf("var aEmitentMarkets"));

        // в каждой ячейке будет строка содержащая имя массива и его значения
        String content[] = stringedSite.split(";");

        // интересуют только эти переменные из урла
        List<String> aEmitentIds = new ArrayList<>();
        List<String> aEmitentCodes = new ArrayList<>();

        // найдем в массиве переменную aEmitentIds и массив ее данных положим в список
        for (String var : content) if (var.contains("aEmitentIds")) aEmitentIds = cutStringToArray(var);
        // повторим для aEmitentCodes
        for (String var : content) if (var.contains("aEmitentCodes")) aEmitentCodes = cutStringToArray(var);

        Iterator<String> idIterator = aEmitentIds.iterator();
        Iterator<String> codeIterator = aEmitentCodes.iterator();
        Map<String,String> temp = new HashMap<>();
        while (idIterator.hasNext() && codeIterator.hasNext()) {
            temp.put(idIterator.next(), codeIterator.next());
        }

        idsCodes=temp;
        if (idIterator.hasNext() || codeIterator.hasNext()) {
            ConsoleHelper.getInstance().writeMessage("ChartCodes: колличество ID " + aEmitentIds.size()+" и колличество КОДОВ " +aEmitentCodes.size()+" инструментов из " + URL_OFCODES + " не равны");
        }
    }

    private List<String> cutStringToArray(String var) {
        return Arrays.asList(var.substring(var.indexOf("["), var.indexOf("]"))
        /* может случайно сместить порядковые номера
        .replaceAll("\'","")*/
                .split(","));
    }

    public static ChartsCodes getInstance() throws IOException, ClassNotFoundException {

        return instance;
    }


    public String getListTikersToString(String key) {
        String result = "";
        for (Map.Entry<String, String> entry : idsCodes.entrySet()) {
            if (entry.getValue().toUpperCase().contains(key.toUpperCase())) result+=entry.getValue()+" "+entry.getKey()+"\n";
        }

        return result;
    }

}
