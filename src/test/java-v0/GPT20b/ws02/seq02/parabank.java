package GPT20b.ws02.seq02;

import java.time.Duration;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import static org.junit.jupiter.api.Assertions.*;

@TestMethodOrder(OrderAnnotation.class)
public class parabank {

    private static WebDriver driver;
    private static WebDriverWait wait;
    private static final String BASE_URL = "https://parabank.parasoft.com/parabank/index.htm";
    private static final String USERNAME = "caio@gmail.com";
    private static final String PASSWORD = "123";

    @BeforeAll
    public static void init() {
        FirefoxOptions options = new FirefoxOptions();
        options.addArguments("--headless");
        driver = new FirefoxDriver(options);
        wait = new WebDriverWait(driver, Duration.ofSeconds(10));
    }

    @AfterAll
    public static void teardown() {
        if (driver != null) {
            driver.quit();
        }
    }

    /* ---------- LOGIN RELATED TESTS ---------- */

    @Test
    @Order(1)
    public void testValidLogin() {
        driver.navigate().to(BASE_URL);
        // Find username field
        WebElement userField = wait.until(ExpectedConditions.elementToBeClickable(By.name("username")));
        WebElement passField = wait.until(ExpectedConditions.elementToBeClickable(By.name("password")));
        WebElement loginButton = wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector("input[type='submit'][value='Login']")));

        userField.clear();
        userField.sendKeys(USERNAME);
        passField.clear();
        passField.sendKeys(PASSWORD);
        loginButton.click();

        // Wait for account summary element to appear
        wait.until(ExpectedConditions.presenceOfElementLocated(By.id("accountSummary")));

        assertTrue(driver.getCurrentUrl().contains("SearchAccount"),
                "URL after login should contain 'SearchAccount'");
        assertTrue(driver.findElements(By.id("accountSummary")).size() > 0,
                "Account summary table should be present after login");

        // Log out to reset state
        logOut();
    }

    @Test
    @Order(2)
    public void testInvalidLogin() {
        driver.navigate().to(BASE_URL);
        WebElement userField = wait.until(ExpectedConditions.elementToBeClickable(By.name("username")));
        WebElement passField = wait.until(ExpectedConditions.elementToBeClickable(By.name("password")));
        WebElement loginButton = wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector("input[type='submit'][value='Login']")));

        userField.clear();
        userField.sendKeys("unknown_user");
        passField.clear();
        passField.sendKeys("wrong_pass");
        loginButton.click();

        WebElement errorMsg = wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("errorField")));
        assertTrue(errorMsg.isDisplayed(), "Error message should be displayed for invalid credentials");
        String errorText = errorMsg.getText();
        assertFalse(errorText.isEmpty(), "Error message should contain text");
    }

    @Test
    @Order(3)
    public void testLogout() {
        loginAndMaintainSession(); // helper to log in
        WebElement logoutLink = wait.until(ExpectedConditions.elementToBeClickable(By.linkText("Logout")));
        logoutLink.click();

        // After logout, login button should appear again
        wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector("input[type='submit'][value='Login']")));

        assertTrue(driver.findElements(By.cssSelector("input[type='submit'][value='Login']")).size() > 0,
                "Login button should be visible after logout");
    }

    /* ---------- EXTERNAL LINKS TESTS ---------- */

    @Test
    @Order(4)
    public void testFooterExternalLinks() {
        loginAndMaintainSession();
    }

    /* ---------- MENU NAVIGATION TESTS ---------- */

    @Test
    @Order(5)
    public void testMenuNavigation() {
        loginAndMaintainSession();

        // Open menu
        WebElement menuButton = wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector("img[alt='Menu']")));
        menuButton.click();

        // Open Account
        WebElement openAccountLink = wait.until(ExpectedConditions.elementToBeClickable(By.linkText("Open Account")));
        openAccountLink.click();

        assertTrue(driver.getCurrentUrl().contains("openAccount"),
                "URL should contain 'openAccount' after clicking Open Account");

        // Back to home
        driver.navigate().back();
    }

    /* ---------- HELPER METHODS ---------- */

    private void loginAndMaintainSession() {
        driver.navigate().to(BASE_URL);
        WebElement userField = wait.until(ExpectedConditions.elementToBeClickable(By.name("username")));
        WebElement passField = wait.until(ExpectedConditions.elementToBeClickable(By.name("password")));
        WebElement loginButton = wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector("input[type='submit'][value='Login']")));

        userField.clear();
        userField.sendKeys(USERNAME);
        passField.clear();
        passField.sendKeys(PASSWORD);
        loginButton.click();

        wait.until(ExpectedConditions.presenceOfElementLocated(By.id("accountSummary")));
    }

    private void logOut() {
        if (driver.findElements(By.linkText("Logout")).size() > 0) {
            WebElement logout = driver.findElement(By.linkText("Logout"));
            logout.click();
            wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector("input[type='submit'][value='Login']")));
        }
    }

 
}