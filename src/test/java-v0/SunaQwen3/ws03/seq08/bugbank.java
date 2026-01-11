package SunaQwen3.ws03.seq08;

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

        WebElement emailField = wait.until(ExpectedConditions.visibilityOfElementLocated(By.name("email")));
        emailField.sendKeys(LOGIN_EMAIL);

        WebElement passwordField = driver.findElement(By.name("password"));
        passwordField.sendKeys(LOGIN_PASSWORD);

        WebElement loginButton = driver.findElement(By.xpath("//button[contains(text(),'Entrar')]"));
        wait.until(ExpectedConditions.elementToBeClickable(loginButton));
        loginButton.click();

        // Wait for navigation or UI change after login
        wait.until(ExpectedConditions.urlContains("dashboard"));
        assertTrue(driver.getCurrentUrl().contains("dashboard"), "URL should contain 'dashboard' after login");

        // Assert presence of expected elements on dashboard
        WebElement logoutButton = wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//button[contains(text(),'Sair')]")));
        assertTrue(logoutButton.isDisplayed(), "Logout button should be visible after login");
    }

    @Test
    @Order(2)
    void testInvalidLoginError() {
        driver.get(BASE_URL);

        WebElement emailField = wait.until(ExpectedConditions.visibilityOfElementLocated(By.name("email")));
        emailField.sendKeys("invalid@example.com");

        WebElement passwordField = driver.findElement(By.name("password"));
        passwordField.sendKeys("wrong");

        WebElement loginButton = driver.findElement(By.xpath("//button[contains(text(),'Entrar')]"));
        wait.until(ExpectedConditions.elementToBeClickable(loginButton));
        loginButton.click();

        // Wait for error message to appear
        WebElement errorMessage = wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector(".alert-danger")));
        assertTrue(errorMessage.isDisplayed(), "Error message should be displayed for invalid login");
        assertTrue(errorMessage.getText().toLowerCase().contains("usuário ou senha inválidos"),
                "Error message should indicate invalid credentials");
    }

    @Test
    @Order(3)
    void testNavigationMenuAllItems() {
        // Ensure logged in first
        loginIfNotOnDashboard();

        WebElement menuButton = wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector("button[aria-label='open drawer']")));
        menuButton.click();

        WebElement allItemsLink = wait.until(ExpectedConditions.elementToBeClickable(By.linkText("Todos os serviços")));
        allItemsLink.click();

        // Assert navigation to correct section
        WebElement servicesHeader = wait.until(ExpectedConditions.visibilityOfElementLocated(By.tagName("h2")));
        assertEquals("Serviços", servicesHeader.getText().trim(), "Should navigate to Services page");
    }

    @Test
    @Order(4)
    void testNavigationMenuAboutExternalLink() {
        // Ensure logged in first
        loginIfNotOnDashboard();

        WebElement menuButton = wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector("button[aria-label='open drawer']")));
        menuButton.click();

        WebElement aboutLink = wait.until(ExpectedConditions.elementToBeClickable(By.linkText("Sobre")));
        String originalWindow = driver.getWindowHandle();
        aboutLink.click();

        // Wait for new tab to open
        wait.until(d -> d.getWindowHandles().size() > 1);
        for (String windowHandle : driver.getWindowHandles()) {
            if (!windowHandle.equals(originalWindow)) {
                driver.switchTo().window(windowHandle);
                break;
            }
        }

        // Assert URL contains expected domain
        String aboutUrl = driver.getCurrentUrl();
        assertTrue(aboutUrl.contains("github") || aboutUrl.contains("bugbank"),
                "About link should navigate to a GitHub or project-related page: " + aboutUrl);

        // Close the new tab and switch back
        driver.close();
        driver.switchTo().window(originalWindow);
    }

    @Test
    @Order(5)
    void testLogoutFunctionality() {
        // Ensure logged in first
        loginIfNotOnDashboard();

        WebElement menuButton = wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector("button[aria-label='open drawer']")));
        menuButton.click();

        WebElement logoutButton = wait.until(ExpectedConditions.elementToBeClickable(By.xpath("//button[contains(text(),'Sair')]")));
        logoutButton.click();

        // Wait for logout confirmation or redirect
        wait.until(ExpectedConditions.urlToBe(BASE_URL));
        assertEquals(BASE_URL, driver.getCurrentUrl(), "Should return to login page after logout");

        // Verify login form is visible
        WebElement loginButton = wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//button[contains(text(),'Entrar')]")));
        assertTrue(loginButton.isDisplayed(), "Login button should be visible after logout");
    }

    @Test
    @Order(6)
    void testFooterSocialLinks() {
        // Ensure logged in first
        loginIfNotOnDashboard();

        // Find all footer social links
        List<WebElement> socialLinks = driver.findElements(By.cssSelector("footer a[href*='twitter'], footer a[href*='facebook'], footer a[href*='linkedin']"));

        String originalWindow = driver.getWindowHandle();

        for (WebElement link : socialLinks) {
            String href = link.getAttribute("href").toLowerCase();
            String linkText = link.getText().toLowerCase();
            String expectedDomain;
            if (href.contains("twitter") || linkText.contains("twitter")) {
                expectedDomain = "twitter.com";
            } else if (href.contains("facebook") || linkText.contains("facebook")) {
                expectedDomain = "facebook.com";
            } else if (href.contains("linkedin") || linkText.contains("linkedin")) {
                expectedDomain = "linkedin.com";
            } else {
                continue; // Skip unknown social links
            }

            // Open link in new tab
            ((JavascriptExecutor) driver).executeScript("window.open('" + href + "','_blank');");
            wait.until(d -> d.getWindowHandles().size() > 1);

            // Switch to new tab
            for (String windowHandle : driver.getWindowHandles()) {
                if (!windowHandle.equals(originalWindow)) {
                    driver.switchTo().window(windowHandle);
                    break;
                }
            }

            // Assert URL contains expected domain
            String currentUrl = driver.getCurrentUrl();
            assertTrue(currentUrl.contains(expectedDomain),
                    "Social link (" + expectedDomain + ") should open correct domain, but got: " + currentUrl);

            // Close tab and return
            driver.close();
            driver.switchTo().window(originalWindow);
        }
    }

    @Test
    @Order(7)
    void testResetAppState() {
        // Ensure logged in first
        loginIfNotOnDashboard();

        WebElement menuButton = wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector("button[aria-label='open drawer']")));
        menuButton.click();

        WebElement resetButton = wait.until(ExpectedConditions.elementToBeClickable(By.xpath("//button[contains(text(),'Resetar estado do app')]")));
        resetButton.click();

        // Confirm reset (if alert appears, accept it)
        try {
            Alert alert = wait.until(ExpectedConditions.alertIsPresent());
            alert.accept();
        } catch (Exception e) {
            // No alert present; continue
        }

        // Assert that app state has been reset (e.g., navigation back to dashboard)
        wait.until(ExpectedConditions.urlContains("dashboard"));
        assertTrue(driver.getCurrentUrl().contains("dashboard"), "Should return to dashboard after reset");
    }

    /**
     * Helper method to ensure the user is logged in and on the dashboard.
     * If not, performs login.
     */
    private void loginIfNotOnDashboard() {
        if (!driver.getCurrentUrl().contains("dashboard")) {
            driver.get(BASE_URL);

            try {
                WebElement emailField = wait.until(ExpectedConditions.visibilityOfElementLocated(By.name("email")));
                emailField.clear();
                emailField.sendKeys(LOGIN_EMAIL);

                WebElement passwordField = driver.findElement(By.name("password"));
                passwordField.clear();
                passwordField.sendKeys(LOGIN_PASSWORD);

                WebElement loginButton = driver.findElement(By.xpath("//button[contains(text(),'Entrar')]"));
                wait.until(ExpectedConditions.elementToBeClickable(loginButton));
                loginButton.click();

                // Wait for dashboard
                wait.until(ExpectedConditions.urlContains("dashboard"));
            } catch (Exception e) {
                fail("Failed to login during setup: " + e.getMessage());
            }
        }
    }
}
