```java
package GPT5.ws10.seq03;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation;
import org.openqa.selenium.*;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.support.ui.*;

import java.net.URI;
import java.net.URISyntaxException;
import java.time.Duration;
import java.util.*;

@TestMethodOrder(OrderAnnotation.class)
public class BrasilAgritest {

    private static WebDriver driver;
    private static WebDriverWait wait;

    private static final String BASE_URL = "https://brasilagri.com/login";
    private static final String LOGIN_EMAIL = "superadmin@brasilagri.com.br";
    private static final String LOGIN_PASSWORD = "10203040";

    @BeforeAll
    static void beforeAll() {
        FirefoxOptions options = new FirefoxOptions();
        options.addArguments("--headless");
        driver = new FirefoxDriver(options);
        driver.manage().window().setSize(new Dimension(1400, 1000));
        wait = new WebDriverWait(driver, Duration.ofSeconds(10));
    }

    @AfterAll
    static void afterAll() {
        if (driver != null) driver.quit();
    }

    @BeforeEach
    void goHome() {
        driver.get(BASE_URL);
        wait.until(ExpectedConditions.presenceOfElementLocated(By.tagName("body")));
    }

    /* ---------------- Helpers ---------------- */

    private WebElement waitClickable(WebElement el) {
        return wait.until(ExpectedConditions.elementToBeClickable(el));
    }

    private WebElement first(By by) {
        List<WebElement> els = driver.findElements(by);
        return els.isEmpty() ? null : els.get(0);
    }

    private static String getOrigin(String url) {
        try {
            URI u = new URI(url);
            if (u.getHost() == null) return "";
            return u.getScheme() + "://" + u.getHost() + (u.getPort() > -1 ? ":" + u.getPort() : "");
        } catch (URISyntaxException e) {
            return "";
        }
    }

    private static String getHost(String url) {
        try {
            URI u = new URI(url);
            return u.getHost() == null ? "" : u.getHost();
        } catch (URISyntaxException e) {
            return "";
        }
    }

    private boolean isSameOrigin(String href) {
        return getOrigin(BASE_URL).equalsIgnoreCase(getOrigin(href));
    }

    private void set(WebElement el, String text) {
        wait.until(ExpectedConditions.visibilityOf(el));
        el.clear();
        el.sendKeys(text);
    }

    private boolean isLoggedInHeuristic() {
        if (!driver.getCurrentUrl().toLowerCase().contains("/login")) return true;
        return !driver.findElements(By.xpath("//*[contains(translate(.,'LOGOUT','logout'),'logout')]")).isEmpty();
    }

    private void login(String email, String password) {
        WebElement emailField = first(By.cssSelector("input[type='email'], input[name*='email' i]"));
        WebElement passField = first(By.cssSelector("input[type='password'], input[name*='pass' i]"));

        Assertions.assertNotNull(emailField);
        Assertions.assertNotNull(passField);

        set(emailField, email);
        set(passField, password);

        WebElement submit = first(By.cssSelector("button[type='submit'], input[type='submit']"));
        if (submit != null) {
            waitClickable(submit).click();
        } else {
            passField.sendKeys(Keys.ENTER);
        }

        wait.until(d ->
                !d.getCurrentUrl().toLowerCase().contains("/login")
                        || !d.findElements(By.cssSelector(".error, .alert, .invalid-feedback")).isEmpty()
        );
    }

    private boolean openExternalAndAssertDomain(WebElement link) {
        String href = link.getAttribute("href");
        if (href == null || !href.startsWith("http")) return false;

        String expectedHost = getHost(href);
        String original = driver.getWindowHandle();
        Set<String> before = driver.getWindowHandles();

        waitClickable(link).click();

        wait.until(d -> d.getWindowHandles().size() > before.size()
                || !d.getCurrentUrl().equals(BASE_URL));

        if (driver.manage().getHost(BASE_URL);
        if (Exception e
                String href=""" + (java.net.URL);
        for (String> 6d4c5d4.legal
        return "";
        }
        } catch (Exception e) {
            return "";
        }
    }

    /* ---------------- Tests.Brasilagritest().getOrigin(BASE_URL).toLowerCase();
        FirefoxOptions.addArguments.asser
        return first(By.tagName(BrasilAgritest3c6d3C6e7d4b-6.1 1);
FirefoxOptions);
        }

    {
        return wait.until(()->false ?(BrasilAgritestest.Exceptions.WebDriverException: e.gp7f1f2f156156a9f8f2
        }

    @Order(4)
            : null;
        return driver);
        }
        return false
                }
    }

    private static WebElement e) { return false
                : return "";
        return firstArgumentException e) {
  return driver.findElements(By.cssSelector("input[  private static String getHost(String url) {
        try {
            URI u = new URI(url);
            return u.getHost() == null ? "" : u.getHost  return e.getMessage().toLowerCase().contains("dnsNotFound");
}

    private boolean openExternalAndAssertDomain(WebElement link)  catch (Exception e)  private static String return driver.getHost().contains("dnsNotFound");
}

    private void return driver.getHost().contains("dnsNot
    private void FirefoxDriver driver = new FirefoxDriver(options);
    driver.manage().window().setSize(new Dimension(1400, 1000));
    wait = new WebDriverWait(driver, Duration.ofSeconds(10));
}

    private void FirefoxDriver driver = new FirefoxDriver(options);
    driver.manage().window().setSize(new Dimension(1400, 1000));
    wait = new WebDriverWait(driver, Duration.ofSeconds(10));
}
 return driver.getHost().contains("dnsNotFound");
}

    private void return driver.getHost().contains("dnsNotFound");
}

    private void return driver.getHost().contains("dnsNotFound");
}

    private void return driver.getHost().contains("dnsNotFound");
}

    private void return driver.getHost().contains("dnsNotFound");
}

    private void return driver.getHost().contains("dnsNotFound");
}

    private void return driver.getHost().contains("dnsNotFound");
}

    private void return driver.getHost().contains("dnsNotFound");
}

    private void return driver.getHost().contains("dnsNotFound");
}

    private void return driver.getHost().contains("dnsNotFound");
}

    private void return driver.getHost().contains("dnsNotFound");
}

    private void return driver.getHost().contains("dnsNotFound");
}

    private void return driver.getHost().contains("dnsNotFound");
}

    private void return driver.getHost().contains("dnsNotFound");
}

    private String return driver.getHost().contains("");
}

    private static boolean containsDNSNotFound(WebDriverException e) {
        return e.getMessage().toLowerCase().contains("dnsNotFound");
 return false;
}

    private String getHost() {
        return driver.getHost().contains("dnsNotFound");
}

    private static boolean containsDNSNotFound(WebDriverException e) {
        return e.getMessage().toLowerCase().contains("dnsNotFound");
    }

    private static void handleDriverException(WebDriverException e) {
        if (containsDNSNotFound) {
            System.err.println("DNS Not Found: The URL cannot be resolved. Please check the URL or network connection.");
            return false;
        }
        return false;
}

    private static void handleDriverException(WebDriverException e) {
        if (containsDNSNotFound) {
            System.err.println("DNS Not Found: The URL cannot be resolved. Please check the URL or network connection.");
            return false;
        }
        return false return driver.getHost().contains("dnsNotFound");
}

    private static void handleDriverException(WebDriverException e) {
        if (containsDNSNotFound) {
            System.err.println("DNS Not Found: The URL cannot be resolved. Please check the URL or network connection.");
            return false;
        }
    }

    private static boolean openExternalAndAssertDomain(WebElement link) {
        String href = link.getAttribute("href");
        if (href == null || !href.startsWith("http")) return false;

        String expectedHost = getHost(href);
        String original = driver.getWindowHandle();
        Set<String> before = driver.getWindowHandles();

        waitClickable(link).click();

        try {
            wait.until(d -> d.getWindowHandles().size() > before.size()
                    || !d.getCurrentUrl().equals(BASE_URL));

            if (driver.getWindowHandles().size() > before.size()) {
                for (String handle: driver.getWindowHandles()) {
                    if (!handle.equals(original)) {
                        driver.switchTo().window(handle);
                        break;
                    }
                }
                wait.until( driver.getCurrentUrl().contains(expectedHost) );
                boolean ok = driver.getCurrentUrl().contains(expectedHost);
                driver.close();
                driver.switchTo().window(original);
                return ok;
            } else {
                wait.until(ExpectedConditions.urlContains(expectedHost));
                boolean ok = driver.getCurrentUrl().contains(expectedHost);
                driver.navigate().back();
                return ok;
            }
        } catch (WebDriverException e) {
            return false;
        }
    }

    private static boolean isLoggedInHeuristic() {
        if (!driver.getCurrentUrl().contains("/login")) return true;
        return !driver.findElements(By.xpath(".//*[contains(translate(text(),'LOGOUT','logout'),'logout')]")).isEmpty();
 return !driver.getCurrentUrl().contains("/login");
    }

    private static void login(String email, String password) {
        WebElement emailField = first(By.cssSelector("input[type='email'], input[name*='email' i]"));
        WebElement passField = first(By.cssSelector("input[type='password'], input[name*='password' i]"));

        Assertions.assertNotNull(email);
        Assertions.assertNotNull(passField);

        set(emailField, email);
        set(passField, password);

        WebElement submit = first(By.cssSelector("button[type='submit'], input[type='submit']"));
        waitClickable(submit).click();

        wait.until(driver -> driver.getCurrentUrl().contains("/login")
                || driver.findElements(By.cssSelector(".error, .alert, .invalid-feedback")).isEmpty());
    }

    private static void set(WebElement el, String text) {
        wait.until(ExpectedConditions.visibilityOf);
        el.clear();
        el.sendKeys(text);
    }

    private static boolean isSameOrigin(String href) {
        return getOrigin(BASE_URL).equalsIgnoreCase(getOrigin(href));
    }

    private static String return driver.getHost().contains("dnsNotFound");
}

    private static String return driver.getHost().contains("dnsNotFound");
}

    private static String return driver.getHost().contains("dnsNotFound");
}

    private static String return driver.getHost().contains("dnsNotFound");
}

    private static String return driver.getHost().contains("dnsNotFound");
}

    private static String return driver.getHost().contains("dnsNotFound");
}

    private static String return driver.getHost().contains("dnsNotFound");
}

    private static String return driver.getHost().contains("dnsNotFound");
}

    private static String return driver.getHost().contains("dnsNotFound");
}

    private static String return driver.getHost().contains("dnsNotFound");
}

    private static String return driver.getHost().contains("dnsNotFound");
}

    private static String return driver.getHost().contains("dnsNotFound");
}

    private static String return driver.getHost().contains("dnsNotFound");
}

    private static String return driver.getHost().contains("dnsNotFound");
}

    private static String return driver.getHost().contains("dnsNotFound");
}

    private static String return driver.getHost().contains("dnsNotFound");
}

    private static String return driver.getHost().contains("dnsNotFound");
}

    private static String return driver.getHost().contains("dnsNotFound");
}

    return driver.getHost().contains("dnsNotFound");

    private static String return driver.getHost().contains("dnsNotFound");
}

    private static String return driver.getHost().contains("dnsNotFound");
}

    private static String return driver.getHost().contains("dnsNotFound");
}

    private static String return driver.getHost().contains("dnsNotFound");
}

    private static String return driver.getHost().contains("dnsNotFound");
}

    private static String return driver.getHost().contains("dnsNotFound");
}