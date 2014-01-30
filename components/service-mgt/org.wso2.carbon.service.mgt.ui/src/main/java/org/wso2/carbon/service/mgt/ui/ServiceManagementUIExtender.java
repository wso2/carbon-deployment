/*                                                                             
 * Copyright 2004,2005 The Apache Software Foundation.                         
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
package org.wso2.carbon.service.mgt.ui;

import org.wso2.carbon.ui.UIExtender;

import java.util.List;
import java.util.ArrayList;
import java.util.Collections;

/**
 * This class is used for extending the service management UI. Other UI components could add to this
 *  UI by calling this OSGi services addItem method
 */
public class ServiceManagementUIExtender implements UIExtender {
    private List<String>  items = new ArrayList<String>();

    /**
     * Add a new UI item
     * @param item  Item to be added. This is an HTML string, which can contain hyperlinks
     */
    public void addItem(String item){
        items.add(item);
    }

    /**
     * Get the list of UI items
     * @return list of available UI items
     */
    public List<String> getItems(){
        return Collections.unmodifiableList(items);
    }

}
