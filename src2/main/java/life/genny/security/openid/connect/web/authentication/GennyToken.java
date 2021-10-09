package life.genny.security.openid.connect.web.authentication;


import java.io.IOException;
import java.io.Serializable;
import java.lang.invoke.MethodHandles;
import java.net.URI;
import java.net.URISyntaxException;
import java.security.Key;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.TimeZone;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.crypto.spec.SecretKeySpec;
import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.xml.bind.DatatypeConverter;

import org.apache.commons.codec.binary.Base64;
import org.eclipse.microprofile.jwt.JsonWebToken;
import org.jboss.logging.Logger;
import org.json.JSONException;
import org.json.JSONObject;

import com.fasterxml.jackson.core.JsonGenerationException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import io.jsonwebtoken.JwtBuilder;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.InvalidKeyException;


@RequestScoped
public class GennyToken implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private static final Logger log = Logger.getLogger(GennyToken.class);
	
	
	String code;
	String userCode;
	String userUUID;
	String token;
	Map<String, Object> adecodedTokenMap = null;
	String realm = null;
	Set<String> userRoles = new HashSet<String>();

	public GennyToken(final String token) {
		if ((token != null) && (!token.isEmpty())) {
			// Getting decoded token in Hash Map from QwandaUtils
			adecodedTokenMap = getJsonMap(token);
			if (adecodedTokenMap == null) {

				log.error("Token is not able to be decoded in GennyToken ..");
			} else {

				// Extracting realm name from iss value

				String realm = null;
				if (adecodedTokenMap.get("iss") != null) {
					String[] issArray = adecodedTokenMap.get("iss").toString().split("/");
					realm = issArray[issArray.length - 1];
				} else if (adecodedTokenMap.get("azp") != null) {
					realm = (adecodedTokenMap.get("azp").toString()); // clientid
				}
//				if ((realm.equals("alyson"))) {
//					String[] issArray = adecodedTokenMap.get("iss").toString().split("/");
//					realm = issArray[issArray.length-1];
//					//realm = (adecodedTokenMap.get("aud").toString()); // handle non Keycloak 6+
//				}

				// Adding realm name to the decoded token
				adecodedTokenMap.put("realm", realm);
				this.token = token;
				this.realm = realm;
				String uuid = adecodedTokenMap.get("sub").toString();
				String username = (String) adecodedTokenMap.get("preferred_username");
				String normalisedUsername = getNormalisedUsername(username);
				this.userUUID = "PER_" + this.getUuid().toUpperCase(); // normalisedUsername.toUpperCase();
				if ("service".equals(username)) {
					this.userCode = "PER_SERVICE";
				} else {
					this.userCode = userUUID; // "PER_" + normalisedUsername.toUpperCase();
												// //normalisedUsername.toUpperCase();
				}
				setupRoles();
			}

		} else {
			log.error("Token is null or zero length in GennyToken ..");
		}

	}

	public GennyToken(final String code, final String token) {

		this(token);
		this.code = code;
		if ("PER_SERVICE".equals(code)) {
			this.userCode = code;
		}
	}

	public GennyToken(final String code, final String id, final String issuer, final String subject, final long ttl,
			final String secret, final String realm, final String username, final String name, final String role) {

		this(code, id, issuer, subject, ttl, secret, realm, username, name, role,
				LocalDateTime.now().plusSeconds(24 * 60 * 60)); // 1 day expiry
	}

	public GennyToken(final String code, final String id, final String issuer, final String subject, final long ttl,
			final String secret, final String realm, final String username, final String name, final String role,
			final LocalDateTime expiryDateTime) {
		adecodedTokenMap = new HashMap<String, Object>();
		adecodedTokenMap.put("preferred_username", username);
		adecodedTokenMap.put("name", name);
		if (username.contains("@")) {
			adecodedTokenMap.put("email", username);
		} else {
			adecodedTokenMap.put("email", username + "@gmail.com");
		}
		String[] names = name.split(" ");
		adecodedTokenMap.put("given_name", names[0].trim());
		adecodedTokenMap.put("family_name", names[1].trim());
		adecodedTokenMap.put("jti", UUID.randomUUID().toString().substring(0, 20));
		adecodedTokenMap.put("sub", id);
		adecodedTokenMap.put("realm", realm);
		adecodedTokenMap.put("azp", realm);
		adecodedTokenMap.put("aud", realm);
		// adecodedTokenMap.put("realm_access", "{ \"roles\": [\"user\",\"" + role +
		// "\"] }");
		adecodedTokenMap.put("exp", expiryDateTime.atZone(ZoneId.of("UTC")).toEpochSecond());
		adecodedTokenMap.put("iat", LocalDateTime.now().atZone(ZoneId.of("UTC")).toEpochSecond());
		adecodedTokenMap.put("auth_time", LocalDateTime.now().atZone(ZoneId.of("UTC")).toEpochSecond());
		adecodedTokenMap.put("session_state", UUID.randomUUID().toString().substring(0, 32)); // TODO set size ot same
																								// as keycloak

		userRoles = new HashSet<String>();
//		  "realm_access": {
//		    "roles": [
//		      "test",
//		      "dev",
//		      "offline_access",
//		      "admin",
//		      "uma_authorization",
//		      "user",
//		      "supervisor"
//		    ]
//		  },

		ArrayJson rj = new ArrayJson();
		userRoles.add("user");
		rj.roles.add("user");
		String[] roles = role.split(",:;");
		for (String r : roles) {
			userRoles.add(r);
			rj.roles.add(r);
		}

		adecodedTokenMap.put("realm_access", rj);

		String jwtToken = null;

		jwtToken = createJwt(id, issuer, subject, ttl, secret, adecodedTokenMap);
		token = jwtToken;
		this.realm = realm;
		if ("service".equals(username)) {
			this.userCode = "PER_SERVICE";
		} else {
//		String normalisedUsername = QwandaUtils.getNormalisedUsername(id);
//		if (normalisedUsername.toUpperCase().startsWith("PER_")) {
//			this.userCode = normalisedUsername.toUpperCase();
//		} else {
			this.userCode = "PER_" + id.toUpperCase();
//		}
		}

		this.code = code;
		setupRoles();
	}

	public GennyToken(final String code, final String realm, final String username, final String name,
			final String role) {
		this(code, "ABBCD", "Genny Project", "Test JWT", 100000, "IamASecret", realm, username, name, role,
				LocalDateTime.now().plusSeconds(24 * 60 * 60));
	}

	public GennyToken(final String uuid, final String code, final String realm, final String username,
			final String name, final String role, LocalDateTime expiryDateTime) {
		this(code, uuid, "Genny Project", "Test JWT", 100000, "IamASecret", realm, username, name, role,
				expiryDateTime);
	}

	public GennyToken(final String code, final String realm, final String username, final String name,
			final String role, LocalDateTime expiryDateTime) {
		this(code, "ABBCD", "Genny Project", "Test JWT", 100000, "IamASecret", realm, username, name, role,
				expiryDateTime);
	}

	public String getToken() {
		return token;
	}

	public Map<String, Object> getAdecodedTokenMap() {
		return adecodedTokenMap;
	}

	public void setAdecodedTokenMap(Map<String, Object> adecodedTokenMap) {
		this.adecodedTokenMap = adecodedTokenMap;
	}

	private void setupRoles() {
		String realm_accessStr = "";
		if (adecodedTokenMap.get("realm_access") == null) {
			userRoles.add("user");
		} else {
			realm_accessStr = adecodedTokenMap.get("realm_access").toString();
			Pattern p = Pattern.compile("(?<=\\[)([^\\]]+)(?=\\])");
			Matcher m = p.matcher(realm_accessStr);

			if (m.find()) {
				String[] roles = m.group(1).split(",");
				for (String role : roles) {
					userRoles.add((String) role.trim());
				}
				;
			}
		}

	}

	public boolean hasRole(final String role) {
		return userRoles.contains(role);
	}

	@Override
	public String toString() {
		return getRealm() + ": " + getCode() + ": " + getUserCode() + ": " + this.userRoles;
	}

	public String getRealm() {
		return realm;
	}

	public String getString(final String key) {
		return (String) adecodedTokenMap.get(key);
	}

	public String getCode() {
		return code;
	}

	public String getSessionCode() {
		return getString("session_state");
	}

	public String getUsername() {
		return getString("preferred_username");
	}

	public String getKeycloakUrl() {
		String fullUrl = getString("iss");
		URI uri;
		try {
			uri = new URI(fullUrl);
			String domain = uri.getHost();
			String proto = uri.getScheme();
			Integer port = uri.getPort();
			return proto + "://" + domain + ":" + port;
		} catch (URISyntaxException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return "http://keycloak.genny.life";
	}

	public String getClientCode() {
		return getString("aud");
	}

	public String getEmail() {
		return getString("email");
	}

	/**
	 * @return the userCode
	 */
	public String getUserCode() {
		return userCode;
		// return "PER_"+this.userUUID.toUpperCase();
	}

	public String setUserCode(String userCode) {
		return this.userCode = userCode;
	}

	public String getUserUUID() {
		return userUUID;
	}


	public LocalDateTime getAuthDateTime() {
		Long auth_timestamp = ((Number) adecodedTokenMap.get("auth_time")).longValue();
		LocalDateTime authTime = LocalDateTime.ofInstant(Instant.ofEpochSecond(auth_timestamp),
				TimeZone.getDefault().toZoneId());
		return authTime;
	}


	public LocalDateTime getExpiryDateTime() {
		Long exp_timestamp = ((Number) adecodedTokenMap.get("exp")).longValue();
		LocalDateTime expTime = LocalDateTime.ofInstant(Instant.ofEpochSecond(exp_timestamp),
				TimeZone.getDefault().toZoneId());
		return expTime;
	}


	public OffsetDateTime getExpiryDateTimeInUTC() {

		Long exp_timestamp = ((Number) adecodedTokenMap.get("exp")).longValue();
		LocalDateTime expTime = LocalDateTime.ofInstant(Instant.ofEpochSecond(exp_timestamp),
				TimeZone.getDefault().toZoneId());
		ZonedDateTime ldtZoned = expTime.atZone(ZoneId.systemDefault());
		ZonedDateTime utcZoned = ldtZoned.withZoneSameInstant(ZoneId.of("UTC"));

		return utcZoned.toOffsetDateTime();
	}


	public Integer getSecondsUntilExpiry() {

		OffsetDateTime expiry = getExpiryDateTimeInUTC();
		LocalDateTime now = LocalDateTime.now(ZoneId.of("UTC"));
		Long diff = expiry.toEpochSecond() - now.toEpochSecond(ZoneOffset.UTC);
		return diff.intValue();
	}

	// JWT Issue DateTime

	public LocalDateTime getiatDateTime() {
		Long iat_timestamp = ((Number) adecodedTokenMap.get("iat")).longValue();
		LocalDateTime iatTime = LocalDateTime.ofInstant(Instant.ofEpochSecond(iat_timestamp),
				TimeZone.getDefault().toZoneId());
		return iatTime;
	}

	// Unique token id

	public String getUniqueId() {
		return (String) adecodedTokenMap.get("jti");
	}


	public String getUuid() {
		String uuid = null;

		try {
			uuid = (String) adecodedTokenMap.get("sub");
		} catch (Exception e) {
			log.info("Not a valid user");
		}

		return uuid;
	}

	public String getEmailUserCode() {
		String username = (String) adecodedTokenMap.get("preferred_username");
		String normalisedUsername = getNormalisedUsername(username);
		return "PER_" + normalisedUsername.toUpperCase();

	}


	public Boolean checkUserCode(String userCode) {
		if (getUserCode().equals(userCode)) {
			return true;
		}
		if (getEmailUserCode().equals(userCode)) {
			return true;
		}
		return false;

	}

	/**
	 * @return the userRoles
	 */
	public Set<String> getUserRoles() {
		return userRoles;
	}

	public String getRealmUserCode() {
		return getRealm() + "+" + getUserCode();
	}

	public String getNormalisedUsername(final String rawUsername) {
		if (rawUsername == null) {
			return null;
		}
		String username = rawUsername.replaceAll("\\&", "_AND_").replaceAll("@", "_AT_").replaceAll("\\.", "_DOT_")
				.replaceAll("\\+", "_PLUS_").toUpperCase();
		// remove bad characters
		username = username.replaceAll("[^a-zA-Z0-9_]", "");
		return username;

	}
	
	// Send the decoded Json token in the map
	public static Map<String, Object> getJsonMap(final String json) {
		final JSONObject jsonObj = getDecodedToken(json);
		return getJsonMap(jsonObj);
	}

	public static Map<String, Object> getJsonMap(final JSONObject jsonObj) {
		final String json = jsonObj.toString();
		Map<String, Object> map = new HashMap<String, Object>();
		try {
			final ObjectMapper mapper = new ObjectMapper();
			// convert JSON string to Map
			final TypeReference<HashMap<String, Object>> typeRef = new TypeReference<HashMap<String, Object>>() {
			};

			map = mapper.readValue(json, typeRef);

		} catch (final JsonGenerationException e) {
			e.printStackTrace();
		} catch (final JsonMappingException e) {
			e.printStackTrace();
		} catch (final IOException e) {
			e.printStackTrace();
		}

		return map;
	}

	// Decode the keycloak token string and send back in Json Format
	public static JSONObject getDecodedToken(final String bearerToken) {
		JSONObject jsonObj = null;
		String decodedJson = null;
		try {
			final String[] jwtToken = bearerToken.split("\\.");
			final Base64 decoder = new Base64(true);
			final byte[] decodedClaims = decoder.decode(jwtToken[1]);
			decodedJson = new String(decodedClaims);
			jsonObj = new JSONObject(decodedJson);
		} catch (final JSONException e1) {
			log.info("bearerToken=" + bearerToken + "  decodedJson=" + decodedJson + ":" + e1.getMessage());
		}
		return jsonObj;
	}

	public static String createJwt(String id, String issuer, String subject, long ttlMillis, String apiSecret,
			Map<String, Object> claims) {

		// The JWT signature algorithm we will be using to sign the token
		SignatureAlgorithm signatureAlgorithm = SignatureAlgorithm.HS256;
		String aud = issuer;
		if (claims.containsKey("aud")) {
			aud = (String) claims.get("aud");
			claims.remove("aud");
		}
		long nowMillis = System.currentTimeMillis();
		Date now = new Date(nowMillis);

		// We will sign our JWT with our ApiKey secret
		byte[] apiKeySecretBytes = DatatypeConverter.parseBase64Binary(apiSecret);

	//	Map<String, Object> claims2 = new HashMap<String, Object>();
	//	claims2.put("preferred_username", (String) claims.get("preferred_username"));

//		claims2.put("name", name.getBytes("UTF-8"));
//		claims2.put("realm", realm);
//		claims2.put("azp", realm);
//		claims2.put("aud", realm);
//		claims2.put("realm_access", "[user," + role + "]");
//		claims2.put("exp", expiryDateTime.atZone(ZoneId.of("UTC")).toEpochSecond());
//		claims2.put("iat", LocalDateTime.now().atZone(ZoneId.of("UTC")).toEpochSecond());
//		claims2.put("auth_time", LocalDateTime.now().atZone(ZoneId.of("UTC")).toEpochSecond());
//		claims2.put("session_state", UUID.randomUUID().toString().substring(0, 32).getBytes("UTF-8")); // TODO set size ot same

//	    SecretKey key = MacProvider.generateKey(SignatureAlgorithm.HS256);
//	    String base64Encoded = TextCodec.BASE64.encode(key.getEncoded());

		// Let's set the JWT Claims
		JwtBuilder builder = Jwts.builder().setId(id).setIssuedAt(now).setSubject(subject).setIssuer(issuer)
				.setAudience(aud).setClaims(claims);
		// .signWith(signatureAlgorithm, signingKey);

		Key key = null;

		try {
			key = new SecretKeySpec(apiKeySecretBytes, signatureAlgorithm.getJcaName());
//	key  = Keys.secretKeyFor(SignatureAlgorithm.HS256);
			builder.signWith(SignatureAlgorithm.HS256, key);
		} catch (Exception e) {
			try {
				key = new SecretKeySpec(apiKeySecretBytes, signatureAlgorithm.getJcaName());
				builder.signWith(SignatureAlgorithm.HS256, key);
			} catch (Exception e1) {
// TODO Auto-generated catch block
				try {
					Key signingKey = new SecretKeySpec(apiKeySecretBytes, signatureAlgorithm.getJcaName());
					builder.signWith(signatureAlgorithm, signingKey);
				} catch (InvalidKeyException e2) {
//log.error("Cannot creating key foor JWT");
				}
			}
		}

		// if it has been specified, let's add the expiration
		if (ttlMillis >= 0) {
			long expMillis = nowMillis + ttlMillis;
			Date exp = new Date(expMillis);
			builder.setExpiration(exp);
		}

		// Builds the JWT and serializes it to a compact, URL-safe string
		return builder.compact();
	}
}