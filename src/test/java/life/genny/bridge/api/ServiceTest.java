package life.genny.bridge.api;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;

import org.junit.jupiter.api.Test;

import com.gargoylesoftware.htmlunit.ElementNotFoundException;
import com.gargoylesoftware.htmlunit.SilentCssErrorHandler;
import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.html.HtmlForm;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import com.gargoylesoftware.htmlunit.util.Cookie;

import io.quarkus.test.junit.QuarkusTest;

@QuarkusTest
public class ServiceTest {

    @Test
    public void testCodeFlowNoConsent() throws IOException {
        try (final WebClient webClient = createWebClient()) {
            HtmlPage page = webClient.getPage("http://localhost:8081/api/service/commands");
            String titleText = page.getTitleText();
            assertEquals("Sign in to quarkus", titleText);

            HtmlForm loginForm = page.getForms().get(0);

            loginForm.getInputByName("username").setValueAttribute("alice");
            loginForm.getInputByName("password").setValueAttribute("alice");

            try {
				page = loginForm.getInputByName("login").click();
				assertEquals(true,false,"The alice login into /api/service/commands should fail because alice is not an admin,test,or dev role");
			} catch (com.gargoylesoftware.htmlunit.FailingHttpStatusCodeException e) {
				assertEquals("403 Forbidden for http://localhost:8081/api/service/commands", e.getMessage());
			}

            page = webClient.getPage("http://localhost:8081/logout");
            // Now try as an admin
            page = webClient.getPage("http://localhost:8081/api/service/commands");
            titleText = page.getTitleText();
            
            assertEquals("Sign in to quarkus", titleText);
            loginForm = page.getForms().get(0);

            loginForm.getInputByName("username").setValueAttribute("admin");
            loginForm.getInputByName("password").setValueAttribute("admin");

//            try {
//				page = loginForm.getInputByName("login").click();
//			} catch (Exception e) {
//				titleText = page.getTitleText();
//		           assertEquals("Welcome to Your Quarkus Application", titleText,
//		                    "Admin user should not be forbidden to access /api/service/commands");
//			}
//           titleText = page.getTitleText();
//           assertEquals("Welcome to Your Quarkus Application", titleText);
//
//
//            page = webClient.getPage("http://localhost:8081/index.html");
//
//            titleText = page.getTitleText();
//            assertEquals("Welcome to Your Quarkus Application", titleText,
//                    "A second request should not redirect and just re-authenticate the user");
        }
    }

//    @Test
//    public void testTokenTimeoutLogout() throws IOException, InterruptedException {
//        try (final WebClient webClient = createWebClient()) {
//            HtmlPage page = webClient.getPage("http://localhost:8081/index.html");
//
//            assertEquals("Sign in to quarkus", page.getTitleText());
//
//            HtmlForm loginForm = page.getForms().get(0);
//
//            loginForm.getInputByName("username").setValueAttribute("alice");
//            loginForm.getInputByName("password").setValueAttribute("alice");
//
//            page = loginForm.getInputByName("login").click();
//
//            assertEquals("Welcome to Your Quarkus Application", page.getTitleText());
//
//            Thread.sleep(5000);
//
//            page = webClient.getPage("http://localhost:8081/index.html");
//
//            Cookie sessionCookie = getSessionCookie(webClient);
//
//            assertNull(sessionCookie);
//
//            page = webClient.getPage("http://localhost:8081/index.html");
//
//            assertEquals("Sign in to quarkus", page.getTitleText());
//        }
//    }
//
//    @Test
//    public void testTokenInjection() throws IOException {
//        try (final WebClient webClient = createWebClient()) {
//            HtmlPage page = webClient.getPage("http://localhost:8081/index.html");
//
//            assertEquals("Sign in to quarkus", page.getTitleText());
//
//            HtmlForm loginForm = page.getForms().get(0);
//
//            loginForm.getInputByName("username").setValueAttribute("alice");
//            loginForm.getInputByName("password").setValueAttribute("alice");
//
//            page = loginForm.getInputByName("login").click();
//
//            assertEquals("Welcome to Your Quarkus Application", page.getTitleText());
//
//            page = webClient.getPage("http://localhost:8081/tokens");
//
//            assertTrue(page.getBody().asText().contains("username"));
//            assertTrue(page.getBody().asText().contains("scopes"));
//            assertTrue(page.getBody().asText().contains("refresh_token: true"));
//        }
//    }

    private Cookie getSessionCookie(WebClient webClient) {
        return webClient.getCookieManager().getCookie("q_session");
    }

    private WebClient createWebClient() {
        WebClient webClient = new WebClient();

        webClient.setCssErrorHandler(new SilentCssErrorHandler());

        return webClient;
    }

}
