package org.opentintents.aainterfaces.client;

import java.util.List;

import org.opentintents.aainterfaces.shared.AppInfo;

import com.google.gwt.user.client.rpc.RemoteService;
import com.google.gwt.user.client.rpc.RemoteServiceRelativePath;

/**
 * The client side stub for the RPC service.
 */
@RemoteServiceRelativePath("greet")
public interface AAInterfacesService extends RemoteService {
	List<String[]> showAllApps()throws IllegalArgumentException;
	List<AppInfo> showAppsForIntent(String action, String mimeType, String uri);
	List<String> showAllProtocols(Integer  type)throws IllegalArgumentException;
}
