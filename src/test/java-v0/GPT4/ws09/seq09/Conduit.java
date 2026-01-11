package GPT4.ws09.seq09;

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
public class Conduit {

    private static WebDriver driver;
    private static WebDriverWait wait;
    private static final String BASE_URL = "https://demo.realworld.io/";

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

    private void openHomePage() {
        driver.get(BASE_URL);
        wait.until(ExpectedConditions.presenceOfElementLocated(By.cssSelector("a.navbar-brand")));
    }

    @Test
    @Order(1)
    public void testHomePageLoads() {
        openHomePage();
        Assertions.assertTrue(driver.getTitle().toLowerCase().contains("conduit"),
                "Homepage title should contain 'Conduit'");
        Assertions.assertTrue(driver.findElement(By.cssSelector("a.navbar-brand")).isDisplayed(),
                "Brand element should be visible");
    }

    @Test
    @Order(2)
    public void testSignInPageLoads() {
        openHomePage();
        WebElement signIn = wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector("a.nav-link[href='#login']")));
        signIn.click();
        wait.until(ExpectedConditions.urlContains("#login"));
        Assertions.assertTrue(driver.getCurrentUrl().contains("#login"),
                "Should navigate to login page");
        Assertions.assertTrue(driver.findElement(By.tagName("h1")).getText().equalsIgnoreCase("sign in"),
                "Login page header should be 'Sign in'");
    }

    @Test
    @Order(3)
    public void testRegisterPageLoads() {
        openHomePage();
        WebElement signUp = wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector("a.nav-link[href='#register']")));
        signUp.click();
        wait.until(ExpectedConditions.urlContains("#register"));
        Assertions.assertTrue(driver.getCurrentUrl().contains("#register"),
                "Should navigate to register page");
        Assertions.assertTrue(driver.findElement(By.tagName("h1")).getText().equalsIgnoreCase("sign up"),
                "Register page header should be 'Sign up'");
    }

    @Test
    @Order(4)
    public void testInvalidLogin() {
        openHomePage();
        WebElement signIn = wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector("a.nav-link[href='#login']")));
        signIn.click();
        wait.until(ExpectedConditions.urlContains("#login"));

        WebElement emailInput = wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector("input[type='email']")));
        WebElement passwordInput = driver.findElement(By.cssSelector("input[type='password']"));
        WebElement signInBtn = driver.findElement(By.cssSelector("button[type='submit']"));

        emailInput.sendKeys("invalid@example.com");
        passwordInput.sendKeys("wrongpassword");
        signInBtn.click();

        WebElement errorList = wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector(".error-messages li")));
        Assertions.assertTrue(errorList.getText().toLowerCase().contains("email or password"),
                "Should show error message on invalid login");
    }

    @Test
    @Order(5)
    public void testGlobalFeedTab() {
        openHomePage();
        List<WebElement> feedTabs = wait.until(ExpectedConditions.presenceOfAllElementsLocatedBy(By.cssSelector("a.nav-link")));
        for (WebElement tab : feedTabs) {
            if (tab.getText().trim().equalsIgnoreCase("Global Feed")) {
                tab.click();
                wait.until(ExpectedConditions.attributeContains(tab, "class", "active"));
                Assertions.assertTrue(tab.getAttribute("class").contains("active"),
                        "Global Feed tab should be active");
                return;
            }
        }
        Assertions.fail("Global Feed tab not found");
    }

    @Test
    @Order(6)
    public void testFooterExternalLink() {
        openHomePage();
        WebElement footer = wait.until(ExpectedConditions.presenceOfElementLocated(By.cssSelector("footer")));
        WebElement footerLink = footer.findElement(By.cssSelector("a[href*='thinkster.io']"));
        String originalWindow = driver.getWindowHandle();
        Set<String> oldWindows = driver.getWindowHandles();
        footerLink.click();

        wait.until(d -> d.getWindowHandles().size() > oldWindows.size());
        Set<String> newWindows = driver.getWindowHandles();
        newWindows.removeAll(oldWindows);
        String newTab = newWindows.iterator().next();
        driver.switchTo().window(newTab);
        wait.until(ExpectedConditions.urlContains("thinkster.io"));
        Assertions.assertTrue(driver.getCurrentUrl().contains("thinkster.io"),
                "External link should go to thinkster.io");
        driver.close();
        driver.switchTo().window(originalWindow);
    }

    @Test
    @Order(7)
    public void testToggleFeedTabs() {
        openHomePage();
        List<WebElement> feedTabs = wait.until(ExpectedConditions.presenceOfAllElementsLocatedBy(By.cssSelector("a.nav-link")));
        boolean foundGlobal = false;
        boolean foundYourFeed = false;
        for (WebElement tab : feedTabs) {
            String text = tab.getText().trim();
            if (text.equalsIgnoreCase("Global Feed")) {
                tab.click();
                wait.until(ExpectedConditions.attributeContains(tab, "class", "active"));
                foundGlobal = true;
            } else if (text.equalsIgnoreCase("Your Feed")) {
                tab.click();
                wait.until(ExpectedConditions.attributeContains(tab, "class", "active"));
                foundYourFeed = true;
            }
        }
        Assertions.assertTrue(foundGlobal || foundYourFeed, "At least one of the feed tabs should be found and clickable");
    }

    @Test
    @Order(8)
    public void testArticlesLoad() {
        openHomePage();
        WebElement globalFeedTab = wait.until(ExpectedConditions.elementToBeClickable(By.xpath("//a[contains(text(),'Global Feed')]")));
        globalFeedTab.click();
        wait.until(ExpectedConditions.attributeContains(globalFeedTab, "class", "active"));
        List<WebElement> articles = wait.until(ExpectedConditions.presenceOfAllElementsLocatedBy(By.cssSelector("div.article-preview")));
        Assertions.assertFalse(articles.isEmpty(), "Articles should be displayed in Global Feed");
    }

}
