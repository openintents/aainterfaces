package org.opentintents.aainterfaces.shared;

import java.io.Serializable;
import java.util.ArrayList;

public class IntentFilter implements Serializable {

	public String action;
	public ArrayList<String> categories;
	public ArrayList<String> data;
	
	public IntentFilter() {
	
	}

}
