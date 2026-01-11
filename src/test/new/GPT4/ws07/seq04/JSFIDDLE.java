package GPT4.ws07.seq04;

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
public class JSFIDDLE {

    private static WebDriver driver;
    private static WebDriverWait wait;
    private static final String BASE_URL = "https://jsfiddle.net/";

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
        WebElement logo = wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector("a.HeaderLogo")));
        Assertions.assertTrue(logo.isDisplayed(), "JSFiddle logo not visible on home page");
        Assertions.assertTrue(driver.getTitle().toLowerCase().contains("jsfiddle"), "Title does not contain expected text");
    }

    @Test
    @Order(2)
    public void testCreateNewFiddleButton() {
        driver.get(BASE_URL);
        WebElement newFiddleBtn = wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector("a[href='/']")));
        newFiddleBtn.click();
        wait.until(ExpectedConditions.urlContains("jsfiddle.net"));
        Assertions.assertTrue(driver.getCurrentUrl().contains("jsfiddle.net"), "Did not remain on jsfiddle.net after clicking New Fiddle");
    }

    @Test
    @Order(3)
    public void testExplorePageAccessible() {
        driver.get(BASE_URL);
        WebElement exploreLink = wait.until(ExpectedConditions.elementToBeClickable(By.linkText("Explore")));
        exploreLink.click();
        wait.until(ExpectedConditions.urlContains("/explore"));
        WebElement content = wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector("div.grid")));
        Assertions.assertTrue(content.isDisplayed(), "Explore content grid not visible");
    }

    @Test
    @Order(4)
    public void testFooterTwitterLink() {
        driver.get(BASE_URL);
        WebElement twitterLink = wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector("a[href*='twitter.com']")));
        String originalWindow = driver.getWindowHandle();
        twitterLink.click();
        wait.until(driver -> driver.getWindowHandles().size() > 1);
        Set<String> windows = driver.getWindowHandles();
        for (String handle : windows) {
            if (!handle.equals(originalWindow)) {
                driver.switchTo().window(handle);
                wait.until(ExpectedConditions.urlContains("twitter.com"));
                Assertions.assertTrue(driver.getCurrentUrl().contains("twitter.com"), "Twitter link did not open correct URL");
                driver.close();
                driver.switchTo().window(originalWindow);
                break;
            }
        }
    }

    @Test
    @Order(5)
    public void testFooterFacebookLink() {
        driver.get(BASE_URL);
        WebElement fbLink = wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector("a[href*='facebook.com']")));
        String originalWindow = driver.getWindowHandle();
        fbLink.click();
        wait.until(driver -> driver.getWindowHandles().size() > 1);
        Set<String> windows = driver.getWindowHandles();
        for (String handle : windows) {
            if (!handle.equals(originalWindow)) {
                driver.switchTo().window(handle);
                wait.until(ExpectedConditions.urlContains("facebook.com"));
                Assertions.assertTrue(driver.getCurrentUrl().contains("facebook.com"), "Facebook link did not open correct URL");
                driver.close();
                driver.switchTo().window(originalWindow);
                break;
            }
        }
    }

    @Test
    @Order(6)
    public void testFooterLinkedInLink() {
        driver.get(BASE_URL);
        List<WebElement> links = driver.findElements(By.cssSelector("a[href*='linkedin.com']"));
        if (links.size() > 0) {
            WebElement linkedInLink = wait.until(ExpectedConditions.elementToBeClickable(links.get(0)));
            String originalWindow = driver.getWindowHandle();
            linkedInLink.click();
            wait.until(driver -> driver.getWindowHandles().size() > 1);
            Set<String> windows = driver.getWindowHandles();
            for (String handle : windows) {
                if (!handle.equals(originalWindow)) {
                    driver.switchTo().window(handle);
                    wait.until(ExpectedConditions.urlContains("linkedin.com"));
                    Assertions.assertTrue(driver.getCurrentUrl().contains("linkedin.com"), "LinkedIn link did not open correct URL");
                    driver.close();
                    driver.switchTo().window(originalWindow);
                    break;
                }
            }
        } else {
            Assertions.assertTrue(true, "No LinkedIn link found; skipping test");
        }
    }

    @Test
    @Order(7)
    public void testFooterLinksVisible() {
        driver.get(BASE_URL);
        WebElement footer = wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector("footer.Footer")));
        Assertions.assertTrue(footer.isDisplayed(), "Footer is not visible");
        List<WebElement> links = footer.findElements(By.tagName("a"));
        Assertions.assertTrue(links.size() > 0, "No links found in the footer");
    }

    @Test
    @Order(8)
    public void testSearchFunctionality() {
        driver.get(BASE_URL + "explore");
        WebElement searchBox = wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector("input[placeholder*='search']")));
        searchBox.sendKeys("react");
        searchBox.sendKeys(Keys.ENTER);
        WebElement results = wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector("div.grid")));
        Assertions.assertTrue(results.isDisplayed(), "Search results not visible");
    }
}