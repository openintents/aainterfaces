package org.openintents.aainterfaces.server;

import java.util.ArrayList;
import java.util.Stack;

public class ParseXmlTree {

	public static App xmlTreeToApp(String[] lines) {
		App app = new App();
		app.setIntentFilters(new ArrayList<IntentFilter>());
		Stack<String> elements = new Stack<String>();

		IntentFilter intentFilter = null;
		int indentIntentFilter = -1;
		Data data = null;
		int lastIndentE = -1;

		for (String line : lines) {

			int j = 0;
			while (line.charAt(j) == ' ') {
				j++;
			}
			char lineTypeChar = line.charAt(j);

			// System.out.println(line);

			if (lineTypeChar == 'N') {
				int posN = line.indexOf("N: ");

			} else if (lineTypeChar == 'E') {

				int posE = line.indexOf("E: ");

				int indentE = posE / 2;
				// System.out.println(indentE + " " + posE);
				for (int i = lastIndentE; i >= indentE; i--) {
					elements.pop();
				}

				if (data != null) {
					intentFilter.getmDataList().add(data);
					data = null;
				}

				if (indentE <= indentIntentFilter) {
					if (intentFilter != null) {
						app.getIntentFilters().add(intentFilter);
					}
					intentFilter = null;
				}

				lastIndentE = indentE;
				// e.g. E: manifest (line=2)
				String[] parts = line.substring(posE + 3).split(" ");
				String elementName = parts[0];
				elements.push(elementName);

				if (elementName.equals("intent-filter")) {
					intentFilter = new IntentFilter();
					intentFilter.setmType(elementToType(elements.get(elements
							.size() - 2)));
					// remember intent
					indentIntentFilter = indentE;
				} else if (elementName.equals("data")) {
					data = new Data();
				}

			} else if (lineTypeChar == 'A') {
				int posA = line.indexOf("A: ");

				// e.g. A: android:versionCode(0x0101021b)=(type 0x10)0x5
				int posEquals = line.indexOf("=");
				String attrValue = line.substring(posEquals + 1);
				String attrName = line.substring(posA + 3, posEquals);

				if (posA == 4 && elements.lastElement().equals("manifest")
						&& attrName.equals("package")) {

					app.setPackageName(convertValue(attrValue));

				} else if (posA == 4
						&& elements.lastElement().equals("manifest")
						&& attrName.startsWith("android:versionCode")) {

					app.setLatestVersionCode(convertValue(attrValue));

				} else if (posA == 12
						&& elements.lastElement().equals("action")) {
					intentFilter.getmActions().add(convertValue(attrValue));

				} else if (posA == 12
						&& elements.lastElement().equals("category")) {

					intentFilter.getmCategories().add(convertValue(attrValue));

				} else if (posA == 12 && elements.lastElement().equals("data")) {
					if (attrName.startsWith("android:host")) {
						data.setmHost(convertValue(attrValue));
					} else if (attrName.startsWith("android:scheme")) {
						data.setmScheme(convertValue(attrValue));
					} else if (attrName.startsWith("android:mimeType")) {
						data.setmMimeType(convertValue(attrValue));
					} else if (attrName.startsWith("android:pathPattern")) {
						data.setmPathPattern(convertValue(attrValue));
					} else if (attrName.startsWith("android:path")) {
						data.setmPath(convertValue(attrValue));
					} else if (attrName.startsWith("android:pathPrefix")) {
						data.setmPathPrefix(convertValue(attrValue));
					}
				}

			}
		}
		return app;
	}

	private static int elementToType(String string) {
		if (string.equals("activity") || string.equals("activity-alias")) {
			return 0;
		} else if (string.equals("receiver")) {
			return 1;
		} else if (string.equals("service")) {
			return 2;
		} else {
			return -1;
		}
	}

	private static String convertValue(String attrValue) {
		if (attrValue.indexOf("(type 0x10)") == 0) {
			return String.valueOf(Long.parseLong(attrValue.substring(13), 16));
		} else if (attrValue.startsWith("\"")) {
			return attrValue.substring(1,
					attrValue.substring(1).indexOf('"') + 1);

		} else {
			return null;
		}
	}

}
