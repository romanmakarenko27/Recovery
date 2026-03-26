package com.schoolday.qa.util;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import io.github.bonigarcia.wdm.WebDriverManager;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.logging.LogEntry;
import org.openqa.selenium.logging.LogType;
import org.openqa.selenium.logging.LoggingPreferences;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.logging.Level;

public class PageInspector {

    private static final Path CODES_FILE = Path.of("district/SchoolDay_reset_codes.txt");

    public static void main(String[] args) throws Exception {
        Properties config = loadProperties();

        WebDriverManager.chromedriver().setup();

        ChromeOptions options = new ChromeOptions();
        options.addArguments("--start-maximized");

        LoggingPreferences logPrefs = new LoggingPreferences();
        logPrefs.enable(LogType.PERFORMANCE, Level.ALL);
        options.setCapability("goog:loggingPrefs", logPrefs);

        WebDriver driver = new ChromeDriver(options);
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(20));

        try {
            String districtUrl = config.getProperty("district.base.url") + config.getProperty("district.login.path");
            String email = config.getProperty("district.test.email");
            String password = config.getProperty("district.test.password");

            // === LOGIN ===
            System.out.println("Navigating to: " + districtUrl);
            driver.get(districtUrl);
            Thread.sleep(2000);
            driver.findElement(By.id("accept-cook1e-btn")).click();

            WebElement orgInput = wait.until(ExpectedConditions.elementToBeClickable(By.id("mat-input-0")));
            orgInput.sendKeys(email);
            Thread.sleep(2000);
            wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector("mat-option"))).click();
            wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("username-input")));

            driver.findElement(By.id("username-input")).sendKeys(email);
            driver.findElement(By.id("password-input")).sendKeys(password);
            driver.findElement(By.id("log-in-button")).click();
            System.out.println("Signed in");

            // Wait for MFA page
            wait.until(ExpectedConditions.visibilityOfElementLocated(
                    By.cssSelector("[data-testid='sms-mfa-code-input']")));
            System.out.println("MFA page loaded. URL: " + driver.getCurrentUrl());

            // === NAVIGATE TO RECOVERY CODE PAGE ===
            wait.until(ExpectedConditions.elementToBeClickable(
                    By.cssSelector("[data-testid='sms-mfa-recovery-btn']"))).click();
            wait.until(ExpectedConditions.visibilityOfElementLocated(
                    By.cssSelector("[data-testid='recovery-code-code-input']")));
            System.out.println("Recovery code page loaded. URL: " + driver.getCurrentUrl());

            // === Submit valid recovery code (first attempt, no lockout) ===
            System.out.println("\n=== SUBMITTING VALID RECOVERY CODE ===");
            String code = consumeRecoveryCode();
            System.out.println("Using code: " + code);
            WebElement codeInput = driver.findElement(
                    By.cssSelector("[data-testid='recovery-code-code-input']"));
            codeInput.clear();
            codeInput.sendKeys(code);
            driver.findElement(By.cssSelector("[data-testid='recovery-code-verify-btn']")).click();
            Thread.sleep(8000);
            System.out.println("URL after valid code: " + driver.getCurrentUrl());

            // === Inspect ETX main page ===
            System.out.println("\n=== ETX MAIN PAGE ===");
            dumpPage(driver, "etx-main-page");

            System.out.println("\nALL VISIBLE BUTTONS/LINKS:");
            for (WebElement el : driver.findElements(By.cssSelector("button, a, [role='button'], [role='link'], [role='menuitem']"))) {
                String text = el.getText().trim();
                if (!text.isBlank() && text.length() < 100) {
                    System.out.println("  tag=" + el.getTagName()
                            + " | text='" + text.replace("\n", " ") + "'"
                            + " | href=" + el.getAttribute("href")
                            + " | class=" + el.getAttribute("class")
                            + " | data-testid=" + el.getAttribute("data-testid")
                            + " | displayed=" + el.isDisplayed());
                }
            }

            System.out.println("\nALL NAV/MENU ITEMS:");
            for (WebElement el : driver.findElements(By.cssSelector("nav a, .nav-item, .menu-item, mat-list-item, [routerlink]"))) {
                String text = el.getText().trim();
                System.out.println("  tag=" + el.getTagName()
                        + " | text='" + text.replace("\n", " ") + "'"
                        + " | href=" + el.getAttribute("href")
                        + " | routerlink=" + el.getAttribute("routerlink")
                        + " | displayed=" + el.isDisplayed());
            }

            System.out.println("\nELEMENTS CONTAINING 'connect' (case-insensitive):");
            for (WebElement el : driver.findElements(By.xpath("//*[contains(translate(text(),'CONNECT','connect'),'connect')]"))) {
                String text = el.getText().trim();
                if (text.length() < 100) {
                    System.out.println("  tag=" + el.getTagName()
                            + " | text='" + text.replace("\n", " ") + "'"
                            + " | href=" + el.getAttribute("href")
                            + " | class=" + el.getAttribute("class")
                            + " | displayed=" + el.isDisplayed());
                }
            }

        } finally {
            driver.quit();
        }
    }

    private static void dumpPage(WebDriver driver, String label) {
        System.out.println("\n--- " + label + " ---");

        System.out.println("HEADINGS:");
        for (WebElement h : driver.findElements(By.cssSelector("h1, h2, h3, h4, div.title"))) {
            String text = h.getText().trim();
            if (!text.isBlank() && text.length() < 200) {
                System.out.println("  tag=" + h.getTagName()
                        + " | class=" + h.getAttribute("class")
                        + " | text='" + text.replace("\n", " ") + "'");
            }
        }

        System.out.println("DATA-TESTID ELEMENTS:");
        for (WebElement el : driver.findElements(By.cssSelector("[data-testid]"))) {
            String text = el.getText();
            System.out.println("  tag=" + el.getTagName()
                    + " | data-testid=" + el.getAttribute("data-testid")
                    + " | text='" + text.substring(0, Math.min(text.length(), 80)).replace("\n", " ") + "'"
                    + " | displayed=" + el.isDisplayed());
        }

        System.out.println("SNACKBARS:");
        for (WebElement el : driver.findElements(By.cssSelector(
                "simple-snack-bar, .mat-mdc-snack-bar-container, .mat-snack-bar-container, snack-bar-container, [role='alert']"))) {
            System.out.println("  tag=" + el.getTagName()
                    + " | class=" + el.getAttribute("class")
                    + " | text='" + el.getText().replace("\n", " ") + "'"
                    + " | displayed=" + el.isDisplayed());
        }

        System.out.println("VALIDATION ERRORS:");
        for (WebElement el : driver.findElements(By.cssSelector(
                ".mat-form-field-invalid, .mat-error, .mat-mdc-form-field-error, mat-error, .error-message, .field-error"))) {
            System.out.println("  tag=" + el.getTagName()
                    + " | class=" + el.getAttribute("class")
                    + " | text='" + el.getText().replace("\n", " ") + "'"
                    + " | displayed=" + el.isDisplayed());
        }
    }

    private static void dumpPerformanceLogs(WebDriver driver, String label) {
        System.out.println("\nPERFORMANCE LOGS (" + label + ") — looking for HTTP status codes:");
        List<LogEntry> logs = driver.manage().logs().get(LogType.PERFORMANCE).getAll();
        for (LogEntry entry : logs) {
            try {
                JsonObject json = JsonParser.parseString(entry.getMessage()).getAsJsonObject();
                JsonObject message = json.getAsJsonObject("message");
                if (message == null) continue;
                String method = message.has("method") ? message.get("method").getAsString() : "";
                if (!"Network.responseReceived".equals(method)) continue;
                JsonObject params = message.getAsJsonObject("params");
                if (params == null) continue;
                JsonObject response = params.getAsJsonObject("response");
                if (response == null) continue;
                JsonElement statusEl = response.get("status");
                String url = response.has("url") ? response.get("url").getAsString() : "";
                if (statusEl != null) {
                    int status = statusEl.getAsInt();
                    if (status >= 400 || url.contains("verify") || url.contains("recovery") || url.contains("mfa")) {
                        System.out.println("  status=" + status + " | url=" + url.substring(0, Math.min(url.length(), 120)));
                    }
                }
            } catch (Exception ignored) {}
        }
    }

    private static String consumeRecoveryCode() throws Exception {
        List<String> lines = new ArrayList<>(Files.readAllLines(CODES_FILE));

        String code = null;
        int codeIndex = -1;
        for (int i = 0; i < lines.size(); i++) {
            String line = lines.get(i).trim();
            if (!line.isBlank() && !line.startsWith("USED:") && !line.startsWith("INVALIDATED:")) {
                code = line;
                codeIndex = i;
                break;
            }
        }

        if (code == null) {
            throw new IllegalStateException("No recovery codes remaining in " + CODES_FILE);
        }

        lines.set(codeIndex, "USED:" + code);
        Files.write(CODES_FILE, lines);

        return code;
    }

    private static Properties loadProperties() throws Exception {
        Properties props = new Properties();
        try (InputStream is = PageInspector.class.getClassLoader().getResourceAsStream("test.properties")) {
            if (is == null) {
                throw new RuntimeException("test.properties not found on classpath");
            }
            props.load(is);
        }
        return props;
    }
}
