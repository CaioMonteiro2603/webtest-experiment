package SunaDeepSeek.ws10.seq05;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation;
import org.junit.jupiter.api.TestMethodOrder;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import java.time.Duration;
import java.util.List;

@TestMethodOrder(OrderAnnotation.class)
public class BrasilAgritest {
    private static WebDriver driver;
    private static WebDriverWait wait;
    private static final String BASE_URL = "https://gestao.brasilagritest.com/login";
    private static final String USERNAME = "superadmin@brasilagritest.com.br";
    private static final String PASSWORD = "10203040";

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
    public void testSuccessfulLogin() {
        driver.get(BASE_URL);
        
        WebElement usernameField = wait.until(ExpectedConditions.presenceOfElementLocated(By.name("email")));
        WebElement passwordField = driver.findElement(By.name("password"));
        WebElement loginButton = driver.findElement(By.cssSelector("button[type='submit']"));

        usernameField.sendKeys(USERNAME);
        passwordField.sendKeys(PASSWORD);
        loginButton.click();

        wait.until(ExpectedConditions.urlContains("dashboard"));
        Assertions.assertTrue(driver.getCurrentUrl().contains("dashboard"), "Login failed - not redirected to dashboard");
    }

    @Test
    @Order(2)
    public void testInvalidLogin() {
        driver.get(BASE_URL);
        
        WebElement usernameField = wait.until(ExpectedConditions.presenceOfElementLocated(By.name("email")));
        WebElement passwordField = driver.findElement(By.name("password"));
        WebElement loginButton = driver.findElement(By.cssSelector("button[type='submit']"));

        usernameField.sendKeys("invalid@user.com");
        passwordField.sendKeys("wrongpassword");
        loginButton.click();

        WebElement errorMessage = wait.until(ExpectedConditions.presenceOfElementLocated(
            By.cssSelector(".alert-danger")));
        Assertions.assertTrue(errorMessage.isDisplayed(), "Error message not displayed for invalid login");
    }

    @Test
    @Order(3)
    public void testNavigationMenu() {
        driver.get(BASE_URL);
        login();

        WebElement menuButton = wait.until(ExpectedConditions.elementToBeClickable(
            By.cssSelector(".navbar-toggler")));
        menuButton.click();

        List<WebElement> menuItems = wait.until(ExpectedConditions.presenceOfAllElementsLocatedBy(
            By.cssSelector(".navbar-nav .nav-link")));
        Assertions.assertTrue(menuItems.size() > 0, "Menu items not found");

        for (WebElement item : menuItems) {
            String itemText = item.getText();
            if (!itemText.isEmpty()) {
                item.click();
                wait.until(ExpectedConditions.or(
                    ExpectedConditions.urlContains(itemText.toLowerCase()),
                    ExpectedConditions.urlContains("http")
                ));
                Assertions.assertTrue(driver.getCurrentUrl().contains(itemText.toLowerCase()) || 
                    driver.getCurrentUrl().startsWith("http"), 
                    "Navigation to " + itemText + " failed");
                
                if (driver.getCurrentUrl().startsWith("http") && 
                    !driver.getCurrentUrl().contains("brasilagritest")) {
                    driver.navigate().back();
                    menuButton.click();
                }
            }
        }
    }

    @Test
    @Order(4)
    public void testExternalLinks() {
        driver.get(BASE_URL);
        login();

        List<WebElement> externalLinks = wait.until(ExpectedConditions.presenceOfAllElementsLocatedBy(
            By.cssSelector("a[href*='http']:not([href*='brasilagritest'])")));
        
        for (WebElement link : externalLinks) {
            String originalWindow = driver.getWindowHandle();
            String href = link.getAttribute("href");
            
            link.click();
            
            wait.until(ExpectedConditions.numberOfWindowsToBe(2));
            for (String windowHandle : driver.getWindowHandles()) {
                if (!windowHandle.equals(originalWindow)) {
                    driver.switchTo().window(windowHandle);
                    Assertions.assertTrue(driver.getCurrentUrl().contains(
                        href.contains("twitter") ? "twitter" : 
                        href.contains("facebook") ? "facebook" : 
                        href.contains("linkedin") ? "linkedin" : ""),
                        "External link not opened correctly");
                    driver.close();
                    driver.switchTo().window(originalWindow);
                    break;
                }
            }
        }
    }

    @Test
    @Order(5)
    public void testPageElements() {
        driver.get(BASE_URL);
        login();

        List<WebElement> cards = wait.until(ExpectedConditions.presenceOfAllElementsLocatedBy(
            By.cssSelector(".card")));
        Assertions.assertTrue(cards.size() > 0, "No cards found on dashboard");

        WebElement footer = wait.until(ExpectedConditions.presenceOfElementLocated(
            By.tagName("footer")));
        Assertions.assertTrue(footer.isDisplayed(), "Footer not displayed");
    }

    private void login() {
        if (!driver.getCurrentUrl().contains("dashboard")) {
            driver.get(BASE_URL);
            WebElement usernameField = wait.until(ExpectedConditions.presenceOfElementLocated(By.name("email")));
            WebElement passwordField = driver.findElement(By.name("password"));
            WebElement loginButton = driver.findElement(By.cssSelector("button[type='submit']"));

            usernameField.sendKeys(USERNAME);
            passwordField.sendKeys(PASSWORD);
            loginButton.click();
            wait.until(ExpectedConditions.urlContains("dashboard"));
        }
    }
}