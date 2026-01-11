package GPT4.ws01.seq03;

import org.junit.jupiter.api.*;
import org.openqa.selenium.*;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;
import java.util.List;
import java.util.Set;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class saucedemo {

    private static WebDriver driver;
    private static WebDriverWait wait;
    private static final String BASE_URL = "https://www.saucedemo.com";
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

    private void login() {
        driver.get(BASE_URL);
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("user-name"))).sendKeys(USERNAME);
        driver.findElement(By.id("password")).sendKeys(PASSWORD);
        wait.until(ExpectedConditions.elementToBeClickable(By.id("login-button"))).click();
        wait.until(ExpectedConditions.urlContains("inventory"));
    }

    private void logout() {
        WebElement menu = wait.until(ExpectedConditions.elementToBeClickable(By.id("react-burger-menu-btn")));
        menu.click();
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("logout_sidebar_link"))).click();
        wait.until(ExpectedConditions.urlContains(BASE_URL));
    }

    private void resetAppState() {
        WebElement menu = wait.until(ExpectedConditions.elementToBeClickable(By.id("react-burger-menu-btn")));
        menu.click();
        wait.until(ExpectedConditions.elementToBeClickable(By.id("reset_sidebar_link"))).click();
        wait.until(ExpectedConditions.elementToBeClickable(By.id("react-burger-cross-btn"))).click();
    }

    @Test
    @Order(1)
    public void testValidLogin() {
        login();
        Assertions.assertTrue(driver.getCurrentUrl().contains("inventory"), "Login did not redirect to inventory page");
        logout();
    }

    @Test
    @Order(2)
    public void testInvalidLogin() {
        driver.get(BASE_URL);
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("user-name"))).sendKeys("invalid_user");
        driver.findElement(By.id("password")).sendKeys("wrong_password");
        driver.findElement(By.id("login-button")).click();
        WebElement error = wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector("h3[data-test='error']")));
        Assertions.assertTrue(error.isDisplayed(), "Error message not displayed for invalid login");
    }

    @Test
    @Order(3)
    public void testSortDropdownOptions() {
        login();
        WebElement dropdown = wait.until(ExpectedConditions.elementToBeClickable(By.className("product_sort_container")));
        dropdown.click();
        List<WebElement> options = dropdown.findElements(By.tagName("option"));
        String[] expected = {"Name (A to Z)", "Name (Z to A)", "Price (low to high)", "Price (high to low)"};
        for (int i = 0; i < options.size(); i++) {
            dropdown = driver.findElement(By.className("product_sort_container"));
            dropdown.click();
            options = dropdown.findElements(By.tagName("option"));
            String optionText = options.get(i).getText();
            Assertions.assertEquals(expected[i], optionText);
            options.get(i).click();
        }
        resetAppState();
        logout();
    }

    @Test
    @Order(4)
    public void testBurgerMenuNavigation() {
        login();
        WebElement menu = wait.until(ExpectedConditions.elementToBeClickable(By.id("react-burger-menu-btn")));
        menu.click();
        wait.until(ExpectedConditions.elementToBeClickable(By.id("inventory_sidebar_link"))).click();
        Assertions.assertTrue(driver.getCurrentUrl().contains("inventory"), "All Items did not redirect properly");
        
        wait.until(ExpectedConditions.elementToBeClickable(By.id("react-burger-cross-btn"))).click();
        
        menu = wait.until(ExpectedConditions.elementToBeClickable(By.id("react-burger-menu-btn")));
        menu.click();
        wait.until(ExpectedConditions.elementToBeClickable(By.id("reset_sidebar_link"))).click();
        wait.until(ExpectedConditions.elementToBeClickable(By.id("react-burger-cross-btn"))).click();
        
        menu = wait.until(ExpectedConditions.elementToBeClickable(By.id("react-burger-menu-btn")));
        menu.click();
        wait.until(ExpectedConditions.elementToBeClickable(By.id("about_sidebar_link"))).click();

        String originalWindow = driver.getWindowHandle();
        wait.until(ExpectedConditions.numberOfWindowsToBe(2));
        Set<String> windows = driver.getWindowHandles();
        windows.remove(originalWindow);
        String newWindow = windows.iterator().next();
        driver.switchTo().window(newWindow);
        Assertions.assertTrue(driver.getCurrentUrl().contains("saucelabs"), "About link did not lead to Sauce Labs");
        driver.close();
        driver.switchTo().window(originalWindow);
        logout();
    }

    @Test
    @Order(5)
    public void testFooterSocialLinks() {
        login();
        String originalWindow = driver.getWindowHandle();

        By[] socialSelectors = {
            By.cssSelector(".social_twitter a"),
            By.cssSelector(".social_facebook a"),
            By.cssSelector(".social_linkedin a")
        };

        String[] expectedDomains = {
            "twitter.com", "facebook.com", "linkedin.com"
        };

        for (int i = 0; i < socialSelectors.length; i++) {
            WebElement link = wait.until(ExpectedConditions.elementToBeClickable(socialSelectors[i]));
            String href = link.getAttribute("href");
            Assertions.assertTrue(href.contains(expectedDomains[i]), "Unexpected URL for social link: " + href);
        }

        logout();
    }
}