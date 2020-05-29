/*
﻿Developed with the contribution of the European Commission - Directorate General for Maritime Affairs and Fisheries
© European Union, 2015-2016.

This file is part of the Integrated Fisheries Data Management (IFDM) Suite. The IFDM Suite is free software: you can
redistribute it and/or modify it under the terms of the GNU General Public License as published by the
Free Software Foundation, either version 3 of the License, or any later version. The IFDM Suite is distributed in
the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License for more details. You should have received a
copy of the GNU General Public License along with the IFDM Suite. If not, see <http://www.gnu.org/licenses/>.
 */
package eu.europa.ec.fisheries.uvms.webgateway;

import eu.europa.ec.fisheries.uvms.commons.date.JsonBConfigurator;
import eu.europa.ec.fisheries.uvms.rest.security.InternalRestTokenHandler;
import eu.europa.ec.fisheries.uvms.rest.security.UnionVMSFeature;
import eu.europa.ec.fisheries.uvms.webgateway.mock.AssetModuleMock;
import eu.europa.ec.fisheries.uvms.webgateway.mock.IncidentModuleMock;
import eu.europa.ec.fisheries.uvms.webgateway.mock.MovementModuleMock;
import eu.europa.ec.fisheries.uvms.webgateway.mock.UnionVMSMock;
import eu.europa.ec.mare.usm.jwt.JwtTokenHandler;
import org.eu.ingwar.tools.arquillian.extension.suite.annotations.ArquillianSuiteDeployment;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.jboss.shrinkwrap.resolver.api.maven.Maven;

import javax.ejb.EJB;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.WebTarget;
import java.io.File;
import java.util.Arrays;

@ArquillianSuiteDeployment
public abstract class BuildStreamCollectorDeployment {

    @EJB
    private JwtTokenHandler tokenHandler;

    @EJB
    private InternalRestTokenHandler internalRestTokenHandler;

    private String token;

    @Deployment(name = "collector", order = 2)
    public static Archive<?> createDeployment() {

        WebArchive testWar = ShrinkWrap.create(WebArchive.class, "test.war");

        File[] files = Maven.resolver().loadPomFromFile("pom.xml").importRuntimeAndTestDependencies().resolve()
                .withTransitivity().asFile();
        testWar.addAsLibraries(files);
        
        testWar.addPackages(true, "eu.europa.ec.fisheries.uvms.webgateway");

        testWar.delete("/WEB-INF/web.xml");
        testWar.addAsWebInfResource("mock-web.xml", "web.xml");

        testWar.deleteClass(UnionVMSMock.class);
        testWar.deleteClass(MovementModuleMock.class);
        testWar.deleteClass(AssetModuleMock.class);
        testWar.deleteClass(IncidentModuleMock.class);
        
        return testWar;
    }

    @Deployment(name = "uvms", order = 1)
    public static Archive<?> createUVMSMock() {

        WebArchive testWar = ShrinkWrap.create(WebArchive.class, "unionvms.war");

        File[] files = Maven.configureResolver().loadPomFromFile("pom.xml")
                .importRuntimeAndTestDependencies()
                .resolve()
                .withTransitivity().asFile();
        testWar.addAsLibraries(files);

        testWar.addClass(UnionVMSMock.class);
        testWar.addClass(MovementModuleMock.class);
        testWar.addClass(AssetModuleMock.class);
        testWar.addClass(IncidentModuleMock.class);

        return testWar;
    }

    protected WebTarget getWebTarget() {
        Client client = ClientBuilder.newClient();
        client.register(JsonBConfigurator.class);
        return client.target("http://localhost:8080/test/rest");
    }

    protected String getToken() {
        if (token == null) {
            token = tokenHandler.createToken("user", 
                    Arrays.asList(UnionVMSFeature.manageManualMovements.getFeatureId(), 
                            UnionVMSFeature.viewMovements.getFeatureId(),
                            UnionVMSFeature.viewManualMovements.getFeatureId(),
                            UnionVMSFeature.manageAlarmsHoldingTable.getFeatureId(),
                            UnionVMSFeature.viewVesselsAndMobileTerminals.getFeatureId(),
                            UnionVMSFeature.manageAlarmsOpenTickets.getFeatureId(),
                            UnionVMSFeature.viewAlarmsOpenTickets.getFeatureId(),
                            UnionVMSFeature.manageVessels.getFeatureId(),
                            UnionVMSFeature.viewAlarmsHoldingTable.getFeatureId()));
        }
        return token;
    }

    protected String getTokenInternalRest() {
        return internalRestTokenHandler.createAndFetchToken("user");
    }
}
