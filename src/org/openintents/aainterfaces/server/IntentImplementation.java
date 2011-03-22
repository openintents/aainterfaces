package org.openintents.aainterfaces.server;

import java.util.ArrayList;
import java.util.List;

import javax.jdo.annotations.Element;
import javax.jdo.annotations.IdGeneratorStrategy;
import javax.jdo.annotations.IdentityType;
import javax.jdo.annotations.PersistenceCapable;
import javax.jdo.annotations.Persistent;
import javax.jdo.annotations.PrimaryKey;

import org.opentintents.aainterfaces.shared.IntentFilter;

import com.google.appengine.api.datastore.Key;

/**
 * Represenation of an intent filter associated to an intent protocol, i.e. just has a single action
 * @author friedger
 *
 */
@PersistenceCapable(identityType = IdentityType.APPLICATION, detachable = "true")
public class IntentImplementation {

	@PrimaryKey
	@Persistent(valueStrategy = IdGeneratorStrategy.IDENTITY)
	private Key id;

	@Persistent
	private String mAction;

	@Persistent
	private String mPackageName;

	@Persistent
	private String mVersionCode;

	@Persistent
	private int mType;

	@Persistent
	private boolean mHasDefaultCategory;

	@Persistent
	private List<String> mCategories = new ArrayList<String>();

	@Persistent
	@Element(dependent = "true")
	private List<Data> mDataList = new ArrayList<Data>();

	@Persistent
	private Key mIntentProtocol = null;

	public IntentImplementation(String packageName, String versionCode, int type) {
		setmPackageName(packageName);
		setmVersionCode(versionCode);
		setmType(type);
	}

	public Key getId() {
		return id;
	}

	public String getmAction() {
		return mAction;
	}

	public void setmAction(String mAction) {
		this.mAction = mAction;
	}

	public String getmPackageName() {
		return mPackageName;
	}

	public void setmPackageName(String mPackageName) {
		this.mPackageName = mPackageName;
	}

	public String getmVersionCode() {
		return mVersionCode;
	}

	public void setmVersionCode(String mVersionCode) {
		this.mVersionCode = mVersionCode;
	}

	public int getmType() {
		return mType;
	}

	public void setmType(int mType) {
		this.mType = mType;
	}

	public boolean ismHasDefaultCategory() {
		return mHasDefaultCategory;
	}

	public void setmHasDefaultCategory(boolean mHasDefaultCategory) {
		this.mHasDefaultCategory = mHasDefaultCategory;
	}

	public List<String> getmCategories() {
		return mCategories;
	}

	public void setmCategories(List<String> mCategories) {
		this.mCategories = mCategories;
	}

	public List<Data> getmDataList() {
		return mDataList;
	}

	public void setmDataList(List<Data> mDataList) {
		this.mDataList = mDataList;
	}

	public Key getmIntentProtocol() {
		return mIntentProtocol;
	}

	public void setmIntentProtocol(Key key) {
		this.mIntentProtocol = key;
	}

	public void addCategory(String category) {
		mCategories.add(category);
	}

	public void addData(String host, String mimeType, String path,
			String pathPattern, String pathPrefix, String port, String scheme) {
		Data data = new Data();
		data.setmHost(host);
		data.setmMimeType(mimeType);
		data.setmPath(path);
		data.setmPathPattern(pathPattern);
		data.setmPathPrefix(pathPrefix);
		data.setmPort(port);
		data.setmScheme(scheme);
		mDataList.add(data);

	}

	public IntentFilter toIntentFilter(){
		IntentFilter intentFilter = new IntentFilter();
		intentFilter.action = getmAction();
		intentFilter.categories = new ArrayList<String>(getmCategories());
		ArrayList<String> dl = new ArrayList<String>();
		for (Data d : getmDataList()){
			dl.add(d.toString());
		}
		intentFilter.data = dl;
		return intentFilter;
	}
}
