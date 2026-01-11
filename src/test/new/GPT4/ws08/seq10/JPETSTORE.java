package GPT4.ws08.seq10;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation;
import org.openqa.selenium.*;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;
import java.util.Set;
import java.net.URI;

@TestMethodOrder(OrderAnnotation.class)
public class JPETSTORE {

    private static WebDriver driver;
    private static WebDriverWait wait;
    private static final String BASE_URL = "https://jpetstore.aspectran.com/";

    @BeforeAll
    public static void setup() {
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

    private void openBase() {
        driver.get(BASE_URL);
        wait.until(ExpectedConditions.presenceOfElementLocated(By.cssSelector("div#LogoContent, #LogoContent, .logo, header")));
        Assertions.assertTrue(driver.getCurrentUrl().startsWith(BASE_URL), "Should be on base URL");
    }

    private String hostOf(String url) {
        try {
            return URI.create(url).getHost();
        } catch (Exception e) {
            return "";
        }
    }

    private void assertExternalOpens(WebElement link) {
        String href = link.getAttribute("href");
        Assumptions.assumeTrue(href != null && href.startsWith("http"), "Not an external link");
        String expectedHost = hostOf(href);
        String originalWindow = driver.getWindowHandle();
        Set<String> before = driver.getWindowHandles();
        link.click();
        wait.until(d -> d.getWindowHandles().size() > before.size() || hostOf(d.getCurrentUrl()).equalsIgnoreCase(expectedHost));
        if (driver.getWindowHandles().size() > before.size()) {
            Set<String> after = driver.getWindowHandles();
            after.removeAll(before);
            String newWindow = after.iterator().next();
            driver.switchTo().window(newWindow);
            wait.until(ExpectedConditions.urlContains(expectedHost));
            Assertions.assertTrue(driver.getCurrentUrl().contains(expectedHost), "External link host mismatch");
            driver.close();
            driver.switchTo().window(originalWindow);
        } else {
            wait.until(ExpectedConditions.urlContains(expectedHost));
            Assertions.assertTrue(driver.getCurrentUrl().contains(expectedHost), "External link host mismatch in same tab");
            driver.navigate().back();
            wait.until(ExpectedConditions.presenceOfElementLocated(By.cssSelector("div#LogoContent, #LogoContent, .logo, header")));
        }
    }

    @Test
    @Order(1)
    public void testHomePageLoads() {
        openBase();
        Assertions.assertTrue(driver.getTitle().toLowerCase().contains("jpetstore"), "Page title should contain 'jpetstore'");
        Assertions.assertTrue(driver.findElement(By.cssSelector("div#LogoContent, #LogoContent, .logo, header")).isDisplayed(), "Logo should be visible");
    }

    @Test
    @Order(2)
    public void testSignInWithValidCredentials() {
        openBase();
        WebElement signInLink = wait.until(ExpectedConditions.elementToBeClickable(By.linkText("Sign In")));
        signInLink.click();
        wait.until(ExpectedConditions.presenceOfElementLocated(By.name("username")));
        driver.findElement(By.name("username")).sendKeys("j2ee");
        driver.findElement(By.name("password")).sendKeys("j2ee");
        driver.findElement(By.name("signon")).click();
        wait.until(ExpectedConditions.presenceOfElementLocated(By.linkText("Sign Out")));
        Assertions.assertTrue(driver.findElement(By.linkText("Sign Out")).isDisplayed(), "Sign out link should appear after login");
    }

    @Test
    @Order(3)
    public void testSignInWithInvalidCredentials() {
        openBase();
        WebElement signInLink = wait.until(ExpectedConditions.elementToBeClickable(By.linkText("Sign In")));
        signInLink.click();
        wait.until(ExpectedConditions.presenceOfElementLocated(By.name("username")));
        driver.findElement(By.name("username")).sendKeys("invalid");
        driver.findElement(By.name("password")).sendKeys("invalid");
        driver.findElement(By.name("signon")).click();
        WebElement message = wait.until(ExpectedConditions.presenceOfElementLocated(By.cssSelector("ul.messages li")));
        Assertions.assertTrue(message.getText().toLowerCase().contains("invalid"), "Should show invalid login message");
    }

    @Test
    @Order(4)
    public void testCategoryNavigation() {
        openBase();
        WebElement fishLink = wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector("area[alt='Fish']")));
        fishLink.click();
        wait.until(ExpectedConditions.urlContains("categoryId=FISH"));
        Assertions.assertTrue(driver.getCurrentUrl().contains("categoryId=FISH"), "Should be in FISH category");
    }

    @Test
    @Order(5)
    public void testSignOut() {
        testSignInWithValidCredentials();
        WebElement signOutLink = wait.until(ExpectedConditions.elementToBeClickable(By.linkText("Sign Out")));
        signOutLink.click();
        wait.until(ExpectedConditions.elementToBeClickable(By.linkText("Sign In")));
        Assertions.assertTrue(driver.findElement(By.linkText("Sign In")).isDisplayed(), "Should be signed out");
    }

    @Test
    @Order(6)
    public void testExternalLinksInFooter() {
        openBase();
        int tested = 0;
        for (WebElement link : driver.findElements(By.cssSelector("a[href]"))) {
            String href = link.getAttribute("href");
            if (href != null && href.startsWith("http") && !hostOf(href).equalsIgnoreCase(hostOf(BASE_URL))) {
                assertExternalOpens(link);
                tested++;
                if (tested >= 2) break;
            }
        }
        Assertions.assertTrue(tested > 0, "Should test at least one external link");
    }

    @Test
    @Order(7)
    public void testAllCategoryLinks() {
        openBase();
        int clicked = 0;
        for (WebElement area : driver.findElements(By.cssSelector("area[alt]"))) {
            String alt = area.getAttribute("alt");
            if (alt != null && !alt.equalsIgnoreCase("Banner")) {
                wait.until(ExpectedConditions.elementToBeClickable(area)).click();
                wait.until(ExpectedConditions.presenceOfElementLocated(By.cssSelector("#Catalog")));
                Assertions.assertTrue(driver.getPageSource().contains("Back"), "Catalog should have a Back link or text");
                driver.navigate().back();
                wait.until(ExpectedConditions.presenceOfElementLocated(By.cssSelector("div#LogoContent, #LogoContent, .logo, header")));
                clicked++;
            }
        }
        Assertions.assertTrue(clicked >= 3, "Should test at least 3 category links");
    }
}