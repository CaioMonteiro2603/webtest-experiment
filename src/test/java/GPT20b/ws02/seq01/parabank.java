package GPT20b.ws02.seq01;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Assertions;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;
import java.util.List;
import java.util.Set;

@TestMethodOrder(OrderAnnotation.class)
public class ParabankTest {

    private static WebDriver driver;
    private static WebDriverWait wait;
    private static final String BASE_URL = "https://parabank.parasoft.com/parabank/index.htm";
    private static final String USERNAME = "caio@gmail.com";
    private static final String PASSWORD = "123";

    @BeforeAll
    public static void setUp() {
        FirefoxOptions options = new FirefoxOptions();
        options.addArguments("--headless"); // REQUIRED
        driver = new FirefoxDriver(options);
        wait = new WebDriverWait(driver, Duration.ofSeconds(10));
    }

    @AfterAll
    public static void tearDown() {
        if (driver != null) {
            driver.quit();
        }
    }

    /*------------------------------ Helper Methods ------------------------------*/

    private void goToBase() {
        driver.get(BASE_URL);
    }

    private void login(String username, String password) {
        goToBase();
        List<WebElement> usernameInputs = driver.findElements(By.name("username"));
        Assertions.assertFalse(usernameInputs.isEmpty(), "Username input not found");
        WebElement usernameInput = usernameInputs.get(0);
        usernameInput.clear();
        usernameInput.sendKeys(username);

        List<WebElement> passwordInputs = driver.findElements(By.name("password"));
        Assertions.assertFalse(passwordInputs.isEmpty(), "Password input not found");
        WebElement passwordInput = passwordInputs.get(0);
        passwordInput.clear();
        passwordInput.sendKeys(password);

        By loginBtnLocator = By.xpath("//input[@type='submit' and @value='Log In']");
        wait.until(ExpectedConditions.elementToBeClickable(loginBtnLocator));
        driver.findElement(loginBtnLocator).click();
    }

    private void waitForElementVisibility(By locator) {
        wait.until(ExpectedConditions.visibilityOfElementLocated(locator));
    }

    private void waitForElementClickability(By locator) {
        wait.until(ExpectedConditions.elementToBeClickable(locator));
    }

    /*------------------------------ Tests ------------------------------*/

    @Test
    @Order(1)
    public void testValidLogin() {
        login(USERNAME, PASSWORD);
        // Verify that Accounts Overview heading is visible
        By accountsHeading = By.xpath("//*[contains(text(),'Accounts Overview')]");
        waitForElementVisibility(accountsHeading);
        Assertions.assertTrue(
                driver.findElement(accountsHeading).isDisplayed(),
                "Accounts Overview heading should be displayed after login.");
    }

    @Test
    @Order(2)
    public void testInvalidLogin() {
        login("invalid_user", "wrong_pass");
        // Verify that error message is displayed
        By errorMsg = By.xpath("//*[contains(text(),'Login failed!')]");
        waitForElementVisibility(errorMsg);
        Assertions.assertTrue(
                driver.findElement(errorMsg).isDisplayed(),
                "Login failed message should be displayed for invalid credentials.");
    }

    @Test
    @Order(3)
    public void testLogout() {
        login(USERNAME, PASSWORD);
        // Click logout
        By logoutLink = By.linkText("Log Out");
        waitForClickability(logoutLink);
        driver.findElement(logoutLink).click();
        // Verify that we are back at login form
        By loginForm = By.xpath("//form[@id='loginForm']");
        waitForElementVisibility(loginForm);
        Assertions.assertTrue(
                driver.findElement(loginForm).isDisplayed(),
                "Login form should be displayed after logout.");
    }

    @Test
    @Order(4)
    public void testFooterSocialLinks() {
        goToBase();
        String[] domains = {"twitter.com", "facebook.com", "linkedin.com"};
        for (String domain : domains) {
            By linkLocator = By.xpath(String.format("//a[contains(@href,'%s')]", domain));
            List<WebElement> links = driver.findElements(linkLocator);
            Assertions.assertFalse(
                    links.isEmpty(),
                    "Footer should contain a link to %s", domain);
            WebElement socialLink = links.get(0);
            String originalWindow = driver.getWindowHandle();
            socialLink.click();

            // Wait for new window
            wait.until(d -> d.getWindowHandles().size() > 1);
            Set<String> windows = driver.getWindowHandles();
            windows.remove(originalWindow);
            String newWindow = windows.iterator().next();
            driver.switchTo().window(newWindow);

            Assertions.assertTrue(
                    driver.getCurrentUrl().contains(domain),
                    "The new window URL should contain %s", domain);

            driver.close();
            driver.switchTo().window(originalWindow);
        }
    }
}