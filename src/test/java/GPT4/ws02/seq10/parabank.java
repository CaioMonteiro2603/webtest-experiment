package GPT4.ws02.seq10;

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
public class parabank {

    private static WebDriver driver;
    private static WebDriverWait wait;
    private static final String BASE_URL = "https://parabank.parasoft.com/parabank/index.htm";

    @BeforeAll
    public static void setUp() {
        FirefoxOptions options = new FirefoxOptions();
        options.addArguments("--headless");
        driver = new FirefoxDriver(options);
        wait = new WebDriverWait(driver, Duration.ofSeconds(10));
        driver.get(BASE_URL);
    }

    @AfterAll
    public static void tearDown() {
        if (driver != null) {
            driver.quit();
        }
    }

    private void login(String username, String password) {
        driver.get(BASE_URL);
        WebElement usernameInput = wait.until(ExpectedConditions.visibilityOfElementLocated(By.name("username")));
        WebElement passwordInput = driver.findElement(By.name("password"));
        WebElement loginButton = driver.findElement(By.cssSelector("input[value='Log In']"));

        usernameInput.clear();
        usernameInput.sendKeys(username);
        passwordInput.clear();
        passwordInput.sendKeys(password);
        loginButton.click();
    }

    private void logoutIfLoggedIn() {
        if (driver.findElements(By.linkText("Log Out")).size() > 0) {
            driver.findElement(By.linkText("Log Out")).click();
            wait.until(ExpectedConditions.visibilityOfElementLocated(By.name("username")));
        }
    }

    @Test
    @Order(1)
    public void testValidLogin() {
        login("caio@gmail.com", "123");
        WebElement welcomeMsg = wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector("#leftPanel h2")));
        Assertions.assertTrue(welcomeMsg.getText().contains("Accounts Overview"), "Login failed or not redirected properly.");
        logoutIfLoggedIn();
    }

    @Test
    @Order(2)
    public void testInvalidLogin() {
        login("invaliduser", "wrongpass");
        WebElement errorMsg = wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector(".error")));
        Assertions.assertTrue(errorMsg.isDisplayed(), "Error message not shown for invalid login.");
    }

    @Test
    @Order(3)
    public void testExternalLink_Twitter() {
        driver.get(BASE_URL);
        String originalWindow = driver.getWindowHandle();
        WebElement twitterLink = wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector("a[href*='twitter.com']")));
        twitterLink.click();
        wait.until(driver -> driver.getWindowHandles().size() > 1);
        Set<String> windows = driver.getWindowHandles();
        windows.remove(originalWindow);
        String newWindow = windows.iterator().next();
        driver.switchTo().window(newWindow);
        String url = driver.getCurrentUrl();
        Assertions.assertTrue(url.contains("twitter.com"), "Twitter link did not open correct domain.");
        driver.close();
        driver.switchTo().window(originalWindow);
    }

    @Test
    @Order(4)
    public void testExternalLink_Facebook() {
        driver.get(BASE_URL);
        String originalWindow = driver.getWindowHandle();
        WebElement facebookLink = wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector("a[href*='facebook.com']")));
        facebookLink.click();
        wait.until(driver -> driver.getWindowHandles().size() > 1);
        Set<String> windows = driver.getWindowHandles();
        windows.remove(originalWindow);
        String newWindow = windows.iterator().next();
        driver.switchTo().window(newWindow);
        String url = driver.getCurrentUrl();
        Assertions.assertTrue(url.contains("facebook.com"), "Facebook link did not open correct domain.");
        driver.close();
        driver.switchTo().window(originalWindow);
    }

    @Test
    @Order(5)
    public void testExternalLink_LinkedIn() {
        driver.get(BASE_URL);
        String originalWindow = driver.getWindowHandle();
        WebElement linkedinLink = wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector("a[href*='linkedin.com']")));
        linkedinLink.click();
        wait.until(driver -> driver.getWindowHandles().size() > 1);
        Set<String> windows = driver.getWindowHandles();
        windows.remove(originalWindow);
        String newWindow = windows.iterator().next();
        driver.switchTo().window(newWindow);
        String url = driver.getCurrentUrl();
        Assertions.assertTrue(url.contains("linkedin.com"), "LinkedIn link did not open correct domain.");
        driver.close();
        driver.switchTo().window(originalWindow);
    }

    @Test
    @Order(6)
    public void testPageLinksOneLevel() {
        driver.get(BASE_URL);
        int linksChecked = 0;
        for (WebElement link : driver.findElements(By.cssSelector("a[href]"))) {
            String href = link.getAttribute("href");
            if (href != null && href.startsWith(BASE_URL) && !href.equals(BASE_URL)) {
                driver.get(href);
                Assertions.assertTrue(driver.getCurrentUrl().startsWith(BASE_URL), "Navigation failed for link: " + href);
                linksChecked++;
                if (linksChecked >= 5) break; // limit to 5 for sanity
            }
        }
    }
}
