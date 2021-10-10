package life.genny.bridge.endpoints.websocket;


import org.eclipse.microprofile.reactive.messaging.Incoming;

import io.quarkus.logging.Log;

import javax.enterprise.context.ApplicationScoped;
import javax.ws.rs.GET;
import javax.ws.rs.Path;

@Path("/frontend")
@ApplicationScoped
public class FrontendWebsocketResource {
//    private double sum;

//    @GET
//    // expose the sum of the collected costs
//    public synchronized double getCosts() {
//        return sum;
//    }

    @Incoming("frontend")
    // consume data from frontend channel
    synchronized void consume(String incomingData) {
       Log.info(incomingData);
    }
}