import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.data.time.Day;
import org.jfree.data.time.ohlc.OHLCSeries;
import org.jfree.data.time.ohlc.OHLCSeriesCollection;
import org.jfree.data.xy.DefaultOHLCDataset;
import org.jfree.data.xy.OHLCDataItem;
import org.jfree.data.xy.OHLCDataset;

import javax.swing.*;
import javax.swing.event.CaretListener;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.BadLocationException;
import java.awt.*;
import java.awt.event.*;
import java.io.FileWriter;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.*;
import java.util.List;

public class ConsoleHelper extends JFrame{

    public static final int MAX_LENGHT_PATTERN = 15;
    private static String LOG = "log.log";

    private static ConsoleHelper instance = new ConsoleHelper();
    private ChartPanel chartPanel;
    private JFreeChart chart;
    private OHLCDataset chartDataSet;
    private OHLCSeriesCollection ohlcSeriesCollection = new OHLCSeriesCollection();


    private final HistoryDownloader downloader[] = new HistoryDownloader[1];
    private ActionListener actionListener = new ActionListener() {
        @Override
        public void actionPerformed(ActionEvent e) {

            new Thread(new Runnable() {
                @Override
                public void run() {
                    if (downloader[0] == null) downloader[0] = new HistoryDownloader(tikerButton.getText());
                    else {
                        downloader[0].interrupt();
                        downloader[0] = new HistoryDownloader(tikerButton.getText());
                    }
                }
            }).start();
        }
    };

    public static ConsoleHelper getInstance() {return instance;}

    private ConsoleHelper() {
        writeLog("\nConsoleHelper запустился");
        mainWindowGeneration();
    }

    List<Bar> barsToDraw = new ArrayList<>();

    private JTextArea messageArea = new JTextArea();
    //    private JScrollPane messagePane = new JScrollPane(messageArea);
    private JScrollPane messagePane = new JScrollPane();


    private JTextField tikerField = new JTextField("короткий код инструмента");
    private JTextArea tikerArea = new JTextArea();

    private JScrollPane tikerSelectorScrollPane = new JScrollPane(tikerArea);
    private JButton tikerButton = new JButton("вначале выберите тикер выше");
    private JPanel tikerPanel = new JPanel(new BorderLayout());

    private JPanel haveTikers = new JPanel();

    private JButton getRecomendsNowButton = new JButton("Получить статистику в неторговое время");


    private JPanel panelSettings = new JPanel(new GridLayout(4,1));
    private JPanel panelMaxLengthField = new JPanel();


    private JPanel panelMaxNumOfPatternsEverLength = new JPanel();

    private final boolean[] tikerFieldIsOnceCliked = {false};

    private MouseAdapter mouseAdapter = new MouseAdapter() {
        @Override
        public void mouseClicked(MouseEvent e) {
            if (!tikerFieldIsOnceCliked[0]) {
                tikerFieldIsOnceCliked[0] = true;
                Thread clearTikerField = new Thread(() -> {
                    tikerField.setText("");
                    try {
                        tikerArea.setText(ChartsCodes.getInstance().getListTikersToString(tikerField.getText()));
                    } catch (IOException | ClassNotFoundException e1) {
                        e1.printStackTrace();
                    }
                });
                clearTikerField.start();
            }

        }
    };

    private DocumentListener documentListener = new DocumentListener() {
        @Override
        public void insertUpdate(DocumentEvent e) {
            changedUpdate(e);
        }

        @Override
        public void removeUpdate(DocumentEvent e) {
            changedUpdate(e);
        }

        @Override
        public void changedUpdate(DocumentEvent e) {
            writeLog("ConsoleHelper вызван tikerField.addInputMethodListener");
            Thread printNewList = new Thread(() -> {
                try {
                    tikerArea.setText(ChartsCodes.getInstance().getListTikersToString(tikerField.getText()));
                } catch (IOException | ClassNotFoundException e1) {
//                    e1.printStackTrace();
                    writeLog("ConsoleHelper ошибка при вызове ChartCodes" + e1.toString());
                }
            });
            printNewList.start();


        }
    };

    private CaretListener listener = e -> {
        try {
            tikerButton.setText(new TikerIdCode().invoke());
        } catch (BadLocationException e1) {
            writeLog("ConsoleHelper: в tikerArea.addCaretListener ошибка при вызове new TikerIdCode().invoke()" + Arrays.toString(e1.getStackTrace()));
        }
    };

    private AdjustmentListener adjustmentListener = e -> e.getAdjustable().setValue(e.getAdjustable().getMaximum());

    private ActionListener actionListener1 = e -> new Thread(new Runnable() {
        @Override
        public void run() {
            HistoryDownloader.skipRefreshOneTime();
        }
    }).start();

    private List<Bar> bars = new ArrayList<Bar>(){{
        add(new Bar("20110728,153500,10.0,15,9,14.0,3900000"));
        add(new Bar("20110728,154000,11.0,17,10,16.0,39720000"));
        add(new Bar("20110728,154500,12.0,19,11,18.0,6080000"));
        add(new Bar("20110728,155000,13.0,21,12.0,20,1070000"));
//            add(new Bar("20110728,155500,14.0,23.0,13.0,21.0,2540000"));
//            add(new Bar("20110728,160000,15.0,25.0,14.0,24,8040000"));
//            add(new Bar("20110728,160000,0.37599,0.37602,0.3753,0.3753,8040000"));
//            add(new Bar("20110728,160500,0.37551,0.37592,0.3751,0.3751,2920000"));
//            add(new Bar("20110728,161000,0.37511,0.3758,0.37461,0.37466,24180000"));
//            add(new Bar("20110728,161500,0.37466,0.37466,0.37354,0.37354,5040000"));
//            add(new Bar("20110728,162000,0.37354,0.37398,0.37342,0.37398,18270000"));
//            add(new Bar("20110728,162500,0.37394,0.37395,0.373,0.373,9320000"));
//            add(new Bar("20110728,163000,0.373,0.37576,0.373,0.37499,23060000"));
//            add(new Bar("20110728,163500,0.37481,0.37649,0.37421,0.376,16620000"));
//            add(new Bar("20110728,164000,0.37598,0.3767,0.37556,0.37598,23770000"));
//            add(new Bar("20110728,164500,0.37598,0.37774,0.37562,0.37568,19090000"));
    }};

    private void mainWindowGeneration(){
        tikerPanel.add(BorderLayout.NORTH, tikerField);
        tikerPanel.add(tikerSelectorScrollPane);
        tikerPanel.add(BorderLayout.SOUTH,tikerButton);
        panelSettings.add(tikerPanel);
        tikerField.addMouseListener(mouseAdapter);
        tikerField.getDocument().addDocumentListener(documentListener);
        tikerButton.setEnabled(false);
        tikerArea.addCaretListener(listener);
        tikerButton.addActionListener(actionListener);
        panelSettings.add(panelMaxLengthField);
        panelSettings.add(panelMaxNumOfPatternsEverLength);
        add(BorderLayout.EAST, panelSettings);
        add(messagePane);

        // автоскролл окна сообщений вниз
        messagePane.getVerticalScrollBar().addAdjustmentListener(adjustmentListener);

        // TODO в панель haveTikers создать и добавить кнопки загрузки тикеров для которых  уже имеются файлы истории
//        panelSettings.add(haveTikers);

        getRecomendsNowButton.addActionListener(actionListener1);
        panelSettings.add(getRecomendsNowButton);

        drawChart(bars);
//        rePaintChart(bars);
//        testDrawChart();
    }

    public void drawChart(List<Bar> bars) {
        writeLog("ConsoleHelper drawChart is run");
//

        OHLCDataItem[] data = new OHLCDataItem[bars.size()];
        OHLCSeries ohlcSeries = new OHLCSeries("A.txt");
        int index = 0;
        for (Bar bar : bars) {
            Date date = bar.getDate();
            double open = bar.OPEN.doubleValue();
            double high = bar.HIGH.doubleValue();
            double low = bar.LOW.doubleValue();
            double close = bar.CLOSE.doubleValue();
            double volume = bar.VOL;
            OHLCDataItem item = new OHLCDataItem(date, open, high, low, close, volume);
            Day period = new Day(date);
//            OHLCItem ohlcItem = new OHLCItem(period,open,high, low, close);
//            ohlcSeries.add(period,open,high, low, close);

            data[index] = item;
            index++;
        }
        String title = "";
        String symbol = "";
        chartDataSet = new DefaultOHLCDataset(symbol, data);




        chart = ChartFactory.createCandlestickChart(title, "", "", chartDataSet, false);
        chartPanel = new ChartPanel(chart);
        chartPanel.setPopupMenu(null);
        add(chartPanel);

////((CandlestickRenderer)((XYPlot)chart.getPlot()).getRenderer()).setAutoWidthMethod(CandlestickRenderer.WIDTHMETHOD_INTERVALDATA);
        NumberAxis numberAxis = (NumberAxis) chart.getXYPlot().getRangeAxis();
        numberAxis.setAutoRangeIncludesZero(false);
        numberAxis.setAutoRangeStickyZero(true);

// както искажает масштаб времени
//        DateAxis dateAxis = (DateAxis)chart.getXYPlot().getDomainAxis();
//        SegmentedTimeline timeline = SegmentedTimeline.newFifteenMinuteTimeline();
//        dateAxis.setTimeline(timeline);

    }



    Map<String,HistoryStorage> storageMap = new HashMap<>();

    public void writeSignals(String nameContract, String idContract, String FILE_EXT, String timeframe) {

        String key = nameContract + idContract + timeframe;
        if (!storageMap.containsKey(key)) storageMap.put(key,new HistoryStorage(nameContract, idContract, FILE_EXT,timeframe));
        else storageMap.get(key).tryRefreshData();
        HistoryStorage historyStorage = storageMap.get(key);

        writeMessage(nameContract + " " + timeframe);
        writeMessage(historyStorage.lastBarsToString(2));

        writeLog("ConsoleHelper показаны последние две свечи");

        // сет содержит найденные паттерны всех длинн TODO нормально не сорирует
        Set<PatternBars> patternBars = new TreeSet<>((o1, o2) -> {
            PatternBars p1 = o1;
            PatternBars p2 = o2;

            int pd1 = p1.percentDiffBetweenLongVsShort();
            int pd2 = p2.percentDiffBetweenLongVsShort();

//                writeLog("pd1:"+pd1+" pd2:"+pd2);

            return Math.abs(pd1-pd2);
        });

        for (int lenghtPattern = 2; lenghtPattern < MAX_LENGHT_PATTERN; lenghtPattern++) {
            PatternBars patternLastBars= new PatternBars(historyStorage,new ExampleOfBarChart(historyStorage,historyStorage.getBars().size()-1-lenghtPattern,lenghtPattern));

            patternBars.add(patternLastBars);
        }
        // теперь в patternBars содержатся найденные паттерны всех длинн

        // выводим краткую истатистику и вытаскиваем входы и выходы сделок лонг и ширт в разные списки
        List<BigDecimal> longOpens = new ArrayList<>();
        List<BigDecimal> longCloses = new ArrayList<>();
        List<BigDecimal> shortOpens = new ArrayList<>();
        List<BigDecimal> shortCloses = new ArrayList<>();

        // собирает открытия и закрытия паттернов разных длинн в обобщенные списки

        for (PatternBars pattern : patternBars) {

            ConsoleHelper.getInstance().writeMessage(pattern.getStatsLongOrShort());
            longOpens.addAll(pattern.getRelativeLongsOpens());
            longCloses.addAll(pattern.geRelativetLongsCloses());
            shortOpens.addAll(pattern.getRelativeShortOpens());
            shortCloses.addAll(pattern.getRelativeShortCloses());
        }
        Collections.sort(longOpens);
        Collections.sort(longCloses);
        Collections.sort(shortOpens);
        Collections.sort(shortCloses);


//        writeMessage("ConsoleHelper 100% сделок LONG открываются не ниже "+longOpens.get());


    }

    public void clearMessageArea() {
        messageArea.setText("");
    }

    public void writeMessage(String message) {
        messageArea.append(message);
        messageArea.append("\n");
        writeLog(message + "\n");
    }

    public void writeLog(String logMessage) {
        System.out.println(logMessage);
        try {
            FileWriter file = new FileWriter(LOG,true);
            file.append(new Date() + " " + logMessage + "\n");
            file.close();
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("При записи лога в файл произошла ошибка");
        }


    }

    private class TikerIdCode {
        public String invoke() throws BadLocationException {
            int lineNumber = tikerArea.getLineOfOffset(tikerArea.getCaretPosition());
            String text[] = tikerArea.getText().split("\n");
            tikerButton.setEnabled(true);
            return text[lineNumber].replace("'","");
        }
    }
}
