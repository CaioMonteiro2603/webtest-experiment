package SunaQwen3.ws03.seq04;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation;
import org.openqa.selenium.*;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@TestMethodOrder(OrderAnnotation.class)
public class bugbank {

    private static WebDriver driver;
    private static WebDriverWait wait;
    private static final String BASE_URL = "https://bugbank.netlify.app/";
    private static final String LOGIN_EMAIL = "caio@gmail.com";
    private static final String PASSWORD = "123";

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
        passwordInput.sendKeys(PASSWORD);

        WebElement loginButton = driver.findElement(By.xpath("//button[contains(text(), 'Entrar')]"));
        loginButton.click();

        // Wait for navigation after login
        wait.until(ExpectedConditions.urlContains("home"));
        assertTrue(driver.getCurrentUrl().contains("home"), "URL should contain 'home' after login");

        WebElement logoutButton = wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//button[contains(text(), 'Sair')]")));
        assertTrue(logoutButton.isDisplayed(), "Logout button should be visible after login");
    }

    @Test
    @Order(2)
    void testInvalidLogin() {
        driver.get(BASE_URL);

        WebElement emailInput = wait.until(ExpectedConditions.visibilityOfElementLocated(By.name("email")));
        emailInput.sendKeys("invalid@example.com");

        WebElement passwordInput = driver.findElement(By.name("password"));
        passwordInput.sendKeys("wrong");

        WebElement loginButton = driver.findElement(By.xpath("//button[contains(text(), 'Entrar')]"));
        loginButton.click();

        // Wait for error message to appear
        WebElement errorMessage = wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector(".alert-danger")));
        assertTrue(errorMessage.isDisplayed(), "Error message should be displayed");
        assertTrue(errorMessage.getText().contains("Usuário ou senha inválidos"), "Error message should indicate invalid credentials");
    }

    @Test
    @Order(3)
    void testMenuNavigationAndResetAppState() {
        // Ensure logged in
        loginIfNotOnHomePage();

        WebElement menuButton = wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector(".navbar-burger")));
        menuButton.click();

        WebElement allItemsLink = wait.until(ExpectedConditions.elementToBeClickable(By.linkText("Todos os itens")));
        allItemsLink.click();

        // Verify we are still on home page (all items is default)
        wait.until(ExpectedConditions.urlContains("home"));
        assertTrue(driver.getCurrentUrl().contains("home"), "Should remain on home page after clicking 'Todos os itens'");

        // Open menu again
        menuButton = wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector(".navbar-burger")));
        menuButton.click();

        WebElement resetButton = wait.until(ExpectedConditions.elementToBeClickable(By.xpath("//button[contains(text(), 'Resetar estado do app')]")));
        resetButton.click();

        // Confirm reset dialog and wait for confirmation
        Alert alert = wait.until(ExpectedConditions.alertIsPresent());
        alert.accept();

        // Wait for any potential reload or state reset
        wait.until(ExpectedConditions.presenceOfElementLocated(By.cssSelector(".card-list")));
    }

    @Test
    @Order(4)
    void testLogoutFunctionality() {
        loginIfNotOnHomePage();

        WebElement menuButton = wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector(".navbar-burger")));
        menuButton.click();

        WebElement logoutButton = wait.until(ExpectedConditions.elementToBeClickable(By.xpath("//button[contains(text(), 'Sair')]")));
        logoutButton.click();

        // Wait for logout confirmation
        Alert alert = wait.until(ExpectedConditions.alertIsPresent());
        alert.accept();

        // Verify redirected to login page
        wait.until(ExpectedConditions.urlToBe(BASE_URL));
        assertEquals(BASE_URL, driver.getCurrentUrl(), "Should be redirected to login page after logout");

        WebElement loginButton = wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//button[contains(text(), 'Entrar')]")));
        assertTrue(loginButton.isDisplayed(), "Login button should be visible on login page");
    }

    @Test
    @Order(5)
    void testExternalLinksInFooter() {
        loginIfNotOnHomePage();

        List<WebElement> footerLinks = driver.findElements(By.cssSelector(".footer a"));
        assertFalse(footerLinks.isEmpty(), "Footer should contain social media links");

        for (WebElement link : footerLinks) {
            String originalWindow = driver.getWindowHandle();
            String linkText = link.getText().toLowerCase();

            // Determine expected domain based on link text
            String expectedDomain = "";
            if (linkText.contains("twitter")) {
                expectedDomain = "twitter.com";
            } else if (linkText.contains("facebook")) {
                expectedDomain = "facebook.com";
            } else if (linkText.contains("linkedin")) {
                expectedDomain = "linkedin.com";
            } else {
                continue; // Skip unknown links
            }

            // Open link in new tab (assume target="_blank")
            String script = "var newWindow = window.open('" + link.getAttribute("href") + "', '_blank');";
            ((JavascriptExecutor) driver).executeScript(script);

            // Switch to new tab
            wait.until(ExpectedConditions.numberOfWindowsToBe(2));
            for (String windowHandle : driver.getWindowHandles()) {
                if (!windowHandle.equals(originalWindow)) {
                    driver.switchTo().window(windowHandle);
                    break;
                }
            }

            // Assert URL contains expected domain
            wait.until(ExpectedConditions.urlContains(expectedDomain));
            assertTrue(driver.getCurrentUrl().contains(expectedDomain), "External page should be from " + expectedDomain);

            // Close external tab and switch back
            driver.close();
            driver.switchTo().window(originalWindow);
        }
    }

    /**
     * Helper method to ensure the user is logged in and on the home page.
     */
    private void loginIfNotOnHomePage() {
        if (!driver.getCurrentUrl().contains("home")) {
            driver.get(BASE_URL);

            // Check if already logged in by presence of logout button
            List<WebElement> logoutButtons = driver.findElements(By.xpath("//button[contains(text(), 'Sair')]"));
            if (!logoutButtons.isEmpty() && logoutButtons.get(0).isDisplayed()) {
                return; // Already logged in
            }

            // Perform login
            WebElement emailInput = wait.until(ExpectedConditions.visibilityOfElementLocated(By.name("email")));
            emailInput.clear();
            emailInput.sendKeys(LOGIN_EMAIL);

            WebElement passwordInput = driver.findElement(By.name("password"));
            passwordInput.clear();
            passwordInput.sendKeys(PASSWORD);

            WebElement loginButton = driver.findElement(By.xpath("//button[contains(text(), 'Entrar')]"));
            loginButton.click();

            // Wait for home page
            wait.until(ExpectedConditions.urlContains("home"));
            wait.until(ExpectedConditions.presenceOfElementLocated(By.xpath("//button[contains(text(), 'Sair')]")));
        }
    }
}