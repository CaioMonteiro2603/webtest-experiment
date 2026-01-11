package GPT20b.ws03.seq04;

import org.junit.jupiter.api.*;
import static org.junit.jupiter.api.Assertions.*;

import org.openqa.selenium.*;
import org.openqa.selenium.firefox.*;
import org.openqa.selenium.support.ui.*;

import java.time.Duration;
import java.util.*;
import java.util.NoSuchElementException;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class bugbank {

    private static final String BASE_URL = "https://bugbank.netlify.app/";
    private static final String USERNAME = "caio@gmail.com";
    private static final String PASSWORD = "123";

    private static WebDriver driver;
    private static WebDriverWait wait;

    @BeforeAll
    public static void init() {
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

    /* ---------------------------------------------------------------------- */
    /* Helper methods                                                          */
    /* ---------------------------------------------------------------------- */

    private WebElement findElement(String... cssSelectors) {
        for (String sel : cssSelectors) {
            List<WebElement> elements =
                    wait.until(d -> d.findElements(By.cssSelector(sel)));
            if (!elements.isEmpty()) {
                return elements.get(0);
            }
        }
        throw new NoSuchElementException(
                "Unable to locate element using selectors: " + Arrays.toString(cssSelectors));
    }

    private void navigateTo(String url) {
        driver.navigate().to(url);
    }

    private void logIn() {
        navigateTo(BASE_URL);

        WebElement usernameField = findElement(
                "input#user-name",
                "input[name='email']",
                "input[type='email']");

        WebElement passwordField = findElement(
                "input#password",
                "input[type='password']");

        usernameField.clear();
        usernameField.sendKeys(USERNAME);
        passwordField.clear();
        passwordField.sendKeys(PASSWORD);

        WebElement loginButton = findElement(
                "button[type='submit']",
                "button#login-button");
        loginButton.click();

        wait.until(ExpectedConditions.or(
                ExpectedConditions.visibilityOfElementLocated(By.cssSelector(".card__title")),
                ExpectedConditions.visibilityOfElementLocated(By.cssSelector(".home__Container"))
        ));

        assertTrue(
                driver.getCurrentUrl().contains("bugbank"),
                "Login did not redirect correctly"
        );
    }

    private void logOut() {
        WebElement burger = wait.until(ExpectedConditions.elementToBeClickable(
                By.cssSelector("#btnLogout, button[class*='logout']")
        ));
        burger.click();

        wait.until(ExpectedConditions.visibilityOfElementLocated(
                By.cssSelector("button[type='submit']")
        ));
    }

    private void openAndVerifyExternalLink(String partialHref, String domain) {
        List<WebElement> links =
                driver.findElements(By.cssSelector("a[href*='" + partialHref + "']"));
        if (links.isEmpty()) return;

        WebElement link = links.get(0);
        String originalWindow = driver.getWindowHandle();
        Set<String> oldWindows = driver.getWindowHandles();

        link.click();

        wait.until(d -> d.getWindowHandles().size() >= oldWindows.size());

        Set<String> newWindows = driver.getWindowHandles();
        newWindows.removeAll(oldWindows);

        if (!newWindows.isEmpty()) {
            String newWindow = newWindows.iterator().next();
            driver.switchTo().window(newWindow);
            wait.until(d -> d.getCurrentUrl().contains(domain));
            driver.close();
            driver.switchTo().window(originalWindow);
        } else {
            wait.until(d -> d.getCurrentUrl().contains(domain));
            driver.navigate().back();
        }
    }

    /* ---------------------------------------------------------------------- */
    /* Tests                                                                   */
    /* ---------------------------------------------------------------------- */

    @Test
    @Order(1)
    public void testInvalidLogin() {
        navigateTo(BASE_URL);

        WebElement username = findElement("input#user-name", "input[type='email']");
        WebElement password = findElement("input#password", "input[type='password']");

        username.sendKeys("wrong@email.com");
        password.sendKeys("wrongpass");

        findElement("button[type='submit']").click();

        WebElement error = wait.until(ExpectedConditions.visibilityOfElementLocated(
                By.cssSelector(".alert, .error, p")
        ));

        assertTrue(error.isDisplayed(), "Error message should be shown");
    }

    @Test
    @Order(2)
    public void testLoginAndLogout() {
        logIn();
        logOut();
    }

    @Test
    @Order(3)
    public void testSortingDropdown() {
        logIn();

        List<WebElement> dropdowns =
                driver.findElements(By.cssSelector("select"));

        if (dropdowns.isEmpty()) {
            logOut();
            return;
        }

        Select select = new Select(dropdowns.get(0));
        List<WebElement> options = select.getOptions();
        assertFalse(options.isEmpty(), "Sorting dropdown should have options");

        String previous = null;

        for (WebElement option : options) {
            select.selectByVisibleText(option.getText());
            WebElement firstItem = wait.until(ExpectedConditions.visibilityOfElementLocated(
                    By.cssSelector(".card__title, h3")
            ));

            if (previous != null) {
                assertNotEquals(previous, firstItem.getText(),
                        "Item order should change after sorting");
            }
            previous = firstItem.getText();
        }

        logOut();
    }

    @Test
    @Order(4)
    public void testFooterSocialLinks() {
        logIn();
        openAndVerifyExternalLink("instagram", "instagram");
        openAndVerifyExternalLink("linkedin", "linkedin");
        logOut();
    }
}
