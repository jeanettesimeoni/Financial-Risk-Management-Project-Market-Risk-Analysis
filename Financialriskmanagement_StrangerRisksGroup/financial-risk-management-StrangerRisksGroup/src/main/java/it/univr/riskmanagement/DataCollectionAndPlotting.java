package it.univr.riskmanagement;

import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JFrame;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.data.time.Day;
import org.jfree.data.time.TimeSeries;
import org.jfree.data.time.TimeSeriesCollection;

/*
This class imports data and produces plots.
*/

public class DataCollectionAndPlotting {


	public static double[] getHistoricalPricesStock1() throws IOException {
		List<Double> pricesList = new ArrayList<>();

		try (InputStream is = DataCollectionAndPlotting.class.getResourceAsStream("/Asset1.xlsx");
		     Workbook workbook = new XSSFWorkbook(is)) {

			Sheet sheet = workbook.getSheetAt(0);
			int columnIndex = 4;

			for (int i = 1; i <= sheet.getLastRowNum(); i++) {
				Row row = sheet.getRow(i);
				if (row == null) continue;

				Cell cell = row.getCell(columnIndex);
				if (cell != null && cell.getCellType() == CellType.NUMERIC) {
					pricesList.add(cell.getNumericCellValue());
				} else if (cell != null && cell.getCellType() == CellType.STRING) {
					try {
						pricesList.add(Double.parseDouble(cell.getStringCellValue().replace("$", "")));
					} catch (NumberFormatException e) {
						System.out.println("Invalid value at row " + (i + 1));
					}
				}
			}
		}

		return pricesList.stream().mapToDouble(Double::doubleValue).toArray();
	}
	
	public static double[] getHistoricalPricesStock2() throws IOException {
		List<Double> pricesList = new ArrayList<>();

		try (InputStream is = DataCollectionAndPlotting.class.getResourceAsStream("/Asset2.xlsx");
		     Workbook workbook = new XSSFWorkbook(is)) {

			Sheet sheet = workbook.getSheetAt(0);
			int columnIndex = 4;

			for (int i = 1; i <= sheet.getLastRowNum(); i++) {
				Row row = sheet.getRow(i);
				if (row == null) continue;

				Cell cell = row.getCell(columnIndex);
				if (cell != null && cell.getCellType() == CellType.NUMERIC) {
					pricesList.add(cell.getNumericCellValue());
				} else if (cell != null && cell.getCellType() == CellType.STRING) {
					try {
						pricesList.add(Double.parseDouble(cell.getStringCellValue().replace("$", "")));
					} catch (NumberFormatException e) {
						System.out.println("Invalid value at row " + (i + 1));
					}
				}
			}
		}

		return pricesList.stream().mapToDouble(Double::doubleValue).toArray();
	}

	public static LocalDate[] getDates() throws IOException {
		List<LocalDate> datesList = new ArrayList<>();
		DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd"); // <-- UPDATED FORMAT

		try (InputStream is = DataCollectionAndPlotting.class.getResourceAsStream("/Asset1.xlsx");
		     Workbook workbook = new XSSFWorkbook(is)) {

			Sheet sheet = workbook.getSheetAt(0);

			for (int i = 1; i <= sheet.getLastRowNum(); i++) {
				Row row = sheet.getRow(i);
				if (row == null) continue;

				Cell cell = row.getCell(0); // First column = date
				if (cell != null) {
					try {
						String cellValue = cell.getStringCellValue().trim();
						LocalDate date = LocalDate.parse(cellValue, formatter);
						datesList.add(date);
					} catch (DateTimeParseException e) {
						System.out.println("Invalid date string at row " + (i + 1) + ": " + cell.getStringCellValue());
					}
				} else {
					System.out.println("Empty cell at row " + (i + 1));
				}
			}
		}
	return datesList.toArray(new LocalDate[0]);
	}

	public static void plotData(LocalDate[] dates, double[] data, String dataType) {
		TimeSeries timeSeries = new TimeSeries(dataType);

		for (int i = 0; i < dates.length; i++) {
			LocalDate date = dates[i];
			timeSeries.add(new Day(date.getDayOfMonth(), date.getMonthValue(), date.getYear()), data[i]);
		}

		TimeSeriesCollection dataset = new TimeSeriesCollection(timeSeries);
		JFreeChart chart = ChartFactory.createTimeSeriesChart(
				dataType + " vs Dates",
				"Date",
				dataType,
				dataset,
				false,
				true,
				false
		);

		JFrame frame = new JFrame(dataType + " Chart");
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.getContentPane().add(new ChartPanel(chart));
		frame.pack();
		frame.setVisible(true);
	}

}
