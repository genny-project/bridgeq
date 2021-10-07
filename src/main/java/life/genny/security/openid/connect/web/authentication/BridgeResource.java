package life.genny.security.openid.connect.web.authentication;

import javax.enterprise.event.Observes;
import javax.inject.Inject;
import javax.transaction.Transactional;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.OPTIONS;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.eclipse.microprofile.jwt.JsonWebToken;
import org.jboss.logging.Logger;

import io.quarkus.runtime.ShutdownEvent;
import io.quarkus.runtime.StartupEvent;
import io.quarkus.security.identity.SecurityIdentity;

@Path("/api")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class BridgeResource {

	private static final Logger log = Logger.getLogger(BridgeResource.class);

	@ConfigProperty(name = "default.realm", defaultValue = "genny")
	String defaultRealm;


	@Inject
	JsonWebToken accessToken;

	@OPTIONS
	public Response opt() {
		return Response.ok().build();
	}

	@GET
	@Produces(MediaType.APPLICATION_JSON)
	@Path("/events/init")
	public Response getInit(@QueryParam("url") final String url) {
		String initJson = "Bridge init url = "+url;
	
		return Response.status(Status.OK).entity(initJson).build();
	}


	@Transactional
	void onStart(@Observes StartupEvent ev) {
		log.info("Bridge Endpoint starting");


	}

	@Transactional
	void onShutdown(@Observes ShutdownEvent ev) {
		log.info("Bridge Endpoint Shutting down");
	}
}