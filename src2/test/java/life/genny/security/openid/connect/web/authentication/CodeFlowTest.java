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
//	final String username="jdoe";
//	final String password="jdoe";
	final String username="alice";
	final String password="asdf1234";
//	final String username="bob";
//	final String password="bob";
	final String adminUsername="alice";
	final String adminPassword="alice";

	
	@Test
	public void testCodeFlowNoConsent() throws IOException {
		try (final WebClient webClient = createWebClient()) {
			HtmlPage page = webClient.getPage("http://localhost:8081/index.html");
			String titleText = page.getTitleText();
			System.out.println(titleText);
	        assertEquals("Sign in to quarkus", titleText,
					"Should say 'Sign in to quarkus'");
	           
			HtmlForm loginForm = page.getForms().get(0);

			loginForm.getInputByName("username").setValueAttribute(username);
			loginForm.getInputByName("password").setValueAttribute(password);
			System.out.println("test login with "+username);
			page = loginForm.getInputByName("login").click();

			titleText = page.getTitleText();
			System.out.println(titleText);
			   assertEquals("Bridge", titleText,
						"Should say 'Bridge'");

			page = webClient.getPage("http://localhost:8081/index.html");
			
			titleText = page.getTitleText();
			System.out.println(titleText);

			assertEquals("Bridge", titleText,
					"A second request should not redirect and just re-authenticate the user");
		}
	}

//	@Test
//	public void testTokenTimeoutLogout() throws IOException, InterruptedException {
//		try (final WebClient webClient = createWebClient()) {
//			HtmlPage page = webClient.getPage("http://localhost:8081/index.html");
//
//			assertTrue(page.getTitleText().contains("Bridge"));
//
//			HtmlForm loginForm = page.getForms().get(0);
//
//			loginForm.getInputByName("username").setValueAttribute(username);
//			loginForm.getInputByName("password").setValueAttribute(password);
//
//			page = loginForm.getInputByName("login").click();
//
//			assertEquals("Bridge", page.getTitleText());
//
//			Thread.sleep(5000);
//
//			page = webClient.getPage("http://localhost:8081/index.html");
//
//			Cookie sessionCookie = getSessionCookie(webClient);
//
//			assertNull(sessionCookie);
//
//			page = webClient.getPage("http://localhost:8081/index.html");
//
//			assertTrue(page.getTitleText().contains("Bridge"));
//		}
//	}

//	@Test
//	public void testTokenInjection() throws IOException {
//		try (final WebClient webClient = createWebClient()) {
//			HtmlPage page = webClient.getPage("http://localhost:8081/index.html");
//
//			assertTrue(page.getTitleText().contains("Bridge"));
//
//			HtmlForm loginForm = page.getForms().get(0);
//
//			loginForm.getInputByName("username").setValueAttribute(username);
//			loginForm.getInputByName("password").setValueAttribute(password);
//
//			page = loginForm.getInputByName("login").click();
//
//			assertEquals("Bridge", page.getTitleText());
//
//			page = webClient.getPage("http://localhost:8081/tokens");
//
//			assertTrue(page.getBody().asText().contains("username"));
//			assertTrue(page.getBody().asText().contains("scopes"));
//			assertTrue(page.getBody().asText().contains("refresh_token: true"));
//		}
//	}

	private Cookie getSessionCookie(WebClient webClient) {
		return webClient.getCookieManager().getCookie("q_session");
	}

	private WebClient createWebClient() {
		WebClient webClient = new WebClient();

		webClient.setCssErrorHandler(new SilentCssErrorHandler());

		return webClient;
	}

}
