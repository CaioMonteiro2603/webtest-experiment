package deepseek.ws07.seq02;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation;
import org.openqa.selenium.*;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import java.time.Duration;

@TestMethodOrder(OrderAnnotation.class)
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
    public void testEditorLoads() {
        driver.get(BASE_URL);
        WebElement editor = wait.until(ExpectedConditions.presenceOfElementLocated(
            By.cssSelector(".CodeMirror")));
        Assertions.assertTrue(editor.isDisplayed(),
            "Expected code editor to be visible");
    }

    @Test
    @Order(2)
    public void testRunButtonFunctionality() {
        driver.get(BASE_URL);
        WebElement runButton = wait.until(ExpectedConditions.elementToBeClickable(
            By.id("run")));
        runButton.click();

        WebElement resultFrame = wait.until(ExpectedConditions.presenceOfElementLocated(
            By.id("result")));
        Assertions.assertTrue(resultFrame.isDisplayed(),
            "Expected result frame after clicking Run");
    }

    @Test
    @Order(3)
    public void testConsolePanelToggle() {
        driver.get(BASE_URL);
        WebElement consoleButton = wait.until(ExpectedConditions.elementToBeClickable(
            By.cssSelector(".sidebarButton.console")));
        consoleButton.click();

        WebElement consolePanel = wait.until(ExpectedConditions.visibilityOfElementLocated(
            By.id("console")));
        Assertions.assertTrue(consolePanel.isDisplayed(),
            "Expected console panel to be visible");
    }

    @Test
    @Order(4)
    public void testExternalLinks() {
        driver.get(BASE_URL);
        String originalWindow = driver.getWindowHandle();

        // Test Documentation link
        WebElement docLink = wait.until(ExpectedConditions.elementToBeClickable(
            By.linkText("Documentation")));
        docLink.click();
        assertExternalLink("jsfiddle.net", originalWindow);

        // Test Blog link
        driver.get(BASE_URL);
        WebElement blogLink = wait.until(ExpectedConditions.elementToBeClickable(
            By.linkText("Blog")));
        blogLink.click();
        assertExternalLink("jsfiddle.net", originalWindow);
    }

    @Test
    @Order(5)
    public void testLoginButton() {
        driver.get(BASE_URL);
        WebElement loginButton = wait.until(ExpectedConditions.elementToBeClickable(
            By.cssSelector(".loginBtn")));
        loginButton.click();

        WebElement loginForm = wait.until(ExpectedConditions.visibilityOfElementLocated(
            By.cssSelector(".auth-form")));
        Assertions.assertTrue(loginForm.isDisplayed(),
            "Expected login form to be visible");
    }

    @Test
    @Order(6)
    public void testForkFunctionality() {
        driver.get(BASE_URL + "show/light/");
        WebElement forkButton = wait.until(ExpectedConditions.elementToBeClickable(
            By.id("fork")));
        forkButton.click();

        WebElement saveButton = wait.until(ExpectedConditions.visibilityOfElementLocated(
            By.id("save")));
        Assertions.assertTrue(saveButton.isDisplayed(),
            "Expected save button after forking");
    }

    private void assertExternalLink(String expectedDomain, String originalWindow) {
        wait.until(ExpectedConditions.numberOfWindowsToBe(2));
        for (String windowHandle : driver.getWindowHandles()) {
            if (!originalWindow.contentEquals(windowHandle)) {
                driver.switchTo().window(windowHandle);
                break;
            }
        }
        Assertions.assertTrue(driver.getCurrentUrl().contains(expectedDomain),
            "Expected to be on " + expectedDomain + " after clicking link");
        driver.close();
        driver.switchTo().window(originalWindow);
    }
}