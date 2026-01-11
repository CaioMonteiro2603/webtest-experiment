package SunaDeepSeek.ws05.seq02;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation;
import org.junit.jupiter.api.TestMethodOrder;
import org.openqa.selenium.*;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import java.time.Duration;
import java.util.List;

@TestMethodOrder(OrderAnnotation.class)
public class TAT {

    private static WebDriver driver;
    private static WebDriverWait wait;
    private static final String BASE_URL = "https://cac-tat.s3.eu-central-1.amazonaws.com/index.html";

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
        Assertions.assertEquals("Central de Atendimento ao Cliente TAT", driver.getTitle());
        Assertions.assertTrue(driver.getCurrentUrl().contains("index.html"));
    }

    @Test
    @Order(2)
    public void testSuccessfulLogin() {
        driver.get(BASE_URL);
        WebElement firstNameInput = wait.until(ExpectedConditions.presenceOfElementLocated(By.cssSelector("[data-testid=first-name]")));
        WebElement lastNameInput = driver.findElement(By.cssSelector("[data-testid=last-name]"));
        WebElement emailInput = driver.findElement(By.cssSelector("[data-testid=email]"));
        WebElement phoneInput = driver.findElement(By.cssSelector("[data-testid=phone]"));
        WebElement textArea = driver.findElement(By.tagName("textarea"));
        WebElement submitButton = driver.findElement(By.tagName("button"));

        firstNameInput.sendKeys("John");
        lastNameInput.sendKeys("Doe");
        emailInput.sendKeys("john.doe@example.com");
        phoneInput.sendKeys("1234567890");
        textArea.sendKeys("Test message");

        submitButton.click();

        wait.until(ExpectedConditions.textToBe(By.tagName("body"), "Form successfully submitted!"));
        Assertions.assertTrue(driver.getPageSource().contains("Form successfully submitted!"));
    }

    @Test
    @Order(3)
    public void testFailedLogin() {
        driver.get(BASE_URL);
        WebElement firstNameInput = driver.findElement(By.cssSelector("[data-testid=first-name]"));
        WebElement lastNameInput = driver.findElement(By.cssSelector("[data-testid=last-name]"));
        WebElement emailInput = driver.findElement(By.cssSelector("[data-testid=email]"));
        WebElement phoneInput = driver.findElement(By.cssSelector("[data-testid=phone]"));
        WebElement textArea = driver.findElement(By.tagName("textarea"));
        WebElement submitButton = driver.findElement(By.tagName("button"));

        firstNameInput.sendKeys("John");
        lastNameInput.sendKeys("Doe");
        emailInput.sendKeys("invalid-email");
        phoneInput.sendKeys("1234567890");
        
        submitButton.click();

        WebElement errorMessage = driver.findElement(By.id("email-error"));
        Assertions.assertTrue(errorMessage.getText().contains("Email is invalid"));
    }

    @Test
    @Order(4)
    public void testSortingDropdown() {
        driver.get(BASE_URL + "?sort=true");
        
        WebElement sortDropdown = wait.until(ExpectedConditions.elementToBeClickable(
            By.cssSelector("[data-testid=sort-dropdown]")));
        sortDropdown.click();

        List<WebElement> options = driver.findElements(By.cssSelector("[data-testid=sort-dropdown] option"));
        Assertions.assertTrue(options.size() > 0);

        sortDropdown.sendKeys("Name");
        List<WebElement> items = driver.findElements(By.cssSelector("[data-testid=contact-item]"));
        Assertions.assertTrue(items.size() > 0);
    }

    @Test
    @Order(5)
    public void testMenuNavigation() {
        driver.get(BASE_URL + "?menu=true");
        
        WebElement menuButton = wait.until(ExpectedConditions.elementToBeClickable(By.id("menu-button")));
        menuButton.click();

        WebElement homeLink = wait.until(ExpectedConditions.elementToBeClickable(By.linkText("Home")));
        homeLink.click();
        Assertions.assertTrue(driver.getCurrentUrl().contains("index.html"));

        menuButton.click();
        WebElement aboutLink = driver.findElement(By.linkText("About"));
        aboutLink.click();
        String originalWindow = driver.getWindowHandle();
        wait.until(ExpectedConditions.numberOfWindowsToBe(2));
        
        for (String windowHandle : driver.getWindowHandles()) {
            if (!windowHandle.equals(originalWindow)) {
                driver.switchTo().window(windowHandle);
                break;
            }
        }
        
        Assertions.assertTrue(driver.getCurrentUrl().contains("about"));
        driver.close();
        driver.switchTo().window(originalWindow);
    }

    @Test
    @Order(6)
    public void testFooterSocialLinks() {
        driver.get(BASE_URL + "?social=true");
        
        WebElement twitterLink = wait.until(ExpectedConditions.elementToBeClickable(
            By.cssSelector("[data-testid=twitter-link]")));
        Assertions.assertTrue(twitterLink.isDisplayed());
        
        WebElement facebookLink = driver.findElement(By.cssSelector("[data-testid=facebook-link]"));
        Assertions.assertTrue(facebookLink.isDisplayed());
        
        WebElement linkedinLink = driver.findElement(By.cssSelector("[data-testid=linkedin-link]"));
        Assertions.assertTrue(linkedinLink.isDisplayed());
    }
}