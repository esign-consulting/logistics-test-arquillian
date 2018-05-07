# logistics-test-arquillian

[![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)](https://opensource.org/licenses/MIT)

Project for Integration testing the [Logistics](https://github.com/esign-consulting/logistics) application. The test is based on [Arquillian](http://arquillian.org) and is executed through [Maven](https://maven.apache.org) **(installation required)**.

In order to run the test, firstly set the appropriate remote host and ports in the [arquillian.xml](src/test/resources/arquillian.xml) file. You must use the ports configured in the *socket-binding-group* of your remote Wildfly **standalone.xml** file. Set the *managementPort* value from the *management-http* socket-binding, and set the *port* value from the *http* socket-binding. Both *managementAddress* and *host* are the same, they must point to your remote Wildfly host. Finally, define the correct *username* of your remote Wildfly administrator.

After that, just execute the command `mvn test -Dpassword=<wildfly_password>`, replacing *<wildfly_password>* with the password of the administrator user of your remote Wildfly instance. Arquillian packages the EJB, deploys it into the remote Wildfly server, executes all the tests and, at the end, undeploys the application.
