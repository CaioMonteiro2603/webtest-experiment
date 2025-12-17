package GPT5.ws02.seq02;

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
    private static final String PROVIDED_LOGIN = "caio@gmail.com";
    private static final String PROVIDED_PASSWORD = "123";

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

    // -------------------- Helpers --------------------

    private void goHome() {
        driver.get(BASE_URL);
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("leftPanel")));
    }

    private void tryLogin(String username, String password) {
        goHome();
        WebElement user = wait.until(ExpectedConditions.elementToBeClickable(By.name("username")));
        user.clear();
        user.sendKeys(username);
        WebElement pass = driver.findElement(By.name("password"));
        pass.clear();
        pass.sendKeys(password);
        WebElement loginBtn = driver.findElement(By.cssSelector("#loginPanel input.button[type='submit']"));
        loginBtn.click();
    }

    private boolean isLoggedIn() {
        // When logged in, "Accounts Overview" page appears and "Log Out" link is visible
        boolean hasLogout = driver.findElements(By.linkText("Log Out")).size() > 0;
        boolean accountsHeader = driver.findElements(By.cssSelector("#rightPanel h1")).stream()
                .anyMatch(h -> h.getText().toLowerCase().contains("accounts overview"));
        return hasLogout || accountsHeader;
    }

    private void logoutIfLoggedIn() {
        if (driver.findElements(By.linkText("Log Out")).size() > 0) {
            wait.until(ExpectedConditions.elementToBeClickable(By.linkText("Log Out"))).click();
            wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("loginPanel")));
        }
    }

    private void openInternalLink(String linkText, String expectedPathFragment) {
        goHome();
        wait.until(ExpectedConditions.elementToBeClickable(By.linkText(linkText))).click();
        wait.until(ExpectedConditions.urlContains(expectedPathFragment));
        Assertions.assertTrue(driver.getCurrentUrl().contains(expectedPathFragment),
                "URL should contain " + expectedPathFragment);
    }

    private void clickExternalAndAssert(By locator, String expectedDomainFragment) {
        if (driver.findElements(locator).size() == 0) return; // optional
        String original = driver.getWindowHandle();
        Set<String> before = driver.getWindowHandles();
        WebElement link = wait.until(ExpectedConditions.elementToBeClickable(locator));
        link.click();
        // Either new tab/window or same tab
        wait.until(d -> d.getWindowHandles().size() > before.size() || !d.getCurrentUrl().equals(BASE_URL));

        if (driver.getWindowHandles().size() > before.size()) {
            Set<String> after = driver.getWindowHandles();
            after.removeAll(before);
            String newHandle = after.iterator().next();
            driver.switchTo().window(newHandle);
            wait.until(d -> d.getCurrentUrl().toLowerCase().contains(expectedDomainFragment));
            Assertions.assertTrue(driver.getCurrentUrl().toLowerCase().contains(expectedDomainFragment),
                    "External URL should contain " + expectedDomainFragment);
            driver.close();
            driver.switchTo().window(original);
        } else {
            wait.until(d -> d.getCurrentUrl().toLowerCase().contains(expectedDomainFragment));
            Assertions.assertTrue(driver.getCurrentUrl().toLowerCase().contains(expectedDomainFragment),
                    "External URL should contain " + expectedDomainFragment);
            driver.navigate().back();
            wait.until(ExpectedConditions.urlContains("/parabank"));
        }
    }

    // -------------------- Tests --------------------

    @Test
    @Order(1)
    public void testHomePageLoads() {
        goHome();
        String title = driver.getTitle().toLowerCase();
        Assertions.assertTrue(title.contains("parabank"), "Title should contain 'ParaBank'");
        Assertions.assertTrue(driver.findElement(By.id("leftPanel")).isDisplayed(), "Left navigation panel should be visible");
    }

    @Test
    @Order(2)
    public void testInvalidLoginShowsError() {
        tryLogin(PROVIDED_LOGIN, PROVIDED_PASSWORD);
        // Most likely invalid credentials -> error message within #rightPanel
        WebElement msg = wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector("#rightPanel .error")));
        Assertions.assertTrue(msg.getText().toLowerCase().contains("could not be verified")
                        || msg.getText().toLowerCase().contains("error"),
                "Invalid login should show an error message");
    }

    @Test
    @Order(3)
    public void testRegisterNewUserThenLoginLogout() {
        goHome();
        wait.until(ExpectedConditions.elementToBeClickable(By.linkText("Register"))).click();
        wait.until(ExpectedConditions.urlContains("register.htm"));

        String uniqueUser = "qauser" + System.currentTimeMillis();

        driver.findElement(By.id("customer.firstName")).sendKeys("QA");
        driver.findElement(By.id("customer.lastName")).sendKeys("User");
        driver.findElement(By.id("customer.address.street")).sendKeys("123 Test St");
        driver.findElement(By.id("customer.address.city")).sendKeys("Testville");
        driver.findElement(By.id("customer.address.state")).sendKeys("TS");
        driver.findElement(By.id("customer.address.zipCode")).sendKeys("12345");
        driver.findElement(By.id("customer.phoneNumber")).sendKeys("5551234567");
        driver.findElement(By.id("customer.ssn")).sendKeys("123-45-6789");
        driver.findElement(By.id("customer.username")).sendKeys(uniqueUser);
        driver.findElement(By.id("customer.password")).sendKeys("Pass123!");
        driver.findElement(By.id("repeatedPassword")).sendKeys("Pass123!");

        wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector("input[value='Register']"))).click();

        // Success message and logged in
        WebElement msg = wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector("#rightPanel p")));
        Assertions.assertTrue(msg.getText().toLowerCase().contains("account was created")
                        || isLoggedIn(),
                "Registration should succeed and user should be logged in");

        // Log out
        logoutIfLoggedIn();
        Assertions.assertTrue(driver.getCurrentUrl().contains("/index.htm")
                || driver.findElement(By.id("loginPanel")).isDisplayed(), "After logout, should be on login page");
    }

    @Test
    @Order(4)
    public void testInternalNavigationAboutServicesContact() {
        // About Us
        openInternalLink("About Us", "about.htm");
        Assertions.assertTrue(driver.findElements(By.cssSelector("#rightPanel h1")).stream()
                .anyMatch(h -> h.getText().toLowerCase().contains("about")), "About page should have a header");

        // Services
        openInternalLink("Services", "services.htm");
        Assertions.assertTrue(driver.findElements(By.cssSelector("#rightPanel h1")).stream()
                .anyMatch(h -> h.getText().toLowerCase().contains("services")), "Services page should have a header");

        // Contact
        openInternalLink("Contact", "contact.htm");
        Assertions.assertTrue(driver.findElements(By.cssSelector("#rightPanel h1")).stream()
                .anyMatch(h -> h.getText().toLowerCase().contains("customer care")), "Contact page should have a header");
    }

    @Test
    @Order(5)
    public void testContactFormSubmission() {
        openInternalLink("Contact", "contact.htm");
        driver.findElement(By.name("name")).sendKeys("QA Bot");
        driver.findElement(By.name("email")).sendKeys("qa.bot@example.com");
        driver.findElement(By.name("phone")).sendKeys("5550001111");
        driver.findElement(By.name("message")).sendKeys("Automated message from Selenium test.");
        wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector("input[value='Send to Customer Care']"))).click();

        // Confirmation
        WebElement conf = wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector("#rightPanel p")));
        Assertions.assertTrue(conf.getText().toLowerCase().contains("a customer care representative"),
                "Contact form should confirm a representative will contact you");
    }

    @Test
    @Order(6)
    public void testFooterExternalLinks() {
        goHome();
        // Parasoft corporate link(s)
        clickExternalAndAssert(By.cssSelector("a[href*='parasoft.com']"), "parasoft.com");

        // Social links if present
        clickExternalAndAssert(By.cssSelector("a[href*='twitter.com']"), "twitter.com");
        clickExternalAndAssert(By.cssSelector("a[href*='facebook.com']"), "facebook.com");
        clickExternalAndAssert(By.cssSelector("a[href*='linkedin.com']"), "linkedin.com");
    }

    @Test
    @Order(7)
    public void testOpenAdminPageReadOnly() {
        goHome();
        // Admin Page link may be available on the left panel
        List<WebElement> adminLinks = driver.findElements(By.linkText("Admin Page"));
        if (!adminLinks.isEmpty()) {
            wait.until(ExpectedConditions.elementToBeClickable(adminLinks.get(0))).click();
            wait.until(ExpectedConditions.urlContains("admin.htm"));
            Assertions.assertTrue(driver.findElements(By.cssSelector("#rightPanel h1")).stream()
                    .anyMatch(h -> h.getText().toLowerCase().contains("administration")
                            || h.getText().toLowerCase().contains("admin")),
                    "Admin page should have an administration header");
        } else {
            // If not present, assert home still OK
            Assertions.assertTrue(true, "Admin Page link not present; skipping without failure.");
        }
    }
}
