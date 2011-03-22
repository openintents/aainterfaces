package org.openintents.aainterfaces.server;

import gwtupload.server.exceptions.UploadActionException;
import gwtupload.server.gae.AppEngineUploadAction;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;

import javax.jdo.PersistenceManager;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathFactory;

import org.apache.commons.fileupload.FileItem;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class UploadServletManifest extends AppEngineUploadAction {

	private static final long serialVersionUID = 1L;

	Hashtable<String, String> receivedContentTypes = new Hashtable<String, String>();
	/**
	 * Maintain a list with received files and their content types.
	 */
	Hashtable<String, File> receivedFiles = new Hashtable<String, File>();

	/**
	 * Override executeAction to save the received files in a custom place and
	 * delete this items from session.
	 */
	@Override
	public String executeAction(HttpServletRequest request,
			List<FileItem> sessionFiles) throws UploadActionException {
		String response = "";
		int cont = 0;
		for (FileItem item : sessionFiles) {
			if (false == item.isFormField()) {
				cont++;
				try {
					// / Create a new file based on the remote file name in the
					// client
					// String saveName =
					// item.getName().replaceAll("[\\\\/><\\|\\s\"'{}()\\[\\]]+",
					// "_");
					// File file =new File("/tmp/" + saveName);

					// / Create a temporary file placed in /tmp (only works in
					// unix)
					// File file = File.createTempFile("upload-", ".bin", new
					// File("/tmp"));

					StringBuffer sb = new StringBuffer();
					String line;
					BufferedReader reader = new BufferedReader(
							new InputStreamReader(item.getInputStream(),
									"UTF-8"));
					while ((line = reader.readLine()) != null) {
						sb.append(line);
					}

					receivedContentTypes.put(item.getFieldName(), item
							.getContentType());

					org.w3c.dom.Document doc = DocumentBuilderFactory
							.newInstance().newDocumentBuilder().parse(
									new ByteArrayInputStream(sb.toString()
											.trim().getBytes()));

					String packageName = doc.getDocumentElement().getAttribute(
							"package");
					String versionCode = doc.getDocumentElement().getAttribute(
							"android:versionCode");

					PersistenceManager pm = PMF.get().getPersistenceManager();

					Integer counter = 0;
					XPath xpath = XPathFactory.newInstance().newXPath();
					App storedApp = null;

					try {

						App app = App.createOrLoadApp(packageName, versionCode, pm);

						
						NodeList filterList = (NodeList) xpath.evaluate(
								"/manifest/application/activity/intent-filter",
								doc, XPathConstants.NODESET);

						counter += addIntentFiltersToApp(filterList, 0,
								packageName, versionCode, pm, app);

						filterList = (NodeList) xpath.evaluate(
								"/manifest/application/receiver/intent-filter",
								doc, XPathConstants.NODESET);

						counter += addIntentFiltersToApp(filterList, 1,
								packageName, versionCode, pm, app);

						filterList = (NodeList) xpath.evaluate(
								"/manifest/application/service/intent-filter",
								doc, XPathConstants.NODESET);

						counter += addIntentFiltersToApp(filterList, 2,
								packageName, versionCode, pm, app);

						storedApp = App.persistApp(pm, app);

					} finally {
						pm.close();
					}

					// TODO fix for latest version
					//App.getMarktInfo(packageName, storedApp.getId());

					// / Compose a xml message with the full file information
					// which can be parsed in client side
					response += "<file-" + cont + "-field>"
							+ item.getFieldName() + "</file-" + cont
							+ "-field>\n";
					response += "<file-" + cont + "-name>"
							+ doc.getDocumentElement().getAttribute("package")
							+ "</file-" + cont + "-name>\n";
					response += "<file-" + cont + "-size>" + counter
							+ "</file-" + cont + "-size>\n";
					response += "<file-" + cont + "-type>"
							+ item.getContentType() + "</file-" + cont
							+ "type>\n";
				} catch (Exception e) {
					throw new UploadActionException(e);
				}
			}
		}

		// / Remove files from session because we have a copy of them
		removeSessionFileItems(request);

		// / Send information of the received files to the client.
		return "<response>\n" + response + "</response>\n";
	}



	

	private int addIntentFiltersToApp(NodeList filterList, int type,
			String packageName, String versionCode, PersistenceManager pm,
			App app) {

		int counter = 0;

		for (int i = 0; i < filterList.getLength(); i++) {
			Node intentFilter = filterList.item(i);
			NodeList children = intentFilter.getChildNodes();

			ArrayList<String> actionList = new ArrayList<String>();
			ArrayList<String> categories = new ArrayList<String>();
			ArrayList<Data> dataList = new ArrayList<Data>();

			for (int j = 0; j < children.getLength(); j++) {
				Node child = children.item(j);

				if ("action".equals(child.getNodeName())) {
					actionList.add(((Element) child)
							.getAttribute("android:name"));
				} else if ("category".equals(child.getNodeName())) {
					categories.add(((Element) child)
							.getAttribute("android:name"));
				} else if ("data".equals(child.getNodeName())) {
					dataList.add(new Data(((Element) child)
							.getAttribute("android:host"), ((Element) child)
							.getAttribute("android:mimeType"),
							((Element) child).getAttribute("android:path"),
							((Element) child)
									.getAttribute("android:pathPattern"),
							((Element) child)
									.getAttribute("android:pathPrefix"),
							((Element) child).getAttribute("android:port"),
							((Element) child).getAttribute("android:scheme")));
				}
			}

			counter = App.addIntentImplementations(app, packageName, versionCode, type, categories, actionList, dataList, pm, counter);
			
		}
		return counter;
	}

	/**
	 * Get the content of an uploaded file.
	 */
	@Override
	public void getUploadedFile(HttpServletRequest request,
			HttpServletResponse response) throws IOException {
		String fieldName = request.getParameter(PARAM_SHOW);
		File f = receivedFiles.get(fieldName);
		if (f != null) {
			response.setContentType(receivedContentTypes.get(fieldName));
			FileInputStream is = new FileInputStream(f);
			copyFromInputStreamToOutputStream(is, response.getOutputStream());
		} else {
			renderXmlResponse(request, response, ERROR_ITEM_NOT_FOUND);
		}
	}

	/**
	 * Remove a file when the user sends a delete request.
	 */
	@Override
	public void removeItem(HttpServletRequest request, String fieldName)
			throws UploadActionException {
		File file = receivedFiles.get(fieldName);
		receivedFiles.remove(fieldName);
		receivedContentTypes.remove(fieldName);
		if (file != null) {
			file.delete();
		}
	}
}
