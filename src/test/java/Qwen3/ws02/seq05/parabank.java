package Qwen3.ws02.seq05;

import org.junit.jupiter.api.*;
import org.openqa.selenium.*;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class parabank {

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
        driver.get("https://parabank.parasoft.com/parabank/index.htm");
        WebElement usernameField = wait.until(ExpectedConditions.elementToBeClickable(By.name("username")));
        usernameField.sendKeys("caio@gmail.com");
        WebElement passwordField = driver.findElement(By.name("password"));
        passwordField.sendKeys("123");
        WebElement loginButton = driver.findElement(By.cssSelector("input[type='submit']"));
        loginButton.click();

        String currentUrl = driver.getCurrentUrl();
        assertTrue(currentUrl.contains("parabank/services"), "Login should redirect to services page");
    }

    @Test
    @Order(2)
    public void testInvalidCredentialsError() {
        driver.get("https://parabank.parasoft.com/parabank/index.htm");
        WebElement usernameField = wait.until(ExpectedConditions.elementToBeClickable(By.name("username")));
        usernameField.sendKeys("invalid_user");
        WebElement passwordField = driver.findElement(By.name("password"));
        passwordField.sendKeys("invalid_password");
        WebElement loginButton = driver.findElement(By.cssSelector("input[type='submit']"));
        loginButton.click();

        WebElement errorElement = driver.findElement(By.cssSelector(".error"));
        assertTrue(errorElement.isDisplayed(), "Error message should be displayed for invalid credentials");
    }

    @Test
    @Order(3)
    public void testNavigationToServices() {
        driver.get("https://parabank.parasoft.com/parabank/index.htm");
        WebElement usernameField = wait.until(ExpectedConditions.elementToBeClickable(By.name("username")));
        usernameField.sendKeys("caio@gmail.com");
        WebElement passwordField = driver.findElement(By.name("password"));
        passwordField.sendKeys("123");
        WebElement loginButton = driver.findElement(By.cssSelector("input[type='submit']"));
        loginButton.click();

        WebElement servicesLink = driver.findElement(By.linkText("Services"));
        servicesLink.click();

        String currentUrl = driver.getCurrentUrl();
        assertTrue(currentUrl.contains("parabank/services"), "Should navigate to services page");
    }

    @Test
    @Order(4)
    public void testNavigationToAccountsOverview() {
        driver.get("https://parabank.parasoft.com/parabank/index.htm");
        WebElement usernameField = wait.until(ExpectedConditions.elementToBeClickable(By.name("username")));
        usernameField.sendKeys("caio@gmail.com");
        WebElement passwordField = driver.findElement(By.name("password"));
        passwordField.sendKeys("123");
        WebElement loginButton = driver.findElement(By.cssSelector("input[type='submit']"));
        loginButton.click();

        WebElement accountsLink = driver.findElement(By.linkText("Accounts Overview"));
        accountsLink.click();

        String currentUrl = driver.getCurrentUrl();
        assertTrue(currentUrl.contains("parabank/accounts"), "Should navigate to accounts overview page");
    }

    @Test
    @Order(5)
    public void testMenuNavigation() {
        driver.get("https://parabank.parasoft.com/parabank/index.htm");
        WebElement usernameField = wait.until(ExpectedConditions.elementToBeClickable(By.name("username")));
        usernameField.sendKeys("caio@gmail.com");
        WebElement passwordField = driver.findElement(By.name("password"));
        passwordField.sendKeys("123");
        WebElement loginButton = driver.findElement(By.cssSelector("input[type='submit']"));
        loginButton.click();

        // Click the menu toggle (if exists)
        try {
            WebElement menuToggle = driver.findElement(By.cssSelector(".menu-toggle"));
            menuToggle.click();
            wait.until(ExpectedConditions.elementToBeClickable(By.linkText("Accounts Overview")));
        } catch (NoSuchElementException ignored) {
            // menu not present
        }

        // Possibly there is a way to open the menu via other elements if not standard
        // But for parabank it may not be a menu toggle like in previous example

        // Navigate to Accounts Overview via URL directly
        driver.get("https://parabank.parasoft.com/parabank/accounts.htm");

        String currentPageUrl = driver.getCurrentUrl();
        assertTrue(currentPageUrl.contains("parabank/accounts"), "Should be at Accounts Overview page");

        // Navigate to Transfer Funds
        driver.get("https://parabank.parasoft.com/parabank/transfer.htm");

        String transferUrl = driver.getCurrentUrl();
        assertTrue(transferUrl.contains("parabank/transfer"), "Should navigate to Transfer Funds page");

        // Navigate to Update Profile
        driver.get("https://parabank.parasoft.com/parabank/update.htm");

        String updateProfileUrl = driver.getCurrentUrl();
        assertTrue(updateProfileUrl.contains("parabank/update"), "Should navigate to Update Profile page");
    }

    @Test
    @Order(6)
    public void testExternalLinksInFooter() {
        driver.get("https://parabank.parasoft.com/parabank/index.htm");

        List<WebElement> footerLinks = driver.findElements(By.cssSelector(".footer a"));
        assertEquals(3, footerLinks.size(), "Should have 3 external links in the footer");

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
    @Order(7)
    public void testHomeLinkFromLoggedInPage() {
        driver.get("https://parabank.parasoft.com/parabank/index.htm");
        WebElement usernameField = wait.until(ExpectedConditions.elementToBeClickable(By.name("username")));
        usernameField.sendKeys("caio@gmail.com");
        WebElement passwordField = driver.findElement(By.name("password"));
        passwordField.sendKeys("123");
        WebElement loginButton = driver.findElement(By.cssSelector("input[type='submit']"));
        loginButton.click();

        // At this point, we are already logged in
        // Try to navigate back to home via element
        try {
            WebElement homeLink = driver.findElement(By.linkText("Home"));
            homeLink.click();
        } catch (NoSuchElementException el) {
            // If no Link, by common practice, likely the main logo is the way back
            try {
                WebElement logoElement = driver.findElement(By.cssSelector(".logo a"));
                logoElement.click();
            } catch (NoSuchElementException e) {
                fail("Could not find Home link or logo to go back to homepage");
            }
        }

        String currentUrl = driver.getCurrentUrl();
        assertTrue(currentUrl.contains("parabank/index"), "Should redirect back to homepage");
    }

    @Test
    @Order(8)
    public void testLogoutFunctionality() {
        driver.get("https://parabank.parasoft.com/parabank/index.htm");
        WebElement usernameField = wait.until(ExpectedConditions.elementToBeClickable(By.name("username")));
        usernameField.sendKeys("caio@gmail.com");
        WebElement passwordField = driver.findElement(By.name("password"));
        passwordField.sendKeys("123");
        WebElement loginButton = driver.findElement(By.cssSelector("input[type='submit']"));
        loginButton.click();

        // Navigate to Logout page directly as it's a GET request typically
        driver.get("https://parabank.parasoft.com/parabank/logout.htm");

        String currentUrl = driver.getCurrentUrl();
        assertTrue(currentUrl.contains("parabank/index"), "Should redirect to index page after logout");

        // Verify that the login form now shows up
        WebElement loginForm = driver.findElement(By.id("loginForm"));
        assertTrue(loginForm.isDisplayed(), "Login form should be displayed after logout");
    }
}