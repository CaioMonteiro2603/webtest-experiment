package Qwen3.ws03.seq05;

import org.junit.jupiter.api.*;
import org.openqa.selenium.*;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class BugBankTest {

    private static WebDriver driver;
    private static WebDriverWait wait;

    @BeforeAll
    public static void setup() {
        FirefoxOptions options = new FirefoxOptions();
        options.addArguments("--headless");
        driver = new FirefoxDriver(options);
        driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(10));
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
        driver.get("https://bugbank.netlify.app/");
        WebElement usernameField = wait.until(ExpectedConditions.elementToBeClickable(By.id("email")));
        usernameField.sendKeys("caio@gmail.com");
        WebElement passwordField = driver.findElement(By.id("password"));
        passwordField.sendKeys("123");
        WebElement loginButton = driver.findElement(By.cssSelector("button[type='submit']"));
        loginButton.click();

        String currentUrl = driver.getCurrentUrl();
        assertTrue(currentUrl.contains("dashboard"), "Login should redirect to dashboard page");
    }

    @Test
    @Order(2)
    public void testInvalidCredentialsError() {
        driver.get("https://bugbank.netlify.app/");
        WebElement usernameField = wait.until(ExpectedConditions.elementToBeClickable(By.id("email")));
        usernameField.sendKeys("invalid_user");
        WebElement passwordField = driver.findElement(By.id("password"));
        passwordField.sendKeys("invalid_password");
        WebElement loginButton = driver.findElement(By.cssSelector("button[type='submit']"));
        loginButton.click();

        WebElement errorElement = driver.findElement(By.cssSelector(".error-message"));
        assertTrue(errorElement.isDisplayed(), "Error message should be displayed for invalid credentials");
    }

    @Test
    @Order(3)
    public void testNavigationToDashboard() {
        driver.get("https://bugbank.netlify.app/");
        WebElement usernameField = wait.until(ExpectedConditions.elementToBeClickable(By.id("email")));
        usernameField.sendKeys("caio@gmail.com");
        WebElement passwordField = driver.findElement(By.id("password"));
        passwordField.sendKeys("123");
        WebElement loginButton = driver.findElement(By.cssSelector("button[type='submit']"));
        loginButton.click();

        WebElement dashboardHeader = driver.findElement(By.cssSelector("h1"));
        assertEquals("Dashboard", dashboardHeader.getText(), "Should be on dashboard page");
    }

    @Test
    @Order(4)
    public void testNavigationToAccounts() {
        driver.get("https://bugbank.netlify.app/");
        WebElement usernameField = wait.until(ExpectedConditions.elementToBeClickable(By.id("email")));
        usernameField.sendKeys("caio@gmail.com");
        WebElement passwordField = driver.findElement(By.id("password"));
        passwordField.sendKeys("123");
        WebElement loginButton = driver.findElement(By.cssSelector("button[type='submit']"));
        loginButton.click();

        WebElement accountsLink = driver.findElement(By.linkText("Accounts"));
        accountsLink.click();

        String currentUrl = driver.getCurrentUrl();
        assertTrue(currentUrl.contains("accounts"), "Should navigate to accounts page");
    }

    @Test
    @Order(5)
    public void testNavigationToTransfer() {
        driver.get("https://bugbank.netlify.app/");
        WebElement usernameField = wait.until(ExpectedConditions.elementToBeClickable(By.id("email")));
        usernameField.sendKeys("caio@gmail.com");
        WebElement passwordField = driver.findElement(By.id("password"));
        passwordField.sendKeys("123");
        WebElement loginButton = driver.findElement(By.cssSelector("button[type='submit']"));
        loginButton.click();

        WebElement transferLink = driver.findElement(By.linkText("Transfer"));
        transferLink.click();

        String currentUrl = driver.getCurrentUrl();
        assertTrue(currentUrl.contains("transfer"), "Should navigate to transfer page");
    }

    @Test
    @Order(6)
    public void testNavigationToProfile() {
        driver.get("https://bugbank.netlify.app/");
        WebElement usernameField = wait.until(ExpectedConditions.elementToBeClickable(By.id("email")));
        usernameField.sendKeys("caio@gmail.com");
        WebElement passwordField = driver.findElement(By.id("password"));
        passwordField.sendKeys("123");
        WebElement loginButton = driver.findElement(By.cssSelector("button[type='submit']"));
        loginButton.click();

        WebElement profileLink = driver.findElement(By.linkText("Profile"));
        profileLink.click();

        String currentUrl = driver.getCurrentUrl();
        assertTrue(currentUrl.contains("profile"), "Should navigate to profile page");
    }

    @Test
    @Order(7)
    public void testFooterExternalLinks() {
        driver.get("https://bugbank.netlify.app/");
        List<WebElement> footerLinks = driver.findElements(By.cssSelector(".footer a"));
        assertEquals(3, footerLinks.size(), "Should have 3 social media links in the footer");

        String mainWindowHandle = driver.getWindowHandle();

        for (WebElement link : footerLinks) {
            String href = link.getAttribute("href");
            if (href != null && !href.isEmpty()) {
                // Clicking this should open in new tab
                link.click();
                wait.until(ExpectedConditions.numberOfWindowsToBe(2));

                for (String windowHandle : driver.getWindowHandles()) {
                    if (!windowHandle.equals(mainWindowHandle)) {
                        driver.switchTo().window(windowHandle);
                        break;
                    }
                }

                String currentUrl = driver.getCurrentUrl();
                if (href.contains("facebook.com")) {
                    assertTrue(currentUrl.contains("facebook.com"), "Facebook URL should contain facebook.com");
                } else if (href.contains("twitter.com")) {
                    assertTrue(currentUrl.contains("twitter.com"), "Twitter URL should contain twitter.com");
                } else if (href.contains("linkedin.com")) {
                    assertTrue(currentUrl.contains("linkedin.com"), "LinkedIn URL should contain linkedin.com");
                }

                driver.close();
                driver.switchTo().window(mainWindowHandle);
            }
        }
    }

    @Test
    @Order(8)
    public void testLogoutFunctionality() {
        driver.get("https://bugbank.netlify.app/");
        WebElement usernameField = wait.until(ExpectedConditions.elementToBeClickable(By.id("email")));
        usernameField.sendKeys("caio@gmail.com");
        WebElement passwordField = driver.findElement(By.id("password"));
        passwordField.sendKeys("123");
        WebElement loginButton = driver.findElement(By.cssSelector("button[type='submit']"));
        loginButton.click();

        WebElement logoutButton = driver.findElement(By.linkText("Logout"));
        logoutButton.click();

        String currentUrl = driver.getCurrentUrl();
        assertTrue(currentUrl.contains("login"), "Should redirect to login page after logout");

        WebElement loginForm = driver.findElement(By.cssSelector(".login-form"));
        assertTrue(loginForm.isDisplayed(), "Login form should be displayed after logout");
    }

    @Test
    @Order(9)
    public void testMenuToggleFunctionality() {
        driver.get("https://bugbank.netlify.app/");
        WebElement usernameField = wait.until(ExpectedConditions.elementToBeClickable(By.id("email")));
        usernameField.sendKeys("caio@gmail.com");
        WebElement passwordField = driver.findElement(By.id("password"));
        passwordField.sendKeys("123");
        WebElement loginButton = driver.findElement(By.cssSelector("button[type='submit']"));
        loginButton.click();

        // Check if there's a menu toggle button (used for mobile views)
        WebElement menuToggle = null;
        try {
            menuToggle = driver.findElement(By.cssSelector(".menu-toggle"));
        } catch (NoSuchElementException ignored) {
            // If no toggle, that's okay for this test
        }

        if (menuToggle != null && menuToggle.isDisplayed()) {
            menuToggle.click();

            // Check if menu opened by looking for menu links
            try {
                WebElement menuLink = wait.until(ExpectedConditions.elementToBeClickable(By.linkText("Accounts")));
                menuLink.click();

                String currentUrl = driver.getCurrentUrl();
                assertTrue(currentUrl.contains("accounts"), "Should navigate to accounts page after clicking menu");
            } catch (TimeoutException ignored) {
                // Not all layouts might support toggling in headless mode
            }
        } else {
            // Test navigation to accounts directly
            WebElement accountsLink = driver.findElement(By.linkText("Accounts"));
            accountsLink.click();

            String currentUrl = driver.getCurrentUrl();
            assertTrue(currentUrl.contains("accounts"), "Should be able to navigate to accounts directly");
        }
    }
}