package GPT4.ws10.seq01;

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
public class BrasilAgritest {

    private static WebDriver driver;
    private static WebDriverWait wait;
    private static final String BASE_URL = "https://beta.brasilagritest.com/login";
    private static final String USER_EMAIL = "superadmin@brasilagritest.com.br";
    private static final String USER_PASSWORD = "10203040";

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

    private void performLogin() {
        driver.get(BASE_URL);
        WebElement emailField = wait.until(ExpectedConditions.visibilityOfElementLocated(By.name("email")));
        WebElement passwordField = driver.findElement(By.name("password"));
        WebElement loginButton = driver.findElement(By.cssSelector("button[type='submit']"));

        emailField.clear();
        emailField.sendKeys(USER_EMAIL);
        passwordField.clear();
        passwordField.sendKeys(USER_PASSWORD);
        loginButton.click();
    }

    private void switchToNewTabAndVerify(String expectedDomain) {
        String originalWindow = driver.getWindowHandle();
        wait.until(driver -> driver.getWindowHandles().size() > 1);
        Set<String> allWindows = driver.getWindowHandles();
        for (String window : allWindows) {
            if (!window.equals(originalWindow)) {
                driver.switchTo().window(window);
                wait.until(ExpectedConditions.urlContains(expectedDomain));
                Assertions.assertTrue(driver.getCurrentUrl().contains(expectedDomain),
                        "URL should contain " + expectedDomain);
                driver.close();
                driver.switchTo().window(originalWindow);
                break;
            }
        }
    }

    @Test
    @Order(1)
    public void testValidLogin() {
        performLogin();
        wait.until(ExpectedConditions.urlContains("/dashboard"));
        Assertions.assertTrue(driver.getCurrentUrl().contains("/dashboard"),
                "Login did not redirect to dashboard");
    }

    @Test
    @Order(2)
    public void testInvalidLogin() {
        driver.get(BASE_URL);
        WebElement emailField = wait.until(ExpectedConditions.visibilityOfElementLocated(By.name("email")));
        WebElement passwordField = driver.findElement(By.name("password"));
        WebElement loginButton = driver.findElement(By.cssSelector("button[type='submit']"));

        emailField.clear();
        emailField.sendKeys("wrong@wrong.com");
        passwordField.clear();
        passwordField.sendKeys("wrongpassword");
        loginButton.click();

        WebElement errorMessage = wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector(".error-message, .alert, .invalid-feedback")));
        Assertions.assertTrue(errorMessage.isDisplayed(), "Error message should be displayed for invalid login");
    }

    @Test
    @Order(3)
    public void testMenuAllItemsNavigation() {
        performLogin();
        WebElement menuButton = wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector("button[aria-label='menu'], .burger, .sidebar-toggle")));
        menuButton.click();

        WebElement allItems = wait.until(ExpectedConditions.elementToBeClickable(By.xpath("//a[contains(text(), 'All Items')]")));
        allItems.click();

        wait.until(ExpectedConditions.urlContains("/dashboard"));
        Assertions.assertTrue(driver.getCurrentUrl().contains("/dashboard"), "Should be redirected to dashboard");
    }

    @Test
    @Order(4)
    public void testMenuResetAppState() {
        performLogin();
        WebElement menuButton = wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector("button[aria-label='menu'], .burger, .sidebar-toggle")));
        menuButton.click();

        WebElement resetOption = wait.until(ExpectedConditions.elementToBeClickable(By.xpath("//a[contains(text(), 'Reset App State')]")));
        resetOption.click();

        Assertions.assertTrue(driver.getCurrentUrl().contains("/dashboard"), "Reset App State should keep user on dashboard");
    }

    @Test
    @Order(5)
    public void testMenuLogout() {
        performLogin();
        WebElement menuButton = wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector("button[aria-label='menu'], .burger, .sidebar-toggle")));
        menuButton.click();

        WebElement logoutOption = wait.until(ExpectedConditions.elementToBeClickable(By.xpath("//a[contains(text(), 'Logout')]")));
        logoutOption.click();

        wait.until(ExpectedConditions.urlContains("/login"));
        Assertions.assertTrue(driver.getCurrentUrl().contains("/login"), "Logout should redirect to login page");
    }

    @Test
    @Order(6)
    public void testMenuAboutExternalLink() {
        performLogin();
        WebElement menuButton = wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector("button[aria-label='menu'], .burger, .sidebar-toggle")));
        menuButton.click();

        WebElement aboutLink = wait.until(ExpectedConditions.elementToBeClickable(By.xpath("//a[contains(text(), 'About')]")));
        aboutLink.click();
        switchToNewTabAndVerify("brasilagritest.com");
    }

    @Test
    @Order(7)
    public void testFooterTwitterLink() {
        performLogin();
        WebElement twitter = wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector("a[href*='twitter.com']")));
        twitter.click();
        switchToNewTabAndVerify("twitter.com");
    }

    @Test
    @Order(8)
    public void testFooterFacebookLink() {
        performLogin();
        WebElement facebook = wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector("a[href*='facebook.com']")));
        facebook.click();
        switchToNewTabAndVerify("facebook.com");
    }

    @Test
    @Order(9)
    public void testFooterLinkedInLink() {
        performLogin();
        WebElement linkedin = wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector("a[href*='linkedin.com']")));
        linkedin.click();
        switchToNewTabAndVerify("linkedin.com");
    }
}
