# KISAN 365 ESP8266 - Wi-Fi only phase

## What this firmware does
- Connects a NodeMCU ESP8266 to the configured 2.4 GHz Wi-Fi.
- Signs in to Firebase Realtime Database with a dedicated Firebase Email/Password user.
- Writes only `lastSeen` every 30 seconds at:
  `/devices/K365-0001/telemetry/lastSeen`
- The Android app expects Unix epoch milliseconds and treats a device as online for 90 seconds.

## What it deliberately does NOT do
- No voltage/current readings.
- No supply ON/OFF detection.
- No motor state detection.
- No relay or contactor control.
Do not connect mains wiring or a motor in this phase. The ESP8266 is only a Wi-Fi/Firebase heartbeat tester.

## Arduino IDE setup
1. Install the ESP8266 board package by Espressif.
2. Select **NodeMCU 1.0 (ESP-12E Module)**.
3. Install **Firebase Arduino Client Library for ESP8266 and ESP32** by Mobizt from Library Manager.
4. Open `ESP8266_KISAN365_WIFI_ONLY.ino`.
5. Fill in Wi-Fi SSID/password, Firebase Web API key, and the dedicated Firebase Email/Password account.
6. Keep `DEVICE_ID` exactly `K365-0001` (must match the QR code and Android app).
7. Upload using a USB data cable; open Serial Monitor at 115200 baud.

## Firebase preparation
1. In the **kisan365-commercial** Firebase project, enable Authentication > Sign-in method > Email/Password.
2. Create a dedicated device user in Authentication > Users. Put that user's email/password in the sketch. Do not use your personal account password in a shared sketch.
3. In Realtime Database, create rules that allow authenticated users to read/write only the device path as appropriate for your deployment. Do not use public `.read: true` / `.write: true` rules.
4. The Android app must also be authenticated to the same Firebase project for protected RTDB reads. A login to a different Firebase project does not grant access to this database.

## Wiring for this phase
- NodeMCU micro-USB -> stable USB 5 V supply.
- No sensor wires.
- No relay wires.
- No 230/415 V wiring.

## Next hardware phase
For three-phase measurement, choose a properly rated, isolated three-phase Modbus energy meter and compatible isolated RS485 interface. Have a qualified electrician install CTs/voltage connections inside a protected enclosure. Only after the meter model is confirmed should sensor wiring and measurement code be added.
