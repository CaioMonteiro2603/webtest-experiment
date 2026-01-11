package deepseek.ws07.seq03;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation;
import org.junit.jupiter.api.TestMethodOrder;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
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
    public void testHomePageLoad() {
        driver.get(BASE_URL);
        WebElement title = wait.until(ExpectedConditions.visibilityOfElementLocated(
            By.cssSelector("h1")));
        Assertions.assertTrue(title.getText().contains("JavaScript") || title.getText().contains("JSFiddle"));
    }

    @Test
    @Order(2)
    public void testEditorInteraction() {
        driver.get(BASE_URL);
        
        // Wait for page to load completely
        wait.until(ExpectedConditions.presenceOfElementLocated(By.cssSelector("body")));
        
        // Look for run button in the main page
        WebElement runButton = wait.until(ExpectedConditions.elementToBeClickable(
            By.cssSelector(".run-button, .run, [data-action='run'], button[title*='run' i]")));
        Assertions.assertTrue(runButton.isDisplayed());
    }

    @Test
    @Order(3)
    public void testNavigationLinks() {
        driver.get(BASE_URL);
        
        // Look for navigation menu items
        WebElement blogLink = wait.until(ExpectedConditions.elementToBeClickable(
            By.xpath("//a[contains(text(),'Blog') or contains(@href,'blog')]")));
        blogLink.click();
        
        // Wait for navigation and check URL
        wait.until(ExpectedConditions.or(
            ExpectedConditions.urlContains("blog"),
            ExpectedConditions.not(ExpectedConditions.urlToBe(BASE_URL))
        ));
        
        // Navigate back to home
        driver.get(BASE_URL);
    }

    @Test
    @Order(4)
    public void testLoginModal() {
        driver.get(BASE_URL);
        
        // Look for login/signin button with various selectors
        WebElement loginButton = wait.until(ExpectedConditions.elementToBeClickable(
            By.xpath("//a[contains(text(),'Login') or contains(text(),'Sign In') or contains(@class,'login')]")));
        loginButton.click();
        
        // Verify modal or login form appears
        WebElement modal = wait.until(ExpectedConditions.visibilityOfElementLocated(
            By.cssSelector(".auth-modal, .login-modal, .modal, form[action*='login']")));
        Assertions.assertTrue(modal.isDisplayed());
    }

    @Test
    @Order(5)
    public void testCodePanes() {
        driver.get(BASE_URL);
        
        // Look for code editor areas with various selectors
        WebElement htmlPane = wait.until(ExpectedConditions.presenceOfElementLocated(
            By.cssSelector(".panel-html, #html, .html-panel, [data-pane='html']")));
        WebElement cssPane = driver.findElement(By.cssSelector(".panel-css, #css, .css-panel, [data-pane='css']"));
        WebElement jsPane = driver.findElement(By.cssSelector(".panel-js, #js, .javascript-panel, [data-pane='javascript']"));
        
        Assertions.assertTrue(htmlPane.isDisplayed());
        Assertions.assertTrue(cssPane.isDisplayed());
        Assertions.assertTrue(jsPane.isDisplayed());
    }

    @Test
    @Order(6)
    public void testSocialLinks() {
        driver.get(BASE_URL);
        
        // Look for social media links with broader selectors
        WebElement twitterLink = wait.until(ExpectedConditions.presenceOfElementLocated(
            By.cssSelector("a[href*='twitter'], a[href*='x.com'], .social-twitter, .fa-twitter")));
        Assertions.assertTrue(twitterLink.isDisplayed());
    }

    @Test
    @Order(7)
    public void testFooterLinks() {
        driver.get(BASE_URL);
        
        // Look for footer links with broader selectors
        WebElement termsLink = wait.until(ExpectedConditions.presenceOfElementLocated(
            By.xpath("//a[contains(text(),'Terms') or contains(@href,'terms') or contains(@href,'legal')]")));
        Assertions.assertTrue(termsLink.isDisplayed());
        
        // Look for privacy link
        WebElement privacyLink = driver.findElement(
            By.xpath("//a[contains(text(),'Privacy') or contains(@href,'privacy')]"));
        Assertions.assertTrue(privacyLink.isDisplayed());
    }
}