package org.openintents.aainterfaces.server;

import java.util.ArrayList;
import java.util.List;

import javax.jdo.PersistenceManager;
import javax.jdo.Query;
import javax.jdo.Transaction;
import javax.jdo.annotations.IdGeneratorStrategy;
import javax.jdo.annotations.IdentityType;
import javax.jdo.annotations.NotPersistent;
import javax.jdo.annotations.PersistenceCapable;
import javax.jdo.annotations.Persistent;
import javax.jdo.annotations.PrimaryKey;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.openintents.aainterfaces.android.Intent;
import org.w3c.dom.NodeList;

import com.gc.android.market.api.MarketSession;
import com.gc.android.market.api.MarketSession.Callback;
import com.gc.android.market.api.model.Market.AppsRequest;
import com.gc.android.market.api.model.Market.AppsResponse;
import com.gc.android.market.api.model.Market.ResponseContext;
import com.google.appengine.api.datastore.Key;

@PersistenceCapable(identityType = IdentityType.APPLICATION, detachable="true")
public class App {

	@PrimaryKey
	@Persistent(valueStrategy = IdGeneratorStrategy.IDENTITY)
	private Key id;
	
	@Persistent
	private String packageName;
	
	@Persistent
	private String latestVersionCode;

	@Persistent
	private String titel;
	
	@Persistent
	private String price;
	
	@Persistent
	private String priceCurrency;
	@Persistent
	private Integer priceMicros;
	
	@Persistent
	private List<IntentImplementation> intents = new ArrayList<IntentImplementation>();

	@NotPersistent
	private List<IntentFilter> intentFilters;
	
	
	public Key getId() {
		return id;
	}

	public String getPackageName() {
		return packageName;
	}

	public void setPackageName(String packageName) {
		this.packageName = packageName;
	}

	public String getLatestVersionCode() {
		return latestVersionCode;
	}

	public void setLatestVersionCode(String latestVersionCode) {
		this.latestVersionCode = latestVersionCode;
	}

	
	public String getTitel() {
		return titel;
	}

	public void setTitel(String titel) {
		this.titel = titel;
	}

	public String getPrice() {
		return price;
	}

	public void setPrice(String price) {
		this.price = price;
	}

	public String getPriceCurrency() {
		return priceCurrency;
	}

	public void setPriceCurrency(String priceCurrency) {
		this.priceCurrency = priceCurrency;
	}

	public Integer getPriceMicros() {
		return priceMicros;
	}

	public void setPriceMicros(Integer priceMicros) {
		this.priceMicros = priceMicros;
	}

	public List<IntentImplementation> getIntents() {
		return intents;
	}

	public void setIntents(List<IntentImplementation> intents) {
		this.intents = intents;
	}
	

	/**
	 * non-persistent data holder for intent filters
	 * @return
	 */
	public List<IntentFilter> getIntentFilters() {
		return intentFilters;
	}

	public void setIntentFilters(List<IntentFilter> intentFilters) {
		this.intentFilters = intentFilters;
	}
	

	public static void getMarktInfo(String packageName, final Key appKey) {
		MarketSession session = new MarketSession();
		session.login("oaapps@gmail.com", "free12free12");

		String query = "pname:" + packageName;
		AppsRequest appsRequest = AppsRequest.newBuilder().setQuery(query)
				.setStartIndex(0).setEntriesCount(10).setWithExtendedInfo(true)
				.build();

		session.append(appsRequest, new Callback<AppsResponse>() {
			@Override
			public void onResult(ResponseContext context, AppsResponse response) {

				// Your code here
				// response.getApp(0).getCreator() ...
				// see AppsResponse class definition for more infos
				PersistenceManager pm = PMF.get().getPersistenceManager();
				App app = pm.getObjectById(App.class, appKey);
				app.setTitel(response.getApp(0).getTitle());
				app.setPrice(response.getApp(0).getPrice());
				app.setPriceCurrency(response.getApp(0).getPriceCurrency());
				app.setPriceMicros(response.getApp(0).getPriceMicros());
				pm.makePersistent(app);
				pm.close();
			}
		});
		session.flush();

	}


	public static App createOrLoadApp(String packageName, String versionCode,
			PersistenceManager pm) {
		Query q = pm.newQuery("select id from "
				+ App.class.getName()
				+ " where packageName == packageNameParameter");
		q.declareParameters("String packageNameParameter");
		List existingApp = (List) q.execute(packageName);

		App app;
		if (existingApp != null && existingApp.size() > 0) {
			app = pm.getObjectById(App.class, existingApp
					.get(0));
			// packagename already set
			app
					.setIntents(new ArrayList<IntentImplementation>());
		} else {
			app = new App();
			app.setPackageName(packageName);
			// intents already set
		}
		app.setLatestVersionCode(versionCode);
		return app;
	}
	

	public static App persistApp(PersistenceManager pm, App app) {
		App storedApp;
		Transaction tx = null;
		try {

			tx = pm.currentTransaction();
			tx.begin();
			storedApp = pm.makePersistent(app);
			tx.commit();

		} finally {
			if (tx != null && tx.isActive()) {
				tx.rollback();
			}
		}
		return storedApp;
	}

	
	/**
	 * 
	 * Add for each action one intent implementation, i.e. an intent filter associated with an intent protocol.
	 * 
	 * @param app
	 * @param packageName
	 * @param versionCode
	 * @param type
	 * @param categories
	 * @param actionList
	 * @param dataList
	 * @param pm
	 * @param counter
	 * @return
	 */
	public static int addIntentImplementations(App app, String packageName, String versionCode,  int type, List<String> categories, List<String> actionList,List<Data> dataList, PersistenceManager pm,  int counter ){
		// mark intent filters that can be recovered by
		boolean hasDefaultCategorie = categories
				.contains(Intent.CATEGORY_DEFAULT);

		for (String action : actionList) {

			// check intents protocol
			Query q = pm.newQuery("select id from "
					+ IntentProtocol.class.getName());
			q.setFilter("action == actionParam");
			q.declareParameters("String actionParam");
			
			List intentProtocolsIds = (List) q.execute(action);
			
			IntentProtocol ip;
			if (intentProtocolsIds == null
					|| intentProtocolsIds.size() == 0) {
				ip = new IntentProtocol();
				ip.setAction(action);
				ip.setType(type);
				pm.makePersistent(ip);

			} else {
				ip = pm.getObjectById(IntentProtocol.class,
						intentProtocolsIds.get(0));

			}

			IntentImplementation ii = new IntentImplementation(packageName, versionCode, type);
			ii.setmIntentProtocol(ip.getId());
			ii.setmAction(action);
			ii.setmCategories(categories);
			ArrayList<Data> clonedDataList = new ArrayList<Data>();
			for (Data d: dataList){
				Data clonedData = new Data(d);
				clonedDataList.add(clonedData);
			}
			ii.setmDataList(clonedDataList);
			ii.setmHasDefaultCategory(hasDefaultCategorie);

			boolean exists = false;
			// add intents filter to app
			for (IntentImplementation existingIi : app.getIntents()) {
				exists = existingIi.getmAction() != null
						&& existingIi.getmAction().equals(ii.getmAction());
				exists = exists && existingIi.getmType() == ii.getmType();
				exists = exists
						&& compareCategories(ii.getmCategories(),
								existingIi.getmCategories()) == 0;
				exists = exists
						&& compareDataList(ii.getmDataList(), existingIi
								.getmDataList()) == 0;
				if (exists) {
					break;
				}
			}

			if (!exists) {
				app.getIntents().add(ii);
				counter++;
			}
		}
		
		return counter;
	}

	

	private static int compareDataList(List<Data> dataList1, List<Data> dataList2) {
		if (dataList1.size() == dataList2.size()) {
			for (Data d1 : dataList1) {
				if (!dataList2.contains(d1)) {
					return -1;
				}
			}
			return 0;
		} else {
			return dataList1.size() - dataList2.size();
		}
	}

	private static int compareCategories(List<String> cats1, List<String> cats2) {
		if (cats1.size() == cats2.size()) {
			for (String s : cats1) {
				if (!cats2.contains(s)) {
					return -1;
				}
			}
			return 0;
		} else {
			return cats1.size() - cats2.size();
		}
	}


}
