import logo from "./logo.svg";
import "./App.css";
import React, { useEffect, useCallback, useRef } from "react";

function App() {
  // 用于标记是否是刷新操作
  const isRefreshing = useRef(false);

  const syncLogEvent = (eventType) => {
    const timestamp = new Date().toISOString();
    const logMessage = `${eventType} at ${timestamp}`;
    console.log("Logging event:", logMessage);

    try {
      const events = JSON.parse(localStorage.getItem("appEvents") || "[]");
      events.push(logMessage);
      localStorage.setItem("appEvents", JSON.stringify(events));
      return true;
    } catch (error) {
      console.error("Failed to log event:", error);
      return false;
    }
  };

  const logEvent = useCallback((eventType) => {
    syncLogEvent(eventType);
  }, []);

  const handleBeforeUnload = useCallback((event) => {
    console.log("BeforeUnload triggered");
    // 只在非刷新时记录关闭事件
    if (!isRefreshing.current) {
      syncLogEvent("APP_CLOSED");
    }
    event.preventDefault();
    event.returnValue = "";
    return "";
  }, []);

  const handleVisibilityChange = useCallback((event) => {
    console.log("Visibility changed:", document.visibilityState);

    if (document.visibilityState === "hidden") {
      syncLogEvent("APP_HIDDEN");
    } else if (document.visibilityState === "visible") {
      syncLogEvent("APP_VISIBLE");
    }
  }, []);

  useEffect(() => {
    console.log("Setting up event listeners...");

    const navigation = performance.getEntriesByType("navigation")[0];
    const navigationType = navigation ? navigation.type : "";
    const sessionId = sessionStorage.getItem("sessionId");
    const currentSessionId = Math.random().toString(36).substring(7);

    if (!sessionId) {
      sessionStorage.setItem("sessionId", currentSessionId);
      syncLogEvent("APP_OPENED");
    } else if (
      navigationType === "reload" ||
      document.referrer === window.location.href
    ) {
      // 标记为刷新操作
      isRefreshing.current = true;
      syncLogEvent("APP_REFRESHED");
    } else {
      // syncLogEvent("APP_OPENED");
    }

    window.addEventListener("beforeunload", handleBeforeUnload, {
      capture: true,
    });
    document.addEventListener("visibilitychange", handleVisibilityChange, {
      capture: true,
    });

    return () => {
      console.log("Cleaning up event listeners...");
      window.removeEventListener("beforeunload", handleBeforeUnload, {
        capture: true,
      });
      document.removeEventListener("visibilitychange", handleVisibilityChange, {
        capture: true,
      });
    };
  }, [handleBeforeUnload, handleVisibilityChange]);

  return (
    <div className="App">
      <header className="App-header">
        <img src={logo} className="App-logo" alt="logo" />
        <p>
          Edit <code>src/App.js</code> and save to reload.
        </p>
        <a
          className="App-link"
          href="https://reactjs.org"
          target="_blank"
          rel="noopener noreferrer"
        >
          Learn React
        </a>
      </header>
    </div>
  );
}

export default App;
