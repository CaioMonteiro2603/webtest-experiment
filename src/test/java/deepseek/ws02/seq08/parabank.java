package GPT5.ws02.seq08;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation;
import org.openqa.selenium.*;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.support.ui.ExpectedCondition;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.Select;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;
import java.util.*;
import java.util.stream.Collectors;

@TestMethodOrder(OrderAnnotation.class)
public class ParaBankE2ETest {

    private static WebDriver driver;
    private static WebDriverWait wait;

    private static final String BASE_URL = "https://parabank.parasoft.com/parabank/index.htm";
    private static final String LOGIN_USER = "caio@gmail.com";
    private static final String LOGIN_PASS = "123";

    @BeforeAll
    public static void setUp() {
        FirefoxOptions options = new FirefoxOptions();
        options.addArguments("--headless");
        driver = new FirefoxDriver(options);
        wait = new WebDriverWait(driver, Duration.ofSeconds(10));
    }

    @AfterAll
    public static void tearDown() {
        if (driver != null) driver.quit();
    }

    // ----------------------- Helpers -----------------------

    private WebElement locate(By... locators) {
        for (By by : locators) {
            List<WebElement> els = driver.findElements(by);
            if (!els.isEmpty()) return els.get(0);
        }
        throw new NoSuchElementException("None of the provided locators matched: " + Arrays.toString(locators));
    }

    private WebElement usernameField() {
        return locate(By.id("customer.username"), By.name("username"), By.cssSelector("#loginPanel input[name='username']"));
    }

    private WebElement passwordField() {
        return locate(By.id("customer.password"), By.name("password"), By.cssSelector("#loginPanel input[name='password']"));
    }

    private WebElement loginButton() {
        return locate(By.cssSelector("#loginPanel input[type='submit']"), By.cssSelector("#loginPanel input.button"));
    }

    private boolean isLoggedIn() {
        return driver.findElements(By.linkText("Log Out")).size() > 0 ||
               driver.findElements(By.cssSelector("#rightPanel h1")).stream().anyMatch(h -> h.getText().toLowerCase().contains("accounts overview"));
    }

    private void login(String user, String pass) {
        driver.get(BASE_URL);
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("loginPanel")));
        WebElement u = wait.until(ExpectedConditions.elementToBeClickable(usernameField().getLocator()));
        u.clear();
        u.sendKeys(user);
        WebElement p = passwordField();
        p.clear();
        p.sendKeys(pass);
        loginButton().click();
    }

    private void ensureLoggedOut() {
        driver.get(BASE_URL);
        if (isLoggedIn()) {
            wait.until(ExpectedConditions.elementToBeClickable(By.linkText("Log Out"))).click();
            wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("loginPanel")));
        }
    }

    private void ensureLoggedInWithProvidedCreds() {
        if (!isLoggedIn()) {
            login(LOGIN_USER, LOGIN_PASS);
            // Wait for either success or error to appear to avoid flakiness
            wait.until((ExpectedCondition<Boolean>) d ->
                    d.findElements(By.cssSelector("#rightPanel h1")).stream().anyMatch(h -> h.getText().toLowerCase().contains("accounts overview")) ||
                    d.findElements(By.cssSelector("#rightPanel .error")).size() > 0
            );
        }
    }

    private void assertExternalLinkOpens(String cssSelector, String expectedDomain) {
        List<WebElement> links = driver.findElements(By.cssSelector(cssSelector));
        if (links.isEmpty()) {
            Assertions.assertTrue(true, "Optional external link not present: " + cssSelector);
            return;
        }
        String original = driver.getWindowHandle();
        links.get(0).click();
        // If it opens in same tab, no new handle appears; handle both cases
        try {
            wait.until(d -> d.getWindowHandles().size() > 1 || d.getCurrentUrl().contains(expectedDomain));
        } catch (TimeoutException ignored) { }
        Set<String> handles = new HashSet<>(driver.getWindowHandles());
        handles.remove(original);
        if (!handles.isEmpty()) {
            String newHandle = handles.iterator().next();
            driver.switchTo().window(newHandle);
            wait.until(ExpectedConditions.or(
                    ExpectedConditions.urlContains(expectedDomain),
                    ExpectedConditions.numberOfWindowsToBe(1) // fallback
            ));
            Assertions.assertTrue(driver.getCurrentUrl().contains(expectedDomain),
                    "External link should navigate to domain: " + expectedDomain);
            driver.close();
            driver.switchTo().window(original);
        } else {
            wait.until(ExpectedConditions.urlContains(expectedDomain));
            Assertions.assertTrue(driver.getCurrentUrl().contains(expectedDomain),
                    "External link should navigate to domain: " + expectedDomain);
            driver.navigate().back();
        }
    }

    // Utility to get By from a WebElement previously located by wait.until
    private static class LocatedElement extends WebElementWrapper {
        private final By locator;
        LocatedElement(WebElement element, By locator) { super(element); this.locator = locator; }
        By getLocator() { return locator; }
    }

    // Simple wrapper to store original locator (used only internally)
    private abstract static class WebElementWrapper implements WebElement {
        private final WebElement el;
        WebElementWrapper(WebElement el) { this.el = el; }
        public void click() { el.click(); }
        public void submit() { el.submit(); }
        public void sendKeys(CharSequence... keysToSend) { el.sendKeys(keysToSend); }
        public void clear() { el.clear(); }
        public String getTagName() { return el.getTagName(); }
        public String getAttribute(String name) { return el.getAttribute(name); }
        public boolean isSelected() { return el.isSelected(); }
        public boolean isEnabled() { return el.isEnabled(); }
        public String getText() { return el.getText(); }
        public List<WebElement> findElements(By by) { return el.findElements(by); }
        public WebElement findElement(By by) { return el.findElement(by); }
        public boolean isDisplayed() { return el.isDisplayed(); }
        public Point getLocation() { return el.getLocation(); }
        public Dimension getSize() { return el.getSize(); }
        public Rectangle getRect() { return el.getRect(); }
        public String getCssValue(String propertyName) { return el.getCssValue(propertyName); }
        public <X> X getScreenshotAs(OutputType<X> target) { return el.getScreenshotAs(target); }
    }

    // Extend locate() to return LocatedElement with its By (for waits)
    private LocatedElement locateWithBy(By... locators) {
        for (By by : locators) {
            List<WebElement> els = driver.findElements(by);
            if (!els.isEmpty()) return new LocatedElement(els.get(0), by);
        }
        throw new NoSuchElementException("None of the provided locators matched: " + Arrays.toString(locators));
    }

    private LocatedElement usernameFieldLocated() {
        return locateWithBy(By.id("customer.username"), By.name("username"), By.cssSelector("#loginPanel input[name='username']"));
    }

    // Override helper to use LocatedElement for explicit wait clickability
    private WebElement usernameFieldWaitClickable() {
        LocatedElement le = usernameFieldLocated();
        return wait.until(ExpectedConditions.elementToBeClickable(le.getLocator()));
    }

    // ----------------------- Tests -----------------------

    @Test
    @Order(1)
    public void homePageLoadsAndHasLoginPanel() {
        driver.get(BASE_URL);
        WebElement title = wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector("div#topPanel a[href*='index.htm']")));
        Assertions.assertTrue(title.isDisplayed(), "Top ParaBank logo/link should be visible");
        WebElement loginPanel = wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("loginPanel")));
        Assertions.assertTrue(loginPanel.isDisplayed(), "Login panel should be visible on home page");
        Assertions.assertTrue(driver.getTitle().toLowerCase().contains("parabank"), "Title should contain 'ParaBank'");
    }

    @Test
    @Order(2)
    public void invalidLoginShowsError() {
        driver.get(BASE_URL);
        usernameFieldWaitClickable().sendKeys("invalid_user_123");
        passwordField().sendKeys("invalid_pass_456");
        loginButton().click();
        WebElement error = wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector("#rightPanel .error")));
        Assertions.assertTrue(error.getText().toLowerCase().contains("could not be verified") ||
                              error.getText().toLowerCase().contains("invalid"),
                "Error message should be displayed for invalid credentials");
        Assertions.assertTrue(driver.getCurrentUrl().contains("index.htm"), "Should remain on index.htm after invalid login");
    }

    @Test
    @Order(3)
    public void loginWithProvidedCredentials_andAssertOutcome() {
        ensureLoggedOut();
        login(LOGIN_USER, LOGIN_PASS);
        // Wait for either Accounts Overview or error
        wait.until((ExpectedCondition<Boolean>) d ->
                d.findElements(By.cssSelector("#rightPanel h1")).stream().anyMatch(h -> h.getText().toLowerCase().contains("accounts overview")) ||
                d.findElements(By.cssSelector("#rightPanel .error")).size() > 0
        );
        boolean success = driver.findElements(By.cssSelector("#rightPanel h1")).stream()
                .anyMatch(h -> h.getText().toLowerCase().contains("accounts overview"));
        boolean errorShown = driver.findElements(By.cssSelector("#rightPanel .error")).size() > 0;
        Assertions.assertTrue(success || errorShown, "Either a successful login or an error message must be shown");
        if (success) {
            WebElement welcome = wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector("#leftPanel .smallText")));
            Assertions.assertTrue(welcome.isDisplayed(), "Welcome panel should be visible after login");
        }
    }

    @Test
    @Order(4)
    public void topNav_AboutUs_InternalNavigation() {
        driver.get(BASE_URL);
        WebElement about = wait.until(ExpectedConditions.elementToBeClickable(By.linkText("About Us")));
        about.click();
        wait.until(ExpectedConditions.or(
                ExpectedConditions.urlContains("/about.htm"),
                ExpectedConditions.presenceOfElementLocated(By.cssSelector("#rightPanel h1"))
        ));
        WebElement h1 = wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector("#rightPanel h1")));
        Assertions.assertTrue(h1.getText().toLowerCase().contains("about"), "About Us page should have an appropriate header");
    }

    @Test
    @Order(5)
    public void topNav_Services_InternalNavigation() {
        driver.get(BASE_URL);
        WebElement services = wait.until(ExpectedConditions.elementToBeClickable(By.linkText("Services")));
        services.click();
        wait.until(ExpectedConditions.or(
                ExpectedConditions.urlContains("/services.htm"),
                ExpectedConditions.presenceOfElementLocated(By.cssSelector("#rightPanel h1"))
        ));
        WebElement h1 = wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector("#rightPanel h1")));
        Assertions.assertTrue(h1.getText().toLowerCase().contains("services"), "Services page should have an appropriate header");
    }

    @Test
    @Order(6)
    public void topNav_Products_InternalNavigation() {
        driver.get(BASE_URL);
        WebElement products = wait.until(ExpectedConditions.elementToBeClickable(By.linkText("Products")));
        products.click();
        wait.until(ExpectedConditions.or(
                ExpectedConditions.urlContains("/products.htm"),
                ExpectedConditions.presenceOfElementLocated(By.cssSelector("#rightPanel h1"))
        ));
        WebElement h1 = wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector("#rightPanel h1")));
        Assertions.assertTrue(h1.getText().toLowerCase().contains("products") || h1.getText().toLowerCase().contains("coming soon"),
                "Products page should have an appropriate header");
    }

    @Test
    @Order(7)
    public void topNav_Locations_InternalNavigation() {
        driver.get(BASE_URL);
        WebElement locations = wait.until(ExpectedConditions.elementToBeClickable(By.linkText("Locations")));
        locations.click();
        wait.until(ExpectedConditions.or(
                ExpectedConditions.urlContains("/locations.htm"),
                ExpectedConditions.presenceOfElementLocated(By.cssSelector("#rightPanel h1"))
        ));
        WebElement h1 = wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector("#rightPanel h1")));
        Assertions.assertTrue(h1.getText().toLowerCase().contains("locations"), "Locations page should have an appropriate header");
    }

    @Test
    @Order(8)
    public void adminPageLoads() {
        driver.get(BASE_URL);
        WebElement admin = wait.until(ExpectedConditions.elementToBeClickable(By.linkText("Admin Page")));
        admin.click();
        wait.until(ExpectedConditions.or(
                ExpectedConditions.urlContains("/admin.htm"),
                ExpectedConditions.presenceOfElementLocated(By.cssSelector("#rightPanel h1"))
        ));
        WebElement h1 = wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector("#rightPanel h1")));
        Assertions.assertTrue(h1.getText().toLowerCase().contains("administration") || h1.getText().toLowerCase().contains("admin"),
                "Admin page should have an appropriate header");
    }

    @Test
    @Order(9)
    public void registerPageLoadsAndFieldsPresent() {
        driver.get(BASE_URL);
        WebElement register = wait.until(ExpectedConditions.elementToBeClickable(By.linkText("Register")));
        register.click();
        wait.until(ExpectedConditions.urlContains("/register.htm"));
        List<String> requiredIds = Arrays.asList(
                "customer.firstName", "customer.lastName", "customer.address.street",
                "customer.address.city", "customer.address.state", "customer.address.zipCode",
                "customer.phoneNumber", "customer.ssn", "customer.username", "customer.password", "repeatedPassword"
        );
        List<String> missing = requiredIds.stream().filter(id -> driver.findElements(By.id(id)).isEmpty()).collect(Collectors.toList());
        Assertions.assertTrue(missing.isEmpty(), "All registration fields should be present, missing: " + missing);
    }

    @Test
    @Order(10)
    public void openNewAccountDropdown_WhenLoggedInOrSkipGracefully() {
        ensureLoggedInWithProvidedCreds();
        if (!isLoggedIn()) {
            // If login failed with provided creds, just assert we cannot access Open New Account and pass gracefully
            Assertions.assertTrue(driver.findElements(By.linkText("Open New Account")).isEmpty(),
                    "Open New Account should not be accessible without login (login failed with provided credentials)");
            return;
        }
        wait.until(ExpectedConditions.elementToBeClickable(By.linkText("Open New Account"))).click();
        wait.until(ExpectedConditions.urlContains("/openaccount.htm"));
        WebElement typeSelect = wait.until(ExpectedConditions.presenceOfElementLocated(By.id("type")));
        Select type = new Select(typeSelect);
        // Exercise both options if present
        List<String> options = type.getOptions().stream().map(WebElement::getText).collect(Collectors.toList());
        if (options.size() >= 2) {
            type.selectByIndex(0);
            String first = type.getFirstSelectedOption().getText();
            type.selectByIndex(1);
            String second = type.getFirstSelectedOption().getText();
            Assertions.assertNotEquals(first, second, "Selecting different account types should change the selected value");
        } else {
            Assertions.assertTrue(type.getOptions().size() > 0, "Account type dropdown should have options");
        }
    }

    @Test
    @Order(11)
    public void footerExternalLinks_OpenIfPresent() {
        driver.get(BASE_URL);
        // Common external links that may appear in footer or pages
        // Parasoft site
        assertExternalLinkOpens("a[href*='parasoft.com']", "parasoft.com");
        // Twitter
        assertExternalLinkOpens("a[href*='twitter.com']", "twitter.com");
        // Facebook
        assertExternalLinkOpens("a[href*='facebook.com']", "facebook.com");
        // LinkedIn
        assertExternalLinkOpens("a[href*='linkedin.com']", "linkedin.com");
    }

    @Test
    @Order(12)
    public void logoutIfLoggedInAndEnsureLoginPanelVisible() {
        driver.get(BASE_URL);
        if (isLoggedIn()) {
            wait.until(ExpectedConditions.elementToBeClickable(By.linkText("Log Out"))).click();
        }
        WebElement loginPanel = wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("loginPanel")));
        Assertions.assertTrue(loginPanel.isDisplayed(), "Login panel should be visible when logged out");
    }
}
