/*
  KISAN 365 - ESP8266 Wi-Fi / Firebase heartbeat firmware
  Phase 1 ONLY: proves Wi-Fi + Firebase connectivity.
  No voltage/current sensing and NO motor/relay control in this build.
  Board: NodeMCU ESP8266 (ESP-12E/ESP-12F)
  Firebase RTDB path: devices/K365-0001/telemetry/lastSeen

  Install Arduino libraries:
    - ESP8266 board package (NodeMCU 1.0)
    - Firebase Arduino Client Library for ESP8266 and ESP32 (Mobizt)
      Library Manager name: Firebase Arduino Client Library for ESP8266 and ESP32
*/

#include <Arduino.h>
#include <ESP8266WiFi.h>
#include <time.h>
#include <FirebaseESP8266.h>
#include <addons/TokenHelper.h>
#include <addons/RTDBHelper.h>

// Fill these locally. Do not share Wi-Fi/Firebase passwords publicly.
#define WIFI_SSID       "YOUR_WIFI_NAME"
#define WIFI_PASSWORD   "YOUR_WIFI_PASSWORD"
#define API_KEY         "YOUR_FIREBASE_WEB_API_KEY"
#define DATABASE_URL    "https://kisan365-commercial-default-rtdb.firebaseio.com/"
#define USER_EMAIL      "YOUR_DEVICE_FIREBASE_USER_EMAIL"
#define USER_PASSWORD   "YOUR_DEVICE_FIREBASE_USER_PASSWORD"
#define DEVICE_ID       "K365-0001"

FirebaseData fbdo;
FirebaseAuth auth;
FirebaseConfig config;

const unsigned long HEARTBEAT_INTERVAL_MS = 30000;
unsigned long lastHeartbeat = 0;

String telemetryPath() {
  return String("/devices/") + DEVICE_ID + "/telemetry";
}

void connectWiFi() {
  if (WiFi.status() == WL_CONNECTED) return;
  WiFi.mode(WIFI_STA);
  WiFi.begin(WIFI_SSID, WIFI_PASSWORD);
  Serial.print("Connecting Wi-Fi");
  unsigned long started = millis();
  while (WiFi.status() != WL_CONNECTED && millis() - started < 25000) {
    delay(400);
    Serial.print(".");
  }
  Serial.println();
  if (WiFi.status() == WL_CONNECTED) {
    Serial.print("Wi-Fi connected. IP: ");
    Serial.println(WiFi.localIP());
  } else {
    Serial.println("Wi-Fi not connected; retrying later.");
  }
}

void setup() {
  Serial.begin(115200);
  delay(200);
  Serial.println();
  Serial.println("KISAN 365 ESP8266 - connectivity-only firmware");

  connectWiFi();

  // NTP time is needed because the Android app checks lastSeen in milliseconds.
  configTime(0, 0, "pool.ntp.org", "time.google.com");
  Serial.print("Waiting for clock");
  time_t now = time(nullptr);
  unsigned long waitStart = millis();
  while (now < 1700000000 && millis() - waitStart < 20000) {
    delay(300);
    now = time(nullptr);
    Serial.print(".");
  }
  Serial.println();

  config.api_key = API_KEY;
  config.database_url = DATABASE_URL;
  auth.user.email = USER_EMAIL;
  auth.user.password = USER_PASSWORD;
  config.token_status_callback = tokenStatusCallback;
  Firebase.reconnectWiFi(true);
  fbdo.setBSSLBufferSize(4096, 1024);
  Firebase.begin(&config, &auth);

  Serial.println("Firebase initialization started.");
  Serial.print("Device ID: ");
  Serial.println(DEVICE_ID);
}

void loop() {
  if (WiFi.status() != WL_CONNECTED) {
    connectWiFi();
    delay(100);
    return;
  }

  if (Firebase.ready() && millis() - lastHeartbeat >= HEARTBEAT_INTERVAL_MS) {
    lastHeartbeat = millis();
    time_t nowSeconds = time(nullptr);

    if (nowSeconds < 1700000000) {
      Serial.println("Waiting for valid NTP time; heartbeat skipped.");
      return;
    }

    // App expects Unix epoch milliseconds and considers device online for 90 seconds.
    double nowMillis = (double)nowSeconds * 1000.0;
    String path = telemetryPath() + "/lastSeen";

    if (Firebase.setDouble(fbdo, path.c_str(), nowMillis)) {
      Serial.println("Heartbeat sent. Device should show ONLINE.");
    } else {
      Serial.print("Firebase write failed: ");
      Serial.println(fbdo.errorReason());
    }
  }

  delay(10);
}
