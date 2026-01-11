package SunaQwen3.ws03.seq02;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation;
import org.openqa.selenium.*;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.junit.jupiter.api.Assertions;

import java.time.Duration;
import java.util.List;


@TestMethodOrder(OrderAnnotation.class)
public class bugbank {

    private static WebDriver driver;
    private static WebDriverWait wait;
    private static final String BASE_URL = "https://bugbank.netlify.app/";
    private static final String LOGIN_EMAIL = "caio@gmail.com";
    private static final String LOGIN_PASSWORD = "123";

    @BeforeAll
    public static void setUp() {
        FirefoxOptions options = new FirefoxOptions();
        options.addArguments("--headless");
        driver = new FirefoxDriver(options);
        wait = new WebDriverWait(driver, Duration.ofSeconds(10));
    }

    @AfterAll
    public static void tearDown() {
        if (driver != null) {
            driver.quit();
        }
    }

    @Test
    @Order(1)
    public void testValidLogin() {
        driver.get(BASE_URL);
        Assertions.assertTrue(driver.getTitle().contains("BugBank"), "Page title should contain 'BugBank'");

        WebElement emailInput = wait.until(ExpectedConditions.visibilityOfElementLocated(By.name("email")));
        emailInput.sendKeys(LOGIN_EMAIL);

        WebElement passwordInput = driver.findElement(By.name("password"));
        passwordInput.sendKeys(LOGIN_PASSWORD);

        WebElement loginButton = driver.findElement(By.xpath("//button[contains(text(),'Acessar')]"));
        loginButton.click();

        // Wait for navigation or element that indicates login success
        wait.until(ExpectedConditions.urlContains("home"));
        Assertions.assertTrue(driver.getCurrentUrl().contains("home"), "URL should contain 'home' after login");
    }

    @Test
    @Order(2)
    public void testInvalidLoginCredentials() {
        driver.get(BASE_URL);

        WebElement emailInput = wait.until(ExpectedConditions.visibilityOfElementLocated(By.name("email")));
        emailInput.sendKeys("invalid@example.com");

        WebElement passwordInput = driver.findElement(By.name("password"));
        passwordInput.sendKeys("wrongpassword");

        WebElement loginButton = driver.findElement(By.xpath("//button[contains(text(),'Acessar')]"));
        loginButton.click();

        // Wait for error message to appear
        WebElement errorMessage = wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector(".kOeYvn")));
        Assertions.assertTrue(errorMessage.isDisplayed(), "Error message should be displayed");
        Assertions.assertTrue(errorMessage.getText().contains("Usuário ou senha inválido"), "Error message should indicate invalid credentials");
    }

    @Test
    @Order(3)
    public void testMenuNavigationAndResetAppState() {
        // Ensure logged in
        loginIfNotOnDashboard();

        // Open menu (assuming burger icon exists)
        WebElement menuButton = wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector(".bm-burger-button")));
        menuButton.click();

        // Click All Items
        WebElement allItemsLink = wait.until(ExpectedConditions.elementToBeClickable(By.linkText("Home")));
        allItemsLink.click();
        wait.until(ExpectedConditions.urlContains("home"));
        Assertions.assertTrue(driver.getCurrentUrl().contains("home"), "Should navigate to home");

        // Open menu again
        menuButton = wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector(".bm-burger-button")));
        menuButton.click();

        // Click Reset App State
        WebElement resetLink = wait.until(ExpectedConditions.elementToBeClickable(By.linkText("Resetar estado da aplicação")));
        resetLink.click();

        // Confirm reset if modal appears
        try {
            WebElement confirmReset = wait.until(ExpectedConditions.elementToBeClickable(By.xpath("//button[contains(text(),'Resetar')]")));
            confirmReset.click();
            wait.until(ExpectedConditions.invisibilityOf(confirmReset));
        } catch (TimeoutException e) {
            // Ignore if no confirmation modal
        }

        // Verify app state reset (e.g., cart empty)
        By cartBadge = By.cssSelector(".cart-badge");
        List<WebElement> badges = driver.findElements(cartBadge);
        if (!badges.isEmpty()) {
            Assertions.assertEquals("0", badges.get(0).getText(), "Cart badge should show 0 after reset");
        }
    }

    @Test
    @Order(4)
    public void testExternalLinksInFooter() {
        loginIfNotOnDashboard();

        // Footer social links
        By twitterLink = By.cssSelector("footer a[href*='twitter']");
        By facebookLink = By.cssSelector("footer a[href*='facebook']");
        By linkedinLink = By.cssSelector("footer a[href*='linkedin']");

        assertExternalLinkOpensAndCloses(twitterLink, "twitter.com");
        assertExternalLinkOpensAndCloses(facebookLink, "facebook.com");
        assertExternalLinkOpensAndCloses(linkedinLink, "linkedin.com");
    }

    @Test
    @Order(5)
    public void testLogoutFunctionality() {
        loginIfNotOnDashboard();

        // Open menu
        WebElement menuButton = wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector(".bm-burger-button")));
        menuButton.click();

        // Click Logout
        WebElement logoutLink = wait.until(ExpectedConditions.elementToBeClickable(By.linkText("Sair")));
        logoutLink.click();

        // Wait for login page
        wait.until(ExpectedConditions.urlToBe(BASE_URL));
        Assertions.assertEquals(BASE_URL, driver.getCurrentUrl(), "Should return to login page after logout");

        // Verify login elements are present
        Assertions.assertTrue(driver.findElements(By.name("email")).size() > 0, "Login email field should be present");
    }

    private void loginIfNotOnDashboard() {
        if (!driver.getCurrentUrl().contains("home")) {
            driver.get(BASE_URL);

            // Fill login form
            WebElement emailInput = wait.until(ExpectedConditions.visibilityOfElementLocated(By.name("email")));
            emailInput.clear();
            emailInput.sendKeys(LOGIN_EMAIL);

            WebElement passwordInput = driver.findElement(By.name("password"));
            passwordInput.clear();
            passwordInput.sendKeys(LOGIN_PASSWORD);

            WebElement loginButton = driver.findElement(By.xpath("//button[contains(text(),'Acessar')]"));
            loginButton.click();

            // Wait for home
            wait.until(ExpectedConditions.urlContains("home"));
        }
    }

    private void assertExternalLinkOpensAndCloses(By locator, String expectedDomain) {
        String originalWindow = driver.getWindowHandle();

        // Click link that opens in new tab
        WebElement link = wait.until(ExpectedConditions.elementToBeClickable(locator));
        link.click();

        // Wait for new window
        wait.until(ExpectedConditions.numberOfWindowsToBe(2));

        // Switch to new window
        for (String windowHandle : driver.getWindowHandles()) {
            if (!windowHandle.equals(originalWindow)) {
                driver.switchTo().window(windowHandle);
                break;
            }
        }

        // Assert URL contains expected domain
        String currentUrl = driver.getCurrentUrl();
        Assertions.assertTrue(currentUrl.contains(expectedDomain), 
                String.format("External page URL should contain '%s', but was: %s", expectedDomain, currentUrl));

        // Close tab and switch back
        driver.close();
        driver.switchTo().window(originalWindow);
    }
}