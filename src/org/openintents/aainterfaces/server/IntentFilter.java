package org.openintents.aainterfaces.server;

import java.util.ArrayList;
import java.util.List;

/**
 * Representation of an intent filter of an app as it is declared in the Manifest
 * @author friedger
 *
 */
public class IntentFilter {
	private int mType;

	private List<String> mActions = new ArrayList<String>();
	
	private List<String> mCategories = new ArrayList<String>();

	private List<Data> mDataList = new ArrayList<Data>();

	public int getmType() {
		return mType;
	}

	public void setmType(int mType) {
		this.mType = mType;
	}

	public List<String> getmActions() {
		return mActions;
	}

	public void setmActions(List<String> mActions) {
		this.mActions = mActions;
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

	
	
}
