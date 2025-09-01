package GPT4.ws07.seq05;

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
public class JSFiddleTest {

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
        WebElement logo = wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector("a[href='/']")));
        Assertions.assertTrue(logo.isDisplayed(), "JSFiddle logo should be visible on home page");
        Assertions.assertTrue(driver.getTitle().toLowerCase().contains("jsfiddle"), "Page title should contain 'JSFiddle'");
    }

    @Test
    @Order(2)
    public void testStartFiddleButtonNavigates() {
        driver.get(BASE_URL);
        WebElement startButton = wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector("a[href='/'] + a")));
        startButton.click();
        wait.until(ExpectedConditions.urlContains("/"));
        Assertions.assertTrue(driver.getCurrentUrl().startsWith(BASE_URL), "Should navigate to base URL or editor");
    }

    @Test
    @Order(3)
    public void testExploreLinkWorks() {
        driver.get(BASE_URL);
        WebElement exploreLink = wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector("a[href='/explore']")));
        exploreLink.click();
        wait.until(ExpectedConditions.urlContains("/explore"));
        WebElement heading = wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector("h1")));
        Assertions.assertTrue(heading.getText().toLowerCase().contains("explore"), "Explore page should load with correct heading");
    }

    @Test
    @Order(4)
    public void testSearchFunctionality() {
        driver.get(BASE_URL + "explore");
        WebElement searchBox = wait.until(ExpectedConditions.elementToBeClickable(By.name("q")));
        searchBox.sendKeys("grid");
        searchBox.sendKeys(Keys.ENTER);
        wait.until(ExpectedConditions.urlContains("q=grid"));
        WebElement results = wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector("ul.fiddles-list")));
        Assertions.assertTrue(results.findElements(By.tagName("li")).size() > 0, "Search results should be displayed for 'grid'");
    }

    @Test
    @Order(5)
    public void testFooterExternalLinks() {
        driver.get(BASE_URL);
        List<WebElement> externalLinks = driver.findElements(By.cssSelector("footer a[target='_blank']"));
        String originalWindow = driver.getWindowHandle();

        for (WebElement link : externalLinks) {
            String href = link.getAttribute("href");
            if (href == null || href.isEmpty()) continue;

            ((JavascriptExecutor) driver).executeScript("window.open(arguments[0])", href);
            wait.until(driver -> driver.getWindowHandles().size() > 1);

            Set<String> windowHandles = driver.getWindowHandles();
            for (String handle : windowHandles) {
                if (!handle.equals(originalWindow)) {
                    driver.switchTo().window(handle);
                    wait.until(webDriver -> webDriver.getCurrentUrl().startsWith("http"));
                    String newUrl = driver.getCurrentUrl();
                    Assertions.assertTrue(newUrl.contains("twitter.com") || newUrl.contains("github.com") || newUrl.contains("facebook.com") || newUrl.contains("linkedin.com"),
                            "External link should lead to a known domain");
                    driver.close();
                    driver.switchTo().window(originalWindow);
                }
            }
        }
    }

    @Test
    @Order(6)
    public void testNewFiddleEditorLoads() {
        driver.get(BASE_URL);
        WebElement newFiddle = wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector("a[href='/'] + a")));
        newFiddle.click();
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector("div#id_code_html")));
        WebElement htmlEditor = driver.findElement(By.cssSelector("div#id_code_html"));
        Assertions.assertTrue(htmlEditor.isDisplayed(), "HTML editor should be visible in new fiddle editor");
    }

    @Test
    @Order(7)
    public void testSupportLink() {
        driver.get(BASE_URL);
        WebElement supportLink = wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector("footer a[href='https://github.com/jsfiddle/jsfiddle-issues']")));
        ((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView(true);", supportLink);
        ((JavascriptExecutor) driver).executeScript("window.open(arguments[0])", supportLink.getAttribute("href"));
        wait.until(driver -> driver.getWindowHandles().size() > 1);
        String originalWindow = driver.getWindowHandle();
        for (String handle : driver.getWindowHandles()) {
            if (!handle.equals(originalWindow)) {
                driver.switchTo().window(handle);
                wait.until(ExpectedConditions.urlContains("github.com"));
                Assertions.assertTrue(driver.getCurrentUrl().contains("github.com"), "Support link should open GitHub issues page");
                driver.close();
                driver.switchTo().window(originalWindow);
            }
        }
    }
}
