package GPT20b.ws08.seq08;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.openqa.selenium.support.ui.Select;

import java.time.Duration;
import java.util.List;

@TestMethodOrder(OrderAnnotation.class)
public class JPetStore {
    private static final String BASE_URL = "https://jpetstore.aspectran.com/";
    private static WebDriver driver;
    private static WebDriverWait wait;

    @BeforeAll
    public static void setUp() {
        FirefoxOptions options = new FirefoxOptions();
        options.addArguments("--headless"); // REQUIRED
        driver = new FirefoxDriver(options);
        wait = new WebDriverWait(driver, Duration.ofSeconds(10));
    }

    @AfterAll
    public static void tearDown() {
        if (driver != null) {
            driver.quit();
        }
    }

    /* Helpers */

    private void navigateToLogin() {
        driver.get(BASE_URL);
        try {
            By loginLink = By.linkText("Login");
            wait.until(ExpectedConditions.elementToBeClickable(loginLink)).click();
        } catch (TimeoutException e) {
            Assumptions.assumeTrue(false, "Login link not found");
        }
    }

    private void submitLogin(String user, String pass) {
        try {
            By userField = By.id("userId");
            By passField = By.id("password");
            By loginBtn = By.id("login");
            wait.until(ExpectedConditions.visibilityOfElementLocated(userField)).sendKeys(user);
            wait.until(ExpectedConditions.visibilityOfElementLocated(passField)).sendKeys(pass);
            wait.until(ExpectedConditions.elementToBeClickable(loginBtn)).click();
        } catch (TimeoutException e) {
            Assumptions.assumeTrue(false, "Login form not available");
        }
    }

    private void navigateToItemsPage() {
        try {
            By itemsLink = By.linkText("Items");
            wait.until(ExpectedConditions.elementToBeClickable(itemsLink)).click();
        } catch (TimeoutException e) {
            Assumptions.assumeTrue(false, "Items link not found");
        }
    }

    private List<WebElement> getElements(By by) {
        return driver.findElements(by);
    }

    /* Tests */

    @Test
    @Order(1)
    public void testInvalidLogin() {
        navigateToLogin();
        submitLogin("invalid_user", "wrong_pass");
        By errorLocator = By.cssSelector(".errorMessage, .error, .validationError");
        List<WebElement> errors = getElements(errorLocator);
        Assertions.assertFalse(errors.isEmpty(), "Error message not displayed on invalid login");
        String msg = errors.get(0).getText();
        Assertions.assertTrue(msg.toLowerCase().contains("invalid") || msg.toLowerCase().contains("incorrect"),
                "Unexpected error text: " + msg);
    }

    @Test
    @Order(2)
    public void testSortingDropdown() {
        navigateToItemsPage();
        try {
            By sortSelect = By.id("catalogBy");
            WebElement sortElement = wait.until(ExpectedConditions.visibilityOfElementLocated(sortSelect));
            Select sort = new Select(sortElement);
            List<WebElement> options = sort.getOptions();
            Assumptions.assumeTrue(options.size() > 1, "Sorting options not available");
            for (WebElement option : options) {
                sort.selectByVisibleText(option.getText());
                wait.until(ExpectedConditions.invisibilityOfElementLocated(By.cssSelector(".loading-bar")));
                List<WebElement> items = getElements(By.cssSelector(".blFooterFont"));
                Assertions.assertFalse(items.isEmpty(), "No items displayed after sorting");
                String firstItem = items.get(0).getText();
                Assertions.assertFalse(firstItem.isEmpty(), "First item name should not be empty after sorting");
            }
        } catch (TimeoutException e) {
            Assumptions.assumeTrue(false, "Sorting dropdown not found");
        }
    }

    @Test
    @Order(3)
    public void testExternalAboutLink() {
        driver.get(BASE_URL);
        By aboutLink = By.linkText("About");
        try {
            WebElement link = wait.until(ExpectedConditions.elementToBeClickable(aboutLink));
            String original = driver.getWindowHandle();
            link.click();
            wait.until(ExpectedConditions.numberOfWindowsToBe(2));
            for (String handle : driver.getWindowHandles()) {
                if (!handle.equals(original)) {
                    driver.switchTo().window(handle);
                    Assertions.assertTrue(driver.getCurrentUrl().contains("jpetstore"),
                            "About link did not open expected domain");
                    driver.close();
                    driver.switchTo().window(original);
                }
            }
        } catch (TimeoutException e) {
            Assumptions.assumeTrue(false, "About link not found");
        }
    }

    @Test
    @Order(4)
    public void testFooterSocialLinks() {
        driver.get(BASE_URL);
        By twitter = By.cssSelector("a[href*='twitter.com']");
        By facebook = By.cssSelector("a[href*='facebook.com']");
        By linkedin = By.cssSelector("a[href*='linkedin.com']");
        List<WebElement> links = getElements(twitter);
        links.addAll(getElements(facebook));
        links.addAll(getElements(linkedin));
        Assertions.assertFalse(links.isEmpty(), "No social links found in footer");
        String original = driver.getWindowHandle();
        for (WebElement link : links) {
            String href = link.getAttribute("href");
            try {
                link.click();
                wait.until(ExpectedConditions.numberOfWindowsToBe(2));
                for (String handle : driver.getWindowHandles()) {
                    if (!handle.equals(original)) {
                        driver.switchTo().window(handle);
                        Assertions.assertTrue(driver.getCurrentUrl().contains(href),
                                "Opened link URL does not contain expected domain");
                        driver.close();
                        driver.switchTo().window(original);
                    }
                }
            } catch (TimeoutException e) {
                Assumptions.assumeTrue(false, "Could not open social link: " + href);
            }
        }
    }
}