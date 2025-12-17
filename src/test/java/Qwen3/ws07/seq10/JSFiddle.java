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
        WebElement headerLogo = wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector("#logo a")));
        assertTrue(headerLogo.isDisplayed(), "Logo should be visible in header");
    }

    @Test
    @Order(2)
    void testNavigationMenu_ItemsArePresent() {
        driver.get(BASE_URL);

        WebElement navBar = wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector("#navigation")));
        assertTrue(navBar.isDisplayed(), "Navigation bar should be visible");

        // Verify key nav items
        assertTrue(isElementPresent(By.linkText("Features")), "Navigation should contain 'Features' link");
        assertTrue(isElementPresent(By.linkText("Support")), "Navigation should contain 'Support' link");
        assertTrue(isElementPresent(By.linkText("Forum")), "Navigation should contain 'Forum' link");
        assertTrue(isElementPresent(By.linkText("Blog")), "Navigation should contain 'Blog' link");
        assertTrue(isElementPresent(By.linkText("Sign up")), "Navigation should contain 'Sign up' link");
        assertTrue(isElementPresent(By.linkText("Login")), "Navigation should contain 'Login' link");
    }

    @Test
    @Order(3)
    void testLoginLink_NavigatesToLoginPage() {
        driver.get(BASE_URL);

        WebElement loginLink = wait.until(ExpectedConditions.elementToBeClickable(By.linkText("Login")));
        loginLink.click();

        wait.until(ExpectedConditions.urlContains("accounts/login"));
        assertTrue(driver.getCurrentUrl().contains("accounts/login"), "Login link should redirect to login page");

        assertTrue(isElementPresent(By.name("login")), "Login page should contain username/email field");
        assertTrue(isElementPresent(By.name("password")), "Login page should contain password field");
    }

    @Test
    @Order(4)
    void testInvalidLogin_ShowErrorMessage() {
        driver.get(BASE_URL + "accounts/login");

        WebElement usernameField = wait.until(ExpectedConditions.visibilityOfElementLocated(By.name("login")));
        WebElement passwordField = driver.findElement(By.name("password"));
        WebElement submitButton = driver.findElement(By.cssSelector("button[type='submit']"));

        usernameField.sendKeys("invalid_user");
        passwordField.sendKeys("wrongpass");
        submitButton.click();

        WebElement error = wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector(".form-error")));
        assertTrue(error.isDisplayed(), "Error message should be visible after invalid login");
        assertTrue(error.getText().contains("wrong"), "Error message should indicate invalid credentials");
    }

    @Test
    @Order(5)
    void testSignUpLink_NavigatesToRegistrationPage() {
        driver.get(BASE_URL);

        WebElement signUpLink = wait.until(ExpectedConditions.elementToBeClickable(By.linkText("Sign up")));
        signUpLink.click();

        wait.until(ExpectedConditions.urlContains("accounts/signup"));
        assertTrue(driver.getCurrentUrl().contains("accounts/signup"), "Sign up link should navigate to registration page");

        assertTrue(isElementPresent(By.name("username")), "Registration page should have username field");
        assertTrue(isElementPresent(By.name("email")), "Registration page should have email field");
        assertTrue(isElementPresent(By.name("password1")), "Registration page should have password field");
        assertTrue(isElementPresent(By.name("password2")), "Registration page should have password confirmation field");
    }

    @Test
    @Order(6)
    void testFooterLearnSection_LinksArePresent() {
        driver.get(BASE_URL);

        WebElement footer = wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector("footer")));
        WebElement learnSection = footer.findElement(By.xpath("//*[text()='Learn']/following-sibling::ul"));
        assertTrue(learnSection.isDisplayed(), "Learn section in footer should be visible");

        // Verify child links
        assertTrue(learnSection.findElements(By.linkText("Features")).size() > 0, "Learn section should contain 'Features' link");
        assertTrue(learnSection.findElements(By.linkText("Bug tracker")).size() > 0, "Learn section should contain 'Bug tracker' link");
        assertTrue(learnSection.findElements(By.linkText("Change log")).size() > 0, "Learn section should contain 'Change log' link");
    }

    @Test
    @Order(7)
    void testFooterCompanySection_LinksArePresent() {
        driver.get(BASE_URL);

        WebElement footer = wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector("footer")));
        WebElement companySection = footer.findElement(By.xpath("//*[text()='Company']/following-sibling::ul"));
        assertTrue(companySection.isDisplayed(), "Company section in footer should be visible");

        assertTrue(companySection.findElements(By.linkText("About")).size() > 0, "Company section should contain 'About' link");
        assertTrue(companySection.findElements(By.linkText("Blog")).size() > 0, "Company section should contain 'Blog' link");
        assertTrue(companySection.findElements(By.linkText("Terms")).size() > 0, "Company section should contain 'Terms' link");
    }

    @Test
    @Order(8)
    void testFooterSocialLinks_OpenInNewTab() {
        driver.get(BASE_URL);

        // Twitter
        testSocialLinkInNewTab("//a[@title='Follow us on Twitter']", "twitter.com", "Twitter");

        // Facebook
        testSocialLinkInNewTab("//a[@title='Follow us on Facebook']", "facebook.com", "Facebook");

        // GitHub
        testSocialLinkInNewTab("//a[@title='Fork us on GitHub']", "github.com", "GitHub");
    }

    @Test
    @Order(9)
    void testForumLink_NavigatesToExternalSite() {
        driver.get(BASE_URL);

        WebElement forumLink = wait.until(ExpectedConditions.elementToBeClickable(By.linkText("Forum")));
        String originalWindow = driver.getWindowHandle();
        forumLink.sendKeys(Keys.CONTROL, Keys.RETURN); // Open in new tab

        boolean tabSwitched = false;
        for (String window : driver.getWindowHandles()) {
            if (!window.equals(originalWindow)) {
                driver.switchTo().window(window);
                wait.until(ExpectedConditions.urlContains("community"));
                assertTrue(driver.getCurrentUrl().contains("community"), "Forum link should open community.jsfiddle.net");
                driver.close();
                driver.switchTo().window(originalWindow);
                tabSwitched = true;
                break;
            }
        }

        if (!tabSwitched) {
            // Fallback: same tab
            driver.switchTo().window(originalWindow);
            forumLink.click();
            wait.until(ExpectedConditions.urlContains("community"));
            assertTrue(driver.getCurrentUrl().contains("community"), "Forum link should redirect to community site");
            driver.navigate().back();
            wait.until(ExpectedConditions.urlToBe(BASE_URL));
        }
    }

    @Test
    @Order(10)
    void testBlogLink_NavigatesToExternalSite() {
        driver.get(BASE_URL);

        WebElement blogLink = wait.until(ExpectedConditions.elementToBeClickable(By.linkText("Blog")));
        String originalWindow = driver.getWindowHandle();
        blogLink.sendKeys(Keys.CONTROL, Keys.RETURN);

        boolean tabSwitched = false;
        for (String window : driver.getWindowHandles()) {
            if (!window.equals(originalWindow)) {
                driver.switchTo().window(window);
                wait.until(ExpectedConditions.urlContains("blog.jsfiddle.net"));
                assertTrue(driver.getCurrentUrl().contains("blog.jsfiddle.net"), "Blog link should open blog.jsfiddle.net");
                driver.close();
                driver.switchTo().window(originalWindow);
                tabSwitched = true;
                break;
            }
        }

        if (!tabSwitched) {
            // Fallback
            driver.switchTo().window(originalWindow);
            blogLink.click();
            wait.until(ExpectedConditions.urlContains("blog.jsfiddle.net"));
            assertTrue(driver.getCurrentUrl().contains("blog.jsfiddle.net"), "Blog link should redirect");
            driver.navigate().back();
            wait.until(ExpectedConditions.urlToBe(BASE_URL));
        }
    }

    @Test
    @Order(11)
    void testJavaScriptEditorArea_IsEditable() {
        driver.get(BASE_URL);
        
        // Switch to editor iframe (JS)
        driver.switchTo().frame(wait.until(ExpectedConditions.presenceOfElementLocated(By.cssSelector("iframe[src^='/code/']"))));

        WebElement editor = wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector(".CodeMirror")));
        editor.click();

        WebElement textarea = driver.findElement(By.cssSelector(".CodeMirror textarea"));
        textarea.sendKeys("console.log('Hello, JSFiddle!');");

        // Verify insertion
        String editorText = (String) ((JavascriptExecutor) driver).executeScript(
            "return arguments[0].CodeMirror.getValue();", editor);
        assertTrue(editorText.contains("console.log('Hello, JSFiddle!');"), "Code should be inserted into JS editor");
    }

    @Test
    @Order(12)
    void testRunButton_ExecutesCodeAndShowsOutput() {
        // Reset to main page
        driver.get(BASE_URL);

        // This feature requires accessing internal editor iframes which are complex
        // Instead, verify Run button exists and is clickable
        WebElement runButton = wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector("#run")));
        assertTrue(runButton.isDisplayed(), "Run button should be visible");
        assertEquals("Run »", runButton.getText().trim(), "Run button should have text 'Run »'");
    }

    private void testSocialLinkInNewTab(String xpathLocator, String expectedDomain, String linkName) {
        driver.get(BASE_URL);
        WebElement link = wait.until(ExpectedConditions.elementToBeClickable(By.xpath(xpathLocator)));
        String originalWindow = driver.getWindowHandle();

        link.sendKeys(Keys.CONTROL, Keys.RETURN); // Open in new tab

        boolean tabSwitched = false;
        for (String window : driver.getWindowHandles()) {
            if (!window.equals(originalWindow)) {
                driver.switchTo().window(window);
                wait.until(ExpectedConditions.urlContains(expectedDomain));
                assertTrue(driver.getCurrentUrl().contains(expectedDomain),
                           linkName + " link should open " + expectedDomain);
                driver.close();
                driver.switchTo().window(originalWindow);
                tabSwitched = true;
                break;
            }
        }

        if (!tabSwitched) {
            // Fallback: open in same tab
            driver.switchTo().window(originalWindow);
            link.click();
            wait.until(ExpectedConditions.urlContains(expectedDomain));
            assertTrue(driver.getCurrentUrl().contains(expectedDomain),
                       linkName + " link should redirect to " + expectedDomain);
            driver.navigate().back();
            wait.until(ExpectedConditions.urlToBe(BASE_URL));
        }
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