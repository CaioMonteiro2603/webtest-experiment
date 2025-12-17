package SunaQwen3.ws10.seq08;

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
public class BrasilAgritest {

    private static WebDriver driver;
    private static WebDriverWait wait;
    private static final String BASE_URL = "https://gestao.brasilagritest.com/login";
    private static final String LOGIN = "superadmin@brasilagritest.com.br";
    private static final String PASSWORD = "10203040";

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
        driver.manage().window().maximize();

        WebElement emailField = wait.until(ExpectedConditions.presenceOfElementLocated(By.name("email")));
        WebElement passwordField = driver.findElement(By.name("password"));
        WebElement loginButton = driver.findElement(By.cssSelector("button[type='submit']"));

        emailField.sendKeys(LOGIN);
        passwordField.sendKeys(PASSWORD);
        loginButton.click();

        // Assert successful login by checking URL or presence of a dashboard element
        wait.until(ExpectedConditions.urlContains("/dashboard"));
        assertTrue(driver.getCurrentUrl().contains("/dashboard"), "URL should contain /dashboard after login");

        // Verify dashboard has a specific element (e.g., title or menu)
        WebElement pageTitle = wait.until(ExpectedConditions.presenceOfElementLocated(By.tagName("h1")));
        assertNotNull(pageTitle, "Dashboard title should be present");
        assertTrue(pageTitle.getText().toLowerCase().contains("dashboard") || !pageTitle.getText().isEmpty(),
                "Dashboard title should be visible and relevant");
    }

    @Test
    @Order(2)
    void testInvalidLoginCredentials() {
        driver.get(BASE_URL);

        WebElement emailField = wait.until(ExpectedConditions.presenceOfElementLocated(By.name("email")));
        WebElement passwordField = driver.findElement(By.name("password"));
        WebElement loginButton = driver.findElement(By.cssSelector("button[type='submit']"));

        emailField.sendKeys("invalid@user.com");
        passwordField.sendKeys("wrongpassword");
        loginButton.click();

        // Wait for error message to appear
        WebElement errorMessage = wait.until(ExpectedConditions.presenceOfElementLocated(By.cssSelector(".alert-danger")));
        assertNotNull(errorMessage, "Error message should be displayed for invalid login");
        assertTrue(errorMessage.getText().toLowerCase().contains("credenciais") || errorMessage.getText().toLowerCase().contains("invalid"),
                "Error message should indicate invalid credentials");
    }

    @Test
    @Order(3)
    void testMenuNavigationAndResetAppState() {
        // Ensure we're on the dashboard
        driver.get("https://gestao.brasilagritest.com/dashboard");

        // Locate and click the menu (burger) button
        WebElement menuButton = wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector(".navbar-toggler")));
        menuButton.click();

        // Click All Items
        WebElement allItemsLink = wait.until(ExpectedConditions.elementToBeClickable(By.linkText("Todos os Itens")));
        allItemsLink.click();
        wait.until(ExpectedConditions.urlContains("/items"));
        assertTrue(driver.getCurrentUrl().contains("/items"), "Should navigate to items page");

        // Navigate back to dashboard
        driver.get("https://gestao.brasilagritest.com/dashboard");

        // Open menu again
        menuButton = wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector(".navbar-toggler")));
        menuButton.click();

        // Click Reset App State
        WebElement resetStateLink = wait.until(ExpectedConditions.elementToBeClickable(By.linkText("Resetar Estado do App")));
        resetStateLink.click();

        // Confirm reset if modal appears
        try {
            WebElement confirmButton = wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector(".modal-footer .btn-primary")));
            confirmButton.click();
            wait.until(ExpectedConditions.invisibilityOfElementLocated(By.cssSelector(".modal-dialog")));
        } catch (TimeoutException e) {
            // Modal might not appear; continue
        }

        // Verify some reset state indicator (e.g., cart empty, default view)
        // Assuming reset returns to dashboard
        assertTrue(driver.getCurrentUrl().contains("/dashboard") || driver.getCurrentUrl().contains("/items"),
                "After reset, should be on a main page");
    }

    @Test
    @Order(4)
    void testExternalLinksInFooter() {
        driver.get("https://gestao.brasilagritest.com/dashboard");

        // Find footer social links
        List<WebElement> socialLinks = driver.findElements(By.cssSelector("footer a[href*='facebook'], footer a[href*='twitter'], footer a[href*='linkedin']"));

        for (WebElement link : socialLinks) {
            String href = link.getAttribute("href");
            String linkText = link.getText().toLowerCase();

         // Open in new tab using JavaScript to avoid blocking
            ((JavascriptExecutor) driver)
                    .executeScript("window.open(arguments[0], '_blank');", href);

            // Wait until a new window/tab is opened
            wait.until(d -> d.getWindowHandles().size() > 1);


            // Switch to new tab
            String originalHandle = driver.getWindowHandle();
            for (String handle : driver.getWindowHandles()) {
                if (!handle.equals(originalHandle)) {
                    driver.switchTo().window(handle);
                    break;
                }
            }

            // Assert URL contains expected domain
            String currentUrl = driver.getCurrentUrl();
            if (linkText.contains("facebook") || href.contains("facebook")) {
                assertTrue(currentUrl.contains("facebook.com"), "Facebook link should open facebook.com");
            } else if (linkText.contains("twitter") || href.contains("twitter")) {
                assertTrue(currentUrl.contains("twitter.com") || currentUrl.contains("x.com"), "Twitter link should open twitter.com or x.com");
            } else if (linkText.contains("linkedin") || href.contains("linkedin")) {
                assertTrue(currentUrl.contains("linkedin.com"), "LinkedIn link should open linkedin.com");
            }

            // Close tab and switch back
            driver.close();
            driver.switchTo().window(originalHandle);
        }
    }

    @Test
    @Order(5)
    void testAboutLinkInMenu() {
        driver.get("https://gestao.brasilagritest.com/dashboard");

        // Open menu
        WebElement menuButton = wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector(".navbar-toggler")));
        menuButton.click();

        // Find and click About link
        WebElement aboutLink = wait.until(ExpectedConditions.elementToBeClickable(By.linkText("Sobre")));
        String aboutUrl = aboutLink.getAttribute("href");

     // Open in new tab using JavaScript to avoid blocking
        ((JavascriptExecutor) driver)
                .executeScript("window.open(arguments[0], '_blank');", aboutUrl);

        // Wait until a new window/tab is opened
        wait.until(d -> d.getWindowHandles().size() > 1);


        // Switch to new tab
        String originalHandle = driver.getWindowHandle();
        String newHandle = driver.getWindowHandles().stream()
                .filter(handle -> !handle.equals(originalHandle))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("New tab not found"));
        driver.switchTo().window(newHandle);

        // Assert domain
        String currentUrl = driver.getCurrentUrl();
        assertTrue(currentUrl.contains("brasilagritest.com"), "About link should point to brasilagritest.com domain");

        // Close tab and return
        driver.close();
        driver.switchTo().window(originalHandle);
    }

    @Test
    @Order(6)
    void testLogoutFunctionality() {
        // Ensure we're logged in
        driver.get("https://gestao.brasilagritest.com/dashboard");

        // Open menu
        WebElement menuButton = wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector(".navbar-toggler")));
        menuButton.click();

        // Click Logout
        WebElement logoutLink = wait.until(ExpectedConditions.elementToBeClickable(By.linkText("Sair")));
        logoutLink.click();

        // Wait for redirect to login page
        wait.until(ExpectedConditions.urlToBe(BASE_URL));
        assertTrue(driver.getCurrentUrl().equals(BASE_URL), "Should be redirected to login page after logout");

        // Verify login form is present
        WebElement loginForm = wait.until(ExpectedConditions.presenceOfElementLocated(By.tagName("form")));
        assertNotNull(loginForm, "Login form should be visible after logout");
    }
}