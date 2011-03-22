package org.openintents.aainterfaces.server;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import javax.jdo.PersistenceManager;
import javax.jdo.Query;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.opentintents.aainterfaces.shared.AppInfo;
import org.opentintents.aainterfaces.shared.IntentFilter;

import com.google.appengine.api.datastore.Blob;
import com.google.appengine.repackaged.org.json.JSONArray;
import com.google.appengine.repackaged.org.json.JSONException;
import com.google.appengine.repackaged.org.json.JSONObject;

public class AAInterfacesHandler extends HttpServlet {

	/**
	 * 
	 */
	private static final long serialVersionUID = -2174772847910069789L;

	private static final Logger log = Logger
			.getLogger(AAInterfacesHandler.class.getName());

	private static final String ACTION_APPS = "apps";
	private static final String ACTION_RESOLVE = "resolve";

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

	protected void handleRequest(HttpServletRequest req,
			HttpServletResponse resp) throws IOException {
		final String action = req.getParameter("req");
		System.out.println(action);
		StringBuffer res = new StringBuffer();

		if (ACTION_APPS.equals(action)) {

			PersistenceManager pm = PMF.get().getPersistenceManager();
			Query query = pm.newQuery(App.class);
			query.setOrdering("titel, packageName");
			List<App> iis = (List<App>) query.execute();

			try {
				JSONArray resultJson = new JSONArray();
				for (App ii : iis) {
					JSONObject app = new JSONObject();
					app.put("titel", ii.getTitel());
					app.put("packageName", ii.getPackageName());
					app.put("price", ii.getPrice());
					app.put("priceCurrency", ii.getPriceCurrency());
					resultJson.put(app);
				}
				res.append(resultJson.toString());

			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			pm.close();
		} else if (ACTION_RESOLVE.equals(action)) {
			String mimeType = req.getParameter("mimeType");
			String intentaction = req.getParameter("action");
			PersistenceManager pm = PMF.get().getPersistenceManager();

			Query query = pm.newQuery(IntentImplementation.class);
			if (mimeType != null) {
				String[] mimeParts = mimeType.split("/");
			}
			query.setFilter("mAction == actionParam");
			query.setOrdering("mPackageName");

			query.declareParameters("String actionParam");
			List<IntentImplementation> iis = (List<IntentImplementation>) query
					.execute(intentaction);

			try {

				JSONArray resultJson = new JSONArray();
				ArrayList<String> apps = new ArrayList<String>();
				for (IntentImplementation ii : iis) {
					// maintain a list of keys
					if (!apps.contains(ii.getmPackageName())) {
						apps.add(ii.getmPackageName());

						JSONObject app = new JSONObject();

						app.put("packageName", ii.getmPackageName());

						Query q = pm.newQuery(App.class);
						q.setFilter("packageName == packageParam");
						q.declareParameters("String packageParam");
						List appsList = (List) q.execute(ii.getmPackageName());
						if (appsList != null && appsList.size() > 0) {
							App a = (App) appsList.get(0);
							app.put("titel", a.getTitel());
							app.put("packagename", a.getPackageName());
							app.put("price", a.getPrice());
							app.put("priceCurrency", a.getPriceCurrency());

						}

						resultJson.put(app);
					}
					
				}
				res.append(resultJson.toString());
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

			pm.close();

		}

		resp.getWriter().print(res.toString());

	}
}
