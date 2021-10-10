package life.genny.bridge.endpoints;

import java.net.URI;
import java.net.URISyntaxException;

import javax.annotation.security.PermitAll;
import javax.enterprise.event.Observes;
import javax.inject.Inject;
import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;
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

import org.apache.commons.lang3.StringUtils;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.eclipse.microprofile.jwt.JsonWebToken;
import org.jboss.logging.Logger;

import io.quarkus.oidc.IdToken;
import io.quarkus.oidc.RefreshToken;
import io.quarkus.runtime.ShutdownEvent;
import io.quarkus.runtime.StartupEvent;

@Path("/api")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class BridgeResource {

	private static final Logger log = Logger.getLogger(BridgeResource.class);
	
	private static final Integer MAX_URL_SIZE = 60;

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
	@Path("/events/init")
	@PermitAll
	public Response getInit(@QueryParam("url") final String url) {
		
		// Check validity of url requested
		if (StringUtils.isBlank(url)) {
			return Response.status(Status.BAD_REQUEST).build();
		} else 	if (url.length()> MAX_URL_SIZE) {
			return Response.status(Status.REQUEST_ENTITY_TOO_LARGE).build();
		}
		
		// Do a check on the username to help test logins
		String userName = "";
		 Object userNameObj = this.idToken.getClaim("preferred_username");

	        if (userNameObj != null) {
	            userName = userNameObj.toString();
	        }
		
/*		{
			realm: "internmatch",
			auth-server-url: "https://keycloak.gada.io/auth",
			ssl-required: "external",
			resource: "internmatch",
			credentials: {
			secret: "dc7d0960-2e1d-4a78-9eef-77678066dbd3"
			},
			policy-enforcer: { },
			vertx_url: "https://internmatch-interns.gada.io/frontend",
			api_url: "https://internmatch-interns.gada.io",
			url: "https://keycloak.gada.io/auth",
			clientId: "internmatch",
			ENV_GENNY_HOST: "https:8088",
			ENV_GENNY_INITURL: "https://internmatch-interns.gada.io",
			ENV_GENNY_BRIDGE_PORT: "8088",
			ENV_GENNY_BRIDGE_VERTEX: "/frontend",
			ENV_MEDIA_PROXY_URL: "https://internmatch-interns.gada.io/web/public",
			ENV_GENNY_BRIDGE_SERVICE: "/api/service",
			ENV_GENNY_BRIDGE_EVENTS: "/api/events",
			ENV_GOOGLE_MAPS_APIKEY: "AIzaSyBbZ0cT40AScAqSeTi_tEcnzAgeW1jTCUg",
			PRI_FAVICON: "https://internmatch-uploads.s3-ap-southeast-2.amazonaws.com/intermatch-favicon.ico",
			PRI_NAME: "INTERNMATCH",
			ENV_GOOGLE_MAPS_APIURL: "https://maps.googleapis.com/maps/api/js",
			ENV_UPPY_URL: "http://uppy-staging.genny.life",
			ENV_KEYCLOAK_REDIRECTURI: "https://keycloak.gada.io/auth",
			ENV_APPCENTER_ANDROID_SECRET: "NO_APPCENTER_ANDROID_SECRET",
			ENV_APPCENTER_IOS_SECRET: "NO_APPCENTER_IOS_SECRET",
			ENV_ANDROID_CODEPUSH_KEY: "NO_ANDROID_CODEPUSH_KEY",
			ENV_LAYOUT_PUBLICURL: "http://layout-cache-staging.genny.life",
			ENV_GUEST_USERNAME: "guest",
			ENV_GUEST_PASSWORD: "asdf1234",
			ENV_SIGNATURE_URL: "https://signatures.outcome-hub.com/signature",
			ENV_USE_CUSTOM_AUTH_LAYOUTS: "FALSE",
			ENV_LAYOUT_QUERY_DIRECTORY: "layouts/internmatch-new"
			}
			*/
		
		log.info("username: ["+userName+"] -> /api/events/init?url="+url);
		
		// Construct the return Json
		// TODO: This is currently not a dynamic multitenanted response until we can fetch the data from the cache
		String keycloakUrl = null;
		String realm = "genny";
		try {
			URI keycloakUri = new URI(keycloakAuthUrl);
			keycloakUrl = keycloakUri.getHost()+"/auth/";
			realm = keycloakUri.getPath().substring(keycloakUri.getPath().lastIndexOf('/') + 1);
		} catch (URISyntaxException e) {
			log.error("KeycloakAuthUrl is bad :"+keycloakAuthUrl);
		}
		
		JsonObjectBuilder responseJsonBuilder = Json.createObjectBuilder()
		.add("realm", realm)
		.add("auth-server-url", keycloakUrl)
		.add("ssl-required", "external")
		.add("resource", keycloakClientId)
		.add("public-client", true)
		.add("confidential-port", 0)

		.add("ENV_GOOGLE_MAPS_APIKEY", googleApiKey)
		.add("ENV_GOOGLE_MAPS_APIURL", "https://maps.googleapis.com/maps/api/js");
		
		if (!StringUtils.isBlank(userName)) {
			responseJsonBuilder.add("USERNAME", userName);
		}
		
		JsonObject responseJson = responseJsonBuilder.build();

        return Response.status(Status.OK).entity(responseJson.toString()).build();
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