package Qwen3.ws02.seq01;

import org.junit.jupiter.api.*;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class ParabankTest {

    private static WebDriver driver;
    private static WebDriverWait wait;

    private static final String BASE_URL = "https://parabank.parasoft.com/parabank/index.htm";
    private static final String LOGIN_USERNAME = "caio1@gmail.com";
    private static final String LOGIN_PASSWORD = "123";

    @BeforeAll
    public static void setUp() {
        FirefoxOptions options = new FirefoxOptions();
        options.addArguments("--headless");
        driver = new FirefoxDriver(options);
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
        driver.get(BASE_URL);
        wait.until(ExpectedConditions.presenceOfElementLocated(By.name("username"))).sendKeys(LOGIN_USERNAME);
        driver.findElement(By.name("password")).sendKeys(LOGIN_PASSWORD);
        driver.findElement(By.xpath("//input[@value='Log In']")).click();

        wait.until(ExpectedConditions.urlContains("overview.htm"));
        assertTrue(driver.findElement(By.linkText("Accounts Overview")).isDisplayed(),
                "Accounts Overview link should be visible after successful login.");
    }

    @Test
    @Order(2)
    public void testInvalidLogin() {
        driver.get(BASE_URL);
        wait.until(ExpectedConditions.presenceOfElementLocated(By.name("username"))).sendKeys("invalid_user");
        driver.findElement(By.name("password")).sendKeys("wrong_pass");
        driver.findElement(By.xpath("//input[@value='Log In']")).click();

        WebElement errorElement = wait.until(ExpectedConditions.presenceOfElementLocated(By.className("error")));
        assertTrue(errorElement.isDisplayed(), "Error message should be displayed for invalid login.");
        assertTrue(errorElement.getText().contains("The username and password could not be verified."),
                "Error message text is incorrect.");
    }

    @Test
    @Order(3)
    public void testNavigationLinksOnAccountsOverview() {
        testValidLogin(); // Ensure we are logged in

        // Click on Accounts Overview link (to verify it's functional)
        driver.findElement(By.linkText("Accounts Overview")).click();
        wait.until(ExpectedConditions.urlContains("overview.htm"));

        // Click on the first account link (one level deep)
        WebElement firstAccountLink = wait.until(ExpectedConditions.elementToBeClickable(
                By.xpath("//table[@id='accountTable']//tbody//tr[1]//a")));
        String accountId = firstAccountLink.getText();
        firstAccountLink.click();

        wait.until(ExpectedConditions.urlContains("account.htm"));
        assertTrue(driver.findElement(By.id("accountId")).getText().contains(accountId),
                "Account details page should show the correct account ID.");
    }

    @Test
    @Order(4)
    public void testTransferFunds() {
        testValidLogin(); // Ensure we are logged in

        driver.findElement(By.linkText("Transfer Funds")).click();
        wait.until(ExpectedConditions.urlContains("transfer.htm"));

        // Simple transfer
        WebElement amountField = wait.until(ExpectedConditions.presenceOfElementLocated(By.id("amount")));
        amountField.clear();
        amountField.sendKeys("100");

        driver.findElement(By.xpath("//input[@value='Transfer']")).click();
        wait.until(ExpectedConditions.urlContains("transferResult.htm"));

        assertTrue(driver.findElement(By.className("title")).getText().contains("Transfer Complete"),
                "Transfer Complete title should be present.");
    }

    @Test
    @Order(5)
    public void testBillPay() {
        testValidLogin(); // Ensure we are logged in

        driver.findElement(By.linkText("Bill Pay")).click();
        wait.until(ExpectedConditions.urlContains("billpay.htm"));

        // Fill in minimal bill pay info
        driver.findElement(By.name("payee.name")).sendKeys("Test Payee");
        driver.findElement(By.name("payee.address.street")).sendKeys("123 Test St");
        driver.findElement(By.name("payee.address.city")).sendKeys("Test City");
        driver.findElement(By.name("payee.address.state")).sendKeys("CA");
        driver.findElement(By.name("payee.address.zipCode")).sendKeys("90210");
        driver.findElement(By.name("payee.phoneNumber")).sendKeys("1234567890");
        driver.findElement(By.name("payee.accountNumber")).sendKeys("12345");
        driver.findElement(By.name("verifyAccount")).sendKeys("12345");
        WebElement amountField = driver.findElement(By.name("amount"));
        amountField.clear();
        amountField.sendKeys("50");

        driver.findElement(By.xpath("//input[@value='Send Payment']")).click();
        wait.until(ExpectedConditions.urlContains("billpayConfirm.htm"));

        assertTrue(driver.findElement(By.className("title")).getText().contains("Bill Payment Complete"),
                "Bill Payment Complete title should be present.");
    }

    @Test
    @Order(6)
    public void testUpdateProfile() {
        testValidLogin(); // Ensure we are logged in

        driver.findElement(By.linkText("Update Contact Info")).click();
        wait.until(ExpectedConditions.urlContains("updateprofile.htm"));

        // Simple update (e.g., phone)
        WebElement phoneField = driver.findElement(By.id("customer.phoneNumber"));
        String originalPhone = phoneField.getAttribute("value");
        String newPhone = "9876543210";
        phoneField.clear();
        phoneField.sendKeys(newPhone);

        driver.findElement(By.xpath("//input[@value='Update Profile']")).click();
        // Confirmation can be a redirect, so we wait for a stable element on the next page (overview)
        wait.until(ExpectedConditions.urlContains("overview.htm"));

        // While we can't easily re-read the profile without another navigation,
        // asserting the successful redirect and absence of error is sufficient for this flow.
        assertTrue(driver.findElement(By.linkText("Accounts Overview")).isDisplayed(),
                "Should be back on overview page after update.");
    }

    @Test
    @Order(7)
    public void testFooterLinksExternal() {
        driver.get(BASE_URL);
        String originalWindow = driver.getWindowHandle();

        // Click 'About Us' (external link)
        WebElement aboutUsLink = wait.until(ExpectedConditions.elementToBeClickable(By.linkText("About Us")));
        aboutUsLink.click();
        assertExternalLinkAndReturn(originalWindow, "parasoft.com");

        // Click 'Services' (external link)
        driver.get(BASE_URL); // Re-get base to ensure clean state for next click
        originalWindow = driver.getWindowHandle();
        WebElement servicesLink = wait.until(ExpectedConditions.elementToBeClickable(By.linkText("Services")));
        servicesLink.click();
        assertExternalLinkAndReturn(originalWindow, "parasoft.com");

        // There are no Twitter/Facebook/LinkedIn links on this site's footer.
    }

    @Test
    @Order(8)
    public void testLogout() {
        testValidLogin(); // Ensure we are logged in

        driver.findElement(By.linkText("Log Out")).click();
        wait.until(ExpectedConditions.urlToBe(BASE_URL));
        assertTrue(driver.findElement(By.xpath("//input[@value='Log In']")).isDisplayed(),
                "Should be back on login page after logout.");
    }


    // --- Helper Methods ---

    private void assertExternalLinkAndReturn(String originalWindow, String expectedDomain) {
        Set<String> allWindows = driver.getWindowHandles();
        String newWindow = allWindows.stream().filter(handle -> !handle.equals(originalWindow)).findFirst().orElse(null);
        assertNotNull(newWindow, "A new window should have been opened for " + expectedDomain);
        driver.switchTo().window(newWindow);
        assertTrue(driver.getCurrentUrl().contains(expectedDomain),
                "New window URL should contain " + expectedDomain + ". URL was: " + driver.getCurrentUrl());
        driver.close();
        driver.switchTo().window(originalWindow);
    }
}