package GPT20b.ws02.seq06;

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
import java.util.List;
import java.util.Set;

@TestMethodOrder(OrderAnnotation.class)
public class parabank {

    private static final String BASE_URL = "https://parabank.parasoft.com/parabank/index.htm";
    private static final String USER_EMAIL = "caio@gmail.com";
    private static final String USER_PASSWORD = "123";

    private static WebDriver driver;
    private static WebDriverWait wait;

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
    public static void tearDown() {
        if (driver != null) {
            driver.quit();
        }
    }

    /* ---------------------------------------------------------------------- */
    /* Helper methods                                                         */
    /* ---------------------------------------------------------------------- */
    private void navigateToLogin() {
        driver.get(BASE_URL);
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.name("username")));
    }

    private void login(String email, String password) {
        navigateToLogin();
        WebElement usernameField = driver.findElement(By.name("username"));
        WebElement passwordField = driver.findElement(By.name("password"));
        WebElement loginBtn = driver.findElement(By.xpath("//input[@value='Log In']"));

        usernameField.clear(); usernameField.sendKeys(email);
        passwordField.clear(); passwordField.sendKeys(password);
        wait.until(ExpectedConditions.elementToBeClickable(loginBtn)).click();

        wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//h1[contains(text(),'Accounts Overview')]")));
    }

    private void logout() {
        List<WebElement> logoutLinks = driver.findElements(By.linkText("Log Out"));
        if (!logoutLinks.isEmpty()) {
            wait.until(ExpectedConditions.elementToBeClickable(logoutLinks.get(0))).click();
            wait.until(ExpectedConditions.visibilityOfElementLocated(By.name("username")));
        }
    }

    private void resetAppState() {
        logout();
        login(USER_EMAIL, USER_PASSWORD);
    }

    private void openLinkAndVerifyExternal(String linkText, String expectedDomain) {
        WebElement link = driver.findElement(By.linkText(linkText));
        wait.until(ExpectedConditions.elementToBeClickable(link)).click();
        switchToNewWindow();
        Assertions.assertTrue(driver.getCurrentUrl().contains(expectedDomain),
                "URL should contain " + expectedDomain + " after clicking " + linkText);
        driver.close();
        driver.switchTo().window(driver.getWindowHandles().iterator().next());
    }

    private void switchToNewWindow() {
        String original = driver.getWindowHandle();
        Set<String> handles = driver.getWindowHandles();
        for (String handle : handles) {
            if (!handle.equals(original)) {
                driver.switchTo().window(handle);
                break;
            }
        }
    }

    /* ---------------------------------------------------------------------- */
    /* Tests                                                                  */
    /* ---------------------------------------------------------------------- */

    @Test
    @Order(1)
    public void testValidLogin() {
        login(USER_EMAIL, USER_PASSWORD);
        Assertions.assertTrue(driver.getCurrentUrl().contains("/parabank/overview.htm"),
                "After login, should be on overview page");
        Assertions.assertTrue(driver.findElements(By.xpath("//h1[contains(text(),'Accounts Overview')]")).size() > 0,
                "Accounts Overview should be displayed after login");
    }

    @Test
    @Order(2)
    public void testInvalidLogin() {
        navigateToLogin();
        WebElement usernameField = driver.findElement(By.name("username"));
        WebElement passwordField = driver.findElement(By.name("password"));
        WebElement loginBtn = driver.findElement(By.xpath("//input[@value='Log In']"));

        usernameField.clear(); usernameField.sendKeys("invalid@test.com");
        passwordField.clear(); passwordField.sendKeys("wrongpassword");
        wait.until(ExpectedConditions.elementToBeClickable(loginBtn)).click();

        WebElement error = wait.until(ExpectedConditions.visibilityOfElementLocated(
                By.className("error")));
        Assertions.assertTrue(error.getText().toLowerCase().contains("error") || error.getText().toLowerCase().contains("failed"),
                "Error message should indicate login failure");
    }

    @Test
    @Order(3)
    public void testNavigateAccountsOverview() {
        login(USER_EMAIL, USER_PASSWORD);
        List<WebElement> accountsLinks = driver.findElements(By.linkText("Accounts Overview"));
        Assertions.assertFalse(accountsLinks.isEmpty(), "Accounts Overview link should exist");
        wait.until(ExpectedConditions.elementToBeClickable(accountsLinks.get(0))).click();
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("accountTable")));
        Assertions.assertTrue(driver.getCurrentUrl().contains("/parabank/overview.htm"),
                "URL should point to accounts overview page");
        resetAppState();
    }

    @Test
    @Order(4)
    public void testNavigateTransferFunds() {
        login(USER_EMAIL, USER_PASSWORD);
        List<WebElement> transferLinks = driver.findElements(By.linkText("Transfer Funds"));
        Assertions.assertFalse(transferLinks.isEmpty(), "Transfer Funds link should exist");
        wait.until(ExpectedConditions.elementToBeClickable(transferLinks.get(0))).click();
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("amount")));
        Assertions.assertTrue(driver.getCurrentUrl().contains("/parabank/transfer.htm"),
                "URL should point to transfer funds page");
        resetAppState();
    }

    @Test
    @Order(5)
    public void testNavigatePayBills() {
        login(USER_EMAIL, USER_PASSWORD);
        List<WebElement> payLinks = driver.findElements(By.linkText("Bill Pay"));
        Assertions.assertFalse(payLinks.isEmpty(), "Bill Pay link should exist");
        wait.until(ExpectedConditions.elementToBeClickable(payLinks.get(0))).click();
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.name("payee.name")));
        Assertions.assertTrue(driver.getCurrentUrl().contains("/parabank/billpay.htm"),
                "URL should point to bill pay page");
        resetAppState();
    }

    @Test
    @Order(6)
    public void testFooterSocialLinks() {
        login(USER_EMAIL, USER_PASSWORD);
        // Twitter
        List<WebElement> twitterLinks = driver.findElements(By.xpath("//a[contains(@href,'twitter.com')]"));
        if (!twitterLinks.isEmpty()) {
            openLinkAndVerifyExternal("Twitter", "twitter.com");
        }
        // Facebook
        List<WebElement> fbLinks = driver.findElements(By.xpath("//a[contains(@href,'facebook.com')]"));
        if (!fbLinks.isEmpty()) {
            openLinkAndVerifyExternal("Facebook", "facebook.com");
        }
        // LinkedIn
        List<WebElement> liLinks = driver.findElements(By.xpath("//a[contains(@href,'linkedin.com')]"));
        if (!liLinks.isEmpty()) {
            openLinkAndVerifyExternal("LinkedIn", "linkedin.com");
        }
        resetAppState();
    }

    @Test
    @Order(7)
    public void testExternalAboutLink() {
        login(USER_EMAIL, USER_PASSWORD);
        List<WebElement> aboutLinks = driver.findElements(By.linkText("About"));
        if (!aboutLinks.isEmpty()) {
            openLinkAndVerifyExternal("About", "parabank.parasoft.com");
        }
        resetAppState();
    }
}