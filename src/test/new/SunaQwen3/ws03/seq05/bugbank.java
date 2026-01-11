package SunaQwen3.ws03.seq05;

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
import java.util.Set;

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
        assertTrue(driver.getTitle().contains("BugBank"), "Page title should contain 'BugBank'");

        WebElement emailInput = wait.until(ExpectedConditions.visibilityOfElementLocated(By.name("email")));
        emailInput.sendKeys(LOGIN_EMAIL);

        WebElement passwordInput = driver.findElement(By.name("password"));
        passwordInput.sendKeys(LOGIN_PASSWORD);

        WebElement loginButton = wait.until(ExpectedConditions.elementToBeClickable(By.xpath("//button[contains(text(), 'Entrar')]")));
        loginButton.click();

        // Wait for navigation after login
        wait.until(ExpectedConditions.urlContains("home"));
        assertTrue(driver.getCurrentUrl().contains("home"), "URL should contain 'home' after login");

        WebElement logoutButton = wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//button[contains(text(), 'Sair')]")));
        assertTrue(logoutButton.isDisplayed(), "Logout button should be visible after login");
    }

    @Test
    @Order(2)
    void testInvalidLoginCredentials() {
        driver.get(BASE_URL);

        WebElement emailInput = wait.until(ExpectedConditions.visibilityOfElementLocated(By.name("email")));
        emailInput.sendKeys("invalid@gmail.com");

        WebElement passwordInput = driver.findElement(By.name("password"));
        passwordInput.sendKeys("wrongpassword");

        WebElement loginButton = wait.until(ExpectedConditions.elementToBeClickable(By.xpath("//button[contains(text(), 'Entrar')]")));
        loginButton.click();

        // Wait for error message to appear
        WebElement errorMessage = wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector(".alert-danger")));
        assertTrue(errorMessage.isDisplayed(), "Error message should be displayed");
        assertTrue(errorMessage.getText().contains("usuário ou senha inválidos"), "Error message should indicate invalid credentials");
    }

    @Test
    @Order(3)
    void testMenuNavigationAndResetAppState() {
        // Ensure logged in
        loginIfNotOnHomePage();

        WebElement menuButton = wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector(".navbar-burger")));
        menuButton.click();

        WebElement allItemsLink = wait.until(ExpectedConditions.elementToBeClickable(By.xpath("//a[contains(text(), 'Todos os itens')]")));
        allItemsLink.click();

        // Verify navigation to home (all items)
        wait.until(ExpectedConditions.urlContains("home"));
        assertTrue(driver.getCurrentUrl().contains("home"), "Should navigate to home after clicking 'Todos os itens'");

        // Open menu again
        menuButton = wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector(".navbar-burger")));
        menuButton.click();

        WebElement resetButton = wait.until(ExpectedConditions.elementToBeClickable(By.xpath("//button[contains(text(), 'Resetar estado do app')]")));
        resetButton.click();

        // Confirm reset dialog if present
        try {
            WebElement confirmReset = driver.findElement(By.xpath("//button[contains(text(), 'Resetar')]"));
            if (confirmReset.isDisplayed()) {
                confirmReset.click();
            }
        } catch (NoSuchElementException e) {
            // Dialog might not appear; continue
        }

        // Wait to ensure reset completed
        wait.until(ExpectedConditions.presenceOfElementLocated(By.cssSelector(".product-card")));
    }

    @Test
    @Order(4)
    void testExternalLinksInFooter() {
        loginIfNotOnHomePage();

        // Find all footer social links
        List<WebElement> socialLinks = driver.findElements(By.cssSelector(".social-icons a"));

        assertNotNull(socialLinks);
        assertTrue(socialLinks.size() >= 3, "At least 3 social links should be present");

        for (int i = 0; i < socialLinks.size(); i++) {
            // Re-locate elements to avoid stale reference
            socialLinks = driver.findElements(By.cssSelector(".social-icons a"));
            WebElement link = socialLinks.get(i);

            String originalWindow = driver.getWindowHandle();
            link.click();

            // Wait for new window to appear
            String newWindow = wait.until(d -> {
                Set<String> handles = d.getWindowHandles();
                handles.remove(originalWindow);
                return handles.size() > 0 ? handles.iterator().next() : null;
            });

            driver.switchTo().window(newWindow);

            // Assert URL contains expected domain
            String currentUrl = driver.getCurrentUrl();
            if (currentUrl.contains("twitter.com")) {
                assertTrue(currentUrl.contains("twitter.com"), "Twitter link should open correct domain");
            } else if (currentUrl.contains("facebook.com")) {
                assertTrue(currentUrl.contains("facebook.com"), "Facebook link should open correct domain");
            } else if (currentUrl.contains("linkedin.com")) {
                assertTrue(currentUrl.contains("linkedin.com"), "LinkedIn link should open correct domain");
            } else {
                fail("Unexpected external link domain: " + currentUrl);
            }

            // Close new tab and switch back
            driver.close();
            driver.switchTo().window(originalWindow);
        }
    }

    @Test
    @Order(5)
    void testLogoutFunctionality() {
        loginIfNotOnHomePage();

        WebElement menuButton = wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector(".navbar-burger")));
        menuButton.click();

        WebElement logoutButton = wait.until(ExpectedConditions.elementToBeClickable(By.xpath("//button[contains(text(), 'Sair')]")));
        logoutButton.click();

        // Wait for logout confirmation
        wait.until(ExpectedConditions.urlToBe(BASE_URL));
        assertTrue(driver.getCurrentUrl().equals(BASE_URL), "Should return to base URL after logout");

        WebElement loginButton = wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//button[contains(text(), 'Entrar')]")));
        assertTrue(loginButton.isDisplayed(), "Login button should be visible after logout");
    }

    private void loginIfNotOnHomePage() {
        if (!driver.getCurrentUrl().contains("home")) {
            driver.get(BASE_URL);

            // Perform login
            WebElement emailInput = wait.until(ExpectedConditions.visibilityOfElementLocated(By.name("email")));
            emailInput.clear();
            emailInput.sendKeys(LOGIN_EMAIL);

            WebElement passwordInput = driver.findElement(By.name("password"));
            passwordInput.clear();
            passwordInput.sendKeys(LOGIN_PASSWORD);

            WebElement loginButton = wait.until(ExpectedConditions.elementToBeClickable(By.xpath("//button[contains(text(), 'Entrar')]")));
            loginButton.click();

            // Wait for home page
            wait.until(ExpectedConditions.urlContains("home"));
        }
    }
}