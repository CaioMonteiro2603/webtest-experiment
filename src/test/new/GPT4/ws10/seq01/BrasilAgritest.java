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
    private static final String BASE_URL = "https://www.saucedemo.com";
    private static final String USER_EMAIL = "standard_user";
    private static final String USER_PASSWORD = "secret_sauce";

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
        WebElement usernameField = wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("user-name")));
        WebElement passwordField = driver.findElement(By.id("password"));
        WebElement loginButton = driver.findElement(By.id("login-button"));

        usernameField.clear();
        usernameField.sendKeys(USER_EMAIL);
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
        wait.until(ExpectedConditions.urlContains("/inventory.html"));
        Assertions.assertTrue(driver.getCurrentUrl().contains("/inventory.html"),
                "Login did not redirect to inventory");
    }

    @Test
    @Order(2)
    public void testInvalidLogin() {
        driver.get(BASE_URL);
        WebElement usernameField = wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("user-name")));
        WebElement passwordField = driver.findElement(By.id("password"));
        WebElement loginButton = driver.findElement(By.id("login-button"));

        usernameField.clear();
        usernameField.sendKeys("wrong_user");
        passwordField.clear();
        passwordField.sendKeys("wrong_password");
        loginButton.click();

        WebElement errorMessage = wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector("[data-test='error']")));
        Assertions.assertTrue(errorMessage.isDisplayed(), "Error message should be displayed for invalid login");
    }

    @Test
    @Order(3)
    public void testMenuAllItemsNavigation() {
        performLogin();
        WebElement menuButton = wait.until(ExpectedConditions.elementToBeClickable(By.id("react-burger-menu-btn")));
        menuButton.click();

        WebElement allItems = wait.until(ExpectedConditions.elementToBeClickable(By.id("inventory_sidebar_link")));
        allItems.click();

        wait.until(ExpectedConditions.urlContains("/inventory.html"));
        Assertions.assertTrue(driver.getCurrentUrl().contains("/inventory.html"), "Should be redirected to inventory");
    }

    @Test
    @Order(4)
    public void testMenuResetAppState() {
        performLogin();
        WebElement menuButton = wait.until(ExpectedConditions.elementToBeClickable(By.id("react-burger-menu-btn")));
        menuButton.click();

        WebElement resetOption = wait.until(ExpectedConditions.elementToBeClickable(By.id("reset_sidebar_link")));
        resetOption.click();

        Assertions.assertTrue(driver.getCurrentUrl().contains("/inventory.html"), "Reset App State should keep user on inventory");
    }

    @Test
    @Order(5)
    public void testMenuLogout() {
        performLogin();
        WebElement menuButton = wait.until(ExpectedConditions.elementToBeClickable(By.id("react-burger-menu-btn")));
        menuButton.click();

        WebElement logoutOption = wait.until(ExpectedConditions.elementToBeClickable(By.id("logout_sidebar_link")));
        logoutOption.click();

        wait.until(ExpectedConditions.urlContains(""));
        Assertions.assertTrue(driver.getCurrentUrl().equals("https://www.saucedemo.com/"), "Logout should redirect to login page");
    }

    @Test
    @Order(6)
    public void testMenuAboutExternalLink() {
        performLogin();
        WebElement menuButton = wait.until(ExpectedConditions.elementToBeClickable(By.id("react-burger-menu-btn")));
        menuButton.click();

        WebElement aboutLink = wait.until(ExpectedConditions.elementToBeClickable(By.id("about_sidebar_link")));
        aboutLink.click();
        switchToNewTabAndVerify("saucelabs.com");
    }

    @Test
    @Order(7)
    public void testFooterTwitterLink() {
        performLogin();
        WebElement twitter = wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector("[data-test='social-twitter']")));
        twitter.click();
        switchToNewTabAndVerify("twitter.com");
    }

    @Test
    @Order(8)
    public void testFooterFacebookLink() {
        performLogin();
        WebElement facebook = wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector("[data-test='social-facebook']")));
        facebook.click();
        switchToNewTabAndVerify("facebook.com");
    }

    @Test
    @Order(9)
    public void testFooterLinkedInLink() {
        performLogin();
        WebElement linkedin = wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector("[data-test='social-linkedin']")));
        linkedin.click();
        switchToNewTabAndVerify("linkedin.com");
    }
}