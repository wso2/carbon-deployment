package org.wso2.carbon.monitoring.jmx;

import javax.management.AttributeList;

/**
 * Created by chamil on 7/3/14.
 */
public class Result {

	private String correlator;
	private AttributeList attributes;

	public Result(String correlator, AttributeList attributes) {
		this.correlator = correlator;
		this.attributes = attributes;
	}

	public String getCorrelator() {
		return correlator;
	}

	public void setCorrelator(String correlator) {
		this.correlator = correlator;
	}

	public AttributeList getAttributes() {
		return attributes;
	}

	public void setAttributes(AttributeList attributes) {
		this.attributes = attributes;
	}

	@Override
	public String toString() {
		return "Result{" +
		       "correlator='" + correlator + '\'' +
		       ", attributes=" + attributes +
		       '}';
	}
}
