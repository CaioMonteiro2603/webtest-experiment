package SunaDeepSeek.ws07.seq01;

import org.junit.jupiter.api.*;
import org.openqa.selenium.*;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import java.time.Duration;
import java.util.List;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class JSFiddle {

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

    @Test
    @Order(1)
    public void testHomePage() {
        driver.get(BASE_URL);
        WebElement logo = wait.until(ExpectedConditions.presenceOfElementLocated(
            By.cssSelector("a.brand")
        ));
        Assertions.assertTrue(logo.isDisplayed(), "Logo should be visible");
    }

    @Test
    @Order(2)
    public void testEditorPage() {
        driver.get(BASE_URL + "z9bcompv/");
        WebElement runButton = wait.until(ExpectedConditions.elementToBeClickable(
            By.cssSelector("button[name='run']")
        ));
        Assertions.assertTrue(runButton.isDisplayed(), "Run button should be visible");
    }

    @Test
    @Order(3)
    public void testExternalLinks() {
        driver.get(BASE_URL);
        List<WebElement> links = driver.findElements(By.cssSelector("footer a"));
        for (WebElement link : links) {
            String href = link.getAttribute("href");
            if (href != null && !href.contains("jsfiddle.net")) {
                String originalWindow = driver.getWindowHandle();
                link.click();
                
                wait.until(ExpectedConditions.numberOfWindowsToBe(2));
                for (String windowHandle : driver.getWindowHandles()) {
                    if (!originalWindow.equals(windowHandle)) {
                        driver.switchTo().window(windowHandle);
                        break;
                    }
                }
                
                Assertions.assertTrue(driver.getCurrentUrl().contains(href.split("/")[2]),
                    "External link should open correct domain");
                driver.close();
                driver.switchTo().window(originalWindow);
            }
        }
    }

    @Test
    @Order(4)
    public void testDocumentationPage() {
        driver.get(BASE_URL + "documentation/");
        WebElement title = wait.until(ExpectedConditions.presenceOfElementLocated(
            By.cssSelector("h1")
        ));
        Assertions.assertTrue(title.getText().contains("Documentation"),
            "Documentation page should have correct title");
    }

    @AfterAll
    public static void tearDown() {
        if (driver != null) {
            driver.quit();
        }
    }
}