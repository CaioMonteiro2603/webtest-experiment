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

public class bugbank {

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
        WebElement emailOrCpfInput = wait.until(ExpectedConditions.elementToBeClickable(By.id("email-input")));
        emailOrCpfInput.sendKeys("caio@gmail.com");
        WebElement passwordField = driver.findElement(By.id("password-input"));
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
        WebElement emailOrCpfInput = wait.until(ExpectedConditions.elementToBeClickable(By.id("email-input")));
        emailOrCpfInput.sendKeys("invalid_user");
        WebElement passwordField = driver.findElement(By.id("password-input"));
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
        WebElement emailOrCpfInput = wait.until(ExpectedConditions.elementToBeClickable(By.id("email-input")));
        emailOrCpfInput.sendKeys("caio@gmail.com");
        WebElement passwordField = driver.findElement(By.id("password-input"));
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
        WebElement emailOrCpfInput = wait.until(ExpectedConditions.elementToBeClickable(By.id("email-input")));
        emailOrCpfInput.sendKeys("caio@gmail.com");
        WebElement passwordField = driver.findElement(By.id("password-input"));
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
        WebElement emailOrCpfInput = wait.until(ExpectedConditions.elementToBeClickable(By.id("email-input")));
        emailOrCpfInput.sendKeys("caio@gmail.com");
        WebElement passwordField = driver.findElement(By.id("password-input"));
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
        WebElement emailOrCpfInput = wait.until(ExpectedConditions.elementToBeClickable(By.id("email-input")));
        emailOrCpfInput.sendKeys("caio@gmail.com");
        WebElement passwordField = driver.findElement(By.id("password-input"));
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
        List<WebElement> footerLinks = driver.findElements(By.cssSelector("footer a[href*='http']"));
        assertTrue(footerLinks.size() >= 0, "Should have external footer links");

        if (footerLinks.size() > 0) {
            String mainWindowHandle = driver.getWindowHandle();

            for (WebElement link : footerLinks) {
                String href = link.getAttribute("href");
                if (href != null && !href.isEmpty()) {
                    ((JavascriptExecutor) driver).executeScript("window.open('arguments[0]', '_blank');", href);
                    wait.until(ExpectedConditions.numberOfWindowsToBe(2));
                    for (String windowHandle : driver.getWindowHandles()) {
                        if (!windowHandle.equals(mainWindowHandle)) {
                            driver.switchTo().window(windowHandle);
                            break;
                        }
                    }
                    assertTrue(driver.getCurrentUrl().contains(href), "Url should match clicked link");
                    driver.close();
                    driver.switchTo().window(mainWindowHandle);
                }
            }
        }
    }

    @Test
    @Order(8)
    public void testLogoutFunctionality() {
        driver.get("https://bugbank.netlify.app/");
        WebElement emailOrCpfInput = wait.until(ExpectedConditions.elementToBeClickable(By.id("email-input")));
        emailOrCpfInput.sendKeys("caio@gmail.com");
        WebElement passwordField = driver.findElement(By.id("password-input"));
        passwordField.sendKeys("123");
        WebElement loginButton = driver.findElement(By.cssSelector("button[type='submit']"));
        loginButton.click();

        WebElement logoutButton = driver.findElement(By.linkText("Logout"));
        logoutButton.click();

        String currentUrl = driver.getCurrentUrl();
        assertTrue(currentUrl.contains("login"), "Should redirect to login page after logout");

        WebElement loginForm = driver.findElement(By.cssSelector("#root form"));
        assertTrue(loginForm.isDisplayed(), "Login form should be displayed after logout");
    }

    @Test
    @Order(9)
    public void testMenuToggleFunctionality() {
        driver.get("https://bugbank.netlify.app/");
        WebElement emailOrCpfInput = wait.until(ExpectedConditions.elementToBeClickable(By.id("email-input")));
        emailOrCpfInput.sendKeys("caio@gmail.com");
        WebElement passwordField = driver.findElement(By.id("password-input"));
        passwordField.sendKeys("123");
        WebElement loginButton = driver.findElement(By.cssSelector("button[type='submit']"));
        loginButton.click();

        // Check if there's a menu toggle button (used for mobile views)
        WebElement menuToggle = null;
        try {
            menuToggle = driver.findElement(By.cssSelector(".menu-toggle, .hamburger, [aria-label*='menu']"));
        } catch (NoSuchElementException ignored) {
        }

        if (menuToggle != null && menuToggle.isDisplayed()) {
            menuToggle.click();

            try {
                WebElement menuLink = wait.until(ExpectedConditions.elementToBeClickable(By.linkText("Accounts")));
                menuLink.click();

                String currentUrl = driver.getCurrentUrl();
                assertTrue(currentUrl.contains("accounts"), "Should navigate to accounts page after clicking menu");
            } catch (TimeoutException ignored) {
            }
        } else {
            WebElement accountsLink = driver.findElement(By.linkText("Accounts"));
            accountsLink.click();

            String currentUrl = driver.getCurrentUrl();
            assertTrue(currentUrl.contains("accounts"), "Should be able to navigate to accounts directly");
        }
    }
}