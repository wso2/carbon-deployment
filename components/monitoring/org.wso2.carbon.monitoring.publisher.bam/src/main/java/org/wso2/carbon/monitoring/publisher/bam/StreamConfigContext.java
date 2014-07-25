package org.wso2.carbon.monitoring.publisher.bam;

/**
 * The configuration context for a give data stream
 */
public class StreamConfigContext {
	private boolean isEnabled;
	private String receiverUrl;
	private String description;
	private String streamName;
	private String nickName;
	private String streamVersion;
	private String username;
	private String password;

	public void setEnabled(boolean isEnabled) {
		this.isEnabled = isEnabled;
	}

	public void setStreamName(String streamName) {
		this.streamName = streamName;
	}

	public void setNickName(String nickName) {
		this.nickName = nickName;
	}

	public void setStreamVersion(String streamVersion) {
		this.streamVersion = streamVersion;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public void setReceiverUrl(String receiverUrl) {
		this.receiverUrl = receiverUrl;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getNickName() {
		return nickName;
	}

	public String getStreamName() {
		return streamName;
	}

	public String getStreamVersion() {
		return streamVersion;
	}

	public String getUsername() {
		return username;
	}


	public String getPassword() {
		return password;
	}


	public String getReceiverUrl() {
		return receiverUrl;
	}


	public String getDescription() {
		return description;
	}

	public boolean isEnabled() {
		return isEnabled;
	}
}
