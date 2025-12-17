package GPT4.ws02.seq01;

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
    private static final String USERNAME = "caio@gmail.com";
    private static final String PASSWORD = "147";

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

    private void performLogin(String username, String password) {
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
            driver.findElement(By.linkText("Log Out")).click();
        }
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
                        "Expected domain not found in URL: " + expectedDomain);
                driver.close();
                driver.switchTo().window(originalWindow);
                break;
            }
        }
    }

    @Test
    @Order(1)
    public void testValidLogin() {
        performLogin(USERNAME, PASSWORD);
        WebElement accountOverview = wait.until(ExpectedConditions.visibilityOfElementLocated(By.linkText("Accounts Overview")));
        Assertions.assertTrue(accountOverview.isDisplayed(), "Login should succeed and Accounts Overview should be visible.");
        logoutIfLoggedIn();
    }

    @Test
    @Order(2)
    public void testInvalidLogin() {
        performLogin("wronguser", "wrongpass");
        WebElement error = wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector("p.error")));
        Assertions.assertTrue(error.getText().contains("error"), "Error message should appear for invalid credentials.");
    }

    @Test
    @Order(3)
    public void testExternalLinkParaSoft() {
        driver.get(BASE_URL);
        WebElement paraSoftLink = wait.until(ExpectedConditions.elementToBeClickable(By.xpath("//a[contains(@href, 'parasoft.com')]")));
        paraSoftLink.click();
        switchToNewTabAndVerify("parasoft.com");
    }

    @Test
    @Order(4)
    public void testRegisterPageNavigation() {
        driver.get(BASE_URL);
        WebElement registerLink = wait.until(ExpectedConditions.elementToBeClickable(By.linkText("Register")));
        registerLink.click();
        wait.until(ExpectedConditions.urlContains("register.htm"));
        Assertions.assertTrue(driver.getCurrentUrl().contains("register.htm"), "Should navigate to registration page.");
    }

    @Test
    @Order(5)
    public void testServicesPageNavigation() {
        driver.get(BASE_URL);
        WebElement servicesLink = wait.until(ExpectedConditions.elementToBeClickable(By.linkText("Services")));
        servicesLink.click();
        wait.until(ExpectedConditions.urlContains("services.htm"));
        Assertions.assertTrue(driver.getCurrentUrl().contains("services.htm"), "Should navigate to services page.");
    }

    @Test
    @Order(6)
    public void testAboutUsPageNavigation() {
        driver.get(BASE_URL);
        WebElement aboutLink = wait.until(ExpectedConditions.elementToBeClickable(By.linkText("About Us")));
        aboutLink.click();
        wait.until(ExpectedConditions.urlContains("about.htm"));
        Assertions.assertTrue(driver.getCurrentUrl().contains("about.htm"), "Should navigate to about us page.");
    }

    @Test
    @Order(7)
    public void testContactPageNavigation() {
        driver.get(BASE_URL);
        WebElement contactLink = wait.until(ExpectedConditions.elementToBeClickable(By.linkText("Contact Us")));
        contactLink.click();
        wait.until(ExpectedConditions.urlContains("contact.htm"));
        Assertions.assertTrue(driver.getCurrentUrl().contains("contact.htm"), "Should navigate to contact page.");
    }

    @Test
    @Order(8)
    public void testForgotLoginInfoPageNavigation() {
        driver.get(BASE_URL);
        WebElement forgotLink = wait.until(ExpectedConditions.elementToBeClickable(By.linkText("Forgot login info?")));
        forgotLink.click();
        wait.until(ExpectedConditions.urlContains("lookup.htm"));
        Assertions.assertTrue(driver.getCurrentUrl().contains("lookup.htm"), "Should navigate to forgot login page.");
    }
}
