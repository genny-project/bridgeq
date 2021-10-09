package life.genny.security.openid.connect.web.authentication;

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
import javax.ws.rs.QueryParam;
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
import io.quarkus.security.identity.SecurityIdentity;

@Path("/api")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class BridgeResource {

	private static final Logger log = Logger.getLogger(BridgeResource.class);

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


	@OPTIONS
	public Response opt() {
		return Response.ok().build();
	}

	@GET
	@Produces(MediaType.APPLICATION_JSON)
	@Path("/events/init")
	@PermitAll
	public Response getInit(@QueryParam("url") final String url) {

        StringBuilder response = new StringBuilder().append("<html>")
                .append("<body>")
                .append("<ul>");

        Object userName = this.idToken.getClaim("preferred_username");

        if (userName != null) {
            response.append("<li>username: ").append(userName.toString()).append("</li>");
        }

        Object scopes = this.accessToken.getClaim("scope");

        if (scopes != null) {
            response.append("<li>scopes: ").append(scopes.toString()).append("</li>");
        }

        response.append("<li>refresh_token: ").append(refreshToken.getToken() != null).append("</li>");

        response.append("<li>Bridge init url = "+url+"</li>").append("</ul>").append("</body>").append("</html>").toString();

        return Response.status(Status.OK).entity(response).build();
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