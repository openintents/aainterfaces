package org.openintents.aainterfaces.server;

import java.io.IOException;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.HttpMethod;
import org.apache.commons.httpclient.NameValuePair;
import org.apache.commons.httpclient.methods.PostMethod;
import org.supercsv.cellprocessor.CellProcessorAdaptor;
import org.supercsv.cellprocessor.ift.StringCellProcessor;
import org.supercsv.util.CSVContext;

public class ParseXmlTreeProcessor extends CellProcessorAdaptor implements
		StringCellProcessor {

	HttpClient mHttpClient = new HttpClient();
	
	public ParseXmlTreeProcessor() {
		super();
	}

	@Override
	public Object execute(Object value, CSVContext context) {
		
		
		PostMethod method = new PostMethod("http://localhost:8888/xmltree");
		method.addParameter(new NameValuePair("xt", (String)value));
		
		try {
			mHttpClient.executeMethod(method);
		} catch (HttpException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		String[] lines = ((String) value).split("\n");		
		
		App app = ParseXmlTree.xmlTreeToApp(lines);
		
		return next.execute(app, context);
	}

}
