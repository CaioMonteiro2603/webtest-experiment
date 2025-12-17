package SunaDeepSeek.ws07.seq05;

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
        wait.until(ExpectedConditions.titleContains("JSFiddle"));
        Assertions.assertTrue(driver.getCurrentUrl().startsWith(BASE_URL), "Should be on JSFiddle homepage");
    }

    @Test
    @Order(2)
    public void testEditorElementsPresent() {
        driver.get(BASE_URL);
        wait.until(ExpectedConditions.presenceOfElementLocated(By.cssSelector(".CodeMirror")));
        Assertions.assertTrue(driver.findElements(By.cssSelector(".panel.panel-html")).size() > 0, "HTML editor should be present");
        Assertions.assertTrue(driver.findElements(By.cssSelector(".panel.panel-css")).size() > 0, "CSS editor should be present");
        Assertions.assertTrue(driver.findElements(By.cssSelector(".panel.panel-js")).size() > 0, "JS editor should be present");
        Assertions.assertTrue(driver.findElements(By.cssSelector(".panel.panel-result")).size() > 0, "Result panel should be present");
    }

    @Test
    @Order(3)
    public void testRunButtonFunctionality() {
        driver.get(BASE_URL);
        WebElement runButton = wait.until(ExpectedConditions.elementToBeClickable(By.id("run")));
        runButton.click();
        
        // Switch to result iframe
        wait.until(ExpectedConditions.frameToBeAvailableAndSwitchToIt(By.cssSelector("iframe[name='result']")));
        try {
            WebElement body = wait.until(ExpectedConditions.presenceOfElementLocated(By.tagName("body")));
            Assertions.assertNotNull(body, "Result frame should have content after run");
        } finally {
            driver.switchTo().defaultContent();
        }
    }

    @Test
    @Order(4)
    public void testNavbarLinks() {
        driver.get(BASE_URL);
        
        // Test Docs link
        WebElement docsLink = wait.until(ExpectedConditions.elementToBeClickable(By.linkText("Docs")));
        docsLink.click();
        wait.until(ExpectedConditions.urlContains("docs"));
        Assertions.assertTrue(driver.getCurrentUrl().contains("docs"), "Should be on docs page");
        
        // Test Blog link (external)
        WebElement blogLink = wait.until(ExpectedConditions.elementToBeClickable(By.linkText("Blog")));
        blogLink.click();
        
        String originalWindow = driver.getWindowHandle();
        wait.until(ExpectedConditions.numberOfWindowsToBe(2));
        
        for (String windowHandle : driver.getWindowHandles()) {
            if (!originalWindow.equals(windowHandle)) {
                driver.switchTo().window(windowHandle);
                break;
            }
        }
        
        wait.until(ExpectedConditions.urlContains("blog"));
        Assertions.assertTrue(driver.getCurrentUrl().contains("blog"), "Should be on blog page");
        driver.close();
        driver.switchTo().window(originalWindow);
    }

    @Test
    @Order(5)
    public void testFooterLinks() {
        driver.get(BASE_URL);
        
        // Scroll to footer
        ((JavascriptExecutor)driver).executeScript("window.scrollTo(0, document.body.scrollHeight)");
        
        // Test Twitter link
        testExternalFooterLink("Twitter", "twitter.com");
        
        // Test GitHub link
        testExternalFooterLink("GitHub", "github.com");
    }

    private void testExternalFooterLink(String linkText, String expectedDomain) {
        WebElement link = wait.until(ExpectedConditions.elementToBeClickable(By.xpath("//footer//a[contains(text(),'" + linkText + "')]")));
        link.click();
        
        String originalWindow = driver.getWindowHandle();
        wait.until(ExpectedConditions.numberOfWindowsToBe(2));
        
        for (String windowHandle : driver.getWindowHandles()) {
            if (!originalWindow.equals(windowHandle)) {
                driver.switchTo().window(windowHandle);
                break;
            }
        }
        
        wait.until(ExpectedConditions.urlContains(expectedDomain));
        Assertions.assertTrue(driver.getCurrentUrl().contains(expectedDomain), "Should be on " + expectedDomain);
        driver.close();
        driver.switchTo().window(originalWindow);
    }

    @Test
    @Order(6)
    public void testAccountDropdown() {
        driver.get(BASE_URL);
        
        // Click account dropdown
        WebElement accountDropdown = wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector(".dropdown-toggle")));
        accountDropdown.click();
        
        // Verify dropdown items
        List<WebElement> dropdownItems = wait.until(ExpectedConditions.presenceOfAllElementsLocatedBy(By.cssSelector(".dropdown-menu li")));
        Assertions.assertTrue(dropdownItems.size() > 0, "Dropdown should have items");
        
        // Test Login link
        WebElement loginLink = wait.until(ExpectedConditions.elementToBeClickable(By.linkText("Login")));
        loginLink.click();
        wait.until(ExpectedConditions.urlContains("login"));
        Assertions.assertTrue(driver.getCurrentUrl().contains("login"), "Should be on login page");
    }

    @Test
    @Order(7)
    public void testLoginFunctionality() {
        driver.get(BASE_URL + "login/");
        
        // Fill in login form (negative test)
        WebElement emailField = wait.until(ExpectedConditions.elementToBeClickable(By.id("login-email")));
        emailField.sendKeys("invalid@example.com");
        
        WebElement passwordField = driver.findElement(By.id("login-password"));
        passwordField.sendKeys("wrongpassword");
        
        WebElement loginButton = driver.findElement(By.cssSelector("button[type='submit']"));
        loginButton.click();
        
        // Verify error message
        WebElement errorMessage = wait.until(ExpectedConditions.presenceOfElementLocated(By.cssSelector(".alert-danger")));
        Assertions.assertTrue(errorMessage.getText().contains("Invalid"), "Should show invalid credentials error");
    }
}