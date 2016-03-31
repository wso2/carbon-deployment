# carbon-deployment

---

|  Branch | Build Status |
| :------------ |:-------------
| master      | [![Build Status](https://wso2.org/jenkins/job/carbon-deployment/badge/icon)](https://wso2.org/jenkins/job/carbon-deployment) |
| release-4.2.x | [![Build Status](https://wso2.org/jenkins/job/carbon-deployment_release-4.2.x/badge/icon)](https://wso2.org/jenkins/job/carbon-deployment_release-4.2.x/) |


---
Carbon 5.x artifact deployment framework - This is an extensible framework where Carbon developers get to incorporate and write their own artifact deployers to deploy its artifacts into a Carbon 5 based product.

This provides following interfaces -
* `org.wso2.carbon.deployment.DeploymentService` - 

User level API for consuming `DeploymentEngine` functionality. An implementation of this is registered as an OSGI service which can be used by developers to deploy/undeploy/redeploy their artifacts.

* `org.wso2.carbon.deployment.Deployer` - 

This interface is used to provide the deployment mechanism in carbon for custom artifacts, where you can write your own Deployer to process a particular ArtifactType. Developers need to develop implementations based on this interface, and register it as an OSGi service (using the org.wso2.carbon.deployment.Deployer as the interface) for the DeploymentEngine to find.

* `org.wso2.carbon.deployment.LifecycleListener` -

This interface can be used to write your own lifecycle listeners to listen on artifact deployment events. The implementation should be registered as an OSGi service with org.wso2.carbon.deployment.LifecycleListener as the interface. This interface receives following events.

```bash
BEFORE_START_EVENT
AFTER_START_EVENT
BEFORE_STOP_EVENT
AFTER_STOP_EVENT
```

## Download

Use Maven snippet:
````xml
<dependency>
    <groupId>org.wso2.carbon.deployment</groupId>
    <artifactId>org.wso2.carbon.deployment.engine</artifactId>
    <version>${carbon.jndi.version}</version>
</dependency>

<dependency>
    <groupId>org.wso2.carbon.deployment</groupId>
    <artifactId>org.wso2.carbon.deployment.notifier</artifactId>
    <version>${carbon.jndi.version}</version>
</dependency>
````

### Snapshot Releases

Use following Maven repository for snapshot versions of Carbon Deployment.

````xml
<repository>
    <id>wso2.snapshots</id>
    <name>WSO2 Snapshot Repository</name>
    <url>http://maven.wso2.org/nexus/content/repositories/snapshots/</url>
    <snapshots>
        <enabled>true</enabled>
        <updatePolicy>daily</updatePolicy>
    </snapshots>
    <releases>
        <enabled>false</enabled>
    </releases>
</repository>
````

### Released Versions

Use following Maven repository for released stable versions of Carbon JNDI.

````xml
<repository>
    <id>wso2.releases</id>
    <name>WSO2 Releases Repository</name>
    <url>http://maven.wso2.org/nexus/content/repositories/releases/</url>
    <releases>
        <enabled>true</enabled>
        <updatePolicy>daily</updatePolicy>
        <checksumPolicy>ignore</checksumPolicy>
    </releases>
</repository>
````
## Building From Source

Clone this repository first (`git clone https://github.com/wso2/carbon-deployment.git`) and use Apache Maven to build `mvn clean install`.


## How to Contribute
* Please report issues at [Carbon JIRA] (https://wso2.org/jira/browse/CARBON).
* Send your pull requests to [master branch] (https://github.com/wso2/carbon-deployment/tree/master) 

## License

Carbon Deployment is available under the Apache 2 License.

## Contact us
WSO2 Carbon developers can be contacted via the mailing lists:

* Carbon Developers List : dev@wso2.org
* Carbon Architecture List : architecture@wso2.org

## Copyright

Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
