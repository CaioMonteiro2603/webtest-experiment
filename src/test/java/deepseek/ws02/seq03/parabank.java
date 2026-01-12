package deepseek.ws02.seq03;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation;
import org.junit.jupiter.api.TestMethodOrder;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.Select;
import org.openqa.selenium.support.ui.WebDriverWait;
import java.time.Duration;

@TestMethodOrder(OrderAnnotation.class)
public class parabank {
    private static WebDriver driver;
    private static WebDriverWait wait;
    private static final String BASE_URL = "https://parabank.parasoft.com/parabank/index.htm";
    private static final String USERNAME = "caio@gmail.com";
    private static final String PASSWORD = "123";

    @BeforeAll
    public static void setup() {
        FirefoxOptions options = new FirefoxOptions();
        options.addArguments("--headless");
        driver = new FirefoxDriver(options);
        wait = new WebDriverWait(driver, Duration.ofSeconds(10));
        createUser(driver);
    }

    private static void createUser(WebDriver driver) {
        driver.get("https://parabank.parasoft.com/parabank/register.htm");
        driver.findElement(By.id("customer.firstName")).click();
        driver.findElement(By.id("customer.firstName")).sendKeys("a");
        driver.findElement(By.id("customer.lastName")).click();
        driver.findElement(By.id("customer.lastName")).sendKeys("a");
        driver.findElement(By.id("customer.address.street")).click();
        driver.findElement(By.id("customer.address.street")).sendKeys("a");
        driver.findElement(By.id("customer.address.city")).click();
        driver.findElement(By.id("customer.address.city")).sendKeys("a");
        driver.findElement(By.id("customer.address.state")).click();
        driver.findElement(By.id("customer.address.state")).sendKeys("a");
        driver.findElement(By.id("customer.address.zipCode")).click();
        driver.findElement(By.id("customer.address.zipCode")).sendKeys("a");
        driver.findElement(By.id("customer.phoneNumber")).click();
        driver.findElement(By.id("customer.phoneNumber")).sendKeys("a");
        driver.findElement(By.id("customer.ssn")).click();
        driver.findElement(By.id("customer.ssn")).sendKeys("a");
        driver.findElement(By.id("customer.username")).click();
        driver.findElement(By.id("customer.username")).sendKeys("caio@gmail.com");
        driver.findElement(By.id("customer.password")).sendKeys("123");
        driver.findElement(By.id("repeatedPassword")).sendKeys("123");
        driver.findElement(By.cssSelector("td > .button")).click();
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
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.name("username"))).sendKeys(USERNAME);
        driver.findElement(By.name("password")).sendKeys(PASSWORD);
        driver.findElement(By.cssSelector("input[value='Log In']")).click();
        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        wait.until(ExpectedConditions.urlContains("overview.htm"));
        Assertions.assertTrue(driver.findElement(By.cssSelector("h1.title")).isDisplayed());
    }

    @Test
    @Order(2)
    public void testInvalidLogin() {
        driver.get(BASE_URL);
        driver.findElement(By.name("username")).sendKeys("invalid_user");
        driver.findElement(By.name("password")).sendKeys("wrong_password");
        driver.findElement(By.cssSelector("input[value='Log In']")).click();
        WebElement errorElement = wait.until(ExpectedConditions.visibilityOfElementLocated(By.className("error")));
        Assertions.assertTrue(errorElement.getText().contains("The username and password could not be verified"));
    }

    @Test
    @Order(3)
    public void testAccountNavigation() {
        loginIfNeeded();
        
        // Test Open New Account
        driver.findElement(By.linkText("Open New Account")).click();
        wait.until(ExpectedConditions.urlContains("openaccount.htm"));
        Assertions.assertTrue(driver.findElement(By.cssSelector("h1.title")).getText().contains("Open New Account"));
        
        // Test Accounts Overview
        driver.findElement(By.linkText("Accounts Overview")).click();
        wait.until(ExpectedConditions.urlContains("overview.htm"));
        Assertions.assertTrue(driver.findElement(By.cssSelector("h1.title")).getText().contains("Accounts Overview"));
    }

    @Test
    @Order(4)
    public void testTransferFunds() {
        loginIfNeeded();
        driver.findElement(By.linkText("Transfer Funds")).click();
        wait.until(ExpectedConditions.urlContains("transfer.htm"));
        
        // Perform transfer
        driver.findElement(By.id("amount")).sendKeys("100");
        new Select(driver.findElement(By.id("fromAccountId"))).selectByIndex(0);
        new Select(driver.findElement(By.id("toAccountId"))).selectByIndex(1);
        driver.findElement(By.cssSelector("input[value='Transfer']")).click();
        
        // Verify transfer completion
        WebElement successMessage = wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector("h1.title")));
        Assertions.assertTrue(successMessage.getText().contains("Transfer Complete!"));
    }

    @Test
    @Order(5)
    public void testFooterLinks() {
        loginIfNeeded();
        
        // Test About Us
        driver.findElement(By.linkText("About Us")).click();
        wait.until(ExpectedConditions.urlContains("about.htm"));
        Assertions.assertTrue(driver.getCurrentUrl().contains("about.htm"));
        
        // Test Services
        driver.findElement(By.linkText("Services")).click();
        wait.until(ExpectedConditions.urlContains("services.htm"));
        Assertions.assertTrue(driver.getCurrentUrl().contains("services.htm"));
        
        // Test Contact (external link)
        String originalWindow = driver.getWindowHandle();
        driver.findElement(By.linkText("Contact")).click();
        wait.until(ExpectedConditions.numberOfWindowsToBe(2));
        for (String windowHandle : driver.getWindowHandles()) {
            if (!originalWindow.equals(windowHandle)) {
                driver.switchTo().window(windowHandle);
                break;
            }
        }
        Assertions.assertTrue(driver.getCurrentUrl().contains("parasoft.com"));
        driver.close();
        driver.switchTo().window(originalWindow);
    }

    @Test
    @Order(6)
    public void testLogout() {
        loginIfNeeded();
        driver.findElement(By.linkText("Log Out")).click();
        wait.until(ExpectedConditions.urlContains("index.htm"));
        Assertions.assertTrue(driver.findElement(By.name("username")).isDisplayed());
    }

    private void loginIfNeeded() {
        if (!driver.getCurrentUrl().contains("overview.htm")) {
            driver.get(BASE_URL);
            wait.until(ExpectedConditions.visibilityOfElementLocated(By.name("username"))).sendKeys(USERNAME);
            driver.findElement(By.name("password")).sendKeys(PASSWORD);
            driver.findElement(By.cssSelector("input[value='Log In']")).click();
            try {
                Thread.sleep(2000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            wait.until(ExpectedConditions.urlContains("overview.htm"));
        }
    }
}