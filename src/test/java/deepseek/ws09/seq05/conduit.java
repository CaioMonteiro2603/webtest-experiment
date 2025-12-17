package deepseek.ws09.seq05;

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
public class conduit {
    private static WebDriver driver;
    private static WebDriverWait wait;
    private static final String BASE_URL = "https://demo.realworld.io/";

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
    public void testHomePageNavigation() {
        driver.get(BASE_URL);
        wait.until(ExpectedConditions.urlContains("realworld"));
        
        WebElement homeLink = wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector("[href='/']")));
        homeLink.click();
        
        wait.until(ExpectedConditions.urlToBe(BASE_URL));
        Assertions.assertTrue(driver.findElement(By.cssSelector(".banner h1")).isDisplayed());
    }

    @Test
    @Order(2)
    public void testUserRegistration() {
        driver.get(BASE_URL + "register");
        
        WebElement usernameField = wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector("input[placeholder='Username']")));
        usernameField.sendKeys("testuser_" + System.currentTimeMillis());
        
        WebElement emailField = driver.findElement(By.cssSelector("input[placeholder='Email']"));
        emailField.sendKeys("test" + System.currentTimeMillis() + "@example.com");
        
        WebElement passwordField = driver.findElement(By.cssSelector("input[placeholder='Password']"));
        passwordField.sendKeys("password123");
        
        WebElement signUpButton = driver.findElement(By.cssSelector("button[type='submit']"));
        signUpButton.click();
        
        wait.until(ExpectedConditions.urlContains(BASE_URL));
        Assertions.assertTrue(driver.findElement(By.cssSelector("[href='#settings']")).isDisplayed());
        
        // Cleanup (logout)
        WebElement settings = wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector("[href='#settings']")));
        settings.click();
        WebElement logout = wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector(".btn-outline-danger")));
        logout.click();
    }

    @Test
    @Order(3)
    public void testUserLogin() {
        driver.get(BASE_URL + "login");
        
        WebElement emailField = wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector("input[placeholder='Email']")));
        emailField.sendKeys("testuser@example.com");
        
        WebElement passwordField = driver.findElement(By.cssSelector("input[placeholder='Password']"));
        passwordField.sendKeys("password123");
        
        WebElement signInButton = driver.findElement(By.cssSelector("button[type='submit']"));
        signInButton.click();
        
        wait.until(ExpectedConditions.urlContains(BASE_URL));
        Assertions.assertTrue(driver.findElement(By.cssSelector("[href='#settings']")).isDisplayed());
        
        // Cleanup (logout)
        WebElement settings = wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector("[href='#settings']")));
        settings.click();
        WebElement logout = wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector(".btn-outline-danger")));
        logout.click();
    }

    @Test
    @Order(4)
    public void testInvalidLogin() {
        driver.get(BASE_URL + "login");
        
        WebElement emailField = wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector("input[placeholder='Email']")));
        emailField.sendKeys("invalid@example.com");
        
        WebElement passwordField = driver.findElement(By.cssSelector("input[placeholder='Password']"));
        passwordField.sendKeys("wrongpassword");
        
        WebElement signInButton = driver.findElement(By.cssSelector("button[type='submit']"));
        signInButton.click();
        
        WebElement errorMessage = wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector(".error-messages")));
        Assertions.assertTrue(errorMessage.getText().contains("email or password is invalid"));
    }

    @Test
    @Order(5)
    public void testArticleCreation() {
        testUserLogin();
        
        WebElement newArticle = wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector("[href='#editor']")));
        newArticle.click();
        
        WebElement titleField = wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector("input[placeholder='Article Title']")));
        titleField.sendKeys("Test Article");
        
        WebElement aboutField = driver.findElement(By.cssSelector("input[placeholder=\"What's this article about?\"]"));
        aboutField.sendKeys("Test description");
        
        WebElement bodyField = driver.findElement(By.cssSelector("textarea[placeholder='Write your article (in markdown)']"));
        bodyField.sendKeys("This is a test article body.");
        
        WebElement publishButton = driver.findElement(By.cssSelector("button[type='button']"));
        publishButton.click();
        
        wait.until(ExpectedConditions.presenceOfElementLocated(By.cssSelector(".article-page")));
        Assertions.assertTrue(driver.getCurrentUrl().contains("article"));
        
        // Cleanup (delete article)
        WebElement deleteButton = wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector(".btn-outline-danger")));
        deleteButton.click();
    }

    @Test
    @Order(6)
    public void testExternalLinks() {
        driver.get(BASE_URL);
        String originalWindow = driver.getWindowHandle();
        
        WebElement conduitLink = wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector("[href='https://github.com/gothinkster/realworld']")));
        conduitLink.click();
        
        for (String windowHandle : driver.getWindowHandles()) {
            if (!windowHandle.equals(originalWindow)) {
                driver.switchTo().window(windowHandle);
                break;
            }
        }
        
        wait.until(ExpectedConditions.urlContains("github.com"));
        driver.close();
        driver.switchTo().window(originalWindow);
    }
}