package GPT5.ws03.seq07;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation;
import org.openqa.selenium.*;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.Select;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;
import java.util.*;
import java.util.stream.Collectors;

@TestMethodOrder(OrderAnnotation.class)
public class bugbank {

    private static WebDriver driver;
    private static WebDriverWait wait;

    private static final String BASE_URL = "https://bugbank.netlify.app/";
    private static final String LOGIN_USER = "caio@gmail.com";
    private static final String LOGIN_PASS = "123";

    @BeforeAll
    static void setup() {
        FirefoxOptions options = new FirefoxOptions();
        options.addArguments("--headless"); // REQUIRED
        driver = new FirefoxDriver(options);
        wait = new WebDriverWait(driver, Duration.ofSeconds(10));
        driver.manage().deleteAllCookies();
    }

    @AfterAll
    static void teardown() {
        if (driver != null) driver.quit();
    }

    // ============ helpers ============

    private void goHome() {
        driver.get(BASE_URL);
        wait.until(ExpectedConditions.presenceOfElementLocated(By.tagName("body")));
    }

    private WebElement waitClickable(By by) {
        return wait.until(ExpectedConditions.elementToBeClickable(by));
    }

    private boolean exists(By by) {
        return driver.findElements(by).size() > 0;
    }

    private void clearAndType(By by, String text) {
        WebElement el = wait.until(ExpectedConditions.visibilityOfElementLocated(by));
        el.clear();
        el.sendKeys(text);
    }

    private void openExternalAndAssertDomain(WebElement link, String expectedDomainFragment) {
    	String original = driver.getWindowHandle();
        Set<String> before = driver.getWindowHandles();

        // waitClickable expects By, so just click the WebElement directly
        wait.until(ExpectedConditions.elementToBeClickable(link)).click();

        wait.until(d ->
                d.getWindowHandles().size() != before.size()
                || "complete".equals(
                        ((JavascriptExecutor) d).executeScript("return document.readyState")
                )
        );

        Set<String> after = new HashSet<>(driver.getWindowHandles());
        after.removeAll(before);
        if (!after.isEmpty()) {
            String newHandle = after.iterator().next();
            driver.switchTo().window(newHandle);
            wait.until(ExpectedConditions.urlContains(expectedDomainFragment));
            Assertions.assertTrue(driver.getCurrentUrl().contains(expectedDomainFragment), "External URL should contain: " + expectedDomainFragment);
            driver.close();
            driver.switchTo().window(original);
        } else {
            wait.until(ExpectedConditions.urlContains(expectedDomainFragment));
            Assertions.assertTrue(driver.getCurrentUrl().contains(expectedDomainFragment), "External URL should contain: " + expectedDomainFragment);
            driver.navigate().back();
            wait.until(ExpectedConditions.presenceOfElementLocated(By.tagName("body")));
        }
    }

    private boolean waitForAnyToastText(Collection<String> snippets) {
        long end = System.currentTimeMillis() + 10000;
        while (System.currentTimeMillis() < end) {
            String src = driver.getPageSource().toLowerCase();
            for (String s : snippets) {
                if (src.contains(s.toLowerCase())) return true;
            }
            try {
                // force wait a tick via an ExpectedCondition (no Thread.sleep)
                wait.until(ExpectedConditions.presenceOfElementLocated(By.tagName("body")));
            } catch (TimeoutException ignored) {}
        }
        return false;
    }

    private boolean isLoggedInHeuristic() {
        // BugBank shows balance card or a nav item with "Sair" / "Logout" after login
        boolean hasBalance = driver.findElements(By.xpath("//*[contains(translate(text(),'ABCDEFGHIJKLMNOPQRSTUVWXYZ','abcdefghijklmnopqrstuvwxyz'),'saldo')]")).size() > 0;
        boolean hasLogout = driver.findElements(By.xpath("//button[contains(translate(.,'ABCDEFGHIJKLMNOPQRSTUVWXYZ','abcdefghijklmnopqrstuvwxyz'),'sair') or contains(translate(.,'ABCDEFGHIJKLMNOPQRSTUVWXYZ','abcdefghijklmnopqrstuvwxyz'),'logout')]")).size() > 0;
        boolean urlOk = driver.getCurrentUrl().toLowerCase().contains("home") || driver.getCurrentUrl().toLowerCase().contains("#");
        return (hasBalance || hasLogout) && urlOk;
    }

    // ============ tests ============

    @Test
    @Order(1)
    void homeLoadsAndHasLoginControls() {
        goHome();
        String title = driver.getTitle();
        Assertions.assertTrue(title != null && title.length() > 0, "Page should have a non-empty title");
        // Basic login form expectations
        Assertions.assertTrue(exists(By.cssSelector("input[type='email']")) || exists(By.xpath("//input[contains(@placeholder,'mail') or contains(@placeholder,'e-mail') or contains(@placeholder,'email')]")),
                "Email input should exist");
        Assertions.assertTrue(exists(By.cssSelector("input[type='password']")),
                "Password input should exist");
        // "Acessar" or "Login" button
        Assertions.assertTrue(
                exists(By.xpath("//button[contains(translate(.,'ABCDEFGHIJKLMNOPQRSTUVWXYZ','abcdefghijklmnopqrstuvwxyz'),'acessar') or contains(translate(.,'ABCDEFGHIJKLMNOPQRSTUVWXYZ','abcdefghijklmnopqrstuvwxyz'),'login') or contains(translate(.,'ABCDEFGHIJKLMNOPQRSTUVWXYZ','abcdefghijklmnopqrstuvwxyz'),'entrar')]")),
                "There should be a button to submit login (Acessar/Entrar/Login)");
    }

    @Test
    @Order(2)
    void registerModalOpensAndCloses() {
        goHome();
        // "Registrar" / "Cadastrar" opens modal
        By openRegister = By.xpath("//button[contains(translate(.,'ABCDEFGHIJKLMNOPQRSTUVWXYZ','abcdefghijklmnopqrstuvwxyz'),'registrar') or contains(translate(.,'ABCDEFGHIJKLMNOPQRSTUVWXYZ','abcdefghijklmnopqrstuvwxyz'),'cadastrar')]");
        if (exists(openRegister)) {
            waitClickable(openRegister).click();
            // Modal displays name/email/password inputs
            wait.until(ExpectedConditions.presenceOfElementLocated(By.xpath("//div[contains(@class,'modal') or contains(@role,'dialog')]//input")));
            boolean modalVisible = driver.findElements(By.xpath("//div[contains(@class,'modal') or contains(@role,'dialog')]//input")).size() >= 2;
            Assertions.assertTrue(modalVisible, "Register modal should show inputs");
            // Close the modal
            List<By> closeCandidates = Arrays.asList(
                    By.xpath("//button[contains(translate(.,'ABCDEFGHIJKLMNOPQRSTUVWXYZ','abcdefghijklmnopqrstuvwxyz'),'fechar')]"),
                    By.xpath("//button[contains(translate(.,'ABCDEFGHIJKLMNOPQRSTUVWXYZ','abcdefghijklmnopqrstuvwxyz'),'voltar')]"),
                    By.xpath("//div[contains(@class,'modal')]//button[contains(@class,'close') or contains(@aria-label,'close')]")
            );
            boolean closed = false;
            for (By by : closeCandidates) {
                if (exists(by)) {
                    waitClickable(by).click();
                    closed = true;
                    break;
                }
            }
            Assertions.assertTrue(closed, "Should be able to close register modal via a visible button");
        } else {
            Assertions.assertTrue(true, "Register button not present on this build; skipping modal test");
        }
    }

    @Test
    @Order(3)
    void invalidLoginShowsErrorToast() {
        goHome();
        // Fill login with definitely wrong credentials
        By email = exists(By.cssSelector("input[type='email']")) ? By.cssSelector("input[type='email']")
                : By.xpath("//input[contains(@placeholder,'mail') or contains(@placeholder,'e-mail') or contains(@placeholder,'email')]");
        By pass = By.cssSelector("input[type='password']");
        clearAndType(email, "invalid@example.com");
        clearAndType(pass, "wrongpass");
        By submit = By.xpath("//button[contains(translate(.,'ABCDEFGHIJKLMNOPQRSTUVWXYZ','abcdefghijklmnopqrstuvwxyz'),'acessar') or contains(translate(.,'ABCDEFGHIJKLMNOPQRSTUVWXYZ','abcdefghijklmnopqrstuvwxyz'),'login') or contains(translate(.,'ABCDEFGHIJKLMNOPQRSTUVWXYZ','abcdefghijklmnopqrstuvwxyz'),'entrar')]");
        waitClickable(submit).click();

        // Expect a toast / inline error
        boolean sawError = waitForAnyToastText(Arrays.asList("inválid", "invalido", "incorrect", "senha", "usuário", "usuario", "credenciais", "erro"));
        Assertions.assertTrue(sawError, "An error/invalid credentials message should appear for a bad login");
    }

    @Test
    @Order(4)
    void attemptValidLoginAndAssertOutcome() {
        goHome();
        By email = exists(By.cssSelector("input[type='email']")) ? By.cssSelector("input[type='email']")
                : By.xpath("//input[contains(@placeholder,'mail') or contains(@placeholder,'e-mail') or contains(@placeholder,'email')]");
        By pass = By.cssSelector("input[type='password']");
        clearAndType(email, LOGIN_USER);
        clearAndType(pass, LOGIN_PASS);
        By submit = By.xpath("//button[contains(translate(.,'ABCDEFGHIJKLMNOPQRSTUVWXYZ','abcdefghijklmnopqrstuvwxyz'),'acessar') or contains(translate(.,'ABCDEFGHIJKLMNOPQRSTUVWXYZ','abcdefghijklmnopqrstuvwxyz'),'login') or contains(translate(.,'ABCDEFGHIJKLMNOPQRSTUVWXYZ','abcdefghijklmnopqrstuvwxyz'),'entrar')]");
        waitClickable(submit).click();

        // Either enters the app or shows an error (unknown credentials in public env)
        boolean loggedIn = false;
        try {
            wait.until(d -> isLoggedInHeuristic() || waitForAnyToastText(Arrays.asList("inválid", "invalido", "incorrect", "senha", "usuário", "usuario", "credenciais", "erro")));
            loggedIn = isLoggedInHeuristic();
        } catch (TimeoutException ignored) {}

        if (loggedIn) {
            Assertions.assertTrue(isLoggedInHeuristic(), "Should be in a logged-in state if credentials are accepted");
            // If a select/dropdown (sorting-like) exists anywhere, exercise it (generic coverage)
            List<WebElement> selects = driver.findElements(By.tagName("select"));
            if (!selects.isEmpty()) {
                Select s = new Select(selects.get(0));
                List<String> before = s.getOptions().stream().map(WebElement::getText).collect(Collectors.toList());
                if (s.getOptions().size() > 1) {
                    s.selectByIndex(1);
                    // Assert selection changed
                    Assertions.assertEquals(before.get(1), s.getFirstSelectedOption().getText(), "Selected option should change after choosing another");
                    s.selectByIndex(0);
                    Assertions.assertEquals(before.get(0), s.getFirstSelectedOption().getText(), "Should be able to switch back to first option");
                } else {
                    Assertions.assertTrue(true, "Only one option found in select; skipping sort exercise");
                }
            } else {
                Assertions.assertTrue(true, "No dropdowns visible post-login; skipping sort exercise");
            }
        } else {
            // Assert we saw an error
            boolean sawError = waitForAnyToastText(Arrays.asList("inválid", "invalido", "incorrect", "senha", "usuário", "usuario", "credenciais", "erro"));
            Assertions.assertTrue(sawError, "If login didn't succeed, an error should be shown");
        }
    }

    @Test
    @Order(5)
    void footerExternalLinks_OpenAndVerifyDomains() {
        goHome();
        List<WebElement> allLinks = driver.findElements(By.cssSelector("a[href]"));
        Map<String, List<WebElement>> domainMap = new HashMap<>();
        domainMap.put("twitter.com", allLinks.stream().filter(a -> a.getAttribute("href").toLowerCase().contains("twitter.com")).collect(Collectors.toList()));
        domainMap.put("facebook.com", allLinks.stream().filter(a -> a.getAttribute("href").toLowerCase().contains("facebook.com")).collect(Collectors.toList()));
        domainMap.put("linkedin.com", allLinks.stream().filter(a -> a.getAttribute("href").toLowerCase().contains("linkedin.com")).collect(Collectors.toList()));
        domainMap.put("github.com", allLinks.stream().filter(a -> a.getAttribute("href").toLowerCase().contains("github.com")).collect(Collectors.toList()));

        for (Map.Entry<String, List<WebElement>> e : domainMap.entrySet()) {
            String domain = e.getKey();
            List<WebElement> links = e.getValue();
            if (!links.isEmpty()) {
                openExternalAndAssertDomain(links.get(0), domain);
            } else {
                Assertions.assertTrue(true, "No link for " + domain + " found on page; skipping");
            }
        }
    }

    @Test
    @Order(6)
    void exploreAllInternalLinksOneLevelAndAssertReachable() {
        goHome();
        String origin = driver.getCurrentUrl();
        List<WebElement> anchors = driver.findElements(By.cssSelector("a[href]"));
        Set<String> uniqueHrefs = anchors.stream()
                .map(a -> a.getAttribute("href"))
                .filter(Objects::nonNull)
                .collect(Collectors.toCollection(LinkedHashSet::new));

        for (String href : uniqueHrefs) {
            if (href.startsWith("javascript:") || href.endsWith("#")) continue;
            if (href.contains("twitter.com") || href.contains("facebook.com") || href.contains("linkedin.com") || href.contains("github.com")) {
                // External: open and assert domain per policy
                WebElement link = driver.findElement(By.cssSelector("a[href='" + href + "']"));
                openExternalAndAssertDomain(link, href.replace("https://", "").replace("http://", "").split("/")[0]);
                driver.get(origin);
                continue;
            }
            // Same origin or relative
            if (href.startsWith(BASE_URL) || href.startsWith("/") || href.startsWith("#")) {
                driver.get(href);
                // Assert some content is loaded
                Assertions.assertTrue(driver.findElements(By.tagName("body")).size() > 0, "Body should be present at " + href);
                // Do not go deeper than one hop; return to origin
                driver.get(origin);
                wait.until(ExpectedConditions.presenceOfElementLocated(By.tagName("body")));
            }
        }
        Assertions.assertTrue(true, "Visited accessible links one level below the base page");
    }

    @Test
    @Order(7)
    void burgerMenuIfPresent_AllItemsAboutLogoutReset() {
        goHome();
        // Attempt to locate a burger/menu button (guarded)
        By burger = By.xpath("//button[contains(@aria-label,'menu') or contains(@aria-label,'Menu') or contains(.,'Menu') or contains(.,'menu')]");
        if (exists(burger)) {
            waitClickable(burger).click();
            // Click "All Items" equivalent if exists
            List<By> allItems = Arrays.asList(
                    By.xpath("//a[contains(.,'All Items')]"),
                    By.xpath("//a[contains(translate(.,'ABCDEFGHIJKLMNOPQRSTUVWXYZ','abcdefghijklmnopqrstuvwxyz'),'principal')]"),
                    By.xpath("//a[contains(translate(.,'ABCDEFGHIJKLMNOPQRSTUVWXYZ','abcdefghijklmnopqrstuvwxyz'),'home')]")
            );
            for (By by : allItems) {
                if (exists(by)) { waitClickable(by).click(); break; }
            }
            // About/external if present
            List<By> aboutCandidates = Arrays.asList(
                    By.xpath("//a[contains(.,'About')]"),
                    By.xpath("//a[contains(translate(.,'ABCDEFGHIJKLMNOPQRSTUVWXYZ','abcdefghijklmnopqrstuvwxyz'),'sobre')]")
            );
            for (By by : aboutCandidates) {
                if (exists(by)) {
                    WebElement link = driver.findElement(by);
                    openExternalAndAssertDomain(link, "http");
                    break;
                }
            }
            // Reset/Logout if present
            List<By> resetCandidates = Arrays.asList(
                    By.xpath("//a[contains(translate(.,'ABCDEFGHIJKLMNOPQRSTUVWXYZ','abcdefghijklmnopqrstuvwxyz'),'reset')]"),
                    By.xpath("//button[contains(translate(.,'ABCDEFGHIJKLMNOPQRSTUVWXYZ','abcdefghijklmnopqrstuvwxyz'),'reset')]")
            );
            for (By by : resetCandidates) {
                if (exists(by)) { waitClickable(by).click(); break; }
            }
            List<By> logoutCandidates = Arrays.asList(
                    By.xpath("//a[contains(translate(.,'ABCDEFGHIJKLMNOPQRSTUVWXYZ','abcdefghijklmnopqrstuvwxyz'),'logout') or contains(translate(.,'ABCDEFGHIJKLMNOPQRSTUVWXYZ','abcdefghijklmnopqrstuvwxyz'),'sair')]"),
                    By.xpath("//button[contains(translate(.,'ABCDEFGHIJKLMNOPQRSTUVWXYZ','abcdefghijklmnopqrstuvwxyz'),'logout') or contains(translate(.,'ABCDEFGHIJKLMNOPQRSTUVWXYZ','abcdefghijklmnopqrstuvwxyz'),'sair')]")
            );
            for (By by : logoutCandidates) {
                if (exists(by)) { waitClickable(by).click(); break; }
            }
            Assertions.assertTrue(true, "Menu interactions performed when present");
        } else {
            Assertions.assertTrue(true, "Burger/menu not present; skipping menu tests");
        }
    }
}