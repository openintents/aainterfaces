package org.openintents.aainterfaces.server;

import javax.jdo.annotations.IdGeneratorStrategy;
import javax.jdo.annotations.PersistenceCapable;
import javax.jdo.annotations.Persistent;
import javax.jdo.annotations.PrimaryKey;
import javax.jdo.listener.StoreCallback;

import org.apache.commons.lang.builder.HashCodeBuilder;

import com.google.appengine.api.datastore.Key;

@PersistenceCapable
public class Data implements StoreCallback {

	@PrimaryKey
	@Persistent(valueStrategy = IdGeneratorStrategy.IDENTITY)
	private Key id;

	@Persistent
	private String mHost;
	@Persistent
	private String mMimeType;

	@Persistent
	private String mMimeType1;

	@Persistent
	private String mMimeType2;

	@Persistent
	private String mPath;
	@Persistent
	private String mPathPattern;
	@Persistent
	private String mPathPrefix;
	@Persistent
	private String mPort;
	@Persistent
	private String mScheme;

	public Data() {

	}

	public Data(String host, String mimeType, String path, String pathPrefix,
			String pathPattern, String port, String scheme) {
		mHost = host;
		mMimeType = mimeType;
		mPath = path;
		mPathPrefix = pathPrefix;
		mPathPattern = pathPattern;
		mPort = port;
		mScheme = scheme;
	}

	public Data(Data d) {
		mHost = d.mHost;
		mMimeType = d.mMimeType;
		mPath = d.mPath;
		mPathPrefix = d.mPathPrefix;
		mPathPattern = d.mPathPattern;
		mPort = d.mPort;
		mScheme = d.mScheme;
	}

	public Key getId() {
		return id;
	}

	public String getmHost() {
		return mHost;
	}

	public void setmHost(String mHost) {
		this.mHost = mHost;
	}

	public String getmMimeType() {
		return mMimeType;
	}

	public void setmMimeType(String mMimeType) {
		this.mMimeType = mMimeType;
	}

	public String getmPath() {
		return mPath;
	}

	public void setmPath(String mPath) {
		this.mPath = mPath;
	}

	public String getmPathPattern() {
		return mPathPattern;
	}

	public void setmPathPattern(String mPathPattern) {
		this.mPathPattern = mPathPattern;
	}

	public String getmPathPrefix() {
		return mPathPrefix;
	}

	public void setmPathPrefix(String mPathPrefix) {
		this.mPathPrefix = mPathPrefix;
	}

	public String getmPort() {
		return mPort;
	}

	public void setmPort(String mPort) {
		this.mPort = mPort;
	}

	public String getmScheme() {
		return mScheme;
	}

	public void setmScheme(String mScheme) {
		this.mScheme = mScheme;
	}

	@Override
	public boolean equals(Object obj) {
		if (obj != null && obj instanceof Data) {
			Data d2 = (Data) obj;
			boolean equals = (getmHost() == null && d2.getmHost() == null)
					|| (getmHost().equals(d2.getmHost()));
			equals = equals
					&& ((getmMimeType() == null && d2.getmMimeType() == null) || (getmMimeType()
							.equals(d2.getmMimeType())));
			equals = equals
					&& ((getmPath() == null && d2.getmPath() == null) || (getmPath()
							.equals(d2.getmPath())));
			equals = equals
					&& ((getmPathPattern() == null && d2.getmPathPattern() == null) || (getmPathPattern()
							.equals(d2.getmPathPattern())));
			equals = equals
					&& ((getmPathPrefix() == null && d2.getmPathPrefix() == null) || (getmPathPrefix()
							.equals(d2.getmPathPrefix())));
			equals = equals
					&& ((getmScheme() == null && d2.getmScheme() == null) || (getmScheme()
							.equals(d2.getmScheme())));

			return equals;
		} else {
			return super.equals(obj);
		}
	}

	@Override
	public int hashCode() {
		HashCodeBuilder hcb = new HashCodeBuilder();
		hcb.append(getmHost()).append(getmMimeType()).append(getmPath())
				.append(getmPathPattern()).append(getmPathPrefix())
				.append(getmScheme());
		return hcb.toHashCode();
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		if (getmMimeType() == null || getmMimeType().length() == 0) {
			sb.append("no mime-type");
		} else {
			sb.append(getmMimeType());
		}
		sb.append(" -- ");

		if (getmScheme() == null || getmScheme().length() == 0) {
			sb.append("anyScheme");
		} else {
			sb.append(getmScheme());
		}
		sb.append("://");

		if (getmHost() == null || getmHost().length() == 0) {
			sb.append("anyHost");
		} else {
			sb.append(getmHost());
		}
		if (getmPath() != null && getmPath().length() > 0) {
			sb.append(getmPath());
		} else if (getmPathPrefix() != null && getmPathPrefix().length() > 0) {
			sb.append(getmPathPrefix() + "*");
		} else if (getmPathPattern() != null && getmPathPattern().length() > 0) {
			sb.append("reg(" + getmPathPattern() + ")");
		}

		return sb.toString();
	}

	public void jdoPreStore() {
		if (mMimeType != null) {
			String[] mimeParts = mMimeType.split("/");
			mMimeType1 = mimeParts[0];
			if (mimeParts.length > 1) {
				mMimeType2 = mimeParts[1];
			} else {
				mMimeType2 = null;
			}
		} else {
			mMimeType1 = null;
			mMimeType2 = null;
		}
	}
}
