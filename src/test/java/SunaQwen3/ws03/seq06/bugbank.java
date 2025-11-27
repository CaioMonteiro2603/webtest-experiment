package SunaQwen3.ws03.seq06;

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
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

@TestMethodOrder(OrderAnnotation.class)
public class BugBankTest {

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
        assertTrue(driver.getTitle().contains("BugBank"), "Page title should contain 'BugBank'");

        WebElement emailInput = wait.until(ExpectedConditions.visibilityOfElementLocated(By.name("email")));
        emailInput.sendKeys(LOGIN_EMAIL);

        WebElement passwordInput = driver.findElement(By.name("password"));
        passwordInput.sendKeys(LOGIN_PASSWORD);

        WebElement loginButton = driver.findElement(By.xpath("//button[contains(text(), 'Entrar')]"));
        loginButton.click();

        // Wait for navigation after login
        wait.until(ExpectedConditions.urlContains("dashboard"));
        assertTrue(driver.getCurrentUrl().contains("dashboard"), "URL should contain 'dashboard' after login");
    }

    @Test
    @Order(2)
    public void testInvalidLogin() {
        driver.get(BASE_URL);

        WebElement emailInput = wait.until(ExpectedConditions.visibilityOfElementLocated(By.name("email")));
        emailInput.sendKeys("invalid@example.com");

        WebElement passwordInput = driver.findElement(By.name("password"));
        passwordInput.sendKeys("wrongpassword");

        WebElement loginButton = driver.findElement(By.xpath("//button[contains(text(), 'Entrar')]"));
        loginButton.click();

        // Wait for error message to appear
        WebElement errorMessage = wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector(".alert-danger")));
        assertTrue(errorMessage.isDisplayed(), "Error message should be displayed");
        assertTrue(errorMessage.getText().contains("Credenciais inválidas"), "Error message should indicate invalid credentials");
    }

    @Test
    @Order(3)
    public void testMenuNavigationAndResetAppState() {
        // Ensure logged in
        loginIfNotOnDashboard();

        // Open menu
        WebElement menuButton = wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector(".navbar-toggler")));
        menuButton.click();

        // Click All Items
        WebElement allItemsLink = wait.until(ExpectedConditions.elementToBeClickable(By.linkText("Todos os itens")));
        allItemsLink.click();
        wait.until(ExpectedConditions.urlContains("dashboard"));
        assertTrue(driver.getCurrentUrl().contains("dashboard"), "Should navigate to dashboard");

        // Open menu again
        menuButton = wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector(".navbar-toggler")));
        menuButton.click();

        // Click Reset App State
        WebElement resetLink = wait.until(ExpectedConditions.elementToBeClickable(By.linkText("Resetar estado do app")));
        resetLink.click();

        // Confirm reset if modal appears
        try {
            WebElement confirmButton = wait.until(ExpectedConditions.elementToBeClickable(By.xpath("//button[contains(text(), 'Resetar')]")));
            confirmButton.click();
            wait.until(ExpectedConditions.invisibilityOf(confirmButton));
        } catch (TimeoutException e) {
            // Modal might not appear every time
        }

        // Verify we're still on dashboard
        assertTrue(driver.getCurrentUrl().contains("dashboard"), "Should remain on dashboard after reset");
    }

    @Test
    @Order(4)
    public void testExternalLinksInFooter() {
        loginIfNotOnDashboard();

        // Footer links: Twitter, Facebook, LinkedIn
        List<String> expectedDomains = List.of("twitter.com", "facebook.com", "linkedin.com");
        List<String> linkTexts = List.of("Twitter", "Facebook", "LinkedIn");

        for (int i = 0; i < linkTexts.size(); i++) {
            String linkText = linkTexts.get(i);
            String expectedDomain = expectedDomains.get(i);

            // Find link in footer
            WebElement link = wait.until(ExpectedConditions.elementToBeClickable(By.linkText(linkText)));
            String originalWindow = driver.getWindowHandle();

            // Click opens new tab
            link.click();

            // Switch to new tab
            Set<String> windowHandles = driver.getWindowHandles();
            String newWindow = windowHandles.stream()
                    .filter(handle -> !handle.equals(originalWindow))
                    .findFirst()
                    .orElseThrow(() -> new RuntimeException("No new window opened"));

            driver.switchTo().window(newWindow);

            // Assert URL contains expected domain
            wait.until(ExpectedConditions.urlContains(expectedDomain));
            assertTrue(driver.getCurrentUrl().contains(expectedDomain),
                    "New tab URL should contain " + expectedDomain);

            // Close new tab and switch back
            driver.close();
            driver.switchTo().window(originalWindow);

            // Wait for original tab to be ready
            wait.until(ExpectedConditions.urlContains("dashboard"));
        }
    }

    @Test
    @Order(5)
    public void testLogoutFunctionality() {
        loginIfNotOnDashboard();

        // Open menu
        WebElement menuButton = wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector(".navbar-toggler")));
        menuButton.click();

        // Click Logout
        WebElement logoutLink = wait.until(ExpectedConditions.elementToBeClickable(By.linkText("Sair")));
        logoutLink.click();

        // Wait for login page
        wait.until(ExpectedConditions.urlToBe(BASE_URL));
        assertTrue(driver.getCurrentUrl().equals(BASE_URL), "Should return to login page after logout");

        // Verify login form is present
        assertTrue(driver.findElement(By.name("email")).isDisplayed(), "Email input should be visible on login page");
    }

    private void loginIfNotOnDashboard() {
        if (!driver.getCurrentUrl().contains("dashboard")) {
            driver.get(BASE_URL);

            // Fill login form
            WebElement emailInput = wait.until(ExpectedConditions.visibilityOfElementLocated(By.name("email")));
            emailInput.clear();
            emailInput.sendKeys(LOGIN_EMAIL);

            WebElement passwordInput = driver.findElement(By.name("password"));
            passwordInput.clear();
            passwordInput.sendKeys(LOGIN_PASSWORD);

            WebElement loginButton = driver.findElement(By.xpath("//button[contains(text(), 'Entrar')]"));
            loginButton.click();

            // Wait for dashboard
            wait.until(ExpectedConditions.urlContains("dashboard"));
        }
    }
}