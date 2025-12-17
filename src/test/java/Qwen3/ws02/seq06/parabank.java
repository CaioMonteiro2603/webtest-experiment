package Qwen3.ws02.seq06;

import org.junit.jupiter.api.*;
import org.openqa.selenium.*;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;

import static org.junit.jupiter.api.Assertions.*;

@TestMethodOrder(org.junit.jupiter.api.MethodOrderer.OrderAnnotation.class)
public class parabank {

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

        String currentUrl = driver.getCurrentUrl();
        assertTrue(currentUrl.contains("welcome.htm"), "Expected to be on welcome page after login");

        WebElement welcomeMessage = driver.findElement(By.tagName("h1"));
        assertTrue(welcomeMessage.getText().contains("Welcome"), "Welcome message should be displayed");
    }

    @Test
    @Order(2)
    public void testInvalidLoginError() {
        driver.get("https://parabank.parasoft.com/parabank/index.htm");
        WebElement usernameField = wait.until(ExpectedConditions.elementToBeClickable(By.name("username")));
        usernameField.sendKeys("invalid@example.com");
        WebElement passwordField = driver.findElement(By.name("password"));
        passwordField.sendKeys("wrongpassword");
        WebElement loginButton = driver.findElement(By.cssSelector("input[type='submit']"));
        loginButton.click();

        WebElement errorMessage = driver.findElement(By.cssSelector(".error"));
        assertTrue(errorMessage.isDisplayed(), "Error message should be displayed for invalid login");

        String expectedErrorMessage = "Login was unsuccessful. Please enter a valid username and password.";
        assertEquals(expectedErrorMessage, errorMessage.getText(), "Error message should match expected text");
    }

    @Test
    @Order(3)
    public void testMenuNavigation() {
        driver.get("https://parabank.parasoft.com/parabank/index.htm");
        WebElement usernameField = wait.until(ExpectedConditions.elementToBeClickable(By.name("username")));
        usernameField.sendKeys("caio@gmail.com");
        WebElement passwordField = driver.findElement(By.name("password"));
        passwordField.sendKeys("123");
        WebElement loginButton = driver.findElement(By.cssSelector("input[type='submit']"));
        loginButton.click();

        // Click on 'Log Out'
        WebElement logoutLink = driver.findElement(By.linkText("Log Out"));
        logoutLink.click();
        assertEquals("https://parabank.parasoft.com/parabank/index.htm", driver.getCurrentUrl(), "Should be back on login page after logout");

        // Re-login
        usernameField = wait.until(ExpectedConditions.elementToBeClickable(By.name("username")));
        usernameField.sendKeys("caio@gmail.com");
        passwordField = driver.findElement(By.name("password"));
        passwordField.sendKeys("123");
        loginButton = driver.findElement(By.cssSelector("input[type='submit']"));
        loginButton.click();

        // Click on 'Services' menu item
        WebElement servicesLink = driver.findElement(By.linkText("Services"));
        servicesLink.click();
        assertTrue(driver.getCurrentUrl().contains("services.htm"), "Should navigate to Services page");

        // Navigate back to homepage
        driver.get("https://parabank.parasoft.com/parabank/index.htm");
        
        // Click on 'About Us' link in footer
        WebElement aboutUsLink = driver.findElement(By.linkText("About Us"));
        String oldTab = driver.getWindowHandle();
        aboutUsLink.click();
        String winHandle = driver.getWindowHandle();
        driver.switchTo().window(winHandle);
        assertTrue(driver.getCurrentUrl().contains("about.htm"), "Should navigate to About Us page");
        driver.close();
        driver.switchTo().window(oldTab);

        // Re-login again to continue testing
        usernameField = wait.until(ExpectedConditions.elementToBeClickable(By.name("username")));
        usernameField.sendKeys("caio@gmail.com");
        passwordField = driver.findElement(By.name("password"));
        passwordField.sendKeys("123");
        loginButton = driver.findElement(By.cssSelector("input[type='submit']"));
        loginButton.click();
        
        // Logout
        logoutLink = driver.findElement(By.linkText("Log Out"));
        logoutLink.click();
    }

    @Test
    @Order(4)
    public void testExternalLinksInFooter() {
        driver.get("https://parabank.parasoft.com/parabank/index.htm"); 
        
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
    public void testAccountCreationAndInfo() {
        driver.get("https://parabank.parasoft.com/parabank/index.htm");
        
        // Register a new account
        WebElement registerLink = driver.findElement(By.linkText("Register"));
        registerLink.click();
        assertTrue(driver.getCurrentUrl().contains("register.htm"), "Should navigate to registration page");

        // Fill out registration form
        WebElement firstNameField = driver.findElement(By.id("firstName"));
        firstNameField.sendKeys("John");
        WebElement lastNameField = driver.findElement(By.id("lastName"));
        lastNameField.sendKeys("Doe");
        WebElement addressField = driver.findElement(By.id("address"));
        addressField.sendKeys("123 Main St");
        WebElement cityField = driver.findElement(By.id("city"));
        cityField.sendKeys("Anytown");
        WebElement stateField = driver.findElement(By.id("state"));
        stateField.sendKeys("CA");
        WebElement zipCodeField = driver.findElement(By.id("zipCode"));
        zipCodeField.sendKeys("12345");
        WebElement phoneField = driver.findElement(By.id("phoneNumber"));
        phoneField.sendKeys("123-456-7890");
        WebElement ssnField = driver.findElement(By.id("ssn"));
        ssnField.sendKeys("123-45-6789");
        WebElement usernameField = driver.findElement(By.id("customerId"));
        usernameField.sendKeys("johndoe");
        WebElement passwordField = driver.findElement(By.id("password"));
        passwordField.sendKeys("mypassword");
        WebElement confirmPasswordField = driver.findElement(By.id("repeatedPassword"));
        confirmPasswordField.sendKeys("mypassword");

        WebElement registerButton = driver.findElement(By.cssSelector("input[type='submit'][value='Register']"));
        registerButton.click();

        // Check if registration was successful
        assertTrue(driver.getCurrentUrl().contains("register-complete.htm"), "Registration should be completed successfully");
    }
}