package Qwen3.ws02.seq02;

import org.junit.jupiter.api.*;
import org.openqa.selenium.*;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;

@TestMethodOrder(org.junit.jupiter.api.MethodOrderer.OrderAnnotation.class)
public class ParaBankTest {
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
        driver.get("https://parabank.parasoft.com/parabank/index.htm");
        
        WebElement usernameField = wait.until(ExpectedConditions.elementToBeClickable(By.name("username")));
        usernameField.sendKeys("caio@gmail.com");
        WebElement passwordField = driver.findElement(By.name("password"));
        passwordField.sendKeys("123");
        WebElement loginButton = driver.findElement(By.cssSelector("input[type='submit']"));
        loginButton.click();

        wait.until(ExpectedConditions.urlContains("welcome.htm"));
        assertTrue(driver.getCurrentUrl().contains("welcome.htm"));
        assertTrue(driver.getTitle().contains("ParaBank"));
    }

    @Test
    @Order(2)
    public void testInvalidCredentialsError() {
        driver.get("https://parabank.parasoft.com/parabank/index.htm");
        WebElement usernameField = wait.until(ExpectedConditions.elementToBeClickable(By.name("username")));
        usernameField.sendKeys("invalid@example.com");
        WebElement passwordField = driver.findElement(By.name("password"));
        passwordField.sendKeys("wrongpassword");
        WebElement loginButton = driver.findElement(By.cssSelector("input[type='submit']"));
        loginButton.click();

        WebElement errorElement = wait.until(ExpectedConditions.presenceOfElementLocated(By.cssSelector(".error")));
        assertTrue(errorElement.isDisplayed());
        assertTrue(errorElement.getText().contains("Authentication failed"));
    }

    @Test
    @Order(3)
    public void testMenuActions() throws InterruptedException {
        driver.get("https://parabank.parasoft.com/parabank/welcome.htm");
        
        // Test Home link
        WebElement homeLink = wait.until(ExpectedConditions.elementToBeClickable(By.linkText("Home")));
        homeLink.click();
        assertEquals("https://parabank.parasoft.com/parabank/index.htm", driver.getCurrentUrl());

        // Test Accounts Overview
        driver.get("https://parabank.parasoft.com/parabank/welcome.htm");
        WebElement accountsLink = wait.until(ExpectedConditions.elementToBeClickable(By.linkText("Accounts Overview")));
        accountsLink.click();
        assertTrue(driver.getCurrentUrl().contains("accounts.htm"));

        // Test Transfer Funds
        driver.get("https://parabank.parasoft.com/parabank/welcome.htm");
        WebElement transferFundsLink = wait.until(ExpectedConditions.elementToBeClickable(By.linkText("Transfer Funds")));
        transferFundsLink.click();
        assertTrue(driver.getCurrentUrl().contains("transfer.htm"));

        // Test Bill Pay
        driver.get("https://parabank.parasoft.com/parabank/welcome.htm");
        WebElement billPayLink = wait.until(ExpectedConditions.elementToBeClickable(By.linkText("Bill Pay")));
        billPayLink.click();
        assertTrue(driver.getCurrentUrl().contains("billpay.htm"));

        // Test Find Transactions
        driver.get("https://parabank.parasoft.com/parabank/welcome.htm");
        WebElement findTransactionsLink = wait.until(ExpectedConditions.elementToBeClickable(By.linkText("Find Transactions")));
        findTransactionsLink.click();
        assertTrue(driver.getCurrentUrl().contains("findtransactions.htm"));

        // Test Update Profile
        driver.get("https://parabank.parasoft.com/parabank/welcome.htm");
        WebElement updateProfileLink = wait.until(ExpectedConditions.elementToBeClickable(By.linkText("Update Profile")));
        updateProfileLink.click();
        assertTrue(driver.getCurrentUrl().contains("updateprofile.htm"));

        // Test Contact
        driver.get("https://parabank.parasoft.com/parabank/welcome.htm");
        WebElement contactLink = wait.until(ExpectedConditions.elementToBeClickable(By.linkText("Contact")));
        contactLink.click();
        assertTrue(driver.getCurrentUrl().contains("contact.htm"));

        // Test Logout
        driver.get("https://parabank.parasoft.com/parabank/welcome.htm");
        WebElement logoutLink = wait.until(ExpectedConditions.elementToBeClickable(By.linkText("Logout")));
        logoutLink.click();
        assertTrue(driver.getCurrentUrl().contains("index.htm"));
    }

    @Test
    @Order(4)
    public void testFooterLinks() {
        driver.get("https://parabank.parasoft.com/parabank/index.htm");
        
        // Test Terms & Conditions
        WebElement termsLink = driver.findElement(By.linkText("Terms & Conditions"));
        termsLink.click();
        String originalWindow = driver.getWindowHandle();
        for (String windowHandle : driver.getWindowHandles()) {
            if (!originalWindow.equals(windowHandle)) {
                driver.switchTo().window(windowHandle);
                assertTrue(driver.getCurrentUrl().contains("terms"));
                driver.close();
                break;
            }
        }
        driver.switchTo().window(originalWindow);

        // Test Privacy Policy
        driver.get("https://parabank.parasoft.com/parabank/index.htm");
        WebElement privacyLink = driver.findElement(By.linkText("Privacy Policy"));
        privacyLink.click();
        originalWindow = driver.getWindowHandle();
        for (String windowHandle : driver.getWindowHandles()) {
            if (!originalWindow.equals(windowHandle)) {
                driver.switchTo().window(windowHandle);
                assertTrue(driver.getCurrentUrl().contains("privacy"));
                driver.close();
                break;
            }
        }
        driver.switchTo().window(originalWindow);

        // Test Contact Us
        driver.get("https://parabank.parasoft.com/parabank/index.htm");
        WebElement contactUsLink = driver.findElement(By.linkText("Contact Us"));
        contactUsLink.click();
        originalWindow = driver.getWindowHandle();
        for (String windowHandle : driver.getWindowHandles()) {
            if (!originalWindow.equals(windowHandle)) {
                driver.switchTo().window(windowHandle);
                assertTrue(driver.getCurrentUrl().contains("contact.htm"));
                driver.close();
                break;
            }
        }
        driver.switchTo().window(originalWindow);
    }

    @Test
    @Order(5)
    public void testRegisterUser() {
        driver.get("https://parabank.parasoft.com/parabank/index.htm");
        WebElement registerLink = wait.until(ExpectedConditions.elementToBeClickable(By.linkText("Register")));
        registerLink.click();

        WebElement firstNameField = wait.until(ExpectedConditions.elementToBeClickable(By.name("customer.firstName")));
        firstNameField.sendKeys("John");
        WebElement lastNameField = driver.findElement(By.name("customer.lastName"));
        lastNameField.sendKeys("Doe");
        WebElement addressField = driver.findElement(By.name("customer.address.street"));
        addressField.sendKeys("123 Main St");
        WebElement cityField = driver.findElement(By.name("customer.address.city"));
        cityField.sendKeys("Anytown");
        WebElement stateField = driver.findElement(By.name("customer.address.state"));
        stateField.sendKeys("CA");
        WebElement zipCodeField = driver.findElement(By.name("customer.address.zipCode"));
        zipCodeField.sendKeys("12345");
        WebElement phoneField = driver.findElement(By.name("customer.phoneNumber"));
        phoneField.sendKeys("555-123-4567");
        WebElement ssnField = driver.findElement(By.name("customer.ssn"));
        ssnField.sendKeys("123-45-6789");
        WebElement usernameField = driver.findElement(By.name("customer.username"));
        usernameField.sendKeys("john_doe");
        WebElement passwordField = driver.findElement(By.name("customer.password"));
        passwordField.sendKeys("password123");
        WebElement confirmPasswordField = driver.findElement(By.name("repeatedPassword"));
        confirmPasswordField.sendKeys("password123");
        
        WebElement registerButton = driver.findElement(By.cssSelector("input[type='submit'][value='Register']"));
        registerButton.click();

        wait.until(ExpectedConditions.urlContains("customerinfo.htm"));
        assertTrue(driver.getCurrentUrl().contains("customerinfo.htm"));
        assertTrue(driver.getTitle().contains("Customer"));
    }

}