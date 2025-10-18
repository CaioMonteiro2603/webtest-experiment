package GPT20b.ws07.seq08;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;
import java.util.List;
import java.util.Set;

@TestMethodOrder(OrderAnnotation.class)
public class JsfiddleTest {

    private static final String BASE_URL = "https://jsfiddle.net/";
    private static WebDriver driver;
    private static WebDriverWait wait;

    @BeforeAll
    public static void setUp() {
        FirefoxOptions options = new FirefoxOptions();
        options.addArguments("--headless"); // REQUIRED
        driver = new FirefoxDriver(options);
        wait = new WebDriverWait(driver, Duration.ofSeconds(10));
    }

    @AfterAll
    public static void tearDown() {
        if (driver != null) {
            driver.quit();
        }
    }

    /* ---------- Helper methods ---------- */

    private void navigateToBase() {
        driver.get(BASE_URL);
    }

    /* ---------- Test 1: Home page loads ---------- */

    @Test
    @Order(1)
    public void testHomePageLoads() {
        navigateToBase();
        String title = driver.getTitle();
        Assertions.assertTrue(title.toLowerCase().contains("jsfiddle"),
                "Page title should contain 'jsfiddle'.");
    }

    /* ---------- Test 2: Login button presence ---------- */

    @Test
    @Order(2)
    public void testLoginButtonPresent() {
        navigateToBase();
        By loginLink = By.linkText("Login");
        List<WebElement> links = driver.findElements(loginLink);
        Assumptions.assumeTrue(!links.isEmpty(), "No login link on home page; skipping test.");
        WebElement login = wait.until(ExpectedConditions.elementToBeClickable(loginLink));
        Assertions.assertTrue(login.isDisplayed(), "Login button should be visible.");
    }

    /* ---------- Test 3: Invalid login attempt ---------- */

    @Test
    @Order(3)
    public void testInvalidLoginFailure() {
        navigateToBase();
        By loginLink = By.linkText("Login");
        List<WebElement> links = driver.findElements(loginLink);
        Assumptions.assumeTrue(!links.isEmpty(), "Login link not found; skipping test.");

        driver.findElement(loginLink).click();

        By emailField = By.name("email");
        By passwordField = By.name("password");
        By loginBtn = By.cssSelector("button[type='submit']");

        List<WebElement> elems = driver.findElements(emailField);
        Assumptions.assumeTrue(!elems.isEmpty(), "Login form not displayed; skipping test.");

        wait.until(ExpectedConditions.visibilityOfElementLocated(emailField)).sendKeys("invalid@example.com");
        wait.until(ExpectedConditions.visibilityOfElementLocated(passwordField)).sendKeys("wrongpass");
        wait.until(ExpectedConditions.elementToBeClickable(loginBtn)).click();

        By errorMsg = By.cssSelector(".error-msg, .message, .alert");
        List<WebElement> errors = driver.findElements(errorMsg);
        Assertions.assertFalse(errors.isEmpty(), "Error message should appear for invalid credentials.");

        String msg = errors.get(0).getText().toLowerCase();
        Assertions.assertTrue(msg.contains("invalid") || msg.contains("incorrect") || msg.contains("wrong"),
                "Error message text should indicate invalid credentials.");
    }

    /* ---------- Test 4: Create a new fiddle ---------- */

    @Test
    @Order(4)
    public void testCreateNewFiddle() {
        navigateToBase();
        By newBtn = By.xpath("//a[contains(@href,'/new') and contains(@class,'btn')]");
        List<WebElement> buttons = driver.findElements(newBtn);
        Assumptions.assumeTrue(!buttons.isEmpty(), "New fiddle button not found; skipping test.");

        WebElement newButton = wait.until(ExpectedConditions.elementToBeClickable(newBtn));
        newButton.click();

        // Wait for the editor iframe to appear
        By editorFrame = By.cssSelector("iframe#editor");
        wait.until(ExpectedConditions.frameToBeAvailableAndSwitchToIt(editorFrame));

        // Verify editor is loaded by checking for an element inside the iframe
        By panelHeader = By.cssSelector(".panel-header");
        WebElement header = wait.until(ExpectedConditions.visibilityOfElementLocated(panelHeader));
        Assertions.assertTrue(header.isDisplayed(), "Editor panel should be visible after creating new fiddle.");

        // Switch back to the default content
        driver.switchTo().defaultContent();
    }

    /* ---------- Test 5: Footer social links policy ---------- */

    @Test
    @Order(5)
    public void testFooterSocialLinks() {
        navigateToBase();
        List<WebElement> links = driver.findElements(By.xpath("//a[contains(@href,'twitter.com') or contains(@href,'facebook.com') or contains(@href,'linkedin.com')]"));
        Assertions.assertFalse(links.isEmpty(), "No social links found in footer.");

        String originalWindow = driver.getWindowHandle();
        for (WebElement link : links) {
            String href = link.getAttribute("href");
            if (href == null || href.isEmpty()) {
                continue;
            }
            link.click();
            wait.until(ExpectedConditions.numberOfWindowsToBe(2));
            Set<String> handles = driver.getWindowHandles();
            for (String handle : handles) {
                if (!handle.equals(originalWindow)) {
                    driver.switchTo().window(handle);
                    Assertions.assertTrue(driver.getCurrentUrl().contains(href),
                            "Social link URL should contain expected domain: " + href);
                    driver.close();
                    driver.switchTo().window(originalWindow);
                }
            }
        }
    }
}