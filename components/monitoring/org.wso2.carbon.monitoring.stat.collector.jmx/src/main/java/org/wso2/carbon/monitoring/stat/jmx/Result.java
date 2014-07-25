/*
 * Copyright 2004,2014 The Apache Software Foundation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.wso2.carbon.monitoring.stat.jmx;

import javax.management.AttributeList;

/**
 * Represents the List of Attributes read from MBeans and the correlator
 */
public class Result {

	private String correlator;
	private AttributeList attributes;

	/**
	 * Construct a result object.
	 * @param correlator The Correlator of the Attributes
	 * @param attributes The List of Attributes.
	 */
	public Result(String correlator, AttributeList attributes) {
		this.correlator = correlator;
		this.attributes = attributes;
	}

	/**
	 * The Correlator of this Result
	 * @return the correlator
	 */
	public String getCorrelator() {
		return correlator;
	}

	/**
	 * set the correlator of the Result
	 * @param correlator
	 */
	public void setCorrelator(String correlator) {
		this.correlator = correlator;
	}

	/**
	 * Get the list of attributes of this Result
	 * @return
	 */
	public AttributeList getAttributes() {
		return attributes;
	}

	/**
	 * sets the Attribute List
	 * @param attributes
	 */
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
