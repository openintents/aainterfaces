package org.opentintents.aainterfaces.client;

import java.util.List;

import org.opentintents.aainterfaces.shared.AppInfo;

import com.google.gwt.user.client.rpc.AsyncCallback;

/**
 * The async counterpart of <code>AAInterfacesService</code>.
 */
public interface AAInterfacesServiceAsync {
	void showAllApps(AsyncCallback<List<String[]>> callback)
			throws IllegalArgumentException;

	void showAppsForIntent(String action, String mimeType, String uri,
			AsyncCallback<List<AppInfo>> callback);

	void showAllProtocols(Integer type, AsyncCallback<List<String>> callback)
			throws IllegalArgumentException;
}
