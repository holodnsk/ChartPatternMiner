import org.jfree.chart.ChartPanel;
import org.jfree.chart.ChartUtilities;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.DateAxis;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.labels.StandardXYToolTipGenerator;
import org.jfree.chart.plot.CombinedDomainXYPlot;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.*;
import org.jfree.data.time.Day;
import org.jfree.data.time.MovingAverage;
import org.jfree.data.time.TimeSeries;
import org.jfree.data.time.TimeSeriesCollection;
import org.jfree.data.time.ohlc.OHLCSeries;
import org.jfree.data.time.ohlc.OHLCSeriesCollection;
import org.jfree.data.xy.IntervalXYDataset;
import org.jfree.data.xy.OHLCDataset;
import org.jfree.ui.ApplicationFrame;
import org.jfree.ui.RefineryUtilities;

import javax.swing.*;
import java.awt.*;
import java.io.BufferedReader;
import java.io.FileReader;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.StringTokenizer;

public class PriceVolumeChart2 extends ApplicationFrame {

    final static String filename = "A.txt";
    static TimeSeries t1 = new TimeSeries("49-day moving average");

    /**
     * Default constructor
     */
    public PriceVolumeChart2(String title)
    {
        super(title);
        JPanel panel = createDemoPanel();
        panel.setPreferredSize(new Dimension(500, 270));
        setContentPane(panel);
    }

    //create price dataset
//hard-coded here
    private static OHLCDataset createPriceDataset(String filename)
    {
        //the following data is taken from http://finance.yahoo.com/
        //for demo purposes...

        OHLCSeries ohlcSeries = new OHLCSeries(filename);

        try {
            BufferedReader in = new BufferedReader(new FileReader(filename));
            DateFormat df = new SimpleDateFormat("yyyyMMdd");
            String inputLine;
            in.readLine();
            while ((inputLine = in.readLine()) != null) {
                StringTokenizer st = new StringTokenizer(inputLine, ",");
                Date date       = df.parse( st.nextToken() );
                double open     = Double.parseDouble( st.nextToken() );
                double high     = Double.parseDouble( st.nextToken() );
                double low      = Double.parseDouble( st.nextToken() );
                double close    = Double.parseDouble( st.nextToken() );
                double volume   = Double.parseDouble( st.nextToken() );
                //double adjClose = Double.parseDouble( st.nextToken() );
                ohlcSeries.add(new Day(date), open, high, low, close);
                t1.add(new Day(date), close);
            }
            in.close();
        }
        catch (Exception e) {
            e.printStackTrace();
        }



        OHLCSeriesCollection dataset = new OHLCSeriesCollection();
        dataset.addSeries(ohlcSeries);

        return dataset;
    }


    //create volume dataset
    private static IntervalXYDataset createVolumeDataset(String filename)
    {
        //create dataset 2...
        TimeSeries s1 = new TimeSeries("Volume");

        try {
            BufferedReader in = new BufferedReader(new FileReader(filename));
            DateFormat df = new SimpleDateFormat("yyyyMMdd");
            String inputLine;
            in.readLine();
            while ((inputLine = in.readLine()) != null) {
                StringTokenizer st = new StringTokenizer(inputLine, ",");
                Date date = df.parse( st.nextToken() );
                st.nextToken();
                st.nextToken();
                st.nextToken();
                st.nextToken();
                double volume   = Double.parseDouble( st.nextToken() );
                //double adjClose = Double.parseDouble( st.nextToken() );
                s1.add(new Day(date), volume);
            }
            in.close();
        }
        catch (Exception e) {
            e.printStackTrace();
        }

        return new TimeSeriesCollection(s1);
    }

    private static JFreeChart createCombinedChart()
    {
        OHLCDataset data1 = createPriceDataset(filename);

        XYItemRenderer renderer1 = new HighLowRenderer();
        renderer1.setBaseToolTipGenerator(new StandardXYToolTipGenerator(
                StandardXYToolTipGenerator.DEFAULT_TOOL_TIP_FORMAT,
                new SimpleDateFormat("d-MMM-yyyy"), new DecimalFormat("0.00")));
        renderer1.setSeriesPaint(0, Color.blue);
        DateAxis domainAxis = new DateAxis("Date");
        NumberAxis rangeAxis = new NumberAxis("Price");
        rangeAxis.setNumberFormatOverride(new DecimalFormat("$0.00"));
        rangeAxis.setAutoRange(true);
        rangeAxis.setAutoRangeIncludesZero(false);
        XYPlot plot1 = new XYPlot(data1, domainAxis, rangeAxis, renderer1);
        plot1.setBackgroundPaint(Color.lightGray);
        plot1.setDomainGridlinePaint(Color.white);
        plot1.setRangeGridlinePaint(Color.white);
        plot1.setRangePannable(true);

        //Overlay the Long-Term Trend Indicator
        TimeSeries dataset3 = MovingAverage.createMovingAverage(t1, "LT", 49, 49);
        TimeSeriesCollection collection = new TimeSeriesCollection();
        collection.addSeries(dataset3);
        plot1.setDataset(1, collection);
        plot1.setRenderer(1, new StandardXYItemRenderer());

        //add a second dataset (volume) and renderer
        IntervalXYDataset data2 = createVolumeDataset(filename);
        XYBarRenderer renderer2 = new XYBarRenderer();
        renderer2.setDrawBarOutline(false);
        renderer2.setBaseToolTipGenerator(new StandardXYToolTipGenerator(
                StandardXYToolTipGenerator.DEFAULT_TOOL_TIP_FORMAT,
                new SimpleDateFormat("d-MMM-yyyy"), new DecimalFormat("0,000.00")));
        renderer2.setSeriesPaint(0, Color.red);

        XYPlot plot2 = new XYPlot(data2, null, new NumberAxis("Volume"), renderer2);
        plot2.setBackgroundPaint(Color.lightGray);
        plot2.setDomainGridlinePaint(Color.white);
        plot2.setRangeGridlinePaint(Color.white);

        CombinedDomainXYPlot cplot = new CombinedDomainXYPlot(domainAxis);
        cplot.add(plot1, 3);
        cplot.add(plot2, 2);
        cplot.setGap(8.0);
        cplot.setDomainGridlinePaint(Color.white);
        cplot.setDomainGridlinesVisible(true);
        cplot.setDomainPannable(true);


        //return the new combined chart
        JFreeChart chart = new JFreeChart("Sun Microsystems (SUNW)",
                JFreeChart.DEFAULT_TITLE_FONT, cplot, false);

        ChartUtilities.applyCurrentTheme(chart);
        renderer2.setShadowVisible(false);
        renderer2.setBarPainter(new StandardXYBarPainter());

        return chart;
    }

    //create a panel
    public static JPanel createDemoPanel()
    {
        JFreeChart chart = createCombinedChart();
        return new ChartPanel(chart);
    }

    public static void main(String[] args) {
        // TODO code application logic here
        PriceVolumeChart2 demo = new PriceVolumeChart2(
                "JFreeChart: CombinedXYPlotDemo1.java (base)");
        demo.pack();
        RefineryUtilities.centerFrameOnScreen(demo);
        demo.setVisible(true);
    }

//Download data from web
}