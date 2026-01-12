package Qwen3.ws02.seq01;

import org.junit.jupiter.api.*;
import org.openqa.selenium.*;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.openqa.selenium.support.ui.ExpectedConditions;
import java.time.Duration;

import static org.junit.jupiter.api.Assertions.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class parabank {

    private static WebDriver driver;
    private static WebDriverWait wait;

    @BeforeAll
    public static void setup() {
        FirefoxOptions options = new FirefoxOptions();
        options.addArguments("--headless");
        driver = new FirefoxDriver(options);
        wait = new WebDriverWait(driver, Duration.ofSeconds(10));
        driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(10));
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
        WebElement passwordField = driver.findElement(By.name("password"));
        WebElement loginButton = driver.findElement(By.xpath("//input[@value='Log In']"));

        usernameField.sendKeys("caio@gmail.com");
        passwordField.sendKeys("123");
        loginButton.click();

        wait.until(ExpectedConditions.urlContains("overview.htm"));
        String currentUrl = driver.getCurrentUrl();
        assertTrue(currentUrl.contains("overview.htm"), "Login should redirect to overview page");
    }

    @Test
    @Order(2)
    public void testInvalidLogin() {
        driver.get("https://parabank.parasoft.com/parabank/index.htm");

        WebElement usernameField = wait.until(ExpectedConditions.elementToBeClickable(By.name("username")));
        WebElement passwordField = driver.findElement(By.name("password"));
        WebElement loginButton = driver.findElement(By.xpath("//input[@value='Log In']"));

        usernameField.sendKeys("invalid_user");
        passwordField.sendKeys("wrong_password");
        loginButton.click();

        WebElement errorMessage = wait.until(ExpectedConditions.presenceOfElementLocated(By.className("error")));
        assertTrue(errorMessage.isDisplayed(), "Error message should be displayed on invalid login");
    }

    @Test
    @Order(3)
    public void testAccountOverview() {
        driver.get("https://parabank.parasoft.com/parabank/index.htm");

        WebElement usernameField = wait.until(ExpectedConditions.elementToBeClickable(By.name("username")));
        WebElement passwordField = driver.findElement(By.name("password"));
        WebElement loginButton = driver.findElement(By.xpath("//input[@value='Log In']"));

        usernameField.sendKeys("caio@gmail.com");
        passwordField.sendKeys("123");
        loginButton.click();

        // Wait for account overview to load
        wait.until(ExpectedConditions.urlContains("overview.htm"));

        // View account details
        WebElement accountLink = wait.until(ExpectedConditions.elementToBeClickable(By.linkText("Accounts Overview")));
        accountLink.click();

        String currentUrl = driver.getCurrentUrl();
        assertTrue(currentUrl.contains("overview.htm"), "Should navigate to accounts overview page");
    }

    @Test
    @Order(4)
    public void testTransferFunds() {
        driver.get("https://parabank.parasoft.com/parabank/index.htm");

        WebElement usernameField = wait.until(ExpectedConditions.elementToBeClickable(By.name("username")));
        WebElement passwordField = driver.findElement(By.name("password"));
        WebElement loginButton = driver.findElement(By.xpath("//input[@value='Log In']"));

        usernameField.sendKeys("caio@gmail.com");
        passwordField.sendKeys("123");
        loginButton.click();

        wait.until(ExpectedConditions.urlContains("overview.htm"));

        // Navigate to transfer funds
        WebElement transferLink = wait.until(ExpectedConditions.elementToBeClickable(By.linkText("Transfer Funds")));
        transferLink.click();

        String currentUrl = driver.getCurrentUrl();
        assertTrue(currentUrl.contains("transfer.htm"), "Should navigate to transfer funds page");

        // Fill form
        WebElement fromAccount = driver.findElement(By.id("fromAccountId"));
        WebElement toAccount = driver.findElement(By.id("toAccountId"));
        WebElement amount = driver.findElement(By.id("amount"));
        WebElement transferButton = driver.findElement(By.xpath("//input[@value='Transfer']"));

        fromAccount.sendKeys("12345");
        toAccount.sendKeys("12346");
        amount.sendKeys("100");

        transferButton.click();

        // Verify successful transfer
        WebElement successMessage = wait.until(ExpectedConditions.presenceOfElementLocated(By.className("title")));
        assertTrue(successMessage.getText().contains("Transfer Complete") || successMessage.getText().contains("Complete"), "Transfer should be complete successfully");
    }

    @Test
    @Order(5)
    public void testServices() {
        driver.get("https://parabank.parasoft.com/parabank/index.htm");

        // Click services link directly since we don't have a valid login to test other areas
        WebElement servicesLink = wait.until(ExpectedConditions.elementToBeClickable(By.linkText("Services")));
        servicesLink.click();

        String currentUrl = driver.getCurrentUrl();
        assertTrue(currentUrl.contains("services.htm"), "Should navigate to services page");
    }

    @Test
    @Order(6)
    public void testExternalLinksInFooter() {
        driver.get("https://parabank.parasoft.com/parabank/index.htm");
        String parentWindow = driver.getWindowHandle();

        // Store original window handles
        java.util.Set<String> originalWindows = driver.getWindowHandles();

        // Footer links - About Us
        WebElement aboutLink = driver.findElement(By.linkText("About Us"));
        aboutLink.click();
        
        // Wait for new window to open
        wait.until(ExpectedConditions.numberOfWindowsToBe(2));

        // Switch to new window
        java.util.Set<String> allWindows = driver.getWindowHandles();
        for (String window : allWindows) {
            if (!originalWindows.contains(window)) {
                driver.switchTo().window(window);
                break;
            }
        }

        String currentUrl = driver.getCurrentUrl();
        assertTrue(currentUrl.contains("about"), "Should open About Us link");
        
        // Close new window and switch back
        if (driver.getWindowHandles().size() > 1) {
            driver.close();
        }
        driver.switchTo().window(parentWindow);

        // Footer links - Contact Us
        WebElement contactLink = driver.findElement(By.linkText("Contact Us"));
        contactLink.click();

        // Wait for new window to open
        wait.until(ExpectedConditions.numberOfWindowsToBe(2));

        // Switch to new window
        allWindows = driver.getWindowHandles();
        for (String window : allWindows) {
            if (!originalWindows.contains(window)) {
                driver.switchTo().window(window);
                break;
            }
        }

        currentUrl = driver.getCurrentUrl();
        assertTrue(currentUrl.contains("contact"), "Should open Contact Us link");
        
        // Close new window and switch back
        if (driver.getWindowHandles().size() > 1) {
            driver.close();
        }
        driver.switchTo().window(parentWindow);
    }
}