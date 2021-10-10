package life.genny.bridge.endpoints;


import javax.annotation.security.PermitAll;
import javax.annotation.security.RolesAllowed;
import javax.enterprise.event.Observes;
import javax.inject.Inject;
import javax.transaction.Transactional;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.OPTIONS;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.eclipse.microprofile.jwt.JsonWebToken;
import org.jboss.logging.Logger;

import io.quarkus.oidc.IdToken;
import io.quarkus.oidc.RefreshToken;
import io.quarkus.runtime.ShutdownEvent;
import io.quarkus.runtime.StartupEvent;
import life.genny.bridge.models.GennyToken;

@Path("/api/service")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)

public class ServiceResource {

	private static final Logger log = Logger.getLogger(ServiceResource.class);

	@ConfigProperty(name = "default.realm", defaultValue = "genny")
	String defaultRealm;


    /**
     * Injection point for the ID Token issued by the OpenID Connect Provider
     */
    @Inject
    @IdToken
    JsonWebToken idToken;

    /**
     * Injection point for the Access Token issued by the OpenID Connect Provider
     */
    @Inject
    JsonWebToken accessToken;

    /**
     * Injection point for the Refresh Token issued by the OpenID Connect Provider
     */
    @Inject
    RefreshToken refreshToken;
    
	@ConfigProperty(name = "quarkus.oidc.auth-server-url", defaultValue = "http://localhost:8180/auth/realms/quarkus")
	String keycloakAuthUrl;

	@ConfigProperty(name = "quarkus.oidc.client-id", defaultValue = "frontend")
	String keycloakClientId;

	@ConfigProperty(name = "google.api.key")
	String googleApiKey;


	@OPTIONS
	public Response opt() {
		return Response.ok().build();
	}

	@GET
	@Produces(MediaType.APPLICATION_JSON)
	@Path("/commands")
	//@PermitAll
	public Response performService() {
		GennyToken gennyToken = new GennyToken(accessToken.getRawToken());
		if (!gennyToken.hasRole("admin")&& !gennyToken.hasRole("dev") && !gennyToken.hasRole("test")) {
			return Response.status(Status.FORBIDDEN).entity("Forbidden").build();
		}
		String initJson = "API Service Commands Call user roles "+gennyToken.getUserRoles();
		
		return Response.status(Status.OK).entity(initJson).build();
	}


	@Transactional
	void onStart(@Observes StartupEvent ev) {
		log.info("Service Endpoint starting: ");
		log.info("KeycloakAuthUrl : "+keycloakAuthUrl);
		log.info("Keycloak ClientId : "+keycloakClientId);

	}

	@Transactional
	void onShutdown(@Observes ShutdownEvent ev) {
		log.info("Service Endpoint Shutting down");
	}
}