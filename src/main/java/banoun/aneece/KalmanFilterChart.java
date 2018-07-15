package banoun.aneece;

import java.awt.BorderLayout;
import java.io.BufferedWriter;
import java.io.Reader;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Random;
import javax.swing.JFrame;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVPrinter;
import org.apache.commons.csv.CSVRecord;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.data.time.Minute;
import org.jfree.data.time.TimeSeries;
import org.jfree.data.time.TimeSeriesCollection;

public class KalmanFilterChart {

    public double[] generateRandomArray(int size) {
        Random ran = new Random();
        double[] randoms = new double[size];
        for (int i = 0; i < size; i++) {
            randoms[i] = ran.nextDouble();
        }
        return randoms;
    }

    private static final String SAMPLE_CSV_FILE = "./sample.csv";

    private Double[] readCsv() {
        List<Double> out = new ArrayList<>();
        try (
                Reader reader = Files.newBufferedReader(Paths.get(SAMPLE_CSV_FILE));
                //            CSVParser csvParser = new CSVParser(reader, CSVFormat.DEFAULT);
                CSVParser csvParser = new CSVParser(reader, CSVFormat.DEFAULT.withHeader("SOURCE")
                        .withIgnoreHeaderCase()
                        .withTrim());) {
            for (CSVRecord csvRecord : csvParser) {

                if (csvRecord.getRecordNumber() != 1) {
                    //                System.out.println("Record: " + csvRecord.get("SOURCE") + " Record No - " + csvRecord.getRecordNumber());
                    Double d = Double.parseDouble(csvRecord.get(0));
                    out.add(d);
//                    System.out.println("Record: "+d+" Record No - " + csvRecord.getRecordNumber());
                }

            }
        } catch (Exception e) {
        }
        Double[] dArr = out.toArray(new Double[out.size()]);
        return dArr;
    }

    private void writeTocsv(double[] in) {
        try (
                BufferedWriter writer = Files.newBufferedWriter(Paths.get(SAMPLE_CSV_FILE));
                CSVPrinter csvPrinter = new CSVPrinter(writer, CSVFormat.DEFAULT.withHeader("SOURCE"));) {
            in = generateRandomArray(50);
            for (double e : in) {
                csvPrinter.printRecord(e);
            }
            csvPrinter.flush();
        } catch (Exception e) {
        }
    }

    private final float ALPHA = 0.2f;

    public Double[] kFilter(Double[] input, Double[] output) {
        if (output == null) {
            return input;
        }

        for (int i = 0; i < input.length; i++) {
            double out = output[i] == null ? 0d : output[i];
            output[i] = out + ALPHA * (input[i] - out);
        }
        return output;
    }

    private void sleeps(long time) {
        try {
            Thread.sleep(time);
        } catch (InterruptedException ex) {
        }
    }

    public void framChart() {
        final JFrame window = new JFrame();
        window.setTitle("Sensor Graph");
        window.setLayout(new BorderLayout());
        window.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        window.setVisible(true);
        final TimeSeriesCollection dataset = new TimeSeriesCollection();
        final JFreeChart chart
                = ChartFactory.createTimeSeriesChart("Filtered Sensor Graph", "Time", "Sensor Data", dataset, true, true, false);

        final ChartPanel chartPanel = new ChartPanel(chart);
        final TimeSeries series = new TimeSeries("Random READING");
        final TimeSeries fseries = new TimeSeries("Filtered READING");
        dataset.addSeries(series);
        dataset.addSeries(fseries);

        window.add(chartPanel, BorderLayout.CENTER);
        window.setExtendedState(JFrame.MAXIMIZED_BOTH);
        Thread thread = new Thread() {
            @Override
            public void run() {
                int size = 50;
                writeTocsv(generateRandomArray(size));
                Double[] in = readCsv();
                Double[] out = new Double[size];
                while (true) {
                    out = kFilter(in, out);
                    try {
                        createDrawChartData(out, in);
                    } catch (Exception e) {
                    }
  //                  sleeps(2000);
                    writeTocsv(generateRandomArray(size));
                    sleeps(3000);
                    in = readCsv();

                }
            }

            private void createDrawChartData(Double[] out, Double[] in) {
                series.clear();
                fseries.clear();
                long startTime = System.currentTimeMillis();
                for (int n = 0; n < in.length; n++) {
                    Date d = new Date(startTime);
                    series.add(new Minute(d), in[n] * 100);
                    fseries.add(new Minute(d), out[n] * 100);
                    startTime += 1000 * 60;
//                    sleeps(20);
                }
            }
        };
        thread.start();
    }

}
