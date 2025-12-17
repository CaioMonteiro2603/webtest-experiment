package GPT4.ws02.seq09;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation;
import org.openqa.selenium.*;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.support.ui.*;

import java.time.Duration;
import java.util.Set;

@TestMethodOrder(OrderAnnotation.class)
public class parabank {

    private static WebDriver driver;
    private static WebDriverWait wait;
    private static final String BASE_URL = "https://parabank.parasoft.com/parabank/index.htm";
    private static final String USERNAME = "caio@gmail.com";
    private static final String PASSWORD = "123";

    @BeforeAll
    public static void setup() {
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

    private void login(String username, String password) {
        driver.get(BASE_URL);
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.name("username"))).clear();
        driver.findElement(By.name("username")).sendKeys(username);
        driver.findElement(By.name("password")).clear();
        driver.findElement(By.name("password")).sendKeys(password);
        wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector("input.button[value='Log In']"))).click();
    }

    private void switchToNewTabAndAssert(String expectedDomain) {
        String originalWindow = driver.getWindowHandle();
        wait.until(driver -> driver.getWindowHandles().size() > 1);
        Set<String> allWindows = driver.getWindowHandles();
        for (String window : allWindows) {
            if (!window.equals(originalWindow)) {
                driver.switchTo().window(window);
                break;
            }
        }
        wait.until(ExpectedConditions.urlContains(expectedDomain));
        Assertions.assertTrue(driver.getCurrentUrl().contains(expectedDomain),
                "Expected URL to contain: " + expectedDomain);
        driver.close();
        driver.switchTo().window(originalWindow);
    }

    @Test
    @Order(1)
    public void testLoginPageLoads() {
        driver.get(BASE_URL);
        Assertions.assertAll(
                () -> Assertions.assertTrue(driver.findElement(By.name("username")).isDisplayed(), "Username field should be visible"),
                () -> Assertions.assertTrue(driver.findElement(By.name("password")).isDisplayed(), "Password field should be visible"),
                () -> Assertions.assertTrue(driver.findElement(By.cssSelector("input.button[value='Log In']")).isDisplayed(), "Login button should be visible")
        );
    }

    @Test
    @Order(2)
    public void testInvalidLoginShowsError() {
        login("invaliduser", "invalidpass");
        WebElement errorMsg = wait.until(ExpectedConditions.visibilityOfElementLocated(By.className("error")));
        Assertions.assertTrue(errorMsg.getText().contains("The username and password could not be verified."),
                "Should show invalid login error");
    }

    @Test
    @Order(3)
    public void testValidLogin() {
        login(USERNAME, PASSWORD);
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("leftPanel")));
        Assertions.assertTrue(driver.findElement(By.linkText("Log Out")).isDisplayed(), "Logout link should be visible after login");
    }

    @Test
    @Order(4)
    public void testAccountOverviewPage() {
        login(USERNAME, PASSWORD);
        WebElement accountOverviewLink = wait.until(ExpectedConditions.elementToBeClickable(By.linkText("Accounts Overview")));
        accountOverviewLink.click();
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.className("title")));
        Assertions.assertTrue(driver.getCurrentUrl().contains("overview.htm"), "URL should contain overview.htm");
        Assertions.assertTrue(driver.findElement(By.className("title")).getText().contains("Accounts Overview"),
                "Page should contain 'Accounts Overview'");
    }

    @Test
    @Order(5)
    public void testFooterTwitterLink() {
        driver.get(BASE_URL);
        WebElement twitterLink = wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector("a[href*='twitter.com']")));
        twitterLink.click();
        switchToNewTabAndAssert("twitter.com");
    }

    @Test
    @Order(6)
    public void testFooterFacebookLink() {
        driver.get(BASE_URL);
        WebElement facebookLink = wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector("a[href*='facebook.com']")));
        facebookLink.click();
        switchToNewTabAndAssert("facebook.com");
    }

    @Test
    @Order(7)
    public void testFooterLinkedInLink() {
        driver.get(BASE_URL);
        WebElement linkedinLink = wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector("a[href*='linkedin.com']")));
        linkedinLink.click();
        switchToNewTabAndAssert("linkedin.com");
    }

    @Test
    @Order(8)
    public void testAboutUsPage() {
        driver.get(BASE_URL);
        WebElement aboutUsLink = wait.until(ExpectedConditions.elementToBeClickable(By.linkText("About Us")));
        aboutUsLink.click();
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.className("title")));
        Assertions.assertTrue(driver.getCurrentUrl().contains("about.htm"), "Should navigate to about.htm page");
        Assertions.assertTrue(driver.findElement(By.className("title")).getText().contains("ParaSoft Demo Website"),
                "Page should contain 'ParaSoft Demo Website'");
    }

    @Test
    @Order(9)
    public void testLogoutFunctionality() {
        login(USERNAME, PASSWORD);
        WebElement logoutLink = wait.until(ExpectedConditions.elementToBeClickable(By.linkText("Log Out")));
        logoutLink.click();
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.name("username")));
        Assertions.assertTrue(driver.getCurrentUrl().contains("index.htm"), "Should return to login page after logout");
    }
}
