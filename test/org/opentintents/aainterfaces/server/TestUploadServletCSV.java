package org.opentintents.aainterfaces.server;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.StringReader;

import junit.framework.TestCase;

import org.openintents.aainterfaces.server.AppokeApp;
import org.openintents.aainterfaces.server.Data;
import org.openintents.aainterfaces.server.IntentFilter;
import org.openintents.aainterfaces.server.ParseXmlTreeProcessor;
import org.openintents.aainterfaces.server.PostServletXmlTree;
import org.supercsv.cellprocessor.Optional;
import org.supercsv.cellprocessor.ift.CellProcessor;
import org.supercsv.io.CsvBeanReader;
import org.supercsv.io.ICsvBeanReader;
import org.supercsv.prefs.CsvPreference;

public class TestUploadServletCSV extends TestCase {

	public void testParseCSVFile() throws IOException {

		FileInputStream f = new FileInputStream(
				"/home/friedger/Desktop/appoke/out.csv");

		StringBuffer sb = new StringBuffer();
		String line;
		BufferedReader reader = new BufferedReader(new InputStreamReader(f,
				"UTF-8"));
		while ((line = reader.readLine()) != null) {
			sb.append(line);
			sb.append("\n");
		}

		ICsvBeanReader inFile = new CsvBeanReader(new StringReader(
				sb.toString()), CsvPreference.EXCEL_NORTH_EUROPE_PREFERENCE);

		final String[] header = inFile.getCSVHeader(true);
		AppokeApp appokeApp;
		CellProcessor[] processors = new CellProcessor[] { new Optional(),
				new Optional(), new ParseXmlTreeProcessor() };

		int countAll = 0;
		int count = 0;
		while ((appokeApp = inFile.read(AppokeApp.class, new String[] {
				"packageName", "downloadUrl", "appFromXmlTree" }, processors)) != null) {
			countAll++;
			boolean hasFilter = false;
			boolean printed = false;
			for (IntentFilter filter : appokeApp.getAppFromXmlTree()
					.getIntentFilters()) {
				if (filter != null) {
					for (String action : filter.getmActions()) {
						if (!"android.intent.action.MAIN".equals(action)) {
							if (filter.getmType() == 1) {
								hasFilter = true;
								if (!printed) {
									count++;
									System.out.println(appokeApp
											.getDownloadUrl());
									printed = true;
								}
								System.out.println("  " + filter.getmType()
										+ "-" + action);
								for (Data d : filter.getmDataList()) {
									System.out.println("    " + d.getmScheme()
											+ ":" + d.getmHost() + " "
											+ d.getmMimeType() + "|"
											+ d.getmPath() + "|"
											+ d.getmPathPattern() + "|"
											+ d.getmPathPrefix());

								}
								for (String d : filter.getmCategories()) {
									System.out.println("    c:" + d);
								}								
							}						
						}
						
					}
				} else {
					// System.out.println("  no filter");
				}
			}
			System.out.println("---");
		}

		System.out.println(count + "/" + countAll);

	}
}
