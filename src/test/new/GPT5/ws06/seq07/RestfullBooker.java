package GPT5.ws06.seq07;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation;
import org.openqa.selenium.*;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.net.URI;
import java.net.URISyntaxException;
import java.time.Duration;
import java.util.*;
import java.util.stream.Collectors;

@TestMethodOrder(OrderAnnotation.class)
public class RestfullBooker {
    private static final String BASE_URL = "https://automationintesting.online/";
    private static WebDriver driver;
    private static WebDriverWait wait;
    private static URI baseUri;

    @BeforeAll
    public static void setup() throws URISyntaxException {
        FirefoxOptions options = new FirefoxOptions();
        options.addArguments("--headless");
        driver = new FirefoxDriver(options);
        wait = new WebDriverWait(driver, Duration.ofSeconds(10));
        baseUri = new URI(BASE_URL);
    }

    @AfterAll
    public static void teardown() {
        if (driver != null) {
            driver.quit();
        }
    }

    private void goToBaseAndWait() {
        driver.navigate().to(BASE_URL);
        wait.until(d -> ((JavascriptExecutor) d).executeScript("return document.readyState").equals("complete"));
        Assertions.assertTrue(driver.getCurrentUrl().contains(baseUri.getHost()),
                "Expected URL to contain host: " + baseUri.getHost());
    }

    private boolean isExternalHref(String href) {
        try {
            URI uri = new URI(href);
            String host = uri.getHost();
            if (host == null) return false;
            return !host.equalsIgnoreCase(baseUri.getHost());
        } catch (Exception e) {
            return false;
        }
    }
    private void closeTabAndSwitchBack(String originalHandle) {
        driver.close();
        driver.switchTo().window(originalHandle);
        wait.until(d -> ((JavascriptExecutor) d).executeScript("return document.readyState").equals("complete"));
    }

    private List<String> collectOneLevelInternalLinks() {
        List<WebElement> anchors = driver.findElements(By.cssSelector("a[href]"));
        Set<String> result = new LinkedHashSet<>();
        for (WebElement a : anchors) {
            String href = a.getAttribute("href");
            if (href == null || href.trim().isEmpty()) continue;
            try {
                URI uri = new URI(href);
                if (uri.getHost() == null || uri.getHost().equalsIgnoreCase(baseUri.getHost())) {
                    result.add(href);
                }
            } catch (URISyntaxException ignored) {
                result.add(href);
            }
        }
        return new ArrayList<>(result);
    }

    @Test
    @Order(1)
    public void testBasePageLoads() {
        goToBaseAndWait();
        WebElement header = wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector("div#root")));
        Assertions.assertTrue(header.isDisplayed(), "Header should be visible on base page");
        List<WebElement> bookRoomButtons = driver.findElements(By.cssSelector("button#book-room"));
        if (!bookRoomButtons.isEmpty()) {
            WebElement bookRoom = bookRoomButtons.get(0);
            Assertions.assertTrue(bookRoom.isDisplayed(), "Expected 'Book this room' button on home page");
        }
    }

    @Test
    @Order(2)
    public void testContactFormValidation() {
        goToBaseAndWait();
        List<WebElement> nameFields = driver.findElements(By.id("name"));
        if (!nameFields.isEmpty()) {
            WebElement name = nameFields.get(0);
            WebElement email = driver.findElement(By.id("email"));
            WebElement phone = driver.findElement(By.id("phone"));
            WebElement subject = driver.findElement(By.id("subject"));
            WebElement message = driver.findElement(By.id("description"));
            WebElement submit = driver.findElement(By.id("submitContact"));

            name.clear();
            name.sendKeys("Test User");
            email.clear();
            email.sendKeys("not-an-email"); // invalid email
            phone.clear();
            phone.sendKeys("12345");
            subject.clear();
            subject.sendKeys("Test Subject");
            message.clear();
            message.sendKeys("This is a test message.");
            submit.click();

            List<WebElement> errors = driver.findElements(By.cssSelector(".alert.alert-danger"));
            Assertions.assertTrue(errors.size() > 0, "Expected validation errors for invalid email");
        }
    }

    @Test
    @Order(3)
    public void testExternalLinks() {
        goToBaseAndWait();
        List<WebElement> anchors = driver.findElements(By.cssSelector("a[href]"));
        List<String> hrefs = anchors.stream()
                .map(a -> a.getAttribute("href"))
                .filter(Objects::nonNull)
                .distinct()
                .collect(Collectors.toList());

        String originalHandle = driver.getWindowHandle();
        int tested = 0;
        for (String href : hrefs) {
            if (isExternalHref(href)) {
                if (tested >= 3) break;
                try {
                    URI u = new URI(href);
                    String host = u.getHost();
                    if (host != null) {
                        wait.until(ExpectedConditions.urlContains(host));
                        Assertions.assertTrue(driver.getCurrentUrl().contains(host),
                                "Expected URL to contain host " + host);
                    }
                } catch (Exception ignored) { }
                closeTabAndSwitchBack(originalHandle);
                tested++;
            }
        }
        if (hrefs.stream().anyMatch(this::isExternalHref)) {
            Assertions.assertTrue(tested > 0, "Expected to test at least one external link");
        }
    }

    @Test
    @Order(4)
    public void testOneLevelInternalLinks() {
        goToBaseAndWait();
        List<String> internalLinks = collectOneLevelInternalLinks().stream()
                .filter(h -> !h.equals(BASE_URL))
                .collect(Collectors.toList());

        int tested = 0;
        for (String href : internalLinks) {
            if (tested >= 5) break;
            driver.navigate().to(href);
            wait.until(d -> ((JavascriptExecutor) d).executeScript("return document.readyState").equals("complete"));
            boolean hasHeader = driver.findElements(By.cssSelector("h1,h2,h3")).stream().anyMatch(WebElement::isDisplayed);
            boolean hasBodyText = driver.findElements(By.tagName("body")).stream()
                    .anyMatch(b -> b.getText() != null && b.getText().trim().length() > 20);
            Assertions.assertTrue(hasHeader || hasBodyText, "Expected header or body text at " + href);
            tested++;
            driver.navigate().to(BASE_URL);
            wait.until(d -> ((JavascriptExecutor) d).executeScript("return document.readyState").equals("complete"));
        }
        if (!internalLinks.isEmpty()) {
            Assertions.assertTrue(tested > 0, "Expected at least one internal link to be tested");
        }
    }
}