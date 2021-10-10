package life.genny.bridge.endpoints;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Properties;

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
import life.genny.qwandautils.GitUtils;

@Path("/version")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class VersionResource {

	private static final Logger log = Logger.getLogger(VersionResource.class);
	
	public static final String GIT_VERSION_PROPERTIES = "GitVersion.properties";
	public static final String PROJECT_DEPENDENCIES = "project_dependencies";

	
	@OPTIONS
	public Response opt() {
		return Response.ok().build();
	}

	@GET
	@Produces(MediaType.APPLICATION_JSON)
	@Path("/")
	@PermitAll
	public Response getVersion() {
		Properties properties = new Properties();
		String versionString = "";
		try {
			properties.load(Thread.currentThread().getContextClassLoader().getResource(GIT_VERSION_PROPERTIES)
					.openStream());
			String projectDependencies = properties.getProperty(PROJECT_DEPENDENCIES);
			versionString = GitUtils.getGitVersionString(projectDependencies);
		} catch (IOException e) {
			log.error("Error reading GitVersion.properties", e);
			return Response.status(Status.INTERNAL_SERVER_ERROR).build();
		}
	
        return Response.status(Status.OK).entity(versionString).build();
	}


	@Transactional
	void onStart(@Observes StartupEvent ev) {
		log.info("Version Endpoint starting");


	}

	@Transactional
	void onShutdown(@Observes ShutdownEvent ev) {
		log.info("Version Endpoint Shutting down");
	}
}