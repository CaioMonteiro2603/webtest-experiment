package GPT4.ws10.seq09;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation;
import org.openqa.selenium.*;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.support.ui.*;

import java.time.Duration;
import java.util.List;
import java.util.Set;

@TestMethodOrder(OrderAnnotation.class)
public class agritest {

    private static WebDriver driver;
    private static WebDriverWait wait;
    private static final String BASE_URL = "https://beta.brasilagritest.com/login";
    private static final String LOGIN = "superadmin@brasilagritest.com.br";
    private static final String PASSWORD = "10203040";

    @BeforeAll
    public static void setUp() {
        FirefoxOptions options = new FirefoxOptions();
        options.addArguments("--headless"); // REQUIRED
        driver = new FirefoxDriver(options);
        wait = new WebDriverWait(driver, Duration.ofSeconds(10));
    }

    @AfterAll
    public static void tearDown() {
        if (driver != null) driver.quit();
    }

    private void openLoginPage() {
        driver.get(BASE_URL);
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector("input[type='email'], input[name='email']")));
    }

    private void login(String email, String pass) {
        openLoginPage();
        WebElement emailInput = wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector("input[type='email'], input[name='email']")));
        WebElement passwordInput = driver.findElement(By.cssSelector("input[type='password'], input[name='password']"));
        WebElement submitBtn = driver.findElement(By.cssSelector("button[type='submit'], button"));
        emailInput.clear();
        emailInput.sendKeys(email);
        passwordInput.clear();
        passwordInput.sendKeys(pass);
        wait.until(ExpectedConditions.elementToBeClickable(submitBtn)).click();
    }

    private boolean isLoggedIn() {
        return driver.getCurrentUrl().contains("/dashboard") ||
               !driver.findElements(By.cssSelector("button[aria-label='Logout'], button.logout")).isEmpty();
    }

    private void logout() {
        if (isLoggedIn()) {
            List<WebElement> logoutButtons = driver.findElements(By.cssSelector("button[aria-label='Logout'], button.logout"));
            if (!logoutButtons.isEmpty()) {
                wait.until(ExpectedConditions.elementToBeClickable(logoutButtons.get(0))).click();
                wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector("input[type='email'], input[name='email']")));
            }
        }
    }

    private void assertExternalLinkOpens(String selector, String expectedDomain) {
        List<WebElement> links = driver.findElements(By.cssSelector(selector));
        if (links.isEmpty()) return;
        String originalWindow = driver.getWindowHandle();
        Set<String> oldWindows = driver.getWindowHandles();
        wait.until(ExpectedConditions.elementToBeClickable(links.get(0))).click();
        wait.until(d -> d.getWindowHandles().size() > oldWindows.size());
        Set<String> newWindows = driver.getWindowHandles();
        newWindows.removeAll(oldWindows);
        String newWindow = newWindows.iterator().next();
        driver.switchTo().window(newWindow);
        wait.until(ExpectedConditions.urlContains(expectedDomain));
        Assertions.assertTrue(driver.getCurrentUrl().contains(expectedDomain),
                "External link should navigate to " + expectedDomain);
        driver.close();
        driver.switchTo().window(originalWindow);
    }

    @Test
    @Order(1)
    public void loginPageElementsVisible() {
        openLoginPage();
        Assertions.assertAll(
                () -> Assertions.assertTrue(driver.findElement(By.cssSelector("input[type='email'], input[name='email']")).isDisplayed(), "Email field should be visible"),
                () -> Assertions.assertTrue(driver.findElement(By.cssSelector("input[type='password'], input[name='password']")).isDisplayed(), "Password field should be visible"),
                () -> Assertions.assertTrue(driver.findElement(By.cssSelector("button[type='submit'], button")).isDisplayed(), "Submit button should be visible")
        );
    }

    @Test
    @Order(2)
    public void invalidLoginShowsError() {
        login("invalid@example.com", "wrongpass");
        WebElement errorMsg = wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector(".alert-danger, .error")));
        Assertions.assertTrue(errorMsg.isDisplayed(), "Error message should be displayed for invalid login");
        Assertions.assertFalse(isLoggedIn(), "User should not be logged in with invalid credentials");
    }

    @Test
    @Order(3)
    public void validLoginNavigatesToDashboard() {
        login(LOGIN, PASSWORD);
        Assertions.assertTrue(isLoggedIn(), "User should be logged in with valid credentials or dashboard visible");
    }

    @Test
    @Order(4)
    public void burgerMenuResetAndLogout() {
        if (!isLoggedIn()) login(LOGIN, PASSWORD);
        List<WebElement> burger = driver.findElements(By.cssSelector("button[aria-label='Menu'], .burger"));
        Assumptions.assumeTrue(!burger.isEmpty(), "Burger menu not present");
        wait.until(ExpectedConditions.elementToBeClickable(burger.get(0))).click();
        boolean menuOpened = !driver.findElements(By.cssSelector("nav, .sidebar")).isEmpty();
        Assertions.assertTrue(menuOpened, "Menu should open");
        List<WebElement> resetLinks = driver.findElements(By.xpath("//a[contains(.,'Reset App State')]|//button[contains(.,'Reset App State')]"));
        if (!resetLinks.isEmpty()) {
            wait.until(ExpectedConditions.elementToBeClickable(resetLinks.get(0))).click();
            wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector(".toast-success, .alert-success")));
        }
        logout();
        Assertions.assertFalse(isLoggedIn(), "Should be logged out after clicking logout");
    }

    @Test
    @Order(5)
    public void footerSocialLinksExternal() {
        openLoginPage();
        assertExternalLinkOpens("a[href*='twitter.com']", "twitter.com");
        assertExternalLinkOpens("a[href*='facebook.com']", "facebook.com");
        assertExternalLinkOpens("a[href*='linkedin.com']", "linkedin.com");
    }
}
