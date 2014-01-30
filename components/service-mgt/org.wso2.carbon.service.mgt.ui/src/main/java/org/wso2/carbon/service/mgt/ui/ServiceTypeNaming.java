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

import java.util.HashMap;

public class ServiceTypeNaming {
    
    private static HashMap map = new HashMap();

    public ServiceTypeNaming() {
        
        if (map.isEmpty()){
            map.put("axis2", "Axis2");
            map.put("data_service", "Data service");
            map.put("js_service", "JS service");
            map.put("sts", "STS");
        }
    }
    
    
    public String convertString(String str) {
        
        if(map.containsKey(str)){
            return map.get(str).toString();
        }

        str = str.replace("_", " ");
        str = toTitleCase(str);
        return str;

    }

    public String toTitleCase(String input) {
        StringBuilder titleCase = new StringBuilder();
        boolean nextTitleCase = true;

        for (char c : input.toCharArray()) {
            if (Character.isSpaceChar(c)) {
                nextTitleCase = true;
            } else if (nextTitleCase) {
                c = Character.toTitleCase(c);
                nextTitleCase = false;
            }

            titleCase.append(c);
        }

        return titleCase.toString();
    }
}
