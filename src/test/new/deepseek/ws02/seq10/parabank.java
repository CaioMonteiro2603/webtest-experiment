package deepseek.ws02.seq10;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import java.time.Duration;

@TestMethodOrder(OrderAnnotation.class)
public class parabank {
    private static WebDriver driver;
    private static final String BASE_URL = "https://parabank.parasoft.com/parabank/index.htm";
    private static final String LOGIN = "caio@gmail.com";
    private static final String PASSWORD = "123";
    private static WebDriverWait wait;

    @BeforeAll
    public static void setup() {
        FirefoxOptions options = new FirefoxOptions();
        options.addArguments("--headless");
        driver = new FirefoxDriver(options);
        wait = new WebDriverWait(driver, Duration.ofSeconds(10));
    }

    @AfterAll
    public static void teardown() {
        if (driver != null) {
            driver.quit();
        }
    }

    @Test
    @Order(1)
    public void testValidLogin() {
        driver.get(BASE_URL);
        WebElement username = wait.until(ExpectedConditions.presenceOfElementLocated(By.name("username")));
        WebElement password = driver.findElement(By.name("password"));
        WebElement loginButton = driver.findElement(By.xpath("//input[@value='Log In']"));

        username.sendKeys(LOGIN);
        password.sendKeys(PASSWORD);
        loginButton.click();

        wait.until(ExpectedConditions.urlContains("overview.htm"));
        WebElement welcomeMessage = driver.findElement(By.xpath("//h1[contains(text(), 'Accounts Overview')]"));
        Assertions.assertTrue(welcomeMessage.isDisplayed(), "Login failed - accounts overview not displayed");
    }

    @Test
    @Order(2)
    public void testInvalidLogin() {
        driver.get(BASE_URL);
        WebElement username = wait.until(ExpectedConditions.presenceOfElementLocated(By.name("username")));
        WebElement password = driver.findElement(By.name("password"));
        WebElement loginButton = driver.findElement(By.xpath("//input[@value='Log In']"));

        username.sendKeys("invalid@email.com");
        password.sendKeys("wrongpass");
        loginButton.click();

        WebElement errorElement = wait.until(ExpectedConditions.presenceOfElementLocated(
            By.xpath("//p[contains(text(), 'The username and password could not be verified.')]")));
        Assertions.assertTrue(errorElement.isDisplayed(), "Error message not displayed for invalid login");
    }

    @Test
    @Order(3)
    public void testAccountServicesNavigation() {
        loginIfNeeded();
        
        // Test Accounts Overview
        navigateAndVerify("//a[text()='Accounts Overview']", "overview.htm", "Accounts Overview");
        
        // Test Transfer Funds
        navigateAndVerify("//a[text()='Transfer Funds']", "transfer.htm", "Transfer Funds");
        
        // Test Bill Pay
        navigateAndVerify("//a[text()='Bill Pay']", "billpay.htm", "Bill Payment Service");
        
        // Test Find Transactions
        navigateAndVerify("//a[text()='Find Transactions']", "findtrans.htm", "Find Transactions");
        
        // Test Update Contact Info
        navigateAndVerify("//a[text()='Update Contact Info']", "updateprofile.htm", "Profile Updated");
    }

    @Test
    @Order(4)
    public void testFooterLinks() {
        loginIfNeeded();
        
        // Test About Us
        testExternalLink("About Us", "parasoft.com");
        
        // Test Services
        testExternalLink("Services", "parasoft.com");
        
        // Test Products
        testExternalLink("Products", "parasoft.com");
        
        // Test Locations
        testExternalLink("Locations", "parasoft.com");
        
        // Test Admin Page
        navigateAndVerify("//a[text()='Admin Page']", "admin.htm", "Administration");
    }

    @Test
    @Order(5)
    public void testLogout() {
        loginIfNeeded();
        
        WebElement logoutLink = wait.until(ExpectedConditions.elementToBeClickable(
            By.xpath("//a[text()='Log Out']")));
        logoutLink.click();
        
        wait.until(ExpectedConditions.urlContains("index.htm"));
        WebElement loginPanel = driver.findElement(By.id("leftPanel"));
        Assertions.assertTrue(loginPanel.isDisplayed(), "Logout failed - login panel not visible");
    }

    private void navigateAndVerify(String linkXpath, String expectedUrlPart, String expectedTitle) {
        WebElement link = wait.until(ExpectedConditions.elementToBeClickable(By.xpath(linkXpath)));
        link.click();
        
        wait.until(ExpectedConditions.urlContains(expectedUrlPart));
        WebElement titleElement = wait.until(ExpectedConditions.presenceOfElementLocated(
            By.xpath("//h1[contains(text(), '" + expectedTitle + "')]")));
        Assertions.assertTrue(titleElement.isDisplayed(), expectedTitle + " page not displayed");
    }

    private void testExternalLink(String linkText, String expectedDomain) {
        String mainWindow = driver.getWindowHandle();
        WebElement link = wait.until(ExpectedConditions.elementToBeClickable(
            By.xpath("//a[text()='" + linkText + "']")));
        link.click();
        
        // Switch to new window
        for (String windowHandle : driver.getWindowHandles()) {
            if (!windowHandle.equals(mainWindow)) {
                driver.switchTo().window(windowHandle);
                break;
            }
        }
        
        wait.until(ExpectedConditions.urlContains(expectedDomain));
        Assertions.assertTrue(driver.getCurrentUrl().contains(expectedDomain), 
            linkText + " link failed - wrong domain");
        driver.close();
        driver.switchTo().window(mainWindow);
    }

    private void loginIfNeeded() {
        if (!driver.getCurrentUrl().contains("overview.htm")) {
            driver.get(BASE_URL);
            WebElement username = wait.until(ExpectedConditions.presenceOfElementLocated(By.name("username")));
            WebElement password = driver.findElement(By.name("password"));
            WebElement loginButton = driver.findElement(By.xpath("//input[@value='Log In']"));

            username.clear();
            username.sendKeys(LOGIN);
            password.clear();
            password.sendKeys(PASSWORD);
            loginButton.click();
            
            try {
                wait.until(ExpectedConditions.urlContains("overview.htm"));
            } catch (Exception e) {
                // If still on login page, try clicking login button again
                if (driver.getCurrentUrl().contains("login.htm")) {
                    loginButton.click();
                    wait.until(ExpectedConditions.urlContains("overview.htm"));
                }
            }
        }
    }
}