package deepseek.ws09.seq10;

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
    private static final String BASE_URL = "https://demo.realworld.io/";
    private static WebDriverWait wait;

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
    public void testHomePageLoad() {
        driver.get(BASE_URL);
        WebElement homePage = wait.until(ExpectedConditions.presenceOfElementLocated(
            By.xpath("//h1[contains(text(), 'conduit')]")));
        Assertions.assertTrue(homePage.isDisplayed(), "Home page did not load");
    }

    @Test
    @Order(2)
    public void testSignUp() {
        driver.get(BASE_URL + "register");
        WebElement username = wait.until(ExpectedConditions.presenceOfElementLocated(
            By.xpath("//input[@placeholder='Username']")));
        username.sendKeys("testuser");
        
        WebElement email = driver.findElement(By.xpath("//input[@placeholder='Email']"));
        email.sendKeys("testuser@example.com");
        
        WebElement password = driver.findElement(By.xpath("//input[@placeholder='Password']"));
        password.sendKeys("testpassword");
        
        WebElement signUpButton = driver.findElement(By.xpath("//button[contains(text(), 'Sign up')]"));
        signUpButton.click();
        
        WebElement settingsLink = wait.until(ExpectedConditions.presenceOfElementLocated(
            By.xpath("//a[contains(text(), 'Settings')]")));
        Assertions.assertTrue(settingsLink.isDisplayed(), "Sign up failed");
    }

    @Test
    @Order(3)
    public void testLogin() {
        driver.get(BASE_URL + "login");
        WebElement email = wait.until(ExpectedConditions.presenceOfElementLocated(
            By.xpath("//input[@placeholder='Email']")));
        email.sendKeys("testuser@example.com");
        
        WebElement password = driver.findElement(By.xpath("//input[@placeholder='Password']"));
        password.sendKeys("testpassword");
        
        WebElement signInButton = driver.findElement(By.xpath("//button[contains(text(), 'Sign in')]"));
        signInButton.click();
        
        WebElement settingsLink = wait.until(ExpectedConditions.presenceOfElementLocated(
            By.xpath("//a[contains(text(), 'Settings')]")));
        Assertions.assertTrue(settingsLink.isDisplayed(), "Login failed");
    }

    @Test
    @Order(4)
    public void testInvalidLogin() {
        driver.get(BASE_URL + "login");
        WebElement email = wait.until(ExpectedConditions.presenceOfElementLocated(
            By.xpath("//input[@placeholder='Email']")));
        email.sendKeys("invalid@example.com");
        
        WebElement password = driver.findElement(By.xpath("//input[@placeholder='Password']"));
        password.sendKeys("wrongpassword");
        
        WebElement signInButton = driver.findElement(By.xpath("//button[contains(text(), 'Sign in')]"));
        signInButton.click();
        
        WebElement errorMessage = wait.until(ExpectedConditions.presenceOfElementLocated(
            By.xpath("//div[contains(text(), 'email or password is invalid')]")));
        Assertions.assertTrue(errorMessage.isDisplayed(), "Error message not shown for invalid login");
    }

    @Test
    @Order(5)
    public void testArticleCreation() {
        loginIfNeeded();
        
        WebElement newArticleLink = wait.until(ExpectedConditions.elementToBeClickable(
            By.xpath("//a[contains(text(), 'New Article')]")));
        newArticleLink.click();
        
        WebElement articleTitle = wait.until(ExpectedConditions.presenceOfElementLocated(
            By.xpath("//input[@placeholder='Article Title']")));
        articleTitle.sendKeys("Test Article Title");
        
        WebElement articleDescription = driver.findElement(By.xpath("//input[@placeholder='What's this article about?']"));
        articleDescription.sendKeys("This is a test article description");
        
        WebElement articleBody = driver.findElement(By.xpath("//textarea[@placeholder='Write your article (in markdown)']"));
        articleBody.sendKeys("This is the body of the test article.");
        
        WebElement publishButton = driver.findElement(By.xpath("//button[contains(text(), 'Publish Article')]"));
        publishButton.click();
        
        WebElement articlePageTitle = wait.until(ExpectedConditions.presenceOfElementLocated(
            By.xpath("//h1[contains(text(), 'Test Article Title')]")));
        Assertions.assertTrue(articlePageTitle.isDisplayed(), "Article creation failed");
    }

    @Test
    @Order(6)
    public void testExternalLinks() {
        driver.get(BASE_URL);
        
        // Test Twitter link
        testExternalLink("Twitter", "twitter.com");
        
        // Test GitHub link
        testExternalLink("GitHub", "github.com");
    }

    @Test
    @Order(7)
    public void testLogout() {
        loginIfNeeded();
        
        WebElement settingsLink = wait.until(ExpectedConditions.elementToBeClickable(
            By.xpath("//a[contains(text(), 'Settings')]")));
        settingsLink.click();
        
        WebElement logoutButton = wait.until(ExpectedConditions.elementToBeClickable(
            By.xpath("//button[contains(text(), 'Or click here to logout')]")));
        logoutButton.click();
        
        WebElement signInLink = wait.until(ExpectedConditions.presenceOfElementLocated(
            By.xpath("//a[contains(text(), 'Sign in')]")));
        Assertions.assertTrue(signInLink.isDisplayed(), "Logout failed");
    }

    private void testExternalLink(String linkText, String expectedDomain) {
        String mainWindow = driver.getWindowHandle();
        WebElement link = wait.until(ExpectedConditions.elementToBeClickable(
            By.xpath("//a[contains(@href, '" + expectedDomain + "')]")));
        link.click();
        
        // Switch to new window if opened
        if (driver.getWindowHandles().size() > 1) {
            for (String windowHandle : driver.getWindowHandles()) {
                if (!windowHandle.equals(mainWindow)) {
                    driver.switchTo().window(windowHandle);
                    break;
                }
            }
            
            wait.until(d -> d.getCurrentUrl().contains(expectedDomain));
            Assertions.assertTrue(driver.getCurrentUrl().contains(expectedDomain), 
                linkText + " link failed - wrong domain");
            driver.close();
            driver.switchTo().window(mainWindow);
        }
    }

    private void loginIfNeeded() {
        if (driver.findElements(By.xpath("//a[contains(text(), 'Settings')]")).size() == 0) {
            driver.get(BASE_URL + "login");
            WebElement email = wait.until(ExpectedConditions.presenceOfElementLocated(
                By.xpath("//input[@placeholder='Email']")));
            email.sendKeys("testuser@example.com");
            
            WebElement password = driver.findElement(By.xpath("//input[@placeholder='Password']"));
            password.sendKeys("testpassword");
            
            WebElement signInButton = driver.findElement(By.xpath("//button[contains(text(), 'Sign in')]"));
            signInButton.click();
            
            wait.until(ExpectedConditions.presenceOfElementLocated(
                By.xpath("//a[contains(text(), 'Settings')]")));
        }
    }
}