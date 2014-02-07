/*
 * Copyright 2005-2007 WSO2, Inc. (http://wso2.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.wso2.carbon.springservices.ui;

/*
 * Bean class to hold the information about spring beans
 */

public class SpringBeansData {
    private String springContext;
    private String[] beans;

    public String getSpringContext() {
        return springContext;
    }

    public void setSpringContext(String springContext) {
        this.springContext = springContext;
    }

    public String[] getBeans() {
        return beans;
    }

    public void setBeans(String[] beans) {
        this.beans = beans;
    }
}