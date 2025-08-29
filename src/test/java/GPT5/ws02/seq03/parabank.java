package GPT5.ws02.seq03;

import org.junit.jupiter.api.*;
import org.openqa.selenium.*;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.support.ui.ExpectedCondition;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;
import java.util.*;
import java.util.stream.Collectors;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class ParaBankHeadlessTest {

    private static WebDriver driver;
    private static WebDriverWait wait;

    private static final String BASE_URL = "https://parabank.parasoft.com/parabank/index.htm";
    private static final String USERNAME = "caio@gmail.com";
    private static final String PASSWORD = "123";

    @BeforeAll
    public static void beforeAll() {
        FirefoxOptions options = new FirefoxOptions();
        options.addArguments("--headless");
        driver = new FirefoxDriver(options);
        wait = new WebDriverWait(driver, Duration.ofSeconds(10));
        driver.manage().window().setSize(new Dimension(1400, 1000));
    }

    @AfterAll
    public static void afterAll() {
        if (driver != null) driver.quit();
    }

    @BeforeEach
    public void goHome() {
        driver.get(BASE_URL);
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("rightPanel")));
    }

    // ----------------- Helpers -----------------

    private boolean isLoggedIn() {
        return driver.findElements(By.linkText("Log Out")).size() > 0
                || driver.findElements(By.xpath("//h1[contains(.,'Accounts Overview')]")).size() > 0;
    }

    private void logoutIfLoggedIn() {
        if (driver.findElements(By.linkText("Log Out")).size() > 0) {
            WebElement logout = wait.until(ExpectedConditions.elementToBeClickable(By.linkText("Log Out")));
            logout.click();
            wait.until(ExpectedConditions.urlContains("/index.htm"));
        }
    }

    private void login(String user, String pass) {
        WebElement username = wait.until(ExpectedConditions.visibilityOfElementLocated(By.name("username")));
        WebElement password = driver.findElement(By.name("password"));
        WebElement loginBtn = driver.findElement(By.cssSelector("input[type='submit'][value='Log In']"));
        username.clear();
        username.sendKeys(user);
        password.clear();
        password.sendKeys(pass);
        wait.until(ExpectedConditions.elementToBeClickable(loginBtn)).click();
    }

    private void assertUrlContains(String fragment) {
        wait.until(ExpectedConditions.urlContains(fragment));
        Assertions.assertTrue(driver.getCurrentUrl().contains(fragment), "URL should contain: " + fragment);
    }

    private void switchToNewWindowIfAnyAndAssertDomain(String expectedDomainFragment) {
        String original = driver.getWindowHandle();
        Set<String> before = driver.getWindowHandles();
        try {
            wait.until((ExpectedCondition<Boolean>) d -> driver.getWindowHandles().size() > before.size());
        } catch (TimeoutException ignored) {}
        Set<String> after = driver.getWindowHandles();
        if (after.size() > before.size()) {
            after.removeAll(before);
            String newHandle = after.iterator().next();
            driver.switchTo().window(newHandle);
            wait.until(ExpectedConditions.urlContains(expectedDomainFragment));
            Assertions.assertTrue(driver.getCurrentUrl().toLowerCase().contains(expectedDomainFragment),
                    "Expected external URL to contain " + expectedDomainFragment);
            driver.close();
            driver.switchTo().window(original);
        } else {
            // Same tab navigation
            wait.until(ExpectedConditions.urlContains(expectedDomainFragment));
            Assertions.assertTrue(driver.getCurrentUrl().toLowerCase().contains(expectedDomainFragment),
                    "Expected external URL to contain " + expectedDomainFragment);
            driver.navigate().back();
            wait.until(ExpectedConditions.urlContains("/parabank"));
        }
    }

    // ----------------- Tests -----------------

    @Test
    @Order(1)
    public void testHomePageLoads() {
        Assertions.assertTrue(driver.getTitle().toLowerCase().contains("parabank"),
                "Title should mention ParaBank");
        Assertions.assertTrue(driver.findElement(By.id("rightPanel")).isDisplayed(),
                "Right panel should be visible");
        Assertions.assertTrue(driver.findElement(By.id("leftPanel")).isDisplayed(),
                "Left panel (Account Services / Login) should be visible");
    }

    @Test
    @Order(2)
    public void testInvalidLoginShowsError() {
        login("invalid_user", "wrong_password");
        WebElement error = wait.until(ExpectedConditions.visibilityOfElementLocated(
                By.cssSelector("#rightPanel .error, #rightPanel p.error")));
        Assertions.assertTrue(error.getText().length() > 0, "An error message should be displayed for invalid login.");
        Assertions.assertTrue(driver.getCurrentUrl().contains("/parabank"),
                "Stays on ParaBank domain after invalid login.");
    }

    @Test
    @Order(3)
    public void testLoginWithProvidedCredentialsOrGracefulError() {
        login(USERNAME, PASSWORD);

        // Either we are successfully logged in (Accounts Overview) or we see an error.
        boolean overview = driver.findElements(By.xpath("//h1[contains(.,'Accounts Overview')]")).size() > 0;
        boolean error = driver.findElements(By.cssSelector("#rightPanel .error, #rightPanel p.error")).size() > 0;

        Assertions.assertTrue(overview || error, "Expected either successful login (Accounts Overview) or an error message.");
        if (overview) {
            WebElement hdr = wait.until(ExpectedConditions.visibilityOfElementLocated(
                    By.xpath("//h1[contains(.,'Accounts Overview')]")));
            Assertions.assertTrue(hdr.isDisplayed(), "Accounts Overview header should be visible.");
            logoutIfLoggedIn();
        }
    }

    @Test
    @Order(4)
    public void testTopNavigationInternalPagesOneLevel() {
        Map<String, String> nav = new LinkedHashMap<>();
        nav.put("About Us", "about.htm");
        nav.put("Services", "services.htm");
        nav.put("Products", "products.htm");
        nav.put("Locations", "locations.htm");
        nav.put("Admin Page", "admin.htm");

        for (Map.Entry<String, String> e : nav.entrySet()) {
            WebElement link = wait.until(ExpectedConditions.elementToBeClickable(By.linkText(e.getKey())));
            link.click();
            assertUrlContains(e.getValue());
            // basic header presence check
            Assertions.assertTrue(driver.findElements(By.cssSelector("#rightPanel h1, #rightPanel h2")).size() > 0,
                    "Expected a header on " + e.getKey() + " page.");
            driver.navigate().back();
            wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("rightPanel")));
        }
    }

    @Test
    @Order(5)
    public void testExternalLinksFromHomeFooterOrContent() {
        // Collect potential external links (one level only)
        List<WebElement> links = driver.findElements(By.cssSelector("a[href]"));
        // Prefer unique domains and limit to a few to avoid flakiness
        List<String> domainsToCheck = Arrays.asList("parasoft.com", "twitter.com", "facebook.com", "linkedin.com", "youtube.com");
        Map<String, WebElement> chosen = new LinkedHashMap<>();
        for (WebElement a : links) {
            String href = a.getAttribute("href");
            if (href == null) continue;
            for (String domain : domainsToCheck) {
                if (href.toLowerCase().contains(domain) && !chosen.containsKey(domain)) {
                    chosen.put(domain, a);
                }
            }
        }

        for (Map.Entry<String, WebElement> entry : chosen.entrySet()) {
            String domain = entry.getKey();
            WebElement a = entry.getValue();
            int beforeCount = driver.getWindowHandles().size();
            wait.until(ExpectedConditions.elementToBeClickable(a)).click();
            try {
                wait.until(d -> driver.getWindowHandles().size() != beforeCount || !driver.getCurrentUrl().contains("/parabank"));
            } catch (TimeoutException ignored) {}
            switchToNewWindowIfAnyAndAssertDomain(domain);
        }
    }

    @Test
    @Order(6)
    public void testAccountServicesPagesWhenLoggedIn() {
        // Attempt login; skip if not possible
        login(USERNAME, PASSWORD);
        boolean loggedIn = isLoggedIn();
        Assumptions.assumeTrue(loggedIn, "Skipping account services tests because login did not succeed with provided credentials.");

        // Accounts Overview
        WebElement overviewHdr = wait.until(ExpectedConditions.visibilityOfElementLocated(
                By.xpath("//h1[contains(.,'Accounts Overview')]")));
        Assertions.assertTrue(overviewHdr.isDisplayed(), "Accounts Overview header should be visible.");

        // Navigate each Account Services link (one level)
        List<String> services = Arrays.asList(
                "Open New Account",
                "Accounts Overview",
                "Transfer Funds",
                "Bill Pay",
                "Find Transactions",
                "Update Contact Info",
                "Request Loan"
        );

        for (String svc : services) {
            WebElement link = wait.until(ExpectedConditions.elementToBeClickable(By.linkText(svc)));
            link.click();
            // Header or form should exist on each page
            Assertions.assertTrue(driver.findElements(By.cssSelector("#rightPanel h1, #rightPanel h2, #rightPanel form")).size() > 0,
                    "Expected header or form on page: " + svc);
        }

        // Return to Accounts Overview and log out to restore known state
        WebElement overviewMenu = wait.until(ExpectedConditions.elementToBeClickable(By.linkText("Accounts Overview")));
        overviewMenu.click();
        Assertions.assertTrue(driver.findElements(By.xpath("//h1[contains(.,'Accounts Overview')]")).size() > 0,
                "Should be on Accounts Overview before logout.");
        logoutIfLoggedIn();
        Assertions.assertTrue(driver.getCurrentUrl().contains("/index.htm"), "Should be redirected to login after logout.");
    }

    @Test
    @Order(7)
    public void testSearchSiteFieldIfPresent() {
        // Some builds show a search input; if present, ensure interaction works without errors
        List<WebElement> search = driver.findElements(By.name("query"));
        if (!search.isEmpty()) {
            WebElement input = search.get(0);
            input.clear();
            input.sendKeys("loan");
            // Look for a search button near the field
            List<WebElement> buttons = driver.findElements(By.cssSelector("input[type='submit'][value='Go']"));
            if (!buttons.isEmpty()) {
                wait.until(ExpectedConditions.elementToBeClickable(buttons.get(0))).click();
                // We expect either a results page or remain; just assert we stayed on site
                Assertions.assertTrue(driver.getCurrentUrl().contains("/parabank"), "Should remain on ParaBank domain after search.");
            }
        }
        // If not present, test is a no-op and passes.
    }

    @Test
    @Order(8)
    public void testLinksInLeftPanelOneLevelWithoutLogin() {
        // Some left panel links are public; verify navigation if available
        List<String> publicLinks = driver.findElements(By.id("leftPanel")).isEmpty()
                ? Collections.emptyList()
                : driver.findElements(By.cssSelector("#leftPanel a"))
                        .stream().map(e -> e.getText().trim()).filter(t -> t.length() > 0).collect(Collectors.toList());

        for (String linkText : publicLinks) {
            // Skip Log In / Register links which are not real pages
            if (linkText.equalsIgnoreCase("Log In") || linkText.equalsIgnoreCase("Register")) continue;
            List<WebElement> linkEls = driver.findElements(By.linkText(linkText));
            if (!linkEls.isEmpty()) {
                wait.until(ExpectedConditions.elementToBeClickable(linkEls.get(0))).click();
                Assertions.assertTrue(driver.getCurrentUrl().contains("/parabank"), "Should remain on ParaBank domain for internal link: " + linkText);
                driver.navigate().back();
                wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("rightPanel")));
            }
        }
    }
}
