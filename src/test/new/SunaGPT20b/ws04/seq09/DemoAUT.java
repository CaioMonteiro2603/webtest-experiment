package SunaGPT20b.ws04.seq09;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;

import org.openqa.selenium.By;
import org.openqa.selenium.Keys;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.WindowType;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.net.URL;
import java.time.Duration;
import java.util.List;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class DemoAUT {

    private static final String BASE_URL = "https://katalon-test.s3.amazonaws.com/aut/html/form.html";
    private static WebDriver driver;
    private static WebDriverWait wait;

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

    @Test
    @Order(1)
    public void testBasePageLoads() {
        driver.get(BASE_URL);
        wait.until(ExpectedConditions.presenceOfElementLocated(By.tagName("form")));
        Assertions.assertTrue(driver.getCurrentUrl().startsWith(BASE_URL),
                "Base URL should be loaded");
    }

    @Test
    @Order(2)
    public void testFormSubmission() {
        driver.get(BASE_URL);
        WebElement form = wait.until(ExpectedConditions.presenceOfElementLocated(By.tagName("form")));

        List<WebElement> inputs = form.findElements(By.xpath(
                ".//input[not(@type='hidden') and not(@type='submit') and not(@type='button') and not(@type='reset') and not(@type='checkbox') and not(@type='radio') and not(@type='file') and not(@type='image') and not(@type='range') and not(@type='color') and not(@type='date') and not(@type='datetime-local') and not(@type='month') and not(@type='time') and not(@type='week') and not(@type='search') and not(@type='tel') and not(@type='url') and not(@type='email') and not(@type='password') and not(@type='number')]"));
        for (WebElement input : inputs) {
            String type = input.getAttribute("type");
            if (type == null || type.isEmpty() || type.equals("text")) {
                input.clear();
                input.sendKeys("test");
            } else if (type.equals("email")) {
                input.clear();
                input.sendKeys("test@example.com");
            } else if (type.equals("password")) {
                input.clear();
                input.sendKeys("Password123");
            } else {
                input.clear();
                input.sendKeys("test");
            }
        }

        List<WebElement> submitButtons = form.findElements(By.xpath(
                ".//button[@type='submit']|.//input[@type='submit']"));
        if (!submitButtons.isEmpty()) {
            WebElement submit = submitButtons.get(0);
            wait.until(ExpectedConditions.elementToBeClickable(submit)).click();
        } else if (!inputs.isEmpty()) {
            inputs.get(0).sendKeys(Keys.ENTER);
        }

        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        
        Assertions.assertNotEquals(BASE_URL, driver.getCurrentUrl(),
                "URL should change after form submission");
    }

    @Test
    @Order(3)
    public void testInternalLinks() {
        driver.get(BASE_URL);
        List<WebElement> internalLinks = driver.findElements(By.xpath(
                "//a[starts-with(@href, 'https://katalon-test.s3.amazonaws.com/aut/html/') and not(@href='" + BASE_URL + "')]"));
        if (!internalLinks.isEmpty()) {
            for (WebElement link : internalLinks) {
                String href = link.getAttribute("href");
                driver.navigate().to(href);
                wait.until(ExpectedConditions.urlContains(href));
                Assertions.assertTrue(driver.getCurrentUrl().startsWith(href),
                        "Navigated to internal link: " + href);
                driver.navigate().back();
                wait.until(ExpectedConditions.urlToBe(BASE_URL));
            }
        } else {
            Assertions.assertTrue(true, "No internal links found to test");
        }
    }

    @Test
    @Order(4)
    public void testExternalLinks() throws Exception {
        driver.get(BASE_URL);
        List<WebElement> externalLinks = driver.findElements(By.xpath(
                "//a[starts-with(@href, 'http') and not(contains(@href, 'katalon-test.s3.amazonaws.com'))]"));
        Assertions.assertTrue(!externalLinks.isEmpty(),
                "There should be external links on the page");

        String originalHandle = driver.getWindowHandle();

        for (WebElement link : externalLinks) {
            String href = link.getAttribute("href");
            driver.switchTo().newWindow(WindowType.TAB);
            driver.get(href);
            wait.until(ExpectedConditions.urlContains(href));
            URL url = new URL(href);
            Assertions.assertTrue(driver.getCurrentUrl().contains(url.getHost()),
                    "External link domain should match: " + href);
            driver.close();
            driver.switchTo().window(originalHandle);
        }
    }
}