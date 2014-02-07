/*
*  Copyright (c) 2005-2010, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.wso2.carbon.service.mgt;

import org.apache.axiom.om.impl.builder.StAXBuilder;
import org.apache.axiom.om.impl.builder.StAXOMBuilder;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMNamespace;
import org.apache.neethi.Policy;
import org.apache.neethi.PolicyComponent;
import org.apache.neethi.Assertion;

import javax.xml.namespace.QName;
import java.io.ByteArrayInputStream;
import java.util.List;

public class TestPolicyUtil extends BaseTestCase {
    
    private static final String TEST_POLICY = "<wsp:Policy wsu:Id=\"binding_level_policy\" " +
            "     xmlns:wsp=\"http://schemas.xmlsoap.org/ws/2004/09/policy\" " +
            "     xmlns:wsu=\"http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-wssecurity-utility-1.0.xsd\" >"+"<wsp:ExactlyOne>" +
            "            <wsp:All>" +
            "                <wsu:Basic256Rsa15 /> " +
            "            </wsp:All>" +
            "        </wsp:ExactlyOne>" +
            "    </wsp:Policy>";
    OMElement root;
    OMElement wrapEle;
    Policy testPol;
    Policy testPol1;
    QName attrName;
    QName attrName1;

    public void setUp() throws Exception{
         super.setUp();
    }


    public void testSteps() throws Exception{

        MethodStuff();
        
    }


    public void MethodStuff() throws Exception{
        
        OMElement element;
        OMElement elm;

        ByteArrayInputStream xmlStream = new ByteArrayInputStream(TEST_POLICY.getBytes());
        StAXBuilder builder = new StAXOMBuilder(xmlStream);
        root = builder.getDocumentElement();

        Policy pol1 = new Policy();
        List<PolicyComponent> pC = pol1.getAssertions();
        assertEquals(0,pC.size());

        pol1 = PolicyUtil.getPolicyFromOMElement(root);
        pC = pol1.getAssertions();
        assertEquals(1,pC.size());

        element = PolicyUtil.getEmptyPolicyAsOMElement();
        OMNamespace nmSpc = element.getNamespace();
        assertEquals("wsp",nmSpc.getPrefix());

        createNewPolicy();
        testPol.addPolicyComponents(pC);

        elm = PolicyUtil.getPolicyAsOMElement(testPol);
        String attr = elm.getAttributeValue(attrName);
        assertEquals("attribute1",attr);

        String ps1 = PolicyUtil.getPolicyAsString(testPol1);
        System.out.println(ps1);

        wrapEle = PolicyUtil.getWrapper("testingPolicy");
        OMNamespace nmSpecs = element.getNamespace();
        assertEquals("wsp",nmSpecs.getPrefix());

    }

    public void createNewPolicy() throws Exception{
        
        attrName = new QName("attr");
        attrName1 = new QName("attr1");

        testPol = new Policy();
        testPol.setName("testPol");
        testPol.setId("pol");
        testPol.addAttribute(attrName,"attribute1");

        testPol1 = new Policy();
        testPol1.setName("testPol1");
        testPol1.setId("pol1");
        testPol1.addAttribute(attrName1,"attribute2");

    }
}
