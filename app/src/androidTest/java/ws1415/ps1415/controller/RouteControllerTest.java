package ws1415.ps1415.controller;


import com.skatenight.skatenightAPI.model.Route;
import com.skatenight.skatenightAPI.model.RoutePoint;
import com.skatenight.skatenightAPI.model.ServerWaypoint;
import com.skatenight.skatenightAPI.model.Text;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import ws1415.AuthenticatedAndroidTestCase;
import ws1415.ps1415.ServiceProvider;
import ws1415.ps1415.task.ExtendedTask;
import ws1415.ps1415.task.ExtendedTaskDelegateAdapter;

/**
 * Testet die Methoden des RouteController.
 * @author Richard Schulze
 */
public class RouteControllerTest extends AuthenticatedAndroidTestCase {
    private Route testRoute;

    private List<Route> routesToDelete;

    public void setUp() throws Exception {
        super.setUp();

        testRoute = new Route();
        testRoute.setLength("9 m");
        testRoute.setName("Test2");
        testRoute.setRouteData(new Text().setValue("g}b|Hevom@GV"));
        testRoute.setRoutePoints(Arrays.asList(
                new RoutePoint().setLongitude(7.62227).setLatitude(51.95748),
                new RoutePoint().setLongitude(7.62215).setLatitude(51.957519999999995)));
        testRoute.setWaypoints(Arrays.asList(
                new ServerWaypoint().setTitle("Wegpunkt 1").setLongitude(7.622272).setLatitude(51.957480999999994),
                new ServerWaypoint().setTitle("Wegpunkt 2").setLongitude(7.622145).setLatitude(51.957513)));
        testRoute = ServiceProvider.getService().routeEndpoint().addRoute(testRoute).execute();

        routesToDelete = new LinkedList<>();
    }

    public void testGetRoutes() throws Exception {
        final CountDownLatch signal = new CountDownLatch(1);
        RouteController.getRoutes(new ExtendedTaskDelegateAdapter<Void, List<Route>>() {
            @Override
            public void taskDidFinish(ExtendedTask task, List<Route> routes) {
                for (Route r : routes) {
                    if (r.getId().equals(testRoute.getId())) {
                        signal.countDown();
                        return;
                    }
                }
                fail("test route not fetched");
            }
        });
        assertTrue("timeout reached", signal.await(10, TimeUnit.SECONDS));
    }

    public void testAddRoute() throws Exception {
        final Route newRoute = new Route();
        newRoute.setLength("9 m");
        newRoute.setName("Neuer Route");
        newRoute.setRouteData(new Text().setValue("g}b|Hevom@GV"));
        newRoute.setRoutePoints(Arrays.asList(
                new RoutePoint().setLongitude(7.62227).setLatitude(51.95748),
                new RoutePoint().setLongitude(7.62215).setLatitude(51.957519999999995)));
        newRoute.setWaypoints(Arrays.asList(
                new ServerWaypoint().setTitle("Wegpunkt 1").setLongitude(7.622272).setLatitude(51.957480999999994),
                new ServerWaypoint().setTitle("Wegpunkt 2").setLongitude(7.622145).setLatitude(51.957513)));
        final CountDownLatch signal = new CountDownLatch(1);
        RouteController.addRoute(new ExtendedTaskDelegateAdapter<Void, Route>() {
            @Override
            public void taskDidFinish(ExtendedTask task, Route route) {
                assertNotNull("no route returned", route);
                assertNotNull("no id assigned", route.getId());
                assertEquals("wrong length", newRoute.getLength(), route.getLength());
                assertEquals("wrong name", newRoute.getName(), route.getName());
                assertEquals("wrong route data", newRoute.getRouteData(), route.getRouteData());
                assertEquals("wrong route points", newRoute.getRoutePoints(), route.getRoutePoints());
                assertEquals("wrong waypoints", newRoute.getWaypoints(), route.getWaypoints());
                routesToDelete.add(route);
                signal.countDown();
            }
        }, newRoute);
        assertTrue("timeout reached", signal.await(10, TimeUnit.SECONDS));
    }

    public void testDeleteRoute() throws Exception {
        final CountDownLatch signal = new CountDownLatch(1);
        RouteController.deleteRoute(new ExtendedTaskDelegateAdapter<Void, Void>() {
            @Override
            public void taskDidFinish(ExtendedTask task, Void aVoid) {
                signal.countDown();
            }
        }, testRoute.getId());
        assertTrue("timeout reached", signal.await(10, TimeUnit.SECONDS));

        List<Route> routes = ServiceProvider.getService().routeEndpoint().getRoutes().execute().getItems();
        if (routes != null) {
            for (Route r : routes) {
                assertFalse("route was not deleted", r.getId().equals(testRoute.getId()));
            }
        }
    }

    public void tearDown() throws Exception {
        super.tearDown();

        if (testRoute != null) {
            ServiceProvider.getService().routeEndpoint().deleteRoute(testRoute.getId()).execute();
            testRoute = null;
        }

        if (routesToDelete != null) {
            for (Route r : routesToDelete) {
                ServiceProvider.getService().routeEndpoint().deleteRoute(r.getId()).execute();
            }
            routesToDelete = null;
        }
    }
}