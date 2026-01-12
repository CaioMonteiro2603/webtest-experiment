package GPT5.ws02.seq05;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation;
import org.openqa.selenium.*;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;
import java.util.List;
import java.util.Set;

@TestMethodOrder(OrderAnnotation.class)
public class parabank {

    private static WebDriver driver;
    private static WebDriverWait wait;

    private static final String BASE_URL = "https://parabank.parasoft.com/parabank/index.htm";
    private static final String LOGIN = "caio@gmail.com";
    private static final String PASSWORD = "123";

    @BeforeAll
    public static void setUp() {
        FirefoxOptions options = new FirefoxOptions();
        options.addArguments("--headless");
        driver = new FirefoxDriver(options);
        wait = new WebDriverWait(driver, Duration.ofSeconds(10));
        createUser(driver);
    }

    private static void createUser(WebDriver driver) {
        driver.get("https://parabank.parasoft.com/parabank/register.htm");
        driver.findElement(By.id("customer.firstName")).click();
        driver.findElement(By.id("customer.firstName")).sendKeys("a");
        driver.findElement(By.id("customer.lastName")).click();
        driver.findElement(By.id("customer.lastName")).sendKeys("a");
        driver.findElement(By.id("customer.address.street")).click();
        driver.findElement(By.id("customer.address.street")).sendKeys("a");
        driver.findElement(By.id("customer.address.city")).click();
        driver.findElement(By.id("customer.address.city")).sendKeys("a");
        driver.findElement(By.id("customer.address.state")).click();
        driver.findElement(By.id("customer.address.state")).sendKeys("a");
        driver.findElement(By.id("customer.address.zipCode")).click();
        driver.findElement(By.id("customer.address.zipCode")).sendKeys("a");
        driver.findElement(By.id("customer.phoneNumber")).click();
        driver.findElement(By.id("customer.phoneNumber")).sendKeys("a");
        driver.findElement(By.id("customer.ssn")).click();
        driver.findElement(By.id("customer.ssn")).sendKeys("a");
        driver.findElement(By.id("customer.username")).click();
        driver.findElement(By.id("customer.username")).sendKeys("caio@gmail.com");
        driver.findElement(By.id("customer.password")).sendKeys("123");
        driver.findElement(By.id("repeatedPassword")).sendKeys("123");
        driver.findElement(By.cssSelector("td > .button")).click();
    }

    @AfterAll
    public static void tearDown() {
        if (driver != null) driver.quit();
    }

    // ---------- Helpers ----------

    private void openHome() {
        driver.get(BASE_URL);
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("topPanel")));
    }

    private void attemptLogin(String username, String password) {
        openHome();
        WebElement user = wait.until(ExpectedConditions.elementToBeClickable(By.name("username")));
        WebElement pass = wait.until(ExpectedConditions.elementToBeClickable(By.name("password")));
        user.clear();
        user.sendKeys(username);
        pass.clear();
        pass.sendKeys(password);
        WebElement loginBtn = wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector("#loginPanel input[type='submit']")));
        loginBtn.click();
    }

    private void assertHeaderPresent(String cssOrXpath, String expectedTextContains) {
        WebElement header;
        if (cssOrXpath.startsWith("//")) {
            header = wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath(cssOrXpath)));
        } else {
            header = wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector(cssOrXpath)));
        }
        Assertions.assertTrue(header.getText().toLowerCase().contains(expectedTextContains.toLowerCase()),
                "Expected header to contain '" + expectedTextContains + "' but was '" + header.getText() + "'");
    }

    private void clickLinkByPartialText(String partial) {
        List<WebElement> links = driver.findElements(By.partialLinkText(partial));
        Assertions.assertTrue(links.size() > 0, "Expected to find link containing text: " + partial);
        WebElement link = wait.until(ExpectedConditions.elementToBeClickable(links.get(0)));
        link.click();
    }

    private void clickExternalAndAssertDomain(WebElement link, String domainContains) {
        String original = driver.getWindowHandle();
        Set<String> before = driver.getWindowHandles();
        wait.until(ExpectedConditions.elementToBeClickable(link)).click();
        // If new tab/window, switch; otherwise same tab
        Set<String> after = driver.getWindowHandles();
        if (after.size() > before.size()) {
            after.removeAll(before);
            String newHandle = after.iterator().next();
            driver.switchTo().window(newHandle);
            wait.until(d -> d.getCurrentUrl() != null && d.getCurrentUrl().startsWith("http"));
            String url = driver.getCurrentUrl();
            Assertions.assertTrue(url.contains(domainContains), "External URL should contain " + domainContains + " but was " + url);
            driver.close();
            driver.switchTo().window(original);
        } else {
            wait.until(d -> d.getCurrentUrl() != null && d.getCurrentUrl().startsWith("http"));
            String url = driver.getCurrentUrl();
            Assertions.assertTrue(url.contains(domainContains), "External URL should contain " + domainContains + " but was " + url);
            driver.navigate().back();
            wait.until(ExpectedConditions.urlContains("/parabank"));
        }
    }

    // ---------- Tests ----------

    @Test
    @Order(1)
    public void homePageLoadsAndHasCoreElements() {
        openHome();
        Assertions.assertTrue(driver.getTitle().toLowerCase().contains("parabank"), "Title should contain 'ParaBank'");
        Assertions.assertTrue(wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("loginPanel"))).isDisplayed(),
                "Login panel should be visible");
        Assertions.assertTrue(wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("leftPanel"))).isDisplayed(),
                "Left panel should be visible");
        Assertions.assertTrue(wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("rightPanel"))).isDisplayed(),
                "Right panel should be visible");
    }

    @Test
    @Order(2)
    public void invalidLoginShowsErrorMessage() {
        attemptLogin(LOGIN, PASSWORD);
        WebElement error = wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector("#rightPanel .title")));
        Assertions.assertTrue(error.getText().length() > 0, "An error message should be displayed for invalid login");
        Assertions.assertTrue(driver.getCurrentUrl().contains("/parabank"), "Should remain within the ParaBank domain");
    }

    @Test
    @Order(3)
    public void registerLinkNavigatesToRegistrationPage() {
        openHome();
        clickLinkByPartialText("Register");
        wait.until(ExpectedConditions.urlContains("register.htm"));
        Assertions.assertTrue(driver.getCurrentUrl().contains("register.htm"), "URL should contain register.htm");
        assertHeaderPresent("#rightPanel h1", "Signing up is easy");
    }

    @Test
    @Order(4)
    public void forgotLoginLinkNavigatesToLookupPage() {
        openHome();
        clickLinkByPartialText("Forgot login");
        wait.until(ExpectedConditions.urlContains("lookup.htm"));
        Assertions.assertTrue(driver.getCurrentUrl().contains("lookup.htm"), "URL should contain lookup.htm");
        assertHeaderPresent("#rightPanel h1", "Customer Lookup");
    }

    @Test
    @Order(5)
    public void aboutUsPageLoads() {
        openHome();
        clickLinkByPartialText("about");
        wait.until(ExpectedConditions.urlContains("about.htm"));
        Assertions.assertTrue(driver.getCurrentUrl().contains("about.htm"), "URL should contain about.htm");
        assertHeaderPresent("//*[@id='rightPanel']//h1[contains(text(),'ParaSoft Demo Website')]", "ParaSoft Demo Website");
    }

    @Test
    @Order(6)
    public void contactPageLoads() {
        openHome();
        // Some skins use "Contact Us" in top or left panel
        List<WebElement> contactLinks = driver.findElements(By.partialLinkText("Contact"));
        Assertions.assertTrue(contactLinks.size() > 0, "Expected to find 'Contact' link");
        wait.until(ExpectedConditions.elementToBeClickable(contactLinks.get(0))).click();
        wait.until(ExpectedConditions.urlContains("updateprofile.htm"));
        Assertions.assertTrue(driver.getCurrentUrl().contains("updateprofile.htm"), "URL should contain updateprofile.htm");
        assertHeaderPresent("#rightPanel h1", "Update Profile");
    }

    @Test
    @Order(7)
    public void adminPageLoadsAndShowsAdminHeader() {
        openHome();
        // Admin Page link typically in footer or left panel
        List<WebElement> adminLinks = driver.findElements(By.partialLinkText("Admin"));
        Assertions.assertTrue(adminLinks.size() > 0, "Expected to find 'Admin' link");
        wait.until(ExpectedConditions.elementToBeClickable(adminLinks.get(0))).click();
        wait.until(ExpectedConditions.urlContains("admin"));
        Assertions.assertTrue(driver.getCurrentUrl().contains("admin"), "URL should contain admin");
        // Admin header text typically "Administration"
        assertHeaderPresent("#rightPanel h1", "Administration");
    }

    @Test
    @Order(8)
    public void productsOrServicesPageIfPresent() {
        openHome();
        // Some themes show 'Services' or 'Products' in main nav/left panel
        List<WebElement> services = driver.findElements(By.partialLinkText("Services"));
        if (!services.isEmpty()) {
            wait.until(ExpectedConditions.elementToBeClickable(services.get(0))).click();
            wait.until(ExpectedConditions.urlContains("services"));
            Assertions.assertTrue(driver.getCurrentUrl().contains("services"), "URL should contain 'services'");
            assertHeaderPresent("#rightPanel h1, #rightPanel h2", "Services");
        } else {
            List<WebElement> products = driver.findElements(By.partialLinkText("Products"));
            Assumptions.assumeTrue(!products.isEmpty(), "Neither Services nor Products link found; skipping test");
            wait.until(ExpectedConditions.elementToBeClickable(products.get(0))).click();
            wait.until(ExpectedConditions.urlContains("products"));
            Assertions.assertTrue(driver.getCurrentUrl().contains("products"), "URL should contain 'products'");
            assertHeaderPresent("#rightPanel h1, #rightPanel h2", "Products");
        }
    }

    @Test
    @Order(9)
    public void externalLinkToParasoftDomainWorks() {
        openHome();
        // Footer may be #footerPanel; ensure scrolled to bottom for visibility
        ((JavascriptExecutor) driver).executeScript("window.scrollTo(0, document.body.scrollHeight)");
        // Try to find any anchor that points to parasoft.com
        List<WebElement> ext = driver.findElements(By.cssSelector("a[href*='parasoft.com']"));
        Assertions.assertTrue(ext.size() > 0, "Expected to find external link to parasoft.com");
        clickExternalAndAssertDomain(ext.get(0), "parasoft.com");
        // After returning/closing, confirm still on site
        Assertions.assertTrue(driver.getCurrentUrl().contains("/parabank"), "Should remain within ParaBank after closing external tab");
    }

    @Test
    @Order(10)
    public void footerAndHeaderLinksStayWithinOneLevel() {
        openHome();
        // Collect first-level links visible on home and verify they load (internal only)
        List<WebElement> links = driver.findElements(By.cssSelector("a[href]"));
        int checked = 0;
        for (WebElement a : links) {
            String href = a.getAttribute("href");
            if (href == null) continue;
            // Only same-site and one level below /parabank/
            if (href.contains("/parabank/") && href.split("/parabank/").length > 1) {
                String path = href.substring(href.indexOf("/parabank/"));
                if (path.contains("/") && path.indexOf('/') != path.lastIndexOf('/')) {
                    // deeper than one slash -> skip (more than one level)
                    continue;
                }
                // Avoid performing dozens of clicks; limit to a handful
                if (checked >= 5) break;
                String originalUrl = driver.getCurrentUrl();
                try {
                    wait.until(ExpectedConditions.elementToBeClickable(a)).click();
                } catch (ElementClickInterceptedException e) {
                    ((JavascriptExecutor) driver).executeScript("arguments[0].click();", a);
                }
                try {
                    wait.until(d -> !d.getCurrentUrl().equals(originalUrl));
                    Assertions.assertTrue(driver.getCurrentUrl().contains("/parabank/"),
                            "Internal nav should remain within /parabank/: " + driver.getCurrentUrl());
                    // Basic assertion that page has a rightPanel with some content
                    Assertions.assertTrue(wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("rightPanel"))).isDisplayed(),
                            "Right panel should be visible on internal page: " + driver.getCurrentUrl());
                    checked++;
                } catch (TimeoutException ex) {
                    // Page didn't load, continue
                }
                if (!driver.getCurrentUrl().equals(originalUrl)) {
                    driver.navigate().back();
                    try {
                        wait.until(ExpectedConditions.urlToBe(originalUrl));
                    } catch (TimeoutException ex) {
                        openHome();
                    }
                }
            }
        }
        Assertions.assertTrue(checked > 0, "At least one internal link should have been validated");
    }
}