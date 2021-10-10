package life.genny.bridge.endpoints;


import javax.enterprise.event.Observes;
import javax.inject.Inject;
import javax.transaction.Transactional;
import javax.ws.rs.Consumes;
import javax.ws.rs.OPTIONS;
import javax.ws.rs.POST;
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
import io.vertx.core.eventbus.DeliveryOptions;
import io.vertx.core.json.JsonObject;
import life.genny.bridge.models.GennyToken;
import life.genny.qwanda.Answer;
import life.genny.qwanda.message.QDataAnswerMessage;
import life.genny.qwandautils.JsonUtils;

@Path("/api/service")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)

public class ServiceResource {

	private static final Logger log = Logger.getLogger(ServiceResource.class);

	@ConfigProperty(name = "default.realm", defaultValue = "genny")
	String defaultRealm;
	
//	@Inject
//	Producer producer;



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

	@POST
	@Produces(MediaType.APPLICATION_JSON)
	@Path("/commands")
	public Response performService(@QueryParam("token") String token, @QueryParam("channel") String channel,@QueryParam("singleSession") Boolean singleSession, JsonObject jsonBody) {
		GennyToken userToken = new GennyToken(accessToken.getRawToken());
		if (!userToken.hasRole("admin")&& !userToken.hasRole("dev") && !userToken.hasRole("test")) {
			return Response.status(Status.FORBIDDEN).entity("Forbidden").build();
		}
		String initJson = "API Service Commands Call user roles "+userToken.getUserRoles();
		
	    log.info("Service Command Call! channel:"+channel+", singleSession:"+(singleSession?"TRUE":"FALSE"));
	    
	    // Add the token to the incoming message
	    jsonBody.put("token", accessToken.getRawToken());

//         final DeliveryOptions options = new DeliveryOptions();
//        options.addHeader("Authorization", "Bearer " + accessToken.getRawToken());
//
//        if ("EVT_MSG".equals(jsonBody.getString("msg_type")) || "events".equals(channel) || "event".equals(channel)) {
//          log.info("EVT API POST   >> EVENT-BUS EVENT:");
//          
//     //     producer.getToEvents().send(jsonBody.toString());
//
//        } else if ( "webcmds".equals(channel)) {
//          log.info("WEBCMD API POST   >> WEB CMDS :" + jsonBody);
//        //  EBCHandlers.sendToClientSessions(userToken, jsonBody, false);
//        } else if ("webdata".equals(channel)) {
//          log.info("WEBDATA API POST   >> WEB DATA :" + jsonBody);
//        //   EBCHandlers.sendToClientSessions(userToken, jsonBody, singleSession);
//
//        } else if (jsonBody.getString("msg_type").equals("CMD_MSG") || "cmds".equals(channel)) {
//          log.info("CMD API POST   >> EVENT-BUS CMD  :" + jsonBody);
//       //    producer.getToCmds().send(jsonBody.toString());
//        } else if (jsonBody.getString("msg_type").equals("MSG_MESSAGE") || "messages".equals(channel)) {
//          log.info("MESSAGES API POST   >> EVENT-BUS MSG DATA :");
//       //   producer.getToMessages().send(jsonBody.toString());
//        } else if ("DATA_MSG".equals(jsonBody.getString("msg_type")) || "answer".equals(channel)) {
//            log.info("ANSWER API POST   >> EVENT-BUS MSG ANSWER :");
//            QDataAnswerMessage msg = JsonUtils.fromJson(jsonBody.toString(), QDataAnswerMessage.class);
//            Answer ans = msg.getItems()[0]; // TODO assume at least one answer
//				JsonObject json = new JsonObject(JsonUtils.toJson(ans));
//				json.put("token", accessToken.getRawToken());
//            log.info("Answer Message:<<"+json.toString()+">>");
//        //    producer.getToAnswer().send(json.toString());
//
//        } else if (jsonBody.getString("msg_type").equals("DATA_MSG") || "data".equals(channel)) {
//          log.info("CMD API POST   >> EVENT-BUS DATA :");
//          log.error("we got into DATA_MSG sending to data");
//          if ("Rule".equals(jsonBody.getString("data_type"))) {
//            log.info("INCOMING RULE !");
//          }
//       //   producer.getToData().send(jsonBody.toString());
//        }
 		
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