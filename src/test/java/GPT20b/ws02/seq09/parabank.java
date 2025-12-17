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

        wait.until(ExpectedConditions.urlContains("main"));
        Assertions.assertTrue(driver.getCurrentUrl().contains("main"),
                "Login did not redirect to main page");
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

        By errorMsg = By.cssSelector("div.error");
        WebElement error = wait.until(ExpectedConditions.visibilityOfElementLocated(errorMsg));
        Assertions.assertTrue(error.getText().contains("Invalid username or password"),
                "Error message not displayed for invalid credentials");
    }

    @Test
    @Order(3)
    public void testOpenAccountNavigation() {
        performLogin();
        By link = By.linkText("Open account");
        wait.until(ExpectedConditions.elementToBeClickable(link)).click();
        By pageTitle = By.cssSelector("h2");
        WebElement title = wait.until(ExpectedConditions.visibilityOfElementLocated(pageTitle));
        Assertions.assertTrue(title.getText().contains("Open account"),
                "Open account page did not load correctly");
    }

    @Test
    @Order(4)
    public void testApplyCreditCardNavigation() {
        performLogin();
        By link = By.linkText("Apply for credit card");
        wait.until(ExpectedConditions.elementToBeClickable(link)).click();
        By pageTitle = By.cssSelector("h2");
        WebElement title = wait.until(ExpectedConditions.visibilityOfElementLocated(pageTitle));
        Assertions.assertTrue(title.getText().contains("Apply for credit card"),
                "Apply for credit card page did not load correctly");
    }

    @Test
    @Order(5)
    public void testTransferFundsNavigation() {
        performLogin();
        By link = By.linkText("Transfer Funds");
        wait.until(ExpectedConditions.elementToBeClickable(link)).click();
        By pageTitle = By.cssSelector("h2");
        WebElement title = wait.until(ExpectedConditions.visibilityOfElementLocated(pageTitle));
        Assertions.assertTrue(title.getText().contains("Transfer Funds"),
                "Transfer Funds page did not load correctly");
    }

    @Test
    @Order(6)
    public void testContactUsNavigation() {
        performLogin();
        By link = By.linkText("Contact Us");
        wait.until(ExpectedConditions.elementToBeClickable(link)).click();
        By pageTitle = By.cssSelector("h1");
        WebElement title = wait.until(ExpectedConditions.visibilityOfElementLocated(pageTitle));
        Assertions.assertTrue(title.getText().contains("Contact"),
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
        Assertions.assertTrue(driver.getCurrentUrl().contains("main"),
                "App state was not reset properly after logout/login");
    }
}