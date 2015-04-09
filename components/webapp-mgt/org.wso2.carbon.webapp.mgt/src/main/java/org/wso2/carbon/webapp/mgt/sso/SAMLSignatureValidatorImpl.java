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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.opensaml.saml2.core.Assertion;
import org.opensaml.saml2.core.Response;
import org.opensaml.saml2.core.Subject;
import org.opensaml.xml.signature.SignatureValidator;
import org.opensaml.xml.validation.ValidationException;
import org.wso2.carbon.base.MultitenantConstants;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.core.util.KeyStoreManager;
import org.wso2.carbon.identity.sso.agent.SSOAgentException;
import org.wso2.carbon.identity.sso.agent.bean.SSOAgentConfig;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.user.api.UserStoreException;
import org.wso2.carbon.utils.multitenancy.MultitenantUtils;
import org.wso2.carbon.webapp.mgt.DataHolder;
import org.wso2.carbon.identity.sso.agent.util.SAMLSignatureValidator;

import java.security.KeyStore;
import java.security.cert.X509Certificate;

public class SAMLSignatureValidatorImpl implements SAMLSignatureValidator {

    private static Log log = LogFactory.getLog(SAMLSignatureValidatorImpl.class);

    @Override
    public void validateSignature(Response response, Assertion assertion, SSOAgentConfig ssoAgentConfig) throws SSOAgentException {

        if (ssoAgentConfig.getSAML2().isResponseSigned()) {
            if (response.getSignature() == null) {
                throw new SSOAgentException("SAML2 Response signing is enabled, but signature element not found in SAML2 Response element");
            } else {
                try {
                    SignatureValidator validator = getSignatureValidator(assertion);
                    validator.validate(response.getSignature());
                } catch (ValidationException e) {
                    throw new SSOAgentException("Signature validation failed for SAML2 Response");
                }
            }
        }
        if (ssoAgentConfig.getSAML2().isAssertionSigned()) {
            if (assertion.getSignature() == null) {
                throw new SSOAgentException("SAML2 Assertion signing is enabled, but signature element not found in SAML2 Assertion element");
            } else {
                try {
                    SignatureValidator validator = getSignatureValidator(assertion);
                    validator.validate(assertion.getSignature());
                } catch (ValidationException e) {
                    throw new SSOAgentException("Signature validation failed for SAML2 Assertion");
                }
            }
        }
    }

    private SignatureValidator getSignatureValidator(Assertion assertion) throws SSOAgentException {

        X509Certificate certificate;

        Subject subject = assertion.getSubject();
        String fqUserName = subject.getNameID().getValue();
        String tenantDomain = MultitenantUtils.getTenantDomain(fqUserName);

        try {
            int tenantId = DataHolder.getRealmService().getTenantManager().getTenantId(tenantDomain);
            if (tenantId != MultitenantConstants.SUPER_TENANT_ID) {
                try {
                    PrivilegedCarbonContext.startTenantFlow();
                    PrivilegedCarbonContext carbonContext = PrivilegedCarbonContext.getThreadLocalCarbonContext();
                    carbonContext.setTenantDomain(tenantDomain, true);
                    DataHolder.getTenantRegistryLoader().loadTenantRegistry(tenantId);
                } catch (RegistryException e) {
                    log.error("Unable to load tenant registry for tenant :: " + tenantDomain, e);
                } finally {
                    PrivilegedCarbonContext.endTenantFlow();
                }
            }
            certificate = getX509CredentialImplForTenant(tenantId, tenantDomain).getEntityCertificate();
        } catch (UserStoreException e) {
            throw new SSOAgentException("unable to get tenant ID for domain : "+tenantDomain, e);
        }
        return new SignatureValidator(new SSOCarbonX509Credential(certificate));
    }

    /**
     * @param tenantID   tenant ID value
     * @param domainName tenant domain name
     * @return SSOCarbonX509Credential
     * @throws SSOAgentException
     */
    private SSOCarbonX509Credential getX509CredentialImplForTenant(int tenantID, String domainName) throws SSOAgentException {

        KeyStoreManager keyStoreManager = KeyStoreManager.getInstance(tenantID);
        SSOCarbonX509Credential credentialImpl;
        X509Certificate x509Certificate;
        try {
            if (tenantID != MultitenantConstants.SUPER_TENANT_ID) {
                KeyStore keystore = keyStoreManager.getKeyStore(generateKSNameFromDomainName(domainName));
                x509Certificate = (X509Certificate) keystore.getCertificate(domainName);
            } else {
                x509Certificate = keyStoreManager.getDefaultPrimaryCertificate();
            }
            credentialImpl = new SSOCarbonX509Credential(x509Certificate);
        } catch (Exception e) {
            String errorMsg = "Error instantiating an X509CredentialImpl object for the public cert.";
            throw new SSOAgentException(errorMsg, e);
        }
        return credentialImpl;
    }

    /**
     * Generate the key store name from the domain name
     *
     * @param tenantDomain tenant domain name
     * @return key store file name
     */
    private static String generateKSNameFromDomainName(String tenantDomain) {
        String ksName = tenantDomain.trim().replace(".", "-");
        return (ksName + ".jks");
    }

}