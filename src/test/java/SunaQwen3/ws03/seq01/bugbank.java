package SunaQwen3.ws03.seq01;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation;
import org.openqa.selenium.*;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import java.time.Duration;
import static org.junit.jupiter.api.Assertions.*;

public class BugBankTest {

    private static WebDriver driver;
    private static WebDriverWait wait;
    private static final String BASE_URL = "https://bugbank.netlify.app/";
    private static final String LOGIN_EMAIL = "caio@gmail.com";
    private static final String LOGIN_PASSWORD = "123";

    @BeforeAll
    static void setUp() {
        FirefoxOptions options = new FirefoxOptions();
        options.addArguments("--headless");
        driver = new FirefoxDriver(options);
        wait = new WebDriverWait(driver, Duration.ofSeconds(10));
    }

    @AfterAll
    static void tearDown() {
        if (driver != null) {
            driver.quit();
        }
    }

    @Test
    @Order(1)
    void testValidLogin() {
        driver.get(BASE_URL);
        assertEquals("BugBank", driver.getTitle());

        WebElement emailInput = wait.until(ExpectedConditions.visibilityOfElementLocated(By.name("email")));
        emailInput.sendKeys(LOGIN_EMAIL);

        WebElement passwordInput = driver.findElement(By.name("password"));
        passwordInput.sendKeys(LOGIN_PASSWORD);

        WebElement loginButton = driver.findElement(By.xpath("//button[contains(text(), 'Entrar')]"));
        loginButton.click();

        // Assert successful login by checking URL or presence of logout button
        WebElement logoutButton = wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//button[contains(text(), 'Sair')]")));
        assertTrue(logoutButton.isDisplayed(), "Logout button should be displayed after login");
    }

    @Test
    @Order(2)
    void testInvalidLoginCredentials() {
        driver.get(BASE_URL);

        WebElement emailInput = wait.until(ExpectedConditions.visibilityOfElementLocated(By.name("email")));
        emailInput.sendKeys("invalid@example.com");

        WebElement passwordInput = driver.findElement(By.name("password"));
        passwordInput.sendKeys("wrongpassword");

        WebElement loginButton = driver.findElement(By.xpath("//button[contains(text(), 'Entrar')]"));
        loginButton.click();

        // Wait for error message to appear
        WebElement errorMessage = wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector(".alert-danger")));
        assertTrue(errorMessage.getText().contains("usuário ou senha inválidos"), "Error message should indicate invalid credentials");
    }

    @Test
    @Order(3)
    void testMenuNavigationAndResetAppState() {
        // Ensure logged in
        loginIfNotLoggedIn();

        // Open menu (hamburger button)
        WebElement menuButton = wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector(".navbar-toggler")));
        menuButton.click();

        // Click All Items
        WebElement allItemsLink = wait.until(ExpectedConditions.elementToBeClickable(By.linkText("Todos os itens")));
        allItemsLink.click();

        // Verify we are still on the main page (no actual navigation change expected)
        assertTrue(driver.getCurrentUrl().contains("bugbank"), "Should remain on BugBank site after clicking All Items");

        // Open menu again
        menuButton = wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector(".navbar-toggler")));
        menuButton.click();

        // Click Reset App State
        WebElement resetLink = wait.until(ExpectedConditions.elementToBeClickable(By.linkText("Resetar estado do app")));
        resetLink.click();

        // Confirm reset (if confirmation dialog appears, handle it)
        try {
            Alert alert = driver.switchTo().alert();
            alert.accept();
        } catch (NoAlertPresentException ignored) {
            // No alert present, continue
        }

        // Verify some reset condition (e.g., cart is empty)
        By cartBadge = By.cssSelector(".cart-badge");
        assertEquals(0, driver.findElements(cartBadge).size(), "Cart should be empty after reset");
    }

    @Test
    @Order(4)
    void testExternalLinksInFooter() {
        loginIfNotLoggedIn();

        // Footer social links: Twitter, Facebook, LinkedIn
        String[] linkTexts = {"Twitter", "Facebook", "LinkedIn"};
        String[] expectedDomains = {"twitter.com", "facebook.com", "linkedin.com"};

        for (int i = 0; i < linkTexts.length; i++) {
            // Re-locate element each time due to possible DOM changes
            WebElement link = wait.until(ExpectedConditions.elementToBeClickable(By.linkText(linkTexts[i])));
            String originalWindow = driver.getWindowHandle();
            link.click();

            // Switch to new tab
            String newWindow = wait.until(d -> {
                for (String handle : d.getWindowHandles()) {
                    if (!handle.equals(originalWindow)) {
                        return handle;
                    }
                }
                return null;
            });
            assertNotNull(newWindow, "New window should open for external link");
            driver.switchTo().window(newWindow);

            // Assert URL contains expected domain
            assertTrue(driver.getCurrentUrl().contains(expectedDomains[i]),
                    "External link should navigate to " + expectedDomains[i]);

            // Close new tab and switch back
            driver.close();
            driver.switchTo().window(originalWindow);
        }
    }

    @Test
    @Order(5)
    void testLogoutFunctionality() {
        loginIfNotLoggedIn();

        // Open menu
        WebElement menuButton = wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector(".navbar-toggler")));
        menuButton.click();

        // Click Logout
        WebElement logoutLink = wait.until(ExpectedConditions.elementToBeClickable(By.linkText("Sair")));
        logoutLink.click();

        // Assert login page is shown
        WebElement loginButton = wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//button[contains(text(), 'Entrar')]")));
        assertTrue(loginButton.isDisplayed(), "Login button should be visible after logout");
    }

    private void loginIfNotLoggedIn() {
        driver.get(BASE_URL);
        try {
            // Check if already logged in by looking for logout button
            driver.findElement(By.xpath("//button[contains(text(), 'Sair')]"));
            // If found, already logged in
        } catch (NoSuchElementException e) {
            // Not logged in, perform login
            WebElement emailInput = wait.until(ExpectedConditions.visibilityOfElementLocated(By.name("email")));
            emailInput.sendKeys(LOGIN_EMAIL);

            WebElement passwordInput = driver.findElement(By.name("password"));
            passwordInput.sendKeys(LOGIN_PASSWORD);

            WebElement loginButton = driver.findElement(By.xpath("//button[contains(text(), 'Entrar')]"));
            loginButton.click();

            // Wait for login to complete
            wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//button[contains(text(), 'Sair')]")));
        }
    }
}