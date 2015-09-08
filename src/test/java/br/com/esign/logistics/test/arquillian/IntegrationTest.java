/*
 * The MIT License
 *
 * Copyright 2015 Esign Consulting Ltda.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package br.com.esign.logistics.test.arquillian;

import br.com.esign.logistics.core.Place;
import br.com.esign.logistics.core.Route;
import br.com.esign.logistics.core.RoutesMap;
import br.com.esign.logistics.core.impl.ChosenRoute;
import br.com.esign.logistics.ejb.RoutesMapEJB;
import java.io.File;
import java.util.List;
import javax.ejb.EJB;
import javax.ejb.EJBException;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.EmptyAsset;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.jboss.shrinkwrap.resolver.api.maven.Maven;
import static org.junit.Assert.*;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.MethodSorters;

/**
 *
 * @author gustavomunizdocarmo
 */
@RunWith(Arquillian.class)
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class IntegrationTest {
    
    private static RoutesMap MAP = new RoutesMap("Arquillian IntegrationTest");
    
    private final Place PLACEA = new Place("A");
    private final Place PLACEB = new Place("B");
    private final Place PLACEC = new Place("C");
    private final Place PLACED = new Place("D");
    private final Route ROUTE1 = new Route(PLACEA, PLACEB, 10);
    private final Route ROUTE2 = new Route(PLACEB, PLACED, 15);
    private final Route ROUTE3 = new Route(PLACEA, PLACEC, 20);
    
    /**
     * Packages the EJB module.
     * @return The WAR to deploy
     */
    @Deployment
    public static WebArchive createDeployment() {
        File[] files = Maven.resolver().loadPomFromFile("pom.xml")
            .importRuntimeDependencies().resolve().withTransitivity().asFile();
        return ShrinkWrap.create(WebArchive.class)
            .addAsLibraries(files)
            .addAsManifestResource(EmptyAsset.INSTANCE, "beans.xml");
    }
    
    /**
     * EJB injection.
     */
    @EJB
    private RoutesMapEJB ejb;
    
    /**
     * Creates a map.
     */
    @Test
    public void testA() {
        RoutesMap map = ejb.createRoutesMap(MAP);
        assertNotNull(map);
        assertNotNull(map.getSlug());
        assertEquals(MAP, map);
    }
    
    /**
     * Checks map existence.
     */
    @Test
    public void testB() {
        List<RoutesMap> list = ejb.listRoutesMaps();
        assertNotNull(list);
        assertTrue(list.contains(MAP));
    }
    
    /**
     * Checks the unique map constraint.
     */
    @Test(expected = EJBException.class)
    public void testC() {
        ejb.createRoutesMap(MAP);
    }
    
    /**
     * Creates routes for the map.
     */
    @Test
    public void testD() {
        Route[] routes = ejb.addRouteToMap(MAP.getSlug(), ROUTE1);
        assertNotNull(routes);
        assertEquals(2, routes.length);
        assertArrayEquals(new Route[] {ROUTE1, ROUTE1.opposite()}, routes);
        
        MAP = ejb.getRoutesMapBySlug(MAP.getSlug());
        assertNotNull(MAP);
        assertTrue(MAP.containsRoute(ROUTE1));
        assertTrue(MAP.containsRoute(ROUTE1.opposite()));
        
        routes = ejb.addRouteToMap(MAP.getSlug(), ROUTE2);
        assertNotNull(routes);
        assertEquals(2, routes.length);
        assertArrayEquals(new Route[] {ROUTE2, ROUTE2.opposite()}, routes);
        
        MAP = ejb.getRoutesMapBySlug(MAP.getSlug());
        assertNotNull(MAP);
        assertTrue(MAP.containsRoute(ROUTE2));
        assertTrue(MAP.containsRoute(ROUTE2.opposite()));
        
        routes = ejb.addRouteToMap(MAP.getSlug(), ROUTE3);
        assertNotNull(routes);
        assertEquals(2, routes.length);
        assertArrayEquals(new Route[] {ROUTE3, ROUTE3.opposite()}, routes);
        
        MAP = ejb.getRoutesMapBySlug(MAP.getSlug());
        assertNotNull(MAP);
        assertTrue(MAP.containsRoute(ROUTE3));
        assertTrue(MAP.containsRoute(ROUTE3.opposite()));
    }
    
    /**
     * Checks the unique route constaint.
     */
    @Test(expected = EJBException.class)
    public void testE() {
        ejb.addRouteToMap(MAP.getSlug(), ROUTE3);
    }
    
    /**
     * Removes last route.
     */
    @Test
    public void testF() {
        ejb.removeRouteFromMap(MAP.getSlug(), ROUTE3);
        
        MAP = ejb.getRoutesMapBySlug(MAP.getSlug());
        assertNotNull(MAP);
        assertFalse(MAP.containsRoute(ROUTE3));
        assertFalse(MAP.containsRoute(ROUTE3.opposite()));
    }
    
    /**
     * Tests best route functionality.
     */
    @Test
    public void testG() {
        Route route = ejb.getBestRoute(MAP.getSlug(), PLACEA.getName(), PLACED.getName(), 10, 2.50);
        assertNotNull(route);
        assertEquals(6.25, ((ChosenRoute) route).getCost(), 0);
        
        List<Route> routes = route.getRoutes();
        assertNotNull(routes);
        assertEquals(2, routes.size());
        assertArrayEquals(new Route[] {ROUTE1, ROUTE2}, routes.toArray());        
    }
    
    /**
     * Removes the map.
     */
    @Test
    public void testH() {
        String slug = MAP.getSlug();
        ejb.removeRoutesMap(slug);
        assertNull(ejb.getRoutesMapBySlug(slug));
    }
    
}
