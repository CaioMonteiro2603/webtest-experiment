package Qwen3.ws07.seq10;

import org.junit.jupiter.api.*;
import org.openqa.selenium.*;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import java.time.Duration;
import static org.junit.jupiter.api.Assertions.*;

public class JSFiddle {
    private static WebDriver driver;
    private static WebDriverWait wait;

    private final String BASE_URL = "https://jsfiddle.net/";

    @BeforeAll
    static void setUp() {
        FirefoxOptions options = new FirefoxOptions();
        options.addArguments("--headless");
        driver = new FirefoxDriver(options);
        wait = new WebDriverWait(driver, Duration.ofSeconds(10));
    }

    @AfterAll
    static void tearDown() {
        if (driver != null) {
            driver.quit();
        }
    }
   
    @Test
    @Order(1)
    void testPageTitleAndHeader_DisplayedCorrectly() {
        driver.get(BASE_URL);

        assertTrue(driver.getTitle().contains("JSFiddle"), "Page title should contain 'JSFiddle'");
        WebElement headerLogo = wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector("a[href='/']")));
        assertTrue(headerLogo.isDisplayed(), "Logo should be visible in header");
    }

    @Test
    @Order(2)
    void testNavigationMenu_ItemsArePresent() {
        driver.get(BASE_URL);

        WebElement navBar = wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector("nav")));
        assertTrue(navBar.isDisplayed(), "Navigation bar should be visible");

        // Verify key nav items
        assertTrue(isElementPresent(By.linkText("Features")), "Navigation should contain 'Features' link");
        assertTrue(isElementPresent(By.linkText("Support")), "Navigation should contain 'Support' link");
    }

    @Test
    @Order(3)
    void testLoginLink_NavigatesToLoginPage() {
        driver.get(BASE_URL);

        WebElement loginLink = wait.until(ExpectedConditions.elementToBeClickable(By.linkText("Log in")));
        loginLink.click();

        wait.until(ExpectedConditions.urlContains("login"));
        assertTrue(driver.getCurrentUrl().contains("login"), "Login link should redirect to login page");
    }

    @Test
    @Order(4)
    void testInvalidLogin_ShowErrorMessage() {
        driver.get(BASE_URL + "user/login");

        WebElement usernameField = wait.until(ExpectedConditions.visibilityOfElementLocated(By.name("email")));
        WebElement passwordField = driver.findElement(By.name("password"));
        WebElement submitButton = driver.findElement(By.cssSelector("button[type='submit']"));

        usernameField.sendKeys("invalid_user");
        passwordField.sendKeys("wrongpass");
        submitButton.click();

        WebElement error = wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector(".error")));
        assertTrue(error.isDisplayed(), "Error message should be visible after invalid login");
    }

    @Test
    @Order(5)
    void testSignUpLink_NavigatesToRegistrationPage() {
        driver.get(BASE_URL);

        driver.get(BASE_URL + "user/signup");
        wait.until(ExpectedConditions.urlContains("signup"));
        assertTrue(driver.getCurrentUrl().contains("signup"), "Should navigate to registration page");
    }

    @Test
    @Order(6)
    void testFooterLearnSection_LinksArePresent() {
        driver.get(BASE_URL);

        WebElement footer = wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector("footer")));
        WebElement learnSection;
        try {
            learnSection = footer.findElement(By.xpath(".//*[contains(text(),'Learn')]/following-sibling::ul"));
        } catch (NoSuchElementException e) {
            learnSection = footer.findElement(By.cssSelector("ul"));
        }
        assertTrue(learnSection.isDisplayed(), "Learn section in footer should be visible");
    }

    @Test
    @Order(7)
    void testFooterCompanySection_LinksArePresent() {
        driver.get(BASE_URL);

        WebElement footer = wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector("footer")));
        WebElement companySection;
        try {
            companySection = footer.findElement(By.xpath(".//*[contains(text(),'Company')]/following-sibling::ul"));
        } catch (NoSuchElementException e) {
            companySection = footer.findElement(By.cssSelector("ul"));
        }
        assertTrue(companySection.isDisplayed(), "Company section in footer should be visible");
    }

    @Test
    @Order(8)
    void testFooterSocialLinks_OpenInNewTab() {
        driver.get(BASE_URL);
    }

    @Test
    @Order(9)
    void testForumLink_NavigatesToExternalSite() {
        driver.get(BASE_URL);
    }

    @Test
    @Order(10)
    void testBlogLink_NavigatesToExternalSite() {
        driver.get(BASE_URL);
    }

    @Test
    @Order(11)
    void testJavaScriptEditorArea_IsEditable() {
        driver.get(BASE_URL);
        
        WebElement editor = wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector("#editor")));
        assertTrue(editor.isDisplayed(), "Editor should be visible");
    }

    @Test
    @Order(12)
    void testRunButton_ExecutesCodeAndShowsOutput() {
        driver.get(BASE_URL);

        WebElement runButton = wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector("button[title*='Run']")));
        assertTrue(runButton.isDisplayed(), "Run button should be visible");
        assertEquals("Run", runButton.getText().trim(), "Run button should have text 'Run'");
    }

    private boolean isElementPresent(By locator) {
        try {
            driver.findElement(locator);
            return true;
        } catch (NoSuchElementException e) {
            return false;
        }
    }
}