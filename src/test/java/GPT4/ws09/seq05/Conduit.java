package GPT4.ws09.seq05;

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
public class Conduit {

    private static WebDriver driver;
    private static WebDriverWait wait;
    private static final String BASE_URL = "https://demo.realworld.io/";

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

    @Test
    @Order(1)
    public void testHomePageLoads() {
        driver.get(BASE_URL);
        WebElement banner = wait.until(ExpectedConditions.visibilityOfElementLocated(By.className("banner")));
        Assertions.assertTrue(banner.isDisplayed(), "Banner should be visible on home page");
        Assertions.assertTrue(driver.getTitle().toLowerCase().contains("conduit"), "Title should contain 'Conduit'");
    }

    @Test
    @Order(2)
    public void testNavigateToSignIn() {
        driver.get(BASE_URL);
        WebElement signIn = wait.until(ExpectedConditions.elementToBeClickable(By.linkText("Sign in")));
        signIn.click();
        wait.until(ExpectedConditions.urlContains("login"));
        WebElement emailField = wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector("input[type='email']")));
        Assertions.assertTrue(emailField.isDisplayed(), "Email input should be visible");
    }
}