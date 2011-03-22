package org.openintents.aainterfaces.server;

import java.io.IOException;
import java.io.StringReader;

import javax.jdo.PersistenceManager;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.supercsv.cellprocessor.Optional;
import org.supercsv.cellprocessor.ift.CellProcessor;
import org.supercsv.io.CsvBeanReader;
import org.supercsv.io.ICsvBeanReader;
import org.supercsv.prefs.CsvPreference;

public class PostServletXmlTree extends HttpServlet {

	final CellProcessor[] processors = new CellProcessor[] { new Optional(),
			new Optional(), new ParseXmlTreeProcessor() };

	private static final long serialVersionUID = 1L;

	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {
		handleRequest(req, resp);
	}

	@Override
	protected void doPost(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {
		handleRequest(req, resp);
	}

	/**
	 * handle a request
	 */
	protected void handleRequest(HttpServletRequest request,
			HttpServletResponse resp) throws ServletException, IOException {
		String response = "";
		int cont = 0;

		String packageName = request.getParameter("pm");

		String downloadUrl = request.getParameter("dl");

		String xmlTree = request.getParameter("xt");

		if (xmlTree != null) {
			String[] lines = xmlTree.split("\r\n");
			if (lines.length <= 1){
				lines = xmlTree.split("\n");				
			}
			parseXmlTree(lines, downloadUrl);
		}

		writeResponse(request, resp, response);
	}

	private void writeResponse(HttpServletRequest request,
			HttpServletResponse resp, String response) throws IOException {

		resp.getWriter().print(
				"<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?><response>"
						+ response + "</response>");

	}

	public String parseXmlTree(String[] lines, String downloadUrl)
			throws IOException {
		StringBuffer sb = new StringBuffer();
		App storedApp = null;

		PersistenceManager pm = PMF.get().getPersistenceManager();

		try {
			App appokeApp = ParseXmlTree.xmlTreeToApp(lines);
			Integer counter = 0;
			String packageName = appokeApp.getPackageName();
			String versionCode = appokeApp.getLatestVersionCode();
			App app = App.createOrLoadApp(packageName, versionCode, pm);

			for (IntentFilter intentFilter : appokeApp.getIntentFilters()) {
				counter += App.addIntentImplementations(app, packageName,
						versionCode, intentFilter.getmType(),
						intentFilter.getmCategories(),
						intentFilter.getmActions(),
						intentFilter.getmDataList(), pm, counter);
			}

			storedApp = App.persistApp(pm, app);
			sb.append(String.format("%d intents for %s", counter, app.getPackageName()));

		} finally {
			pm.close();
		}

		// TODO fix for latest version
		// App.getMarktInfo(storedApp.getPackageName(),storedApp.getId());
		
		return sb.toString();
	}

	public void parseCSVFile(StringBuffer sb) throws IOException {
		ICsvBeanReader inFile = new CsvBeanReader(new StringReader(
				sb.toString()), CsvPreference.EXCEL_NORTH_EUROPE_PREFERENCE);
		try {
			final String[] header = inFile.getCSVHeader(true);
			AppokeApp appokeApp;
			while ((appokeApp = inFile.read(AppokeApp.class, new String[] {
					"packageName", "downloadUrl", "appFromXmlTree" },
					processors)) != null) {

				System.out.println(appokeApp.getDownloadUrl());

				App storedApp = null;

				PersistenceManager pm = PMF.get().getPersistenceManager();

				try {
					Integer counter = 0;
					String packageName = appokeApp.getPackageName();
					String versionCode = appokeApp.getLatestVersionCode();
					App app = App.createOrLoadApp(packageName, versionCode, pm);

					for (IntentFilter intentFilter : appokeApp
							.getIntentFilters()) {
						counter += App.addIntentImplementations(app,
								packageName, versionCode,
								intentFilter.getmType(),
								intentFilter.getmCategories(),
								intentFilter.getmActions(),
								intentFilter.getmDataList(), pm, counter);
					}

					storedApp = App.persistApp(pm, appokeApp);

				} finally {
					pm.close();
				}

				App.getMarktInfo(storedApp.getPackageName(), storedApp.getId());
			}
		} finally {
			inFile.close();
		}
	}

	
}
