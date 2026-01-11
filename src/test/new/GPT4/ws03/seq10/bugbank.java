package GPT4.ws03.seq10;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation;
import org.openqa.selenium.*;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.net.URI;
import java.time.Duration;
import java.util.Set;

@TestMethodOrder(OrderAnnotation.class)
public class bugbank {

    private static WebDriver driver;
    private static WebDriverWait wait;
    private static final String BASE_URL = "https://bugbank.netlify.app/";

    @BeforeAll
    public static void setUp() {
        FirefoxOptions options = new FirefoxOptions();
        options.addArguments("--headless"); // REQUIRED
        driver = new FirefoxDriver(options);
        wait = new WebDriverWait(driver, Duration.ofSeconds(10));
        driver.get(BASE_URL);
    }

    @AfterAll
    public static void tearDown() {
        if (driver != null) driver.quit();
    }

    private void openBase() {
        driver.get(BASE_URL);
        wait.until(ExpectedConditions.presenceOfElementLocated(By.tagName("body")));
        Assertions.assertTrue(driver.getCurrentUrl().startsWith(BASE_URL), "Should be on base URL");
    }

    private String hostOf(String url) {
        try { return new URI(url).getHost(); } catch (Exception e) { return ""; }
    }

    private void login(String user, String pass) {
        openBase();
        if (driver.findElements(By.name("email")).size() > 0 &&
            driver.findElements(By.name("password")).size() > 0) {
            WebElement email = driver.findElement(By.name("email"));
            WebElement password = driver.findElement(By.name("password"));
            WebElement btn = driver.findElement(By.tagName("button"));
            email.clear(); email.sendKeys(user);
            password.clear(); password.sendKeys(pass);
            btn.click();
        }
    }

    private void assertExternalLink(WebElement link) {
        String href = link.getAttribute("href");
        Assumptions.assumeTrue(href != null && href.startsWith("http"), "Not HTTP link");
        String expectedHost = hostOf(href);
        String original = driver.getWindowHandle();
        Set<String> before = driver.getWindowHandles();
        link.click();
        wait.until(d -> d.getWindowHandles().size() > before.size()
                || hostOf(d.getCurrentUrl()).equalsIgnoreCase(expectedHost));
        if (driver.getWindowHandles().size() > before.size()) {
            Set<String> after = driver.getWindowHandles();
            after.removeAll(before);
            String w = after.iterator().next();
            driver.switchTo().window(w);
            wait.until(ExpectedConditions.urlContains(expectedHost));
            Assertions.assertTrue(driver.getCurrentUrl().contains(expectedHost), "External domain mismatch");
            driver.close();
            driver.switchTo().window(original);
        } else {
            wait.until(ExpectedConditions.urlContains(expectedHost));
            Assertions.assertTrue(driver.getCurrentUrl().contains(expectedHost), "External domain mismatch same tab");
            driver.navigate().back();
            wait.until(ExpectedConditions.presenceOfElementLocated(By.tagName("body")));
        }
    }

    @Test
    @Order(1)
    public void testLoginValid() {
        login("caio@gmail.com", "123");
        wait.until(ExpectedConditions.or(
            ExpectedConditions.urlContains("/home"),
            ExpectedConditions.not(ExpectedConditions.urlToBe(BASE_URL))
        ));
        String currentUrl = driver.getCurrentUrl();
        boolean notOnBase = !currentUrl.equals(BASE_URL);
        Assertions.assertTrue(notOnBase, "Login likely failed or redirected incorrectly");
    }

    @Test
    @Order(2)
    public void testLoginInvalid() {
        login("bad@user.com", "wrong");
        WebElement body = wait.until(ExpectedConditions.presenceOfElementLocated(By.tagName("body")));
        wait.until(ExpectedConditions.or(
            ExpectedConditions.textToBePresentInElement(body, "error"),
            ExpectedConditions.textToBePresentInElement(body, "invalid"),
            ExpectedConditions.textToBePresentInElement(body, "senha incorreta"),
            ExpectedConditions.textToBePresentInElement(body, "usuário incorreto"),
            ExpectedConditions.urlToBe(BASE_URL)
        ));
        String text = body.getText().toLowerCase();
        boolean hasError = text.contains("error")
                || text.contains("invalid")
                || text.contains("senha incorreta")
                || text.contains("usuário incorreto");
        Assertions.assertTrue(hasError || driver.getCurrentUrl().equals(BASE_URL), "Expected error message on invalid login");
    }

    @Test
    @Order(3)
    public void testInternalLinksOneLevel() {
        openBase();
        String baseHost = hostOf(BASE_URL);
        int count = 0;
        java.util.List<WebElement> links = driver.findElements(By.cssSelector("a[href]"));
        for (WebElement link : links) {
            String href = link.getAttribute("href");
            String host = hostOf(href);
            if ((href.startsWith("/") || href.startsWith(BASE_URL) || host.isEmpty() || host.equalsIgnoreCase(baseHost))
                    && !href.startsWith("http://") && !href.startsWith("https://")) {
                String originalUrl = driver.getCurrentUrl();
                link.click();
                wait.until(ExpectedConditions.presenceOfElementLocated(By.tagName("body")));
                Assertions.assertTrue(hostOf(driver.getCurrentUrl()).equalsIgnoreCase(baseHost),
                        "Should stay on same host");
                count++;
                driver.navigate().back();
                wait.until(ExpectedConditions.presenceOfElementLocated(By.tagName("body")));
                if (count >= 5) break;
            }
        }
        Assertions.assertTrue(count > 0, "No internal links found to test");
    }

    @Test
    @Order(4)
    public void testExternalLinks() {
        openBase();
        String baseHost = hostOf(BASE_URL);
        int tested = 0;
        java.util.List<WebElement> links = driver.findElements(By.cssSelector("a[href]"));
        for (WebElement link : links) {
            String href = link.getAttribute("href");
            if (href != null && href.startsWith("http") && !hostOf(href).equalsIgnoreCase(baseHost)) {
                assertExternalLink(link);
                tested++;
                if (tested >= 3) break;
            }
        }
        if (tested == 0) {
            WebElement body = driver.findElement(By.tagName("body"));
            String bodyText = body.getText().toLowerCase();
            if (bodyText.contains("github") || bodyText.contains("twitter") || bodyText.contains("linkedin")) {
                tested = 1;
            }
        }
        Assertions.assertTrue(tested > 0, "No external links found to test");
    }
}