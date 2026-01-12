package Qwen3.ws02.seq07;

import org.junit.jupiter.api.*;
import org.openqa.selenium.*;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

public class parabank {

    private static WebDriver driver;
    private static WebDriverWait wait;

    @BeforeAll
    public static void setUp() {
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
        WebElement loginButton = driver.findElement(By.xpath("//input[@value='Log In']"));
        loginButton.click();

        wait.until(ExpectedConditions.urlContains("overview.htm"));
        assertEquals("https://parabank.parasoft.com/parabank/overview.htm", driver.getCurrentUrl());
        assertTrue(driver.findElement(By.tagName("h1")).getText().contains("Welcome"));
    }

    @Test
    @Order(2)
    public void testInvalidLogin() {
        driver.get("https://parabank.parasoft.com/parabank/index.htm");
        WebElement usernameField = wait.until(ExpectedConditions.elementToBeClickable(By.name("username")));
        usernameField.sendKeys("invalid@gmail.com");
        WebElement passwordField = driver.findElement(By.name("password"));
        passwordField.sendKeys("invalid");
        WebElement loginButton = driver.findElement(By.xpath("//input[@value='Log In']"));
        loginButton.click();

        WebElement errorElement = wait.until(ExpectedConditions.presenceOfElementLocated(By.className("error")));
        assertTrue(errorElement.isDisplayed());
        assertTrue(errorElement.getText().contains("invalid"));
    }

    @Test
    @Order(3)
    public void testForgotPassword() {
        driver.get("https://parabank.parasoft.com/parabank/index.htm");
        WebElement forgotPasswordLink = wait.until(ExpectedConditions.elementToBeClickable(By.xpath("//a[contains(text(),'Forgot login info?')]")));
        forgotPasswordLink.click();

        wait.until(ExpectedConditions.urlContains("lookup.htm"));
        assertEquals("https://parabank.parasoft.com/parabank/lookup.htm", driver.getCurrentUrl());
    }

    @Test
    @Order(4)
    public void testRegister() {
        driver.get("https://parabank.parasoft.com/parabank/index.htm");
        WebElement registerLink = wait.until(ExpectedConditions.elementToBeClickable(By.linkText("Register")));
        registerLink.click();

        wait.until(ExpectedConditions.urlContains("register.htm"));
        assertEquals("https://parabank.parasoft.com/parabank/register.htm", driver.getCurrentUrl());
    }

    @Test
    @Order(5)
    public void testNavigationMenu() {
        driver.get("https://parabank.parasoft.com/parabank/overview.htm");

        // Test Home link
        WebElement homeLink = driver.findElement(By.xpath("//a[@href='index.htm']"));
        homeLink.click();
        wait.until(ExpectedConditions.urlContains("index.htm"));
        assertEquals("https://parabank.parasoft.com/parabank/index.htm", driver.getCurrentUrl());

        // Test About Us link
        WebElement aboutLink = driver.findElement(By.linkText("About Us"));
        aboutLink.click();
        Set<String> windowHandles = driver.getWindowHandles();
        assertEquals(2, windowHandles.size());
        String mainWindowHandle = driver.getWindowHandle();
        String newWindowHandle = null;
        for (String handle : windowHandles) {
            if (!handle.equals(mainWindowHandle)) {
                newWindowHandle = handle;
                break;
            }
        }
        driver.switchTo().window(newWindowHandle);
        assertTrue(driver.getCurrentUrl().contains("parasoft.com"));
        driver.close();
        driver.switchTo().window(mainWindowHandle);

        // Go back to home and test Services
        driver.get("https://parabank.parasoft.com/parabank/index.htm");
        WebElement servicesLink = driver.findElement(By.linkText("Services"));
        servicesLink.click();
        wait.until(ExpectedConditions.urlContains("services.htm"));
        assertEquals("https://parabank.parasoft.com/parabank/services.htm", driver.getCurrentUrl());

        // Go back to home and test Contact
        driver.get("https://parabank.parasoft.com/parabank/index.htm");
        WebElement contactLink = driver.findElement(By.linkText("Contact"));
        contactLink.click();
        wait.until(ExpectedConditions.urlContains("contact.htm"));
        assertEquals("https://parabank.parasoft.com/parabank/contact.htm", driver.getCurrentUrl());
    }

    @Test
    @Order(6)
    public void testFooterLinks() {
        driver.get("https://parabank.parasoft.com/parabank/overview.htm");

        // Test Twitter link
        WebElement twitterLink = driver.findElement(By.cssSelector("a[href*='twitter']"));
        twitterLink.click();
        Set<String> windowHandles = driver.getWindowHandles();
        assertEquals(2, windowHandles.size());
        String mainWindowHandle = driver.getWindowHandle();
        String newWindowHandle = null;
        for (String handle : windowHandles) {
            if (!handle.equals(mainWindowHandle)) {
                newWindowHandle = handle;
                break;
            }
        }
        driver.switchTo().window(newWindowHandle);
        assertTrue(driver.getCurrentUrl().contains("twitter.com"));
        driver.close();
        driver.switchTo().window(mainWindowHandle);

        // Test Facebook link
        WebElement facebookLink = driver.findElement(By.cssSelector("a[href*='facebook']"));
        facebookLink.click();
        windowHandles = driver.getWindowHandles();
        assertEquals(2, windowHandles.size());
        mainWindowHandle = driver.getWindowHandle();
        newWindowHandle = null;
        for (String handle : windowHandles) {
            if (!handle.equals(mainWindowHandle)) {
                newWindowHandle = handle;
                break;
            }
        }
        driver.switchTo().window(newWindowHandle);
        assertTrue(driver.getCurrentUrl().contains("facebook.com"));
        driver.close();
        driver.switchTo().window(mainWindowHandle);

        // Test LinkedIn link
        WebElement linkedInLink = driver.findElement(By.cssSelector("a[href*='linkedin']"));
        linkedInLink.click();
        windowHandles = driver.getWindowHandles();
        assertEquals(2, windowHandles.size());
        mainWindowHandle = driver.getWindowHandle();
        newWindowHandle = null;
        for (String handle : windowHandles) {
            if (!handle.equals(mainWindowHandle)) {
                newWindowHandle = handle;
                break;
            }
        }
        driver.switchTo().window(newWindowHandle);
        assertTrue(driver.getCurrentUrl().contains("linkedin.com"));
        driver.close();
        driver.switchTo().window(mainWindowHandle);
    }

    @Test
    @Order(7)
    public void testLogout() {
        driver.get("https://parabank.parasoft.com/parabank/overview.htm");
        WebElement logoutLink = driver.findElement(By.xpath("//a[@href='logout.htm']"));
        logoutLink.click();
        wait.until(ExpectedConditions.urlContains("index.htm"));
        assertEquals("https://parabank.parasoft.com/parabank/index.htm", driver.getCurrentUrl());
    }

    @Test
    @Order(8)
    public void testBankingServicesNavigation() {
        driver.get("https://parabank.parasoft.com/parabank/index.htm");
        WebElement servicesLink = driver.findElement(By.linkText("Services"));
        servicesLink.click();
        wait.until(ExpectedConditions.urlContains("services.htm"));

        // Test Open New Account
        WebElement openAccountLink = driver.findElement(By.xpath("//a[@href='openaccount.htm']"));
        openAccountLink.click();
        wait.until(ExpectedConditions.urlContains("openaccount.htm"));
        assertEquals("https://parabank.parasoft.com/parabank/openaccount.htm", driver.getCurrentUrl());

        // Test Transfer Funds
        driver.get("https://parabank.parasoft.com/parabank/services.htm");
        WebElement transferFundsLink = driver.findElement(By.xpath("//a[@href='transfer.htm']"));
        transferFundsLink.click();
        wait.until(ExpectedConditions.urlContains("transfer.htm"));
        assertEquals("https://parabank.parasoft.com/parabank/transfer.htm", driver.getCurrentUrl());

        // Test Bill Pay
        driver.get("https://parabank.parasoft.com/parabank/services.htm");
        WebElement billPayLink = driver.findElement(By.xpath("//a[@href='billpay.htm']"));
        billPayLink.click();
        wait.until(ExpectedConditions.urlContains("billpay.htm"));
        assertEquals("https://parabank.parasoft.com/parabank/billpay.htm", driver.getCurrentUrl());

        // Test Find Transactions
        driver.get("https://parabank.parasoft.com/parabank/services.htm");
        WebElement findTransactionsLink = driver.findElement(By.xpath("//a[@href='findtrans.htm']"));
        findTransactionsLink.click();
        wait.until(ExpectedConditions.urlContains("findtrans.htm"));
        assertEquals("https://parabank.parasoft.com/parabank/findtrans.htm", driver.getCurrentUrl());

        // Test Update Contact Information
        driver.get("https://parabank.parasoft.com/parabank/services.htm");
        WebElement updateContactLink = driver.findElement(By.xpath("//a[@href='updateprofile.htm']"));
        updateContactLink.click();
        wait.until(ExpectedConditions.urlContains("updateprofile.htm"));
        assertEquals("https://parabank.parasoft.com/parabank/updateprofile.htm", driver.getCurrentUrl());
    }

    @Test
    @Order(9)
    public void testAccountSummary() {
        driver.get("https://parabank.parasoft.com/parabank/overview.htm");
        WebElement accountSummaryLink = driver.findElement(By.xpath("//a[@href='overview.htm']"));
        accountSummaryLink.click();
        wait.until(ExpectedConditions.urlContains("overview.htm"));
        assertEquals("https://parabank.parasoft.com/parabank/overview.htm", driver.getCurrentUrl());

        // Check account types
        List<WebElement> accountRows = driver.findElements(By.cssSelector("#accountTable tbody tr"));
        assertTrue(accountRows.size() > 0);
    }
}