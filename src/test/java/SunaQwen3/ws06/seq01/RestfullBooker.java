package SunaQwen3.ws06.seq01;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation;
import org.openqa.selenium.*;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import java.time.Duration;
import static org.junit.jupiter.api.Assertions.*;

@TestMethodOrder(OrderAnnotation.class)
public class RestfullBooker {
    private static WebDriver driver;
    private static WebDriverWait wait;
    private static final String BASE_URL = "https://automationintesting.online/";

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
        assertEquals("https://automationintesting.online/#/admin", driver.getCurrentUrl(), "Redirects to admin page after login");
        assertTrue(driver.getTitle().contains("Admin"), "Page title contains 'Admin'");
    }

    @Test
    @Order(2)
    public void testInvalidLogin() {
        driver.get(BASE_URL);
        WebElement usernameField = wait.until(ExpectedConditions.visibilityOfElementLocated(By.name("username")));
        usernameField.clear();
        usernameField.sendKeys("invalid");
        WebElement passwordField = driver.findElement(By.name("password"));
        passwordField.clear();
        passwordField.sendKeys("wrong");
        WebElement loginButton = driver.findElement(By.cssSelector("button[type='submit']"));
        loginButton.click();
        WebElement errorMessage = wait.until(ExpectedConditions.visibilityOfElementLocated(By.className("alert")));
        assertTrue(errorMessage.getText().contains("Invalid"), "Error message appears on invalid login");
    }

    @Test
    @Order(3)
    public void testMenuNavigation() {
        driver.get(BASE_URL);
        WebElement menuButton = wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector(".navbar-toggler")));
        menuButton.click();
        WebElement allItemsLink = wait.until(ExpectedConditions.elementToBeClickable(By.linkText("All Items")));
        allItemsLink.click();
        assertTrue(driver.getCurrentUrl().contains("#/admin"), "Navigates to All Items page");
        menuButton = wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector(".navbar-toggler")));
        menuButton.click();
        WebElement aboutLink = wait.until(ExpectedConditions.elementToBeClickable(By.linkText("About")));
        aboutLink.click();
        String originalWindow = driver.getWindowHandle();
        for (String windowHandle : driver.getWindowHandles()) {
            if (!windowHandle.equals(originalWindow)) {
                driver.switchTo().window(windowHandle);
                break;
            }
        }
        assertTrue(driver.getCurrentUrl().contains("github.com"), "About link opens GitHub page");
        driver.close();
        driver.switchTo().window(originalWindow);
        menuButton = wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector(".navbar-toggler")));
        menuButton.click();
        WebElement logoutLink = wait.until(ExpectedConditions.elementToBeClickable(By.linkText("Logout")));
        logoutLink.click();
        wait.until(ExpectedConditions.urlContains("#/login"));
        assertEquals(BASE_URL + "#/login", driver.getCurrentUrl(), "Logout redirects to login page");
    }

    @Test
    @Order(4)
    public void testFooterSocialLinks() {
        driver.get(BASE_URL);
        String originalWindow = driver.getWindowHandle();
        WebElement twitterLink = wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector("footer a[href*='twitter']")));
        twitterLink.click();
        for (String windowHandle : driver.getWindowHandles()) {
            if (!windowHandle.equals(originalWindow)) {
                driver.switchTo().window(windowHandle);
                break;
            }
        }
        assertTrue(driver.getCurrentUrl().contains("twitter.com"), "Twitter link opens correct domain");
        driver.close();
        driver.switchTo().window(originalWindow);
        WebElement facebookLink = wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector("footer a[href*='facebook']")));
        facebookLink.click();
        for (String windowHandle : driver.getWindowHandles()) {
            if (!windowHandle.equals(originalWindow)) {
                driver.switchTo().window(windowHandle);
                break;
            }
        }
        assertTrue(driver.getCurrentUrl().contains("facebook.com"), "Facebook link opens correct domain");
        driver.close();
        driver.switchTo().window(originalWindow);
        WebElement linkedinLink = wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector("footer a[href*='linkedin']")));
        linkedinLink.click();
        for (String windowHandle : driver.getWindowHandles()) {
            if (!windowHandle.equals(originalWindow)) {
                driver.switchTo().window(windowHandle);
                break;
            }
        }
        assertTrue(driver.getCurrentUrl().contains("linkedin.com"), "LinkedIn link opens correct domain");
        driver.close();
        driver.switchTo().window(originalWindow);
    }

    @Test
    @Order(5)
    public void testResetAppState() {
        driver.get(BASE_URL);
        WebElement menuButton = wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector(".navbar-toggler")));
        menuButton.click();
        WebElement resetLink = wait.until(ExpectedConditions.elementToBeClickable(By.linkText("Reset App State")));
        resetLink.click();
        Alert alert = wait.until(ExpectedConditions.alertIsPresent());
        alert.accept();
        WebElement successMessage = wait.until(ExpectedConditions.visibilityOfElementLocated(By.className("alert")));
        assertTrue(successMessage.getText().contains("reset"), "App reset confirmation message appears");
    }
}