package org.opentintents.aainterfaces.client;

import gwtupload.client.IUploader;
import gwtupload.client.MultiUploader;
import gwtupload.client.IUploadStatus.Status;
import gwtupload.client.IUploader.Utils;

import java.util.List;

import org.opentintents.aainterfaces.shared.AppInfo;
import org.opentintents.aainterfaces.shared.FieldVerifier;
import org.opentintents.aainterfaces.shared.IntentFilter;

import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.event.dom.client.KeyUpEvent;
import com.google.gwt.event.dom.client.KeyUpHandler;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.RootPanel;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.xml.client.Document;
import com.google.gwt.xml.client.XMLParser;

/**
 * Entry point classes define <code>onModuleLoad()</code>.
 */
public class Aainterfaces implements EntryPoint {
	/**
	 * The message displayed to the user when the server cannot be reached or
	 * returns an error.
	 */
	private static final String SERVER_ERROR = "An error occurred while "
			+ "attempting to contact the server. Please check your network "
			+ "connection and try again.";

	/**
	 * Create a remote service proxy to talk to the server-side Greeting
	 * service.
	 */
	private final AAInterfacesServiceAsync aaInterfacesService = GWT
			.create(AAInterfacesService.class);

	/**
	 * This is the entry point method.
	 */
	public void onModuleLoad() {
		final Button sendButton = new Button("Search");
		final Label errorLabel = new Label();
		final TextBox searchField = new TextBox();
		searchField.setTitle("intent protocol");
		
		// Create a new uploader panel and attach it to the document
		MultiUploader defaultUploader = new MultiUploader();

		// We can add style names to widgets
		sendButton.addStyleName("sendButton");

		// Add the ui to the RootPanel
		// Use RootPanel.get() to get the entire body element
		RootPanel.get("searchContainer").add(searchField);
		RootPanel.get("sendButtonContainer").add(sendButton);
		RootPanel.get("errorLabelContainer").add(errorLabel);
		RootPanel.get("uploaderContainer").add(defaultUploader);
		

		// Focus the cursor on the name field when the app loads
		searchField.setFocus(true);
		searchField.selectAll();

		

		// Create a handler for the sendButton and searchField
		class MyHandler implements ClickHandler, KeyUpHandler {
			/**
			 * Fired when the user clicks on the sendButton.
			 */
			public void onClick(ClickEvent event) {
				sendQueryToServer();
			}

			/**
			 * Fired when the user types in the searchField.
			 */
			public void onKeyUp(KeyUpEvent event) {
				if (event.getNativeKeyCode() == KeyCodes.KEY_ENTER) {
					sendQueryToServer();
				}
			}

			/**
			 * Send the name from the nameField to the server and wait for a
			 * response.
			 */
			private void sendQueryToServer() {
				RootPanel resultContainer = RootPanel.get("resultContainer");
				resultContainer.clear();

				// First, we validate the input.
				errorLabel.setText("");
				String textToServer = searchField.getText();
				if (!FieldVerifier.isValidQuery(textToServer)) {
					errorLabel.setText("Please enter at least four characters");
					return;
				}

				// Then, we send the input to the server.
				sendButton.setEnabled(false);

				aaInterfacesService.showAppsForIntent(textToServer, null, null,
						new AsyncCallback<List<AppInfo>>() {
							public void onFailure(Throwable caught) {
								sendButton.setEnabled(true);
								RootPanel resultContainer = RootPanel.get("resultContainer");
								resultContainer.clear();
								resultContainer.add(new Label(caught.getMessage()));
							}

							public void onSuccess(List<AppInfo> result) {
								sendButton.setEnabled(true);
								RootPanel resultContainer = RootPanel.get("resultContainer");
								resultContainer.clear();
								resultContainer.add(new Label ("result count: " + result.size()));
								for (AppInfo app : result) {
									Label l = new Label(app.packageName);
									l.setStylePrimaryName("app-package");
									resultContainer.add(l);
									VerticalPanel vp = new VerticalPanel();
									vp.setStylePrimaryName("intent-filter-list");
									
									for (IntentFilter filter: app.filters){
										
										Label l2 = new Label(filter.action);
										l2.setStylePrimaryName("intent-filter-action");										
										vp.add(l2);
										
										for (String cat : filter.categories){
											Label l3 = new Label(cat);
											l3.setStylePrimaryName("intent-filter");										
											vp.add(l3);
										}

										for (String data : filter.data){
											Label l3 = new Label(data);
											l3.setStylePrimaryName("intent-filter");										
											vp.add(l3);
										}										
										
										

									}
									resultContainer.add(vp);
								}
							}
						});
			}
		}

		// Add a handler to send the name to the server
		MyHandler handler = new MyHandler();
		sendButton.addClickHandler(handler);
		searchField.addKeyUpHandler(handler);

		// Add a finish handler which will load the image once the upload
		// finishes
		defaultUploader.addOnFinishUploadHandler(onFinishUploaderHandler);

		// show the apps and protocols on the left and right side
		loadApps();
		loadProts();
	}

	private void loadProts() {

		loadProt(0, "prots0Container");
		loadProt(1, "prots1Container");
		loadProt(2, "prots2Container");

	}

	private void loadProt(int type, final String panelName) {
		aaInterfacesService.showAllProtocols(type,
				new AsyncCallback<List<String>>() {

					@Override
					public void onFailure(Throwable caught) {
						// TODO Auto-generated method stub

					}

					@Override
					public void onSuccess(List<String> result) {
						RootPanel prots = RootPanel.get(panelName);
						prots.clear();
						for (String action : result) {
							Label l = new Label(action);
							final String finalAction = action;
							l.addClickHandler(new ClickHandler() {
								
								@Override
								public void onClick(ClickEvent event) {
									TextBox searchField = (TextBox) RootPanel.get("searchContainer").getWidget(0);
									searchField.setText(finalAction);									
								}
							});
							prots.add(l);
						}

					}

				});
	}

	private void loadApps() {
		aaInterfacesService.showAllApps(new AsyncCallback<List<String[]>>() {

			@Override
			public void onFailure(Throwable caught) {
				// TODO Auto-generated method stub

			}

			@Override
			public void onSuccess(List<String[]> result) {
				RootPanel apps = RootPanel.get("appsContainer");
				apps.clear();
				for (String[] appInfo : result) {
					if (appInfo[0] != null && appInfo[0].length() > 0){					
						apps.add(new Label(appInfo[0]));
					} else {
						apps.add(new Label(appInfo[1]));
					}
				}

			}

		});
	}

	// Load the image in the document and in the case of success attach it to
	// the viewer
	private IUploader.OnFinishUploaderHandler onFinishUploaderHandler = new IUploader.OnFinishUploaderHandler() {
		public void onFinish(IUploader uploader) {
			if (uploader.getStatus() == Status.SUCCESS) {

				// The server can send information to the client.
				// You can parse this information using XML or JSON libraries
				if (uploader.getServerResponse() != null) {
					Document doc = XMLParser
							.parse(uploader.getServerResponse());
					String size = Utils.getXmlNodeValue(doc, "file-1-size");
					String type = Utils.getXmlNodeValue(doc, "file-1-type");
					String name = Utils.getXmlNodeValue(doc, "file-1-name");
					System.out.println(size + " " + type + " " + name);

					Label result = new Label("Added " + size
							+ " protocol(s) for " + name);
					RootPanel container = RootPanel.get("resultContainer");
					container.clear();
					container.add(result);
				}
			}
			uploader.reset();

			loadApps();

			loadProts();
		};
	};

}
