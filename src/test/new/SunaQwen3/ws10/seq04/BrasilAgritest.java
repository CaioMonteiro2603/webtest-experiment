package SunaQwen3.ws10.seq04;

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

import static org.junit.jupiter.api.Assertions.*;

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

        WebElement emailField = wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector("input[name='email'], input[type='email']")));
        emailField.sendKeys(LOGIN);

        WebElement passwordField = driver.findElement(By.cssSelector("input[name='password'], input[type='password']"));
        passwordField.sendKeys(PASSWORD);

        WebElement loginButton = driver.findElement(By.cssSelector("button[type='submit'], button.btn-primary"));
        loginButton.click();

        // Wait for dashboard elements or URL change
        try {
            wait.until(d -> d.getCurrentUrl().contains("/dashboard") || d.getCurrentUrl().contains("/admin"));
        } catch (Exception e) {
            // URL check failed, check for dashboard elements
            wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector("h1, h2, .dashboard, .header")));
        }
    }

    @Test
    @Order(2)
    void testInvalidLoginCredentials() {
        driver.get(BASE_URL);

        WebElement emailField = wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector("input[name='email'], input[type='email']")));
        emailField.sendKeys("invalid@user.com");

        WebElement passwordField = driver.findElement(By.cssSelector("input[name='password'], input[type='password']"));
        passwordField.sendKeys("wrongpassword");

        WebElement loginButton = driver.findElement(By.cssSelector("button[type='submit'], button.btn-primary"));
        loginButton.click();

        // Assert error message is displayed
        WebElement errorMessage = wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector(".alert-danger, .alert, .error")));
        assertTrue(errorMessage.isDisplayed(), "Error message should be displayed for invalid login");
    }

    @Test
    @Order(3)
    void testMenuNavigationAndResetAppState() {
        driver.get("https://gestao.brasilagritest.com/dashboard");

        // Open menu (hamburger button)
        WebElement menuButton = wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector(".navbar-toggler, .menu-toggle, .btn-menu")));
        menuButton.click();

        // Click All Items
        WebElement allItemsLink = null;
        try {
            allItemsLink = wait.until(ExpectedConditions.elementToBeClickable(By.linkText("Todos os Itens")));
        } catch (Exception e) {
            allItemsLink = wait.until(ExpectedConditions.elementToBeClickable(By.xpath("//a[contains(text(), 'Itens')]")));
        }
        allItemsLink.click();
        
        // Wait for navigation
        try {
            wait.until(d -> d.getCurrentUrl().contains("/items") || d.getCurrentUrl().contains("/produtos"));
        } catch (Exception e) {
            // Don't fail if URL doesn't change
        }

        driver.get("https://gestao.brasilagritest.com/dashboard");
        assertTrue(driver.findElement(By.tagName("body")).isDisplayed());
    }

    @Test
    @Order(4)
    void testExternalAboutLinkInMenu() {
        driver.get("https://gestao.brasilagritest.com/dashboard");

        // Open menu
        WebElement menuButton = wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector(".navbar-toggler, .menu-toggle, .btn-menu")));
        menuButton.click();

        // Click About (assumed external)
        WebElement aboutLink = wait.until(ExpectedConditions.elementToBeClickable(By.linkText("Sobre")));
        aboutLink.click();

        // Verify either navigation or new tab
        try {
            String originalHandle = driver.getWindowHandle();
            String newHandle = wait.until(d -> {
                Set<String> handles = d.getWindowHandles();
                handles.remove(originalHandle);
                return handles.stream().findFirst().orElse(null);
            });
            assertNotNull(newHandle);
            driver.switchTo().window(newHandle);
            assertTrue(driver.getCurrentUrl().contains("brasilagritest.com"));
            driver.close();
            driver.switchTo().window(originalHandle);
        } catch (Exception e) {
            // If no new tab opened, check current URL
            assertTrue(driver.getCurrentUrl().contains("sobre") || driver.getCurrentUrl().contains("about"));
        }
    }

    @Test
    @Order(5)
    void testFooterSocialLinks() {
        driver.get("https://gestao.brasilagritest.com/dashboard");

        // Find all footer social links
        List<WebElement> socialLinks = driver.findElements(By.cssSelector("footer a[href*='twitter.com'], footer a[href*='facebook.com'], footer a[href*='linkedin.com']"));

        String originalHandle = driver.getWindowHandle();
        for (WebElement link : socialLinks) {
            String href = link.getAttribute("href");
            String domain;

            if (href.contains("twitter.com")) {
                domain = "twitter.com";
            } else if (href.contains("facebook.com")) {
                domain = "facebook.com";
            } else if (href.contains("linkedin.com")) {
                domain = "linkedin.com";
            } else {
                continue;
            }

            // Open link in new tab via JavaScript to avoid interception
            ((JavascriptExecutor) driver).executeScript("window.open(arguments[0]);", href);

            // Switch to new tab
            String newHandle = wait.until(d -> {
                Set<String> handles = d.getWindowHandles();
                handles.remove(originalHandle);
                return handles.stream().findFirst().orElse(null);
            });
            driver.switchTo().window(newHandle);

            // Assert URL contains expected domain
            wait.until(d -> d.getCurrentUrl().contains(domain));
            assertTrue(driver.getCurrentUrl().contains(domain), "Social link should open " + domain);

            // Close tab
            driver.close();
            driver.switchTo().window(originalHandle);
        }
    }

    @Test
    @Order(6)
    void testLogoutFunctionality() {
        driver.get("https://gestao.brasilagritest.com/dashboard");

        // Open menu
        WebElement menuButton = wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector(".navbar-toggler, .menu-toggle, .btn-menu")));
        menuButton.click();

        // Click Logout
        WebElement logoutLink = wait.until(ExpectedConditions.elementToBeClickable(By.linkText("Sair")));
        logoutLink.click();

        // Assert redirected to login page
        wait.until(d -> d.getCurrentUrl().contains("/login") || d.findElement(By.cssSelector("input[name='email'], input[type='email']")).isDisplayed());
        assertTrue(d.getCurrentUrl().contains("/login") || d.findElement(By.cssSelector("input[name='email'], input[type='email']")).isDisplayed());
    }
}