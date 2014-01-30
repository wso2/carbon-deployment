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
package org.wso2.carbon.jarservices;

/**
 *
 */
public class DuplicateServiceException extends Exception {
    private String message;

    public String getMsg() {
        return message;
    }

    public void setMsg(String message) {
        this.message = message;
    }

    public DuplicateServiceException() {
        super();
    }

    public DuplicateServiceException(String message) {
        super(message);
        this.message = message;
    }

    public DuplicateServiceException(String message, Throwable cause) {
        super(message, cause);
        this.message = message;
    }

    public DuplicateServiceException(Throwable cause) {
        super(cause);
    }
}
