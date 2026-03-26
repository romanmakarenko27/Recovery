package com.schoolday.qa.pages;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;

public class DistrictLoginPage {

    private final WebDriver driver;
    private final WebDriverWait wait;

    // Locators — cookie consent
    private final By acceptCookiesButton = By.id("accept-cook1e-btn");

    // Locators — organization search step
    private final By organizationInput = By.id("mat-input-0");
    private final By organizationDropdownItem = By.cssSelector("mat-option");

    // Locators — credentials step (after selecting organization)
    private final By usernameInput = By.id("username-input");
    private final By passwordInput = By.id("password-input");
    private final By signInButton = By.id("log-in-button");

    public DistrictLoginPage(WebDriver driver) {
        this.driver = driver;
        this.wait = new WebDriverWait(driver, Duration.ofSeconds(15));
    }

    public DistrictLoginPage open(String url) {
        driver.get(url);
        acceptCookiesIfPresent();
        wait.until(ExpectedConditions.visibilityOfElementLocated(organizationInput));
        return this;
    }

    private void acceptCookiesIfPresent() {
        try {
            WebElement button = new WebDriverWait(driver, Duration.ofSeconds(5))
                    .until(ExpectedConditions.elementToBeClickable(acceptCookiesButton));
            button.click();
        } catch (Exception ignored) {
            // No cookie banner — continue
        }
    }

    public DistrictLoginPage enterOrganization(String orgName) {
        WebElement input = wait.until(ExpectedConditions.elementToBeClickable(organizationInput));
        input.clear();
        input.sendKeys(orgName);
        return this;
    }

    public DistrictLoginPage selectOrganizationFromDropdown() {
        WebElement option = wait.until(ExpectedConditions.elementToBeClickable(organizationDropdownItem));
        option.click();
        // Wait for page to transition to credentials step
        wait.until(ExpectedConditions.visibilityOfElementLocated(usernameInput));
        return this;
    }

    public DistrictLoginPage enterUsername(String username) {
        WebElement input = wait.until(ExpectedConditions.visibilityOfElementLocated(usernameInput));
        input.clear();
        input.sendKeys(username);
        return this;
    }

    public DistrictLoginPage enterPassword(String password) {
        WebElement input = wait.until(ExpectedConditions.visibilityOfElementLocated(passwordInput));
        input.clear();
        input.sendKeys(password);
        return this;
    }

    public void clickSignIn() {
        wait.until(ExpectedConditions.elementToBeClickable(signInButton)).click();
    }

    public void login(String orgName, String email, String password) {
        enterOrganization(orgName);
        selectOrganizationFromDropdown();
        enterUsername(email);
        enterPassword(password);
        clickSignIn();
    }
}
