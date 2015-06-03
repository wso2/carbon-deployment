/*
*  Copyright (c) WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
*
*  WSO2 Inc. licenses this file to you under the Apache License,
*  Version 2.0 (the "License"); you may not use this file except
*  in compliance with the License.
*  You may obtain a copy of the License at
*
*    http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing,
* software distributed under the License is distributed on an
* "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
* KIND, either express or implied.  See the License for the
* specific language governing permissions and limitations
* under the License.
*/

package org.wso2.carbon.webapp.mgt.sso;

import org.opensaml.xml.security.credential.Credential;
import org.opensaml.xml.security.credential.CredentialContextSet;
import org.opensaml.xml.security.credential.UsageType;
import org.opensaml.xml.security.x509.X509Credential;

import javax.crypto.SecretKey;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.cert.X509CRL;
import java.security.cert.X509Certificate;
import java.util.Collection;

public class SSOCarbonX509Credential implements X509Credential {

    private PublicKey publicKey = null;
    private X509Certificate entityCertificate = null;
    private PrivateKey privateKey = null;

    public SSOCarbonX509Credential(X509Certificate cert) {
        publicKey = cert.getPublicKey();
        this.entityCertificate = cert;
    }

    /**
     * Retrieves the publicKey
     */
    public PublicKey getPublicKey() {
        return publicKey;
    }

    public PrivateKey getPrivateKey() {
        return privateKey;
    }

    public X509Certificate getEntityCertificate() {
        return entityCertificate;
    }

    // ********** Not implemented **************************************************************

    public Collection<X509CRL> getCRLs() {
        // TODO Auto-generated method stub
        return null;
    }

    public Collection<X509Certificate> getEntityCertificateChain() {
        // TODO Auto-generated method stub
        return null;
    }

    public CredentialContextSet getCredentalContextSet() {
        // TODO Auto-generated method stub
        return null;
    }

    public Class<? extends Credential> getCredentialType() {
        // TODO Auto-generated method stub
        return null;
    }

    public String getEntityId() {
        // TODO Auto-generated method stub
        return null;
    }

    public Collection<String> getKeyNames() {
        // TODO Auto-generated method stub
        return null;
    }

    public SecretKey getSecretKey() {
        // TODO Auto-generated method stub
        return null;
    }

    public UsageType getUsageType() {
        // TODO Auto-generated method stub
        return null;
    }
}