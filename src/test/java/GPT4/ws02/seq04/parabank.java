package GPT4.ws02.seq04;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation;
import org.openqa.selenium.*;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;
import java.util.Set;

@TestMethodOrder(OrderAnnotation.class)
public class ParabankTest {
    private static WebDriver driver;
    private static WebDriverWait wait;
    private static final String BASE_URL = "https://parabank.parasoft.com/parabank/index.htm";

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
        WebElement usernameField = wait.until(ExpectedConditions.elementToBeClickable(By.name("username")));
        WebElement passwordField = driver.findElement(By.name("password"));
        WebElement loginBtn = driver.findElement(By.cssSelector("input.button[value='Log In']"));
        usernameField.clear();
        usernameField.sendKeys(username);
        passwordField.clear();
        passwordField.sendKeys(password);
        loginBtn.click();
    }

    private void logoutIfNeeded() {
        if (driver.findElements(By.linkText("Log Out")).size() > 0) {
            driver.findElement(By.linkText("Log Out")).click();
            wait.until(ExpectedConditions.presenceOfElementLocated(By.name("username")));
        }
    }

    @Test
    @Order(1)
    public void testValidLogin() {
        login("caio@gmail.com", "123");
        WebElement welcome = wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector("#leftPanel h2")));
        Assertions.assertTrue(welcome.getText().contains("Welcome"), "Login failed or welcome message not displayed");
        logoutIfNeeded();
    }

    @Test
    @Order(2)
    public void testInvalidLogin() {
        login("invalid_user", "wrong_pass");
        WebElement error = wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector(".error")));
        Assertions.assertTrue(error.getText().contains("error"), "Error message not shown for invalid login");
    }

    @Test
    @Order(3)
    public void testNavigateAccountOverview() {
        login("caio@gmail.com", "123");
        WebElement accountOverview = wait.until(ExpectedConditions.elementToBeClickable(By.linkText("Accounts Overview")));
        accountOverview.click();
        WebElement header = wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector("#rightPanel h1")));
        Assertions.assertEquals("Accounts Overview", header.getText(), "Failed to load Accounts Overview page");
        logoutIfNeeded();
    }

    @Test
    @Order(4)
    public void testOpenExternalLinkAbout() {
        driver.get(BASE_URL);
        WebElement aboutLink = wait.until(ExpectedConditions.elementToBeClickable(By.linkText("ParaSoft")));
        String original = driver.getWindowHandle();
        aboutLink.click();

        wait.until(driver -> driver.getWindowHandles().size() > 1);
        Set<String> windows = driver.getWindowHandles();
        for (String handle : windows) {
            if (!handle.equals(original)) {
                driver.switchTo().window(handle);
                wait.until(ExpectedConditions.urlContains("parasoft"));
                Assertions.assertTrue(driver.getCurrentUrl().contains("parasoft"), "External 'About' link did not navigate to parasoft domain");
                driver.close();
                driver.switchTo().window(original);
                break;
            }
        }
    }

    @Test
    @Order(5)
    public void testFooterSocialLinks() {
        driver.get(BASE_URL);
        String original = driver.getWindowHandle();

        WebElement twitterLink = wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector("a[href*='twitter']")));
        twitterLink.click();
        switchToNewTabAndVerify("twitter", original);

        driver.get(BASE_URL);
        WebElement facebookLink = wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector("a[href*='facebook']")));
        facebookLink.click();
        switchToNewTabAndVerify("facebook", original);

        driver.get(BASE_URL);
        WebElement linkedinLink = wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector("a[href*='linkedin']")));
        linkedinLink.click();
        switchToNewTabAndVerify("linkedin", original);
    }

    private void switchToNewTabAndVerify(String domain, String originalWindow) {
        wait.until(driver -> driver.getWindowHandles().size() > 1);
        Set<String> handles = driver.getWindowHandles();
        for (String handle : handles) {
            if (!handle.equals(originalWindow)) {
                driver.switchTo().window(handle);
                wait.until(driver1 -> driver1.getCurrentUrl().contains(domain));
                Assertions.assertTrue(driver.getCurrentUrl().contains(domain), "External link did not navigate to " + domain);
                driver.close();
                driver.switchTo().window(originalWindow);
                break;
            }
        }
    }
}
