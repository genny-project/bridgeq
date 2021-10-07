package life.genny.security.openid.connect.web.authentication;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;

import org.junit.jupiter.api.Test;

import com.gargoylesoftware.htmlunit.SilentCssErrorHandler;
import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.html.HtmlForm;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import com.gargoylesoftware.htmlunit.util.Cookie;

import io.quarkus.test.junit.QuarkusTest;

@QuarkusTest
public class CodeFlowTest {

	final String project="quarkus";
	final String username="alice";
	final String password="alice";
	
	@Test
	public void testCodeFlowNoConsent() throws IOException {
		try (final WebClient webClient = createWebClient()) {
			HtmlPage page = webClient.getPage("http://localhost:8081/index.html");
			String titleText = page.getTitleText();
			System.out.println(titleText);
	        assertTrue(titleText.contains("in to "+project));
	           
			HtmlForm loginForm = page.getForms().get(0);

			loginForm.getInputByName("username").setValueAttribute(username);
			loginForm.getInputByName("password").setValueAttribute(password);

			page = loginForm.getInputByName("login").click();

			titleText = page.getTitleText();
			System.out.println(titleText);
			assertEquals("Welcome to Your Quarkus Application", titleText);

			page = webClient.getPage("http://localhost:8081/index.html");
			
			titleText = page.getTitleText();
			System.out.println(titleText);

			assertEquals("Welcome to Your Quarkus Application", titleText,
					"A second request should not redirect and just re-authenticate the user");
		}
	}

	@Test
	public void testTokenTimeoutLogout() throws IOException, InterruptedException {
		try (final WebClient webClient = createWebClient()) {
			HtmlPage page = webClient.getPage("http://localhost:8081/index.html");

			assertTrue(page.getTitleText().contains("in to "+project));

			HtmlForm loginForm = page.getForms().get(0);

			loginForm.getInputByName("username").setValueAttribute(username);
			loginForm.getInputByName("password").setValueAttribute(password);

			page = loginForm.getInputByName("login").click();

			assertEquals("Welcome to Your Quarkus Application", page.getTitleText());

			Thread.sleep(5000);

			page = webClient.getPage("http://localhost:8081/index.html");

			Cookie sessionCookie = getSessionCookie(webClient);

			assertNull(sessionCookie);

			page = webClient.getPage("http://localhost:8081/index.html");

			assertTrue(page.getTitleText().contains("in to "+project));
		}
	}

	@Test
	public void testTokenInjection() throws IOException {
		try (final WebClient webClient = createWebClient()) {
			HtmlPage page = webClient.getPage("http://localhost:8081/index.html");

			assertTrue(page.getTitleText().contains("in to "+project));

			HtmlForm loginForm = page.getForms().get(0);

			loginForm.getInputByName("username").setValueAttribute(username);
			loginForm.getInputByName("password").setValueAttribute(password);

			page = loginForm.getInputByName("login").click();

			assertEquals("Welcome to Your Quarkus Application", page.getTitleText());

			page = webClient.getPage("http://localhost:8081/tokens");

			assertTrue(page.getBody().asText().contains("username"));
			assertTrue(page.getBody().asText().contains("scopes"));
			assertTrue(page.getBody().asText().contains("refresh_token: true"));
		}
	}

	private Cookie getSessionCookie(WebClient webClient) {
		return webClient.getCookieManager().getCookie("q_session");
	}

	private WebClient createWebClient() {
		WebClient webClient = new WebClient();

		webClient.setCssErrorHandler(new SilentCssErrorHandler());

		return webClient;
	}

}
