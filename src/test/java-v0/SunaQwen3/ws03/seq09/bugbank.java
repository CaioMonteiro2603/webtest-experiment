package SunaQwen3.ws03.seq09;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation;
import org.openqa.selenium.*;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import static org.junit.jupiter.api.Assertions.*;

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
        assertEquals("BugBank", driver.getTitle(), "Page title should be 'BugBank'");

        WebElement emailInput = wait.until(ExpectedConditions.visibilityOfElementLocated(By.name("email")));
        emailInput.sendKeys(LOGIN_EMAIL);

        WebElement passwordInput = driver.findElement(By.name("password"));
        passwordInput.sendKeys(LOGIN_PASSWORD);

        WebElement loginButton = driver.findElement(By.xpath("//button[contains(text(), 'Entrar')]"));
        loginButton.click();

        // Wait for navigation after login
        wait.until(ExpectedConditions.urlContains("home"));
        assertTrue(driver.getCurrentUrl().contains("home"), "URL should contain 'home' after login");
    }

    @Test
    @Order(2)
    void testInvalidLoginError() {
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
        assertTrue(errorMessage.getText().contains("usuário ou senha inválidos"), "Error message should indicate invalid credentials");
    }

    @Test
    @Order(3)
    void testMenuNavigationAndResetAppState() {
        // Ensure we're logged in
        loginIfNotOnHomePage();

        // Open menu
        WebElement menuButton = wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector(".navbar-toggler")));
        menuButton.click();

        // Click All Items
        WebElement allItemsLink = wait.until(ExpectedConditions.elementToBeClickable(By.linkText("Todos os itens")));
        allItemsLink.click();
        wait.until(ExpectedConditions.urlContains("home"));
        assertTrue(driver.getCurrentUrl().contains("home"), "Should navigate to home after clicking 'Todos os itens'");

        // Open menu again
        menuButton = wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector(".navbar-toggler")));
        menuButton.click();

        // Click Reset App State
        WebElement resetLink = wait.until(ExpectedConditions.elementToBeClickable(By.linkText("Resetar estado do app")));
        resetLink.click();

        // Confirm reset (if modal appears)
        try {
            WebElement confirmButton = driver.findElement(By.xpath("//button[contains(text(), 'Resetar')]"));
            if (confirmButton.isDisplayed()) {
                confirmButton.click();
            }
        } catch (NoSuchElementException ignored) {
            // No confirmation modal
        }

        // Wait to ensure reset completed
        wait.until(ExpectedConditions.stalenessOf(driver.findElement(By.tagName("body"))));
        assertTrue(driver.getCurrentUrl().contains("home"), "URL should remain on home after reset");
    }

    @Test
    @Order(4)
    void testLogoutFunctionality() {
        loginIfNotOnHomePage();

        // Open menu
        WebElement menuButton = wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector(".navbar-toggler")));
        menuButton.click();

        // Click Logout
        WebElement logoutLink = wait.until(ExpectedConditions.elementToBeClickable(By.linkText("Sair")));
        logoutLink.click();

        // Wait for login page
        wait.until(ExpectedConditions.urlToBe(BASE_URL));
        assertEquals(BASE_URL, driver.getCurrentUrl(), "Should return to login page after logout");
        assertTrue(driver.findElement(By.xpath("//button[contains(text(), 'Entrar')]")).isDisplayed(), "Login button should be visible");
    }

    @Test
    @Order(5)
    void testAboutExternalLink() {
        loginIfNotOnHomePage();

        // Open menu
        WebElement menuButton = wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector(".navbar-toggler")));
        menuButton.click();

        // Click About (assumed to open in new tab)
        WebElement aboutLink = wait.until(ExpectedConditions.elementToBeClickable(By.linkText("Sobre")));
        aboutLink.click();

        // Switch to new tab
        String originalWindow = driver.getWindowHandle();
        wait.until(ExpectedConditions.numberOfWindowsToBe(2));
        for (String windowHandle : driver.getWindowHandles()) {
            if (!windowHandle.equals(originalWindow)) {
                driver.switchTo().window(windowHandle);
                break;
            }
        }

        // Assert URL contains expected domain
        String currentUrl = driver.getCurrentUrl();
        assertTrue(currentUrl.contains("github") || currentUrl.contains("bugbank"), "About link should open GitHub or related domain");

        // Close new tab and switch back
        driver.close();
        driver.switchTo().window(originalWindow);
        assertEquals("home", driver.getCurrentUrl().split("/"), "Should return to home page in original tab");
    }

    @Test
    @Order(6)
    void testFooterSocialLinks() {
        loginIfNotOnHomePage();

        // Find all footer links
        List<WebElement> footerLinks = driver.findElements(By.cssSelector("footer a"));

        String originalWindow = driver.getWindowHandle();
        for (int i = 0; i < footerLinks.size(); i++) {
            // Re-locate elements to avoid stale reference
            footerLinks = driver.findElements(By.cssSelector("footer a"));
            WebElement link = footerLinks.get(i);
            String linkText = link.getText();

            // Open link in new tab using JavaScript to avoid interception
            ((JavascriptExecutor) driver).executeScript("window.open(arguments[0])", link.getAttribute("href"));

            // Switch to new tab
            wait.until(ExpectedConditions.numberOfWindowsToBe(2));
            for (String windowHandle : driver.getWindowHandles()) {
                if (!windowHandle.equals(originalWindow)) {
                    driver.switchTo().window(windowHandle);
                    break;
                }
            }

            // Assert domain based on link text
            String currentUrl = driver.getCurrentUrl();
            if (linkText.contains("Twitter")) {
                assertTrue(currentUrl.contains("twitter.com") || currentUrl.contains("x.com"), "Twitter link should open correct domain");
            } else if (linkText.contains("Facebook")) {
                assertTrue(currentUrl.contains("facebook.com"), "Facebook link should open correct domain");
            } else if (linkText.contains("LinkedIn")) {
                assertTrue(currentUrl.contains("linkedin.com"), "LinkedIn link should open correct domain");
            }

            // Close new tab and switch back
            driver.close();
            driver.switchTo().window(originalWindow);
        }
    }

    private void loginIfNotOnHomePage() {
        if (!driver.getCurrentUrl().contains("home")) {
            driver.get(BASE_URL);

            // Clear inputs first
            WebElement emailInput = wait.until(ExpectedConditions.visibilityOfElementLocated(By.name("email")));
            emailInput.clear();
            emailInput.sendKeys(LOGIN_EMAIL);

            WebElement passwordInput = driver.findElement(By.name("password"));
            passwordInput.clear();
            passwordInput.sendKeys(LOGIN_PASSWORD);

            WebElement loginButton = driver.findElement(By.xpath("//button[contains(text(), 'Entrar')]"));
            loginButton.click();

            wait.until(ExpectedConditions.urlContains("home"));
        }
    }
}