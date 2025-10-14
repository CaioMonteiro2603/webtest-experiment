package deepseek.ws07.seq05;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import java.time.Duration;

@TestMethodOrder(OrderAnnotation.class)
public class JsFiddleTest {
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
    public void testJsFiddleHomePage() {
        driver.get(BASE_URL);
        wait.until(ExpectedConditions.urlContains("jsfiddle.net"));
        
        WebElement title = wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector(".home-logo")));
        Assertions.assertTrue(title.isDisplayed(), "Home page logo should be displayed");
    }

    @Test
    @Order(2)
    public void testExternalLinks() {
        driver.get(BASE_URL);
        String originalWindow = driver.getWindowHandle();

        // Test GitHub link
        WebElement githubLink = wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector("a[href*='github']")));
        githubLink.click();
        switchToNewWindowAndAssertDomain("github.com", originalWindow);

        // Test Twitter link
        WebElement twitterLink = wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector("a[href*='twitter']")));
        twitterLink.click();
        switchToNewWindowAndAssertDomain("twitter.com", originalWindow);
    }

    @Test
    @Order(3)
    public void testLoginPage() {
        driver.get(BASE_URL);
        
        WebElement loginButton = wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector(".login-button")));
        loginButton.click();

        WebElement emailField = wait.until(ExpectedConditions.elementToBeClickable(By.id("email")));
        emailField.sendKeys("test@example.com");
        
        WebElement passwordField = driver.findElement(By.id("password"));
        passwordField.sendKeys("password123");
        
        WebElement submitButton = driver.findElement(By.cssSelector(".submit-button"));
        submitButton.click();

        WebElement errorMessage = wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector(".error-message")));
        Assertions.assertTrue(errorMessage.getText().contains("Invalid credentials"), "Error message should indicate invalid credentials");
    }

    @Test
    @Order(4)
    public void testAboutPage() {
        driver.get(BASE_URL);
        
        WebElement aboutLink = wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector("a[href*='about']")));
        aboutLink.click();

        wait.until(ExpectedConditions.urlContains("about"));
        Assertions.assertTrue(driver.getCurrentUrl().contains("about"), "URL should contain 'about'");
    }

    @Test
    @Order(5)
    public void testResetState() {
        driver.get(BASE_URL);
        
        WebElement loginButton = wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector(".login-button")));
        loginButton.click();

        WebElement resetButton = wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector(".reset-button")));
        resetButton.click();

        WebElement emailField = wait.until(ExpectedConditions.elementToBeClickable(By.id("email")));
        Assertions.assertEquals("", emailField.getAttribute("value"), "Email field should be empty after reset");
    }

    private void switchToNewWindowAndAssertDomain(String domain, String originalWindow) {
        for (String windowHandle : driver.getWindowHandles()) {
            if (!windowHandle.equals(originalWindow)) {
                driver.switchTo().window(windowHandle);
                break;
            }
        }
        Assertions.assertTrue(driver.getCurrentUrl().contains(domain), "URL should contain the expected domain");
        driver.close();
        driver.switchTo().window(originalWindow);
    }
}