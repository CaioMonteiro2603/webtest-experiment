package SunaQwen3.ws06.seq08;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation;
import org.openqa.selenium.*;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import java.time.Duration;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

@TestMethodOrder(OrderAnnotation.class)
public class RestfullBooker {
    private static WebDriver driver;
    private static WebDriverWait wait;
    private static final String BASE_URL = "https://automationintesting.online/";
    private static final String USERNAME = "admin";
    private static final String PASSWORD = "password";

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
    public void testValidLogin() {
        driver.get(BASE_URL);
        assertEquals("https://automationintesting.online/#/login", driver.getCurrentUrl());
        
        WebElement usernameField = wait.until(ExpectedConditions.visibilityOfElementLocated(By.name("username")));
        WebElement passwordField = driver.findElement(By.name("password"));
        WebElement loginButton = driver.findElement(By.id("doLogin"));

        usernameField.sendKeys(USERNAME);
        passwordField.sendKeys(PASSWORD);
        loginButton.click();

        WebElement welcomeText = wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//h2[contains(text(), 'Welcome')]")));
        assertTrue(welcomeText.isDisplayed(), "Welcome text should be displayed after login");
        assertTrue(driver.getCurrentUrl().contains("#/admin"), "URL should contain #/admin after login");
    }

    @Test
    @Order(2)
    public void testInvalidLogin() {
        driver.get(BASE_URL);
        
        WebElement usernameField = wait.until(ExpectedConditions.visibilityOfElementLocated(By.name("username")));
        WebElement passwordField = driver.findElement(By.name("password"));
        WebElement loginButton = driver.findElement(By.id("doLogin"));

        usernameField.sendKeys("invalid");
        passwordField.sendKeys("invalid");
        loginButton.click();

        WebElement errorAlert = wait.until(ExpectedConditions.visibilityOfElementLocated(By.className("alert")));
        assertTrue(errorAlert.isDisplayed(), "Error alert should be displayed for invalid credentials");
        assertTrue(errorAlert.getText().contains("Invalid"), "Error message should indicate invalid credentials");
    }

    @Test
    @Order(3)
    public void testMenuAllItems() {
        navigateToAdminPage();
        
        WebElement menuButton = wait.until(ExpectedConditions.elementToBeClickable(By.className("navbar-toggler")));
        menuButton.click();

        WebElement allItemsLink = wait.until(ExpectedConditions.elementToBeClickable(By.linkText("All Items")));
        allItemsLink.click();

        wait.until(ExpectedConditions.urlContains("#/admin"));
        assertTrue(driver.getCurrentUrl().contains("#/admin"), "URL should contain #/admin after clicking All Items");
    }

    @Test
    @Order(4)
    public void testMenuAboutExternalLink() {
        navigateToAdminPage();
        
        WebElement menuButton = wait.until(ExpectedConditions.elementToBeClickable(By.className("navbar-toggler")));
        menuButton.click();

        WebElement aboutLink = wait.until(ExpectedConditions.elementToBeClickable(By.linkText("About")));
        String originalWindow = driver.getWindowHandle();
        aboutLink.click();

        wait.until(d -> driver.getWindowHandles().size() > 1);
        for (String windowHandle : driver.getWindowHandles()) {
            if (!windowHandle.equals(originalWindow)) {
                driver.switchTo().window(windowHandle);
                break;
            }
        }

        assertTrue(driver.getCurrentUrl().contains("github.com"), "About link should open GitHub page");
        driver.close();
        driver.switchTo().window(originalWindow);
    }

    @Test
    @Order(5)
    public void testMenuLogout() {
        navigateToAdminPage();
        
        WebElement menuButton = wait.until(ExpectedConditions.elementToBeClickable(By.className("navbar-toggler")));
        menuButton.click();

        WebElement logoutLink = wait.until(ExpectedConditions.elementToBeClickable(By.linkText("Logout")));
        logoutLink.click();

        WebElement loginForm = wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("loginForm")));
        assertTrue(loginForm.isDisplayed(), "Login form should be displayed after logout");
        assertTrue(driver.getCurrentUrl().contains("#/login"), "URL should contain #/login after logout");
    }

    @Test
    @Order(6)
    public void testMenuResetAppState() {
        navigateToAdminPage();
        
        WebElement menuButton = wait.until(ExpectedConditions.elementToBeClickable(By.className("navbar-toggler")));
        menuButton.click();

        WebElement resetLink = wait.until(ExpectedConditions.elementToBeClickable(By.linkText("Reset App State")));
        resetLink.click();

        driver.switchTo().alert().accept();

        WebElement successMessage = wait.until(ExpectedConditions.visibilityOfElementLocated(By.className("alert")));
        assertTrue(successMessage.isDisplayed(), "Success message should appear after reset");
        assertTrue(successMessage.getText().contains("success"), "Success message should confirm reset");
    }

    @Test
    @Order(7)
    public void testFooterTwitterLink() {
        navigateToAdminPage();
        
        List<WebElement> footerLinks = driver.findElements(By.cssSelector("footer a"));
        WebElement twitterLink = null;
        for (WebElement link : footerLinks) {
            if (link.getAttribute("href").contains("twitter.com") || link.getAttribute("href").contains("x.com")) {
                twitterLink = link;
                break;
            }
        }
        assertNotNull(twitterLink, "Twitter link should be present in footer");

        String originalWindow = driver.getWindowHandle();
        twitterLink.click();

        wait.until(d -> driver.getWindowHandles().size() > 1);
        for (String windowHandle : driver.getWindowHandles()) {
            if (!windowHandle.equals(originalWindow)) {
                driver.switchTo().window(windowHandle);
                break;
            }
        }

        assertTrue(driver.getCurrentUrl().contains("twitter.com") || driver.getCurrentUrl().contains("x.com"), "Twitter link should open Twitter/x domain");
        driver.close();
        driver.switchTo().window(originalWindow);
    }

    @Test
    @Order(8)
    public void testFooterFacebookLink() {
        navigateToAdminPage();
        
        List<WebElement> footerLinks = driver.findElements(By.cssSelector("footer a"));
        WebElement facebookLink = null;
        for (WebElement link : footerLinks) {
            if (link.getAttribute("href").contains("facebook.com")) {
                facebookLink = link;
                break;
            }
        }
        if (facebookLink == null) {
            for (WebElement link : footerLinks) {
                if (link.getAttribute("href").contains("fb.com") || link.getAttribute("href").contains("meta.com")) {
                    facebookLink = link;
                    break;
                }
            }
        }
        assertNotNull(facebookLink, "Facebook link should be present in footer");

        String originalWindow = driver.getWindowHandle();
        facebookLink.click();

        wait.until(d -> driver.getWindowHandles().size() > 1);
        for (String windowHandle : driver.getWindowHandles()) {
            if (!windowHandle.equals(originalWindow)) {
                driver.switchTo().window(windowHandle);
                break;
            }
        }

        assertTrue(driver.getCurrentUrl().contains("facebook.com") || driver.getCurrentUrl().contains("fb.com") || driver.getCurrentUrl().contains("meta.com"), "Facebook link should open Facebook domain");
        driver.close();
        driver.switchTo().window(originalWindow);
    }

    @Test
    @Order(9)
    public void testFooterLinkedInLink() {
        navigateToAdminPage();
        
        List<WebElement> footerLinks = driver.findElements(By.cssSelector("footer a"));
        WebElement linkedinLink = null;
        for (WebElement link : footerLinks) {
            if (link.getAttribute("href").contains("linkedin.com")) {
                linkedinLink = link;
                break;
            }
        }
        assertNotNull(linkedinLink, "LinkedIn link should be present in footer");

        String originalWindow = driver.getWindowHandle();
        linkedinLink.click();

        wait.until(d -> driver.getWindowHandles().size() > 1);
        for (String windowHandle : driver.getWindowHandles()) {
            if (!windowHandle.equals(originalWindow)) {
                driver.switchTo().window(windowHandle);
                break;
            }
        }

        assertTrue(driver.getCurrentUrl().contains("linkedin.com"), "LinkedIn link should open LinkedIn domain");
        driver.close();
        driver.switchTo().window(originalWindow);
    }

    private void navigateToAdminPage() {
        driver.get(BASE_URL);
        if (driver.getCurrentUrl().contains("#/login")) {
            WebElement usernameField = wait.until(ExpectedConditions.visibilityOfElementLocated(By.name("username")));
            WebElement passwordField = driver.findElement(By.name("password"));
            WebElement loginButton = driver.findElement(By.id("doLogin"));

            usernameField.sendKeys(USERNAME);
            passwordField.sendKeys(PASSWORD);
            loginButton.click();

            wait.until(ExpectedConditions.urlContains("#/admin"));
        }
    }
}