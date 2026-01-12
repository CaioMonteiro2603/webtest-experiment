package GPT20b.ws07.seq02;

import java.net.URI;
import java.time.Duration;
import java.util.List;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation;
import org.openqa.selenium.*;
import org.openqa.selenium.firefox.*;
import org.openqa.selenium.support.ui.*;

@TestMethodOrder(OrderAnnotation.class)
public class JSFiddle {

    private static WebDriver driver;
    private static WebDriverWait wait;

    private static final String BASE_URL =
            "https://cac-tat.s3.eu-central-1.amazonaws.com/index.html";
    private static final String USER_EMAIL = "caio@gmail.com";
    private static final String USER_PASSWORD = "123";

    @BeforeAll
    public static void initDriver() {
        FirefoxOptions options = new FirefoxOptions();
        options.addArguments("--headless");

        driver = new FirefoxDriver(options);
        wait = new WebDriverWait(driver, Duration.ofSeconds(10));
    }

    @AfterAll
    public static void closeDriver() {
        if (driver != null) driver.quit();
    }



    /* ------------------------------------------------------------ */

    @Test
    @Order(2)
    public void testValidLogin() {
        driver.navigate().to(BASE_URL);

        WebElement email = wait.until(ExpectedConditions.visibilityOfElementLocated(
                By.cssSelector("input[type='email'], input[name='login']")
        ));
        email.sendKeys(USER_EMAIL);

        WebElement password = wait.until(ExpectedConditions.visibilityOfElementLocated(
                By.cssSelector("input[name='password']")
        ));
        password.sendKeys(USER_PASSWORD);

        wait.until(ExpectedConditions.elementToBeClickable(
                By.cssSelector("button[type='submit']")
        )).click();

        wait.until(ExpectedConditions.or(
                ExpectedConditions.urlContains("home"),
                ExpectedConditions.urlContains("dashboard"),
                ExpectedConditions.presenceOfElementLocated(
                        By.cssSelector(".profile")
                )
        ));

        Assertions.assertTrue(
                driver.getCurrentUrl().contains("home")
                        || driver.getCurrentUrl().contains("dashboard"),
                "Login should redirect to home or dashboard"
        );

        logoutIfPresent();
    }

    /* ------------------------------------------------------------ */

    @Test
    @Order(3)
    public void testInvalidLogin() {
        driver.navigate().to(BASE_URL);

        wait.until(ExpectedConditions.visibilityOfElementLocated(
                By.cssSelector("input[name='login']")
        )).sendKeys("wrong@email.com");

        wait.until(ExpectedConditions.visibilityOfElementLocated(
                By.cssSelector("input[name='password']")
        )).sendKeys("wrongpass");

        wait.until(ExpectedConditions.elementToBeClickable(
                By.cssSelector("button[type='submit']")
        )).click();

        WebElement error = wait.until(ExpectedConditions.visibilityOfElementLocated(
                By.cssSelector(".error")
        ));

        Assertions.assertFalse(error.getText().isBlank(),
                "Error message should be visible");
    }

    /* ------------------------------------------------------------ */

    @Test
    @Order(4)
    public void testSortingDropdown() {
        driver.navigate().to(BASE_URL);

        WebElement selectEl = wait.until(ExpectedConditions.presenceOfElementLocated(
                By.cssSelector("select[name='sort']")
        ));

        Select select = new Select(selectEl);
        List<WebElement> options = select.getOptions();

        Assumptions.assumeTrue(options.size() > 1,
                "Sorting dropdown has insufficient options");

        String previous = null;

        for (WebElement opt : options) {
            select.selectByVisibleText(opt.getText());

            wait.until(ExpectedConditions.presenceOfElementLocated(
                    By.cssSelector(".product-item")
            ));

            String current = getFirstProductName();
            if (previous != null) {
                Assertions.assertNotEquals(previous, current,
                        "Sorting should change first item");
            }
            previous = current;
        }
    }

    /* ------------------------------------------------------------ */

    @Test
    @Order(5)
    public void testExternalLinksOnHome() throws Exception {
        driver.navigate().to(BASE_URL);

        List<WebElement> links =
                driver.findElements(By.cssSelector("a[href^='http']"));

        Assumptions.assumeTrue(!links.isEmpty(),
                "No external links found");

        for (WebElement link : links) {
            String href = link.getAttribute("href");
            URI uri = new URI(href);

            if (!uri.getHost().contains("cac-tat")) {
                checkExternalLink(link, uri.getHost());
            }
        }
    }

    /* ------------------------------------------------------------ */

    private void logoutIfPresent() {
        List<WebElement> logout =
                driver.findElements(By.linkText("Logout"));
        if (!logout.isEmpty()) {
            logout.get(0).click();
            wait.until(ExpectedConditions.elementToBeClickable(
                    By.cssSelector("button[type='submit']")
            ));
        }
    }

    private String getFirstProductName() {
        List<WebElement> items =
                driver.findElements(By.cssSelector(
                        ".product-item .name"));
        return items.isEmpty() ? "" : items.get(0).getText();
    }

    private void checkExternalLink(WebElement link, String host) {
        String original = driver.getWindowHandle();

        link.click();

        wait.until(d -> d.getWindowHandles().size() > 1);

        for (String h : driver.getWindowHandles()) {
            if (!h.equals(original)) {
                driver.switchTo().window(h);

                wait.until(ExpectedConditions.urlContains(host));

                Assertions.assertTrue(
                        driver.getCurrentUrl().contains(host),
                        "External link should contain host: " + host
                );

                driver.close();
                driver.switchTo().window(original);
                break;
            }
        }
    }
}