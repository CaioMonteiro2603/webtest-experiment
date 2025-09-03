package GTP4.ws07.seq07;

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
public class JsfiddleNetTest {

    private static WebDriver driver;
    private static WebDriverWait wait;
    private static final String BASE_URL = "https://jsfiddle.net/";

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
    public void testHomePageLoads() {
        driver.get(BASE_URL);
        WebElement logo = wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector("a[href='/']")));
        Assertions.assertTrue(logo.isDisplayed(), "JSFiddle logo link should be displayed");
        Assertions.assertTrue(driver.getTitle().toLowerCase().contains("jsfiddle"), "Page title should contain 'jsfiddle'");
    }

    @Test
    @Order(2)
    public void testNewFiddleButtonCreatesNewFiddle() {
        driver.get(BASE_URL);
        WebElement newButton = wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector("a[href='/']")));
        newButton.click();
        WebElement htmlPanel = wait.until(ExpectedConditions.presenceOfElementLocated(By.cssSelector("div#id_code_html")));
        Assertions.assertTrue(htmlPanel.isDisplayed(), "HTML code panel should be visible on new fiddle");
    }

    @Test
    @Order(3)
    public void testFrameworksDropdownOpens() {
        driver.get(BASE_URL);
        WebElement settingsBtn = wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector("button[data-label='Settings']")));
        settingsBtn.click();
        WebElement frameworksDropdown = wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector("select#framework")));
        Assertions.assertTrue(frameworksDropdown.isDisplayed(), "Frameworks dropdown should be visible");
    }

    @Test
    @Order(4)
    public void testSignInPageNavigation() {
        driver.get(BASE_URL);
        WebElement signInLink = wait.until(ExpectedConditions.elementToBeClickable(By.linkText("Sign in")));
        signInLink.click();
        wait.until(ExpectedConditions.urlContains("/account/login/"));
        Assertions.assertTrue(driver.getCurrentUrl().contains("/account/login"), "URL should contain /account/login");
        WebElement usernameInput = wait.until(ExpectedConditions.visibilityOfElementLocated(By.name("username")));
        Assertions.assertTrue(usernameInput.isDisplayed(), "Username input should be visible on sign in page");
    }

    @Test
    @Order(5)
    public void testInvalidLoginShowsError() {
        driver.get(BASE_URL + "account/login/");
        WebElement username = wait.until(ExpectedConditions.elementToBeClickable(By.name("username")));
        WebElement password = driver.findElement(By.name("password"));
        WebElement loginButton = driver.findElement(By.cssSelector("form[action='/account/login/'] button[type='submit']"));

        username.sendKeys("invalidUser");
        password.sendKeys("invalidPass");
        loginButton.click();

        WebElement errorMsg = wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector(".errorlist")));
        Assertions.assertTrue(errorMsg.getText().toLowerCase().contains("please enter a correct username"), "Error message should be displayed for invalid login");
    }

    @Test
    @Order(6)
    public void testFooterTwitterLink() {
        driver.get(BASE_URL);
        String originalWindow = driver.getWindowHandle();
        WebElement twitterLink = wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector("a[href*='twitter.com/jsfiddle']")));
        twitterLink.click();

        wait.until(driver -> driver.getWindowHandles().size() > 1);
        Set<String> windows = driver.getWindowHandles();
        windows.remove(originalWindow);
        driver.switchTo().window(windows.iterator().next());

        wait.until(ExpectedConditions.urlContains("twitter.com"));
        Assertions.assertTrue(driver.getCurrentUrl().contains("twitter.com"), "Should open Twitter in new tab");

        driver.close();
        driver.switchTo().window(originalWindow);
    }

    @Test
    @Order(7)
    public void testFooterFacebookLink() {
        driver.get(BASE_URL);
        String originalWindow = driver.getWindowHandle();
        WebElement facebookLink = wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector("a[href*='facebook.com/jsfiddle']")));
        facebookLink.click();

        wait.until(driver -> driver.getWindowHandles().size() > 1);
        Set<String> windows = driver.getWindowHandles();
        windows.remove(originalWindow);
        driver.switchTo().window(windows.iterator().next());

        wait.until(ExpectedConditions.urlContains("facebook.com"));
        Assertions.assertTrue(driver.getCurrentUrl().contains("facebook.com"), "Should open Facebook in new tab");

        driver.close();
        driver.switchTo().window(originalWindow);
    }

    @Test
    @Order(8)
    public void testFooterLinkedInLink() {
        driver.get(BASE_URL);
        String originalWindow = driver.getWindowHandle();
        WebElement linkedinLink = wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector("a[href*='linkedin.com']")));
        linkedinLink.click();

        wait.until(driver -> driver.getWindowHandles().size() > 1);
        Set<String> windows = driver.getWindowHandles();
        windows.remove(originalWindow);
        driver.switchTo().window(windows.iterator().next());

        wait.until(ExpectedConditions.urlContains("linkedin.com"));
        Assertions.assertTrue(driver.getCurrentUrl().contains("linkedin.com"), "Should open LinkedIn in new tab");

        driver.close();
        driver.switchTo().window(originalWindow);
    }
}
