package GPT4.ws03.seq04;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation;
import org.openqa.selenium.*;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;
import java.util.List;
import java.util.Set;

@TestMethodOrder(OrderAnnotation.class)
public class bugbank {
    private static WebDriver driver;
    private static WebDriverWait wait;
    private static final String BASE_URL = "https://bugbank.netlify.app/";
    private static final String LOGIN = "caio@gmail.com";
    private static final String PASSWORD = "123";

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

    private void openLoginModal() {
        driver.get(BASE_URL);
        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        List<WebElement> loginButtons = driver.findElements(By.xpath("//button[contains(text(),'Entre')]"));
        if (!loginButtons.isEmpty()) {
            wait.until(ExpectedConditions.elementToBeClickable(loginButtons.get(0))).click();
        } else {
            WebElement loginButton = wait.until(ExpectedConditions.elementToBeClickable(By.id("btnLogin")));
            loginButton.click();
        }
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector("form")));
    }

    private void performLogin(String email, String password) {
        openLoginModal();
        WebElement emailField = driver.findElement(By.name("email"));
        WebElement passwordField = driver.findElement(By.name("password"));
        WebElement accessBtn = driver.findElement(By.cssSelector("button[type='submit']"));
        emailField.clear();
        emailField.sendKeys(email);
        passwordField.clear();
        passwordField.sendKeys(password);
        accessBtn.click();
    }

    private void logoutIfLoggedIn() {
        List<WebElement> logoutBtn = driver.findElements(By.id("btnExit"));
        if (!logoutBtn.isEmpty()) {
            logoutBtn.get(0).click();
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    @Test
    @Order(1)
    public void testValidLogin() {
        performLogin(LOGIN, PASSWORD);
        WebElement welcome = wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("textName")));
        Assertions.assertTrue(welcome.getText().contains("Bem vindo"), "Login failed or welcome message not displayed");
        logoutIfLoggedIn();
    }

    @Test
    @Order(2)
    public void testInvalidLogin() {
        performLogin("invalid@email.com", "wrongpass");
        WebElement errorModal = wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("modalText")));
        Assertions.assertTrue(errorModal.getText().toLowerCase().contains("dados"), "Expected error message not displayed");
        WebElement closeBtn = driver.findElement(By.id("btnCloseModal"));
        closeBtn.click();
    }

    @Test
    @Order(3)
    public void testAboutExternalLink() {
        driver.get(BASE_URL);
        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        List<WebElement> aboutLinks = driver.findElements(By.cssSelector("a[href*='linkedin']"));
        if (aboutLinks.isEmpty()) {
            aboutLinks = driver.findElements(By.xpath("//a[contains(@href,'linkedin')]"));
        }
        Assertions.assertFalse(aboutLinks.isEmpty(), "No LinkedIn link found");
        WebElement aboutLink = aboutLinks.get(0);
        String originalWindow = driver.getWindowHandle();
        wait.until(ExpectedConditions.elementToBeClickable(aboutLink));
        ((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView(true);", aboutLink);
        aboutLink.click();

        wait.until(driver -> driver.getWindowHandles().size() > 1);
        Set<String> handles = driver.getWindowHandles();
        for (String handle : handles) {
            if (!handle.equals(originalWindow)) {
                driver.switchTo().window(handle);
                wait.until(ExpectedConditions.urlContains("linkedin"));
                Assertions.assertTrue(driver.getCurrentUrl().contains("linkedin"), "Did not navigate to LinkedIn page");
                driver.close();
                driver.switchTo().window(originalWindow);
                break;
            }
        }
    }

    @Test
    @Order(4)
    public void testRegisterAccountButton() {
        driver.get(BASE_URL);
        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        List<WebElement> registerButtons = driver.findElements(By.id("btnRegister"));
        if (registerButtons.isEmpty()) {
            registerButtons = driver.findElements(By.xpath("//button[contains(text(),'Registrar')]"));
        }
        Assertions.assertFalse(registerButtons.isEmpty(), "No register button found");
        WebElement registerBtn = registerButtons.get(0);
        wait.until(ExpectedConditions.elementToBeClickable(registerBtn));
        registerBtn.click();
        WebElement registerTitle = wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector("h1")));
        Assertions.assertTrue(registerTitle.getText().contains("Registro"), "Register page not displayed");
    }

    @Test
    @Order(5)
    public void testFooterLinksPresence() {
        driver.get(BASE_URL);
        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        List<WebElement> links = driver.findElements(By.cssSelector("footer a"));
        if (links.isEmpty()) {
            links = driver.findElements(By.xpath("//footer//a"));
        }
        Assertions.assertFalse(links.isEmpty(), "No footer social links found");

        for (WebElement link : links) {
            String href = link.getAttribute("href");
            if (href != null) {
                String domain = href.contains("twitter") ? "twitter" :
                                href.contains("facebook") ? "facebook" :
                                href.contains("linkedin") ? "linkedin" : "";
                if (!domain.isEmpty()) {
                    String originalWindow = driver.getWindowHandle();
                    wait.until(ExpectedConditions.elementToBeClickable(link));
                    ((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView(true);", link);
                    link.click();
                    wait.until(driver -> driver.getWindowHandles().size() > 1);
                    Set<String> handles = driver.getWindowHandles();
                    for (String handle : handles) {
                        if (!handle.equals(originalWindow)) {
                            driver.switchTo().window(handle);
                            wait.until(ExpectedConditions.urlContains(domain));
                            Assertions.assertTrue(driver.getCurrentUrl().contains(domain), "External link did not open " + domain);
                            driver.close();
                            driver.switchTo().window(originalWindow);
                            break;
                        }
                    }
                }
            }
        }
    }

    @Test
    @Order(6)
    public void testTransferButtonVisibilityAfterLogin() {
        performLogin(LOGIN, PASSWORD);
        WebElement transferBtn = wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("btnTransfer")));
        Assertions.assertTrue(transferBtn.isDisplayed(), "Transfer button not visible after login");
        logoutIfLoggedIn();
    }

    @Test
    @Order(7)
    public void testResetAppState() {
        performLogin(LOGIN, PASSWORD);
        WebElement resetBtn = wait.until(ExpectedConditions.elementToBeClickable(By.id("btnReset")));
        resetBtn.click();
        wait.until(ExpectedConditions.alertIsPresent());
        driver.switchTo().alert().accept();
        WebElement welcome = wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("textName")));
        Assertions.assertTrue(welcome.isDisplayed(), "Reset App State failed");
        logoutIfLoggedIn();
    }
}