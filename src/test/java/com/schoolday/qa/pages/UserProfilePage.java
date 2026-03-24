package com.schoolday.qa.pages;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;

public class UserProfilePage {

    private final WebDriver driver;
    private final WebDriverWait wait;

    // Locators — toolbar / user menu
    private final By userMenuButton = By.cssSelector("[data-testid='standard-page-user-menu-trigger-btn']");
    private final By userName = By.cssSelector("[data-testid='standard-page-user-name']");
    private final By myProfileMenuItem = By.xpath("//*[normalize-space()='My Profile']");

    // Locators — profile page
    private final By tabGroup = By.cssSelector("[data-testid='user-profile-tab-group-tabs']");
    private final By profileTab = By.cssSelector("[data-testid='user-profile-tab-profile']");
    private final By securitySettingsTab = By.cssSelector("[data-testid='user-profile-tab-security-settings']");
    private final By emailNotificationsTab = By.cssSelector("[data-testid='user-profile-tab-email-notifications']");
    private final By editOrganizationTab = By.cssSelector("[data-testid='user-profile-tab-edit-organization']");

    // Locators — security settings content (lazy-loaded when tab is clicked)
    private final By securitySettingsComponent = By.cssSelector("ngc-user-security-settings");

    public UserProfilePage(WebDriver driver) {
        this.driver = driver;
        this.wait = new WebDriverWait(driver, Duration.ofSeconds(15));
    }

    public UserProfilePage navigateTo(String baseUrl) {
        driver.get(baseUrl + "/user/profile");
        wait.until(ExpectedConditions.urlContains("/user/profile"));
        waitForPage();
        return this;
    }

    public UserProfilePage waitForPage() {
        wait.until(ExpectedConditions.visibilityOfElementLocated(tabGroup));
        return this;
    }

    public boolean isTabGroupDisplayed() {
        try {
            return driver.findElement(tabGroup).isDisplayed();
        } catch (Exception e) {
            return false;
        }
    }

    public boolean isProfileTabDisplayed() {
        try {
            return driver.findElement(profileTab).isDisplayed();
        } catch (Exception e) {
            return false;
        }
    }

    public boolean isSecuritySettingsTabDisplayed() {
        try {
            return driver.findElement(securitySettingsTab).isDisplayed();
        } catch (Exception e) {
            return false;
        }
    }

    public boolean isEmailNotificationsTabDisplayed() {
        try {
            return driver.findElement(emailNotificationsTab).isDisplayed();
        } catch (Exception e) {
            return false;
        }
    }

    public boolean isEditOrganizationTabDisplayed() {
        try {
            return driver.findElement(editOrganizationTab).isDisplayed();
        } catch (Exception e) {
            return false;
        }
    }

    public String getUserName() {
        return wait.until(ExpectedConditions.visibilityOfElementLocated(userName)).getText();
    }

    public void clickSecuritySettingsTab() {
        WebElement tab = wait.until(ExpectedConditions.elementToBeClickable(securitySettingsTab));
        tab.click();
        wait.until(ExpectedConditions.presenceOfElementLocated(securitySettingsComponent));
    }

    public boolean isSecuritySettingsContentLoaded() {
        try {
            // The Angular component may not report isDisplayed() due to tab animation/transforms,
            // but its presence in DOM with child content confirms the tab loaded.
            WebElement component = driver.findElement(securitySettingsComponent);
            return component != null && !component.getAttribute("innerHTML").isBlank();
        } catch (Exception e) {
            return false;
        }
    }

    public void clickUserMenu() {
        WebElement menu = wait.until(ExpectedConditions.elementToBeClickable(userMenuButton));
        menu.click();
    }

    public boolean isUserMenuButtonDisplayed() {
        try {
            return driver.findElement(userMenuButton).isDisplayed();
        } catch (Exception e) {
            return false;
        }
    }

    public void clickMyProfile() {
        WebElement item = wait.until(ExpectedConditions.elementToBeClickable(myProfileMenuItem));
        item.click();
        wait.until(ExpectedConditions.urlContains("/user/profile"));
        waitForPage();
    }
}
