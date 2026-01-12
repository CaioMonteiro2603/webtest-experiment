package GPT20b.ws02.seq09;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation;
import org.junit.jupiter.api.Assertions;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;
import java.util.List;
import java.util.Set;

@TestMethodOrder(OrderAnnotation.class)
public class parabank {

    private static WebDriver driver;
    private static WebDriverWait wait;
    private static final String BASE_URL = "https://parabank.parasoft.com/parabank/index.htm";
    private static final String USERNAME = "caio@gmail.com";
    private static final String PASSWORD = "123";

    @BeforeAll
    public static void setUpDriver() {
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

    /* ---------- Helper Methods ---------- */

    private void performLogin() {
        driver.get(BASE_URL);
        By userInput = By.cssSelector("input[name='username']");
        By passInput = By.cssSelector("input[name='password']");
        By loginBtn = By.cssSelector("input[value='Log In']");

        wait.until(ExpectedConditions.elementToBeClickable(userInput)).clear();
        driver.findElement(userInput).sendKeys(USERNAME);
        driver.findElement(passInput).clear();
        driver.findElement(passInput).sendKeys(PASSWORD);
        wait.until(ExpectedConditions.elementToBeClickable(loginBtn)).click();

        wait.until(ExpectedConditions.urlContains("overview.htm"));
        Assertions.assertTrue(driver.getCurrentUrl().contains("overview.htm"),
                "Login did not redirect to overview page");
    }

    private void performLogout() {
        By logoutLink = By.linkText("Log Out");
        wait.until(ExpectedConditions.elementToBeClickable(logoutLink)).click();
        wait.until(ExpectedConditions.urlContains("index.htm"));
        Assertions.assertTrue(driver.getCurrentUrl().contains("index.htm"),
                "Logout did not redirect to login page");
    }

    private String getCurrentWindowHandle() {
        return driver.getWindowHandle();
    }
    /* ---------- Tests ---------- */

    @Test
    @Order(1)
    public void testValidLogin() {
        performLogin();
    }

    @Test
    @Order(2)
    public void testInvalidLogin() {
        driver.get(BASE_URL);
        By userInput = By.cssSelector("input[name='username']");
        By passInput = By.cssSelector("input[name='password']");
        By loginBtn = By.cssSelector("input[value='Log In']");

        wait.until(ExpectedConditions.elementToBeClickable(userInput)).clear();
        driver.findElement(userInput).sendKeys("wrong_user");
        driver.findElement(passInput).clear();
        driver.findElement(passInput).sendKeys("wrong_pass");
        wait.until(ExpectedConditions.elementToBeClickable(loginBtn)).click();

        By errorMsg = By.cssSelector("p.error");
        WebElement error = wait.until(ExpectedConditions.visibilityOfElementLocated(errorMsg));
        Assertions.assertTrue(error.getText().contains("The username and password could not be verified."),
                "Error message not displayed for invalid credentials");
    }

    @Test
    @Order(3)
    public void testOpenAccountNavigation() {
        performLogin();
        By link = By.linkText("Open New Account");
        wait.until(ExpectedConditions.elementToBeClickable(link)).click();
        By pageTitle = By.cssSelector("h1");
        WebElement title = wait.until(ExpectedConditions.visibilityOfElementLocated(pageTitle));
        Assertions.assertTrue(title.getText().contains("Open New Account"),
                "Open account page did not load correctly");
    }

    @Test
    @Order(4)
    public void testApplyCreditCardNavigation() {
        performLogin();
        By link = By.linkText("Apply for a Loan");
        wait.until(ExpectedConditions.elementToBeClickable(link)).click();
        By pageTitle = By.cssSelector("h1");
        WebElement title = wait.until(ExpectedConditions.visibilityOfElementLocated(pageTitle));
        Assertions.assertTrue(title.getText().contains("Apply for a Loan"),
                "Apply for loan page did not load correctly");
    }

    @Test
    @Order(5)
    public void testTransferFundsNavigation() {
        performLogin();
        By link = By.linkText("Transfer Funds");
        wait.until(ExpectedConditions.elementToBeClickable(link)).click();
        By pageTitle = By.cssSelector("h1");
        WebElement title = wait.until(ExpectedConditions.visibilityOfElementLocated(pageTitle));
        Assertions.assertTrue(title.getText().contains("Transfer Funds"),
                "Transfer Funds page did not load correctly");
    }

    @Test
    @Order(6)
    public void testContactUsNavigation() {
        performLogin();
        By link = By.linkText("contact");
        wait.until(ExpectedConditions.elementToBeClickable(link)).click();
        By pageTitle = By.cssSelector("h1");
        WebElement title = wait.until(ExpectedConditions.visibilityOfElementLocated(pageTitle));
        Assertions.assertTrue(title.getText().contains("Customer Care"),
                "Contact Us page did not load correctly");
    }

    @Test
    @Order(7)
    public void testExternalLinksHandling() {
        performLogin();
        // Find any anchor with target="_blank" which indicates external link
        List<WebElement> externalLinks = driver.findElements(By.cssSelector("a[target='_blank']"));
        Assertions.assertTrue(!externalLinks.isEmpty(), "No external link found to test");

        String originalHandle = getCurrentWindowHandle();
        WebElement externalLink = externalLinks.get(0);
        String expectedDomain = externalLink.getAttribute("href");
        externalLink.click();

        // Switch to new window/tab
        Set<String> handles = driver.getWindowHandles();
        String newHandle = "";
        for (String handle : handles) {
            if (!handle.equals(originalHandle)) {
                newHandle = handle;
                break;
            }
        }
        Assertions.assertFalse(newHandle.isEmpty(), "New window/tab was not opened");
        driver.switchTo().window(newHandle);
        wait.until(ExpectedConditions.urlContains(expectedDomain));
        Assertions.assertTrue(driver.getCurrentUrl().contains(expectedDomain),
                "External link URL does not contain expected domain");

        // Close new window and switch back
        driver.close();
        driver.switchTo().window(originalHandle);
    }

    @Test
    @Order(8)
    public void testLogout() {
        performLogin();
        performLogout();
    }

    @Test
    @Order(9)
    public void testResetAppState() {
        performLogin();
        // Since Parabank does not provide a "Reset App State" option,
        // we simulate state reset by logging out and logging back in
        performLogout();
        performLogin();
        Assertions.assertTrue(driver.getCurrentUrl().contains("overview.htm"),
                "App state was not reset properly after logout/login");
    }
}