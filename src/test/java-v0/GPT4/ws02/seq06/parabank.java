package GPT4.ws02.seq06;

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
    private static final String LOGIN = "caio@gmail.com";
    private static final String PASSWORD = "123";

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

    private void login(String username, String password) {
        driver.get(BASE_URL);
        WebElement usernameField = wait.until(ExpectedConditions.visibilityOfElementLocated(By.name("username")));
        WebElement passwordField = driver.findElement(By.name("password"));
        WebElement loginButton = driver.findElement(By.cssSelector("input.button"));
        usernameField.clear();
        usernameField.sendKeys(username);
        passwordField.clear();
        passwordField.sendKeys(password);
        loginButton.click();
    }

    private void logoutIfLoggedIn() {
        if (driver.findElements(By.linkText("Log Out")).size() > 0) {
            WebElement logoutLink = wait.until(ExpectedConditions.elementToBeClickable(By.linkText("Log Out")));
            logoutLink.click();
        }
    }

    @Test
    @Order(1)
    public void testInvalidLogin() {
        login("wronguser", "wrongpass");
        WebElement errorMessage = wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector("#rightPanel p")));
        Assertions.assertTrue(errorMessage.getText().toLowerCase().contains("error"), "Error message should be displayed for invalid login.");
    }

    @Test
    @Order(2)
    public void testValidLoginAndLogout() {
        login(LOGIN, PASSWORD);
        Assertions.assertTrue(driver.findElements(By.linkText("Log Out")).size() > 0, "Logout link should be visible after login.");
        logoutIfLoggedIn();
        Assertions.assertTrue(driver.findElements(By.name("username")).size() > 0, "Login form should be visible after logout.");
    }

    @Test
    @Order(3)
    public void testOpenAboutPage() {
        driver.get(BASE_URL);
        WebElement aboutLink = wait.until(ExpectedConditions.elementToBeClickable(By.linkText("About Us")));
        aboutLink.click();
        WebElement title = wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector("#rightPanel h1")));
        Assertions.assertEquals("About Us", title.getText(), "Should navigate to About Us page.");
    }

    @Test
    @Order(4)
    public void testOpenServicesPage() {
        driver.get(BASE_URL);
        WebElement servicesLink = wait.until(ExpectedConditions.elementToBeClickable(By.linkText("Services")));
        servicesLink.click();
        WebElement title = wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector("#rightPanel h1")));
        Assertions.assertTrue(title.getText().contains("Services"), "Should navigate to Services page.");
    }

    @Test
    @Order(5)
    public void testFooterExternalLinks() {
        driver.get(BASE_URL);
        String originalWindow = driver.getWindowHandle();

        WebElement homeLink = wait.until(ExpectedConditions.elementToBeClickable(By.linkText("www.parasoft.com")));
        homeLink.click();

        wait.until(driver -> driver.getWindowHandles().size() > 1);
        Set<String> windows = driver.getWindowHandles();
        windows.remove(originalWindow);
        String newWindow = windows.iterator().next();
        driver.switchTo().window(newWindow);

        wait.until(ExpectedConditions.urlContains("parasoft.com"));
        Assertions.assertTrue(driver.getCurrentUrl().contains("parasoft.com"), "External link should navigate to parasoft.com");

        driver.close();
        driver.switchTo().window(originalWindow);
    }
}
