package GPT4.ws01.seq01;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation;
import org.openqa.selenium.*;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;
import java.util.List;
import java.util.Set;

@TestMethodOrder(OrderAnnotation.class)
public class saucedemo {

    private static WebDriver driver;
    private static WebDriverWait wait;
    private static final String BASE_URL = "https://www.saucedemo.com/v1/index.html";
    private static final String USERNAME = "standard_user";
    private static final String PASSWORD = "secret_sauce";

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

    private void performLogin() {
        driver.get(BASE_URL);
        WebElement userField = wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("user-name")));
        WebElement passField = driver.findElement(By.id("password"));
        WebElement loginBtn = driver.findElement(By.id("login-button"));

        userField.clear();
        userField.sendKeys(USERNAME);
        passField.clear();
        passField.sendKeys(PASSWORD);
        loginBtn.click();
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
        wait.until(ExpectedConditions.urlContains("inventory"));
        Assertions.assertTrue(driver.getCurrentUrl().contains("inventory"),
                "Should be redirected to inventory after login");
    }

    @Test
    @Order(2)
    public void testInvalidLogin() {
        driver.get(BASE_URL);
        WebElement userField = wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("user-name")));
        WebElement passField = driver.findElement(By.id("password"));
        WebElement loginBtn = driver.findElement(By.id("login-button"));

        userField.clear();
        userField.sendKeys("invalid_user");
        passField.clear();
        passField.sendKeys("wrong_password");
        loginBtn.click();

        WebElement errorMsg = wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector("[data-test='error']")));
        Assertions.assertTrue(errorMsg.isDisplayed(), "Error message should appear for invalid login");
    }

    @Test
    @Order(3)
    public void testSortDropdown() {
        performLogin();
        WebElement sortDropdown = wait.until(ExpectedConditions.elementToBeClickable(By.className("product_sort_container")));
        sortDropdown.click();

        List<WebElement> options = sortDropdown.findElements(By.tagName("option"));
        for (WebElement option : options) {
            option.click();
            wait.until(ExpectedConditions.textToBePresentInElement(sortDropdown, option.getText()));
            Assertions.assertEquals(option.getText(), sortDropdown.getAttribute("value").toUpperCase(),
                    "Dropdown value should match selected option");
        }
    }

    @Test
    @Order(4)
    public void testMenuAllItemsNavigation() {
        performLogin();
        WebElement menuBtn = wait.until(ExpectedConditions.elementToBeClickable(By.id("react-burger-menu-btn")));
        menuBtn.click();

        WebElement allItems = wait.until(ExpectedConditions.elementToBeClickable(By.id("inventory_sidebar_link")));
        allItems.click();

        wait.until(ExpectedConditions.urlContains("inventory"));
        Assertions.assertTrue(driver.getCurrentUrl().contains("inventory"), "All Items should navigate to inventory");
    }

    @Test
    @Order(5)
    public void testMenuResetAppState() {
        performLogin();
        WebElement menuBtn = wait.until(ExpectedConditions.elementToBeClickable(By.id("react-burger-menu-btn")));
        menuBtn.click();

        WebElement resetLink = wait.until(ExpectedConditions.elementToBeClickable(By.id("reset_sidebar_link")));
        resetLink.click();

        WebElement closeBtn = wait.until(ExpectedConditions.elementToBeClickable(By.id("react-burger-cross-btn")));
        closeBtn.click();

        Assertions.assertTrue(driver.findElements(By.className("shopping_cart_badge")).isEmpty(),
                "Cart badge should be cleared after reset");
    }

    @Test
    @Order(6)
    public void testMenuLogout() {
        performLogin();
        WebElement menuBtn = wait.until(ExpectedConditions.elementToBeClickable(By.id("react-burger-menu-btn")));
        menuBtn.click();

        WebElement logout = wait.until(ExpectedConditions.elementToBeClickable(By.id("logout_sidebar_link")));
        logout.click();

        wait.until(ExpectedConditions.urlContains("index"));
        Assertions.assertTrue(driver.getCurrentUrl().contains("index"), "Logout should redirect to login page");
    }

    @Test
    @Order(7)
    public void testMenuAboutExternalLink() {
        performLogin();
        WebElement menuBtn = wait.until(ExpectedConditions.elementToBeClickable(By.id("react-burger-menu-btn")));
        menuBtn.click();

        WebElement aboutLink = wait.until(ExpectedConditions.elementToBeClickable(By.id("about_sidebar_link")));
        aboutLink.click();

        switchToNewTabAndVerify("saucelabs.com");
    }

    @Test
    @Order(8)
    public void testFooterTwitterLink() {
        performLogin();
        WebElement twitter = wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector("a.social_twitter")));
        twitter.click();
        switchToNewTabAndVerify("twitter.com");
    }

    @Test
    @Order(9)
    public void testFooterFacebookLink() {
        performLogin();
        WebElement facebook = wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector("a.social_facebook")));
        facebook.click();
        switchToNewTabAndVerify("facebook.com");
    }

    @Test
    @Order(10)
    public void testFooterLinkedInLink() {
        performLogin();
        WebElement linkedin = wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector("a.social_linkedin")));
        linkedin.click();
        switchToNewTabAndVerify("linkedin.com");
    }
}
