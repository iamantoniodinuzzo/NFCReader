# NFC Reader & HCE Service App

📱 Welcome to the NFC Reader & HCE Service app! This open-source application is designed to allow users to read messages sent by an Host-based Card Emulation (HCE) via NFC. The app is divided into two separate modules: the "app" module, which serves as an NFC reader, and the "hce" module, which implements the service for sending messages via NFC.

## Module "app" (NFC Reader)

📚 The "app" module of the application is the NFC reader. To use this module, follow these steps:

1. Ensure that your device has NFC enabled. 📡
2. Start the application and select the "app" module from the initial screen. 📲
3. Bring your NFC-enabled device close to the NFC tag or HCE device that is sending the message. 📤
4. The app will automatically detect the NFC message and display it on the screen. 📩

## Module "hce" (HCE Service)

🚀 The "hce" module of the application is the HCE service. To use this module, follow these steps:

1. Ensure that your device has NFC enabled. 📡
2. Start the application and select the "hce" module from the initial screen. 📲
3. This module implements the HCE service, allowing your device to emulate an NFC card and send messages to NFC readers. 📡
4. Use other NFC applications or devices to read the messages sent by the "hce" module of the app. 📥

## Resources Used

In the implementation of this application, the following resources were used:

📦 GitHub repository for the HCE service: [GitHub Repository](https://github.com/underwindfall/NFCAndroid/tree/master/app)

🔗 Stack Overflow for NFC reader implementation: [Stack Overflow Post](https://stackoverflow.com/questions/64920307/how-to-write-ndef-records-to-nfc-tag/64921434#64921434)

We hope this application serves your NFC reading and writing needs. If you have any questions or issues, please consult the documentation within the repository or reach out to the developer. 🤝

Thank you for using the NFC Reader & HCE Service App! 🙌📱
