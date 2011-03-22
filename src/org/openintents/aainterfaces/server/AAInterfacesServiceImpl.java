package org.openintents.aainterfaces.server;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.jdo.PersistenceManager;
import javax.jdo.Query;

import org.opentintents.aainterfaces.client.AAInterfacesService;
import org.opentintents.aainterfaces.shared.AppInfo;
import org.opentintents.aainterfaces.shared.FieldVerifier;
import org.opentintents.aainterfaces.shared.IntentFilter;

import com.google.gwt.user.server.rpc.RemoteServiceServlet;

/**
 * The server side implementation of the RPC service.
 */
@SuppressWarnings("serial")
public class AAInterfacesServiceImpl extends RemoteServiceServlet implements
		AAInterfacesService {

	public List<String[]> showAllApps() {
		PersistenceManager pm = PMF.get().getPersistenceManager();
		Query query = pm.newQuery(App.class);
		query.setOrdering("titel, packageName");
		List<App> iis = (List<App>) query.execute();

		List<String[]> results = new ArrayList<String[]>();
		for (App ii : iis) {
			results.add(new String[]{ii.getTitel(), ii.getPackageName()});
		}
		pm.close();
		return results;

	}

	public List<AppInfo> showAppsForIntent(String action, String mimeType,
			String uri) {
		// Verify that the input is valid.
		if (!FieldVerifier.isValidQuery(action)) {
			// If the input is not valid, throw an IllegalArgumentException back
			// to
			// the client.
			throw new IllegalArgumentException(
					"Action must be at least 4 characters long");
		}

		PersistenceManager pm = PMF.get().getPersistenceManager();
	
		Query query = pm.newQuery(IntentImplementation.class);
		if (mimeType != null) {
			String[] mimeParts = mimeType.split("/");
		}
		query.setFilter("mAction == actionParam");
		query.setOrdering("mPackageName");	

		query.declareParameters("String actionParam");
		List<IntentImplementation> iis = (List<IntentImplementation>) query
				.execute(action);		

		// group by package name
		Map<String, List<IntentImplementation>> groupedIis = new HashMap<String, List<IntentImplementation>>();
		ArrayList<String> apps = new ArrayList<String>();
		for (IntentImplementation ii : iis) {
			
			
			List<IntentImplementation> iiList;
			if (!groupedIis.containsKey(ii.getmPackageName())) {
				iiList = new ArrayList<IntentImplementation>();
				groupedIis.put(ii.getmPackageName(), iiList);
				// maintain a list of keys
				apps.add(ii.getmPackageName());
			} else {
				iiList = groupedIis.get(ii.getmPackageName());
			}
			iiList.add(ii);
		}


		List<AppInfo> result = new ArrayList<AppInfo>();
		
		for (String packageName : apps) {
			iis = groupedIis.get(packageName);
			ArrayList<IntentFilter> filters = new ArrayList<IntentFilter>();
			for (int i = 0; i < iis.size(); i++) {
				IntentImplementation ii = iis.get(i);

				ArrayList<String>dataList = new ArrayList<String>();
				if (ii.getmDataList() != null) {
					for (int j = 0; j < ii.getmDataList().size(); j++) {
						dataList.add(ii.getmDataList().get(j).toString());						
					}
				} 
				
				IntentFilter intentFilter = new IntentFilter();
				intentFilter.action = ii.getmAction();
				intentFilter.categories = new ArrayList<String>(ii.getmCategories());
				intentFilter.data = dataList;

				filters.add(intentFilter);
			}
			
			
			
			AppInfo app = new AppInfo();
			app.packageName = packageName;
			app.filters =  filters;
			
			Query q = pm.newQuery(App.class);
			q.setFilter("packageName == packageParam");
			q.declareParameters("String packageParam");
			List appsList = (List)q.execute(packageName);
			if (appsList != null && appsList.size() > 0){
				app.titel = ((App) appsList.get(0)).getTitel();
			}
			
			result.add(app);
		}
		
		pm.close();

		return result;
	}

	public List<String> showAllProtocols(Integer type) {
		PersistenceManager pm = PMF.get().getPersistenceManager();
		Query query = pm.newQuery(IntentProtocol.class);
		query.setOrdering("action");
		List<IntentProtocol> iis;

		if (type != null && type >= 0 && type <= 2) {
			query.setFilter("type == typeParam");
			query.declareParameters("int typeParam");
			iis = (List<IntentProtocol>) query.execute(type);
		} else {
			iis = (List<IntentProtocol>) query.execute();
		}

		List<String> results = new ArrayList<String>();
		for (IntentProtocol ii : iis) {
			results.add(ii.getAction());
		}

		pm.close();
		return results;

	}
}
