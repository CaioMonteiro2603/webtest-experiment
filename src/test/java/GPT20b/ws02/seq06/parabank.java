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
public class ParabankTestSuite {

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
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("inputEmail")));
    }

    private void login(String email, String password) {
        navigateToLogin();
        WebElement emailField = driver.findElement(By.id("inputEmail"));
        WebElement passField = driver.findElement(By.id("inputPassword"));
        WebElement loginBtn = driver.findElement(By.id("loginSubmit"));

        emailField.clear(); emailField.sendKeys(email);
        passField.clear(); passField.sendKeys(password);
        wait.until(ExpectedConditions.elementToBeClickable(loginBtn)).click();

        wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//h3[contains(text(),'Welcome')]")));
    }

    private void logout() {
        List<WebElement> logoutLinks = driver.findElements(By.linkText("Log Out"));
        if (!logoutLinks.isEmpty()) {
            wait.until(ExpectedConditions.elementToBeClickable(logoutLinks.get(0))).click();
            wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("inputEmail")));
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
        Assertions.assertTrue(driver.getCurrentUrl().contains("/parabank/index.htm"),
                "After login, should remain on index page");
        Assertions.assertTrue(driver.findElements(By.xpath("//h3[contains(text(),'Welcome')]")).size() > 0,
                "Welcome element should be displayed after login");
    }

    @Test
    @Order(2)
    public void testInvalidLogin() {
        navigateToLogin();
        WebElement emailField = driver.findElement(By.id("inputEmail"));
        WebElement passField = driver.findElement(By.id("inputPassword"));
        WebElement loginBtn = driver.findElement(By.id("loginSubmit"));

        emailField.clear(); emailField.sendKeys("invalid@test.com");
        passField.clear(); passField.sendKeys("wrongpassword");
        wait.until(ExpectedConditions.elementToBeClickable(loginBtn)).click();

        WebElement error = wait.until(ExpectedConditions.visibilityOfElementLocated(
                By.cssSelector("div.loginForm > div.error")));
        Assertions.assertTrue(error.getText().toLowerCase().contains("login failed"),
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
        Assertions.assertTrue(driver.getCurrentUrl().contains("/parabank/accounts.htm"),
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
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector("input[name='amount']")));
        Assertions.assertTrue(driver.getCurrentUrl().contains("/parabank/transfer.htm"),
                "URL should point to transfer funds page");
        resetAppState();
    }

    @Test
    @Order(5)
    public void testNavigatePayBills() {
        login(USER_EMAIL, USER_PASSWORD);
        List<WebElement> payLinks = driver.findElements(By.linkText("Pay Bills"));
        Assertions.assertFalse(payLinks.isEmpty(), "Pay Bills link should exist");
        wait.until(ExpectedConditions.elementToBeClickable(payLinks.get(0))).click();
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector("input[name='payee']")));
        Assertions.assertTrue(driver.getCurrentUrl().contains("/parabank/payee.htm"),
                "URL should point to pay bills page");
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
        List<WebElement> aboutLinks = driver.findElements(By.linkText("About Parabank"));
        if (!aboutLinks.isEmpty()) {
            openLinkAndVerifyExternal("About Parabank", "parabank.parasoft.com");
        }
        resetAppState();
    }
}