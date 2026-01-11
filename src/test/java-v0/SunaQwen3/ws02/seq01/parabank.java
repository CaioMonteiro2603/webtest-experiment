package SunaQwen3.ws02.seq01;

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
public class parabank {

    private static WebDriver driver;
    private static WebDriverWait wait;
    private static final String BASE_URL = "https://parabank.parasoft.com/parabank/index.htm";
    private static final String USERNAME = "caio@gmail.com";
    private static final String PASSWORD = "123";

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
        assertEquals("ParaBank | Welcome | Online Banking", driver.getTitle());

        WebElement usernameField = wait.until(ExpectedConditions.presenceOfElementLocated(By.name("username")));
        WebElement passwordField = driver.findElement(By.name("password"));
        WebElement loginButton = driver.findElement(By.xpath("//input[@value='Log In']"));

        usernameField.sendKeys(USERNAME);
        passwordField.sendKeys(PASSWORD);
        loginButton.click();

        wait.until(ExpectedConditions.urlContains("overview.htm"));
        assertTrue(driver.getCurrentUrl().contains("overview.htm"));
        assertTrue(driver.getPageSource().contains("Accounts Overview"));
        assertTrue(driver.getPageSource().contains("Welcome"));
    }

    @Test
    @Order(2)
    void testInvalidLogin() {
        driver.get(BASE_URL);

        WebElement usernameField = wait.until(ExpectedConditions.presenceOfElementLocated(By.name("username")));
        WebElement passwordField = driver.findElement(By.name("password"));
        WebElement loginButton = driver.findElement(By.xpath("//input[@value='Log In']"));

        usernameField.sendKeys("invaliduser");
        passwordField.sendKeys("wrongpass");
        loginButton.click();

        WebElement error = wait.until(ExpectedConditions.presenceOfElementLocated(By.cssSelector(".error")));
        assertEquals("The username and password could not be verified.", error.getText());
    }

    @Test
    @Order(3)
    void testAccessAdminPage() {
        // After login from previous test, verify we can access admin
        driver.get("https://parabank.parasoft.com/parabank/admin.htm");

        wait.until(ExpectedConditions.presenceOfElementLocated(By.id("adminForm")));
        assertTrue(driver.getPageSource().contains("Parameter Name"));
        assertTrue(driver.getPageSource().contains("Parameter Value"));
    }

    @Test
    @Order(4)
    void testAccessServicesPage() {
        driver.get("https://parabank.parasoft.com/parabank/services.htm");

        wait.until(ExpectedConditions.presenceOfElementLocated(By.tagName("h1")));
        assertEquals("ParaBank Services", driver.getTitle());
        assertTrue(driver.getPageSource().contains("ParaBank offers a variety of services"));
    }

    @Test
    @Order(5)
    void testAccessAboutPage() {
        driver.get("https://parabank.parasoft.com/parabank/about.htm");

        wait.until(ExpectedConditions.presenceOfElementLocated(By.tagName("h1")));
        assertEquals("About ParaBank", driver.getTitle());
        assertTrue(driver.getPageSource().contains("About ParaBank"));
        assertTrue(driver.getPageSource().contains("ParaSoft Corporation"));
    }

    @Test
    @Order(6)
    void testAccessContactPage() {
        driver.get("https://parabank.parasoft.com/parabank/contact.htm");

        wait.until(ExpectedConditions.presenceOfElementLocated(By.name("NAME")));
        assertEquals("Contact ParaBank", driver.getTitle());
        assertTrue(driver.getPageSource().contains("Contact ParaBank"));
    }

    @Test
    @Order(7)
    void testExternalFooterLinks() {
        driver.get(BASE_URL);

        // Store original window handle
        String originalWindow = driver.getWindowHandle();

        // Test Facebook link
        WebElement facebookLink = wait.until(ExpectedConditions.presenceOfElementLocated(By.cssSelector("a[href='https://www.facebook.com/Parasoft']")));
        facebookLink.click();

        // Switch to new tab
        wait.until(ExpectedConditions.numberOfWindowsToBe(2));
        for (String windowHandle : driver.getWindowHandles()) {
            if (!windowHandle.equals(originalWindow)) {
                driver.switchTo().window(windowHandle);
                break;
            }
        }

        // Assert URL contains expected domain
        assertTrue(driver.getCurrentUrl().contains("facebook.com"));
        driver.close();

        // Switch back
        driver.switchTo().window(originalWindow);

        // Test Twitter link
        WebElement twitterLink = wait.until(ExpectedConditions.presenceOfElementLocated(By.cssSelector("a[href='https://twitter.com/parasoft']")));
        twitterLink.click();

        wait.until(ExpectedConditions.numberOfWindowsToBe(2));
        for (String windowHandle : driver.getWindowHandles()) {
            if (!windowHandle.equals(originalWindow)) {
                driver.switchTo().window(windowHandle);
                break;
            }
        }

        assertTrue(driver.getCurrentUrl().contains("twitter.com"));
        driver.close();
        driver.switchTo().window(originalWindow);

        // Test LinkedIn link
        WebElement linkedinLink = wait.until(ExpectedConditions.presenceOfElementLocated(By.cssSelector("a[href='https://www.linkedin.com/company/parasoft']")));
        linkedinLink.click();

        wait.until(ExpectedConditions.numberOfWindowsToBe(2));
        for (String windowHandle : driver.getWindowHandles()) {
            if (!windowHandle.equals(originalWindow)) {
                driver.switchTo().window(windowHandle);
                break;
            }
        }

        assertTrue(driver.getCurrentUrl().contains("linkedin.com"));
        driver.close();
        driver.switchTo().window(originalWindow);
    }

    @Test
    @Order(8)
    void testAccountServicesMenu() {
        driver.get(BASE_URL);

        // Login first
        WebElement usernameField = wait.until(ExpectedConditions.presenceOfElementLocated(By.name("username")));
        WebElement passwordField = driver.findElement(By.name("password"));
        WebElement loginButton = driver.findElement(By.xpath("//input[@value='Log In']"));

        usernameField.sendKeys(USERNAME);
        passwordField.sendKeys(PASSWORD);
        loginButton.click();

        wait.until(ExpectedConditions.urlContains("overview.htm"));

        // Click on Admin Page link in services
        WebElement adminLink = wait.until(ExpectedConditions.elementToBeClickable(By.linkText("Admin Page")));
        adminLink.click();

        wait.until(ExpectedConditions.presenceOfElementLocated(By.id("adminForm")));
        assertTrue(driver.getPageSource().contains("Parameter Name"));

        // Go back to overview
        driver.get("https://parabank.parasoft.com/parabank/overview.htm");
        wait.until(ExpectedConditions.presenceOfElementLocated(By.id("accountId")));

        // Click on Bill Pay
        WebElement billPayLink = wait.until(ExpectedConditions.elementToBeClickable(By.linkText("Bill Pay")));
        billPayLink.click();

        wait.until(ExpectedConditions.presenceOfElementLocated(By.name("payeeName")));
        assertTrue(driver.getPageSource().contains("Bill Payment Service"));

        // Go back and click Transfer Funds
        driver.get("https://parabank.parasoft.com/parabank/overview.htm");
        WebElement transferLink = wait.until(ExpectedConditions.elementToBeClickable(By.linkText("Transfer Funds")));
        transferLink.click();

        wait.until(ExpectedConditions.presenceOfElementLocated(By.name("fromAccountId")));
        assertTrue(driver.getPageSource().contains("Transfer Funds"));

        // Go back and click Update Contact Info
        driver.get("https://parabank.parasoft.com/parabank/overview.htm");
        WebElement updateInfoLink = wait.until(ExpectedConditions.elementToBeClickable(By.linkText("Update Contact Info")));
        updateInfoLink.click();

        wait.until(ExpectedConditions.presenceOfElementLocated(By.name("customer.firstName")));
        assertTrue(driver.getPageSource().contains("Update Profile"));

        // Go back and click Request Loan
        driver.get("https://parabank.parasoft.com/parabank/overview.htm");
        WebElement loanLink = wait.until(ExpectedConditions.elementToBeClickable(By.linkText("Request Loan")));
        loanLink.click();

        wait.until(ExpectedConditions.presenceOfElementLocated(By.name("amount")));
        assertTrue(driver.getPageSource().contains("Loan Request"));
    }

    @Test
    @Order(9)
    void testLogoutFunctionality() {
        // Ensure logged in
        driver.get("https://parabank.parasoft.com/parabank/overview.htm");
        if (!driver.getCurrentUrl().contains("overview.htm")) {
            // Login again
            driver.get(BASE_URL);
            WebElement usernameField = wait.until(ExpectedConditions.presenceOfElementLocated(By.name("username")));
            WebElement passwordField = driver.findElement(By.name("password"));
            WebElement loginButton = driver.findElement(By.xpath("//input[@value='Log In']"));

            usernameField.sendKeys(USERNAME);
            passwordField.sendKeys(PASSWORD);
            loginButton.click();

            wait.until(ExpectedConditions.urlContains("overview.htm"));
        }

        // Click logout
        WebElement logoutLink = wait.until(ExpectedConditions.elementToBeClickable(By.linkText("Log Out")));
        logoutLink.click();

        wait.until(ExpectedConditions.presenceOfElementLocated(By.name("username")));
        assertTrue(driver.getCurrentUrl().contains("index.htm"));
        assertTrue(driver.getPageSource().contains("Please enter your username and password"));
    }

    @Test
    @Order(10)
    void testRegisterNewUser() {
        driver.get(BASE_URL);

        WebElement registerLink = wait.until(ExpectedConditions.elementToBeClickable(By.linkText("Register")));
        registerLink.click();

        wait.until(ExpectedConditions.presenceOfElementLocated(By.id("customer.firstName")));

        // Fill registration form with test data
        driver.findElement(By.name("customer.firstName")).sendKeys("John");
        driver.findElement(By.name("customer.lastName")).sendKeys("Doe");
        driver.findElement(By.name("customer.address.street")).sendKeys("123 Main St");
        driver.findElement(By.name("customer.address.city")).sendKeys("Anytown");
        driver.findElement(By.name("customer.address.state")).sendKeys("CA");
        driver.findElement(By.name("customer.address.zipCode")).sendKeys("90210");
        driver.findElement(By.name("customer.phoneNumber")).sendKeys("555-123-4567");
        driver.findElement(By.name("customer.ssn")).sendKeys("123-45-6789");
        driver.findElement(By.name("username")).sendKeys("johndoe_test");
        driver.findElement(By.name("password")).sendKeys("test123");
        driver.findElement(By.name("repeatedPassword")).sendKeys("test123");

        WebElement registerButton = driver.findElement(By.xpath("//input[@value='Register']"));
        registerButton.click();

        // Wait for confirmation
        wait.until(ExpectedConditions.presenceOfElementLocated(By.className("title")));
        assertTrue(driver.getPageSource().contains("Your account was created successfully"));
        assertTrue(driver.getPageSource().contains("You are now logged in"));
    }
}