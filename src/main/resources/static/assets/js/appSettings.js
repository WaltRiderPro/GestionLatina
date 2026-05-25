// App settings default
const initialTheme =
    document.documentElement.getAttribute("data-bs-theme") ||
    document.cookie.match(/(?:^|; )theme=([^;]+)/)?.[1] ||
    "light";

// App settings default
let appSettings = {
    appTheme: initialTheme,
    appSidebar: "full",
    appColor: "blue",
};

// Update settings
function setAppSettings(newSettings = {}) {
    appSettings = {
        ...appSettings,
        ...newSettings,
    };
    applySettings();
}

// Apply settings to DOM
function applySettings() {
    document.documentElement.setAttribute(
        "data-bs-theme",
        appSettings.appTheme,
    );

    if (window.innerWidth >= 1480) {
        document.documentElement.setAttribute(
            "data-app-sidebar",
            appSettings.appSidebar,
        );
    }

    document.documentElement.setAttribute(
        "data-color-theme",
        appSettings.appColor,
    );
}

// Initialize
document.addEventListener("DOMContentLoaded", applySettings);
window.setAppSettings = setAppSettings;
