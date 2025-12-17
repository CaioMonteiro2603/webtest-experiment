package Qwen3.ws03.seq06;

import org.junit.jupiter.api.*;
import org.openqa.selenium.*;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@TestMethodOrder(org.junit.jupiter.api.MethodOrderer.OrderAnnotation.class)
public class bugbank {

    private static WebDriver driver;
    private static WebDriverWait wait;

    @BeforeAll
    public static void setUp() {
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
        WebElement emailField = wait.until(ExpectedConditions.elementToBeClickable(By.id("email")));
        emailField.sendKeys("caio@gmail.com");
        WebElement passwordField = driver.findElement(By.id("password"));
        passwordField.sendKeys("123");
        WebElement loginButton = driver.findElement(By.cssSelector("button[type='submit']"));
        loginButton.click();

        String currentUrl = driver.getCurrentUrl();
        assertTrue(currentUrl.contains("dashboard"), "Expected to be on dashboard page after login");

        WebElement dashboardHeader = driver.findElement(By.tagName("h1"));
        assertTrue(dashboardHeader.getText().contains("Dashboard"), "Dashboard header should be displayed");
    }

    @Test
    @Order(2)
    public void testInvalidLoginError() {
        driver.get("https://bugbank.netlify.app/");
        WebElement emailField = wait.until(ExpectedConditions.elementToBeClickable(By.id("email")));
        emailField.sendKeys("invalid@example.com");
        WebElement passwordField = driver.findElement(By.id("password"));
        passwordField.sendKeys("wrongpassword");
        WebElement loginButton = driver.findElement(By.cssSelector("button[type='submit']"));
        loginButton.click();

        WebElement errorMessage = driver.findElement(By.cssSelector(".error-message"));
        assertTrue(errorMessage.isDisplayed(), "Error message should be displayed for invalid login");

        String expectedErrorMessage = "Invalid credentials";
        assertEquals(expectedErrorMessage, errorMessage.getText(), "Error message should match expected text");
    }

    @Test
    @Order(3)
    public void testMenuNavigation() {
        driver.get("https://bugbank.netlify.app/");
        WebElement emailField = wait.until(ExpectedConditions.elementToBeClickable(By.id("email")));
        emailField.sendKeys("caio@gmail.com");
        WebElement passwordField = driver.findElement(By.id("password"));
        passwordField.sendKeys("123");
        WebElement loginButton = driver.findElement(By.cssSelector("button[type='submit']"));
        loginButton.click();

        // Click on menu button
        WebElement menuButton = driver.findElement(By.cssSelector(".menu-button"));
        menuButton.click();
        
        // Click on 'Accounts' menu item
        WebElement accountsLink = driver.findElement(By.linkText("Accounts"));
        accountsLink.click();
        assertTrue(driver.getCurrentUrl().contains("accounts"), "Should navigate to Accounts page");

        // Navigate back to dashboard
        driver.get("https://bugbank.netlify.app/dashboard");

        // Click on 'Transfers' menu item
        menuButton = driver.findElement(By.cssSelector(".menu-button"));
        menuButton.click();
        WebElement transfersLink = driver.findElement(By.linkText("Transfers"));
        transfersLink.click();
        assertTrue(driver.getCurrentUrl().contains("transfers"), "Should navigate to Transfers page");

        // Navigate back to dashboard
        driver.get("https://bugbank.netlify.app/dashboard");
        
        // Click on menu button again
        menuButton = driver.findElement(By.cssSelector(".menu-button"));
        menuButton.click();
        
        // Click on 'Logout'
        WebElement logoutLink = driver.findElement(By.linkText("Logout"));
        logoutLink.click();
        assertEquals("https://bugbank.netlify.app/", driver.getCurrentUrl(), "Should be back on login page after logout");
    }

    @Test
    @Order(4)
    public void testExternalLinksInFooter() {
        driver.get("https://bugbank.netlify.app/");
        
        // Twitter link
        WebElement twitterLink = driver.findElement(By.cssSelector("a[href*='twitter']"));
        String oldTab = driver.getWindowHandle();
        twitterLink.click();
        String winHandle = driver.getWindowHandle();
        driver.switchTo().window(winHandle);
        assertTrue(driver.getCurrentUrl().contains("twitter.com"), "Twitter link should navigate to Twitter website");
        driver.close();
        driver.switchTo().window(oldTab);

        // Facebook link
        WebElement facebookLink = driver.findElement(By.cssSelector("a[href*='facebook']"));
        oldTab = driver.getWindowHandle();
        facebookLink.click();
        winHandle = driver.getWindowHandle();
        driver.switchTo().window(winHandle);
        assertTrue(driver.getCurrentUrl().contains("facebook.com"), "Facebook link should navigate to Facebook website");
        driver.close();
        driver.switchTo().window(oldTab);

        // LinkedIn link
        WebElement linkedinLink = driver.findElement(By.cssSelector("a[href*='linkedin']"));
        oldTab = driver.getWindowHandle();
        linkedinLink.click();
        winHandle = driver.getWindowHandle();
        driver.switchTo().window(winHandle);
        assertTrue(driver.getCurrentUrl().contains("linkedin.com"), "LinkedIn link should navigate to LinkedIn website");
        driver.close();
        driver.switchTo().window(oldTab);
    }

    @Test
    @Order(5)
    public void testAccountManagement() {
        driver.get("https://bugbank.netlify.app/");
        WebElement emailField = wait.until(ExpectedConditions.elementToBeClickable(By.id("email")));
        emailField.sendKeys("caio@gmail.com");
        WebElement passwordField = driver.findElement(By.id("password"));
        passwordField.sendKeys("123");
        WebElement loginButton = driver.findElement(By.cssSelector("button[type='submit']"));
        loginButton.click();

        // Navigate to Accounts page
        WebElement menuButton = driver.findElement(By.cssSelector(".menu-button"));
        menuButton.click();
        WebElement accountsLink = driver.findElement(By.linkText("Accounts"));
        accountsLink.click();
        
        // Verify we are on accounts page
        assertTrue(driver.getCurrentUrl().contains("accounts"), "Should be on accounts page");
        
        // Check accounts list
        List<WebElement> accountItems = driver.findElements(By.cssSelector(".account-item"));
        assertTrue(accountItems.size() > 0, "Should have at least one account listed");
    }

    @Test
    @Order(6)
    public void testTransferFunctionality() {
        driver.get("https://bugbank.netlify.app/");
        WebElement emailField = wait.until(ExpectedConditions.elementToBeClickable(By.id("email")));
        emailField.sendKeys("caio@gmail.com");
        WebElement passwordField = driver.findElement(By.id("password"));
        passwordField.sendKeys("123");
        WebElement loginButton = driver.findElement(By.cssSelector("button[type='submit']"));
        loginButton.click();

        // Navigate to Transfers page
        WebElement menuButton = driver.findElement(By.cssSelector(".menu-button"));
        menuButton.click();
        WebElement transfersLink = driver.findElement(By.linkText("Transfers"));
        transfersLink.click();

        // Verify we are on transfers page
        assertTrue(driver.getCurrentUrl().contains("transfers"), "Should be on transfers page");
        
        // Test transfer field interaction (without actually sending money)
        WebElement fromAccountField = driver.findElement(By.id("fromAccount"));
        assertTrue(fromAccountField.isDisplayed(), "From account field should be displayed");
        
        WebElement toAccountField = driver.findElement(By.id("toAccount"));
        assertTrue(toAccountField.isDisplayed(), "To account field should be displayed");
        
        WebElement amountField = driver.findElement(By.id("amount"));
        assertTrue(amountField.isDisplayed(), "Amount field should be displayed");
    }
}