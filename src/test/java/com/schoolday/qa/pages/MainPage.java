package com.schoolday.qa.pages;

import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;
import java.util.List;
import java.util.Set;

public class MainPage {

    private final WebDriver driver;
    private final WebDriverWait wait;

    private final By connectLinks = By.xpath("//a[normalize-space()='Connect']");

    public MainPage(WebDriver driver) {
        this.driver = driver;
        this.wait = new WebDriverWait(driver, Duration.ofSeconds(15));
    }

    public MainPage waitForPage() {
        wait.until(d -> d.getCurrentUrl().contains("/mainPage") || d.getCurrentUrl().contains("/#/mainPage"));
        return this;
    }

    public void clickConnect() {
        // Wait until at least one Connect link is visible (page may still be rendering)
        WebElement visibleLink = wait.until(d -> {
            List<WebElement> links = d.findElements(connectLinks);
            for (WebElement link : links) {
                if (link.isDisplayed()) {
                    return link;
                }
            }
            return null;
        });

        String originalWindow = driver.getWindowHandle();
        Set<String> windowsBefore = driver.getWindowHandles();

        // Use JavaScript click — the link has empty href and uses JS navigation
        ((JavascriptExecutor) driver).executeScript("arguments[0].click();", visibleLink);

        // Wait for either a new window/tab or URL change in current window
        wait.until(d -> {
            Set<String> currentWindows = d.getWindowHandles();
            if (currentWindows.size() > windowsBefore.size()) {
                return true;
            }
            return d.getCurrentUrl().contains("connect-qa.gg4l.com");
        });

        // If a new window opened, switch to it
        Set<String> windowsAfter = driver.getWindowHandles();
        if (windowsAfter.size() > windowsBefore.size()) {
            for (String handle : windowsAfter) {
                if (!handle.equals(originalWindow)) {
                    driver.switchTo().window(handle);
                    break;
                }
            }
            wait.until(d -> d.getCurrentUrl().contains("connect-qa.gg4l.com"));
        }
    }
}
