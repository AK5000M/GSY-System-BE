## Version

    API_VER: 1.0

### Register

### Login

### QR code generate

    Get: "/api/v1/user/generate-qr",
    Authorization: "Bearer ${token}",
    Request: {
        "userId": string
    }
    Response: {
        qrCode: "qrcode image url"
    }

### Device Apis

    POST: "/device/add/",
    Authorization: "Bearer ${token}",
    Request: {
        "deviceId": string,
        "deviceInfo": string,
        "hwid": string,
        "installationDate": string,
        "manufacturer", string,
        "models": string,
        "version": string
    },
    Response: {
        "success": true
    }

### Socket IO

    - Send
        const message = JSON.stringify(
            {
                "action": string,
                "params": {
                    "keywords": string
                }
            }
        );

    - Receive
        JSON.parse(message)

    1. Add new device
        Request: {
            "action": "addDevice",
            "params": {
            "deviceId", string,
            "userId": string,
            "deviceInfo": string,
            "hwid": string,
            "installationDate": string,
            "manufacturer": string,
            "models": string,
            "version": string
            }
        }

        Response: {
            "action": "addDeviceSuccess",
            "params": true
            -- Or --
            "action": "addDeviceFailed",
            "params": false
        }

    2. Online Setting
        Request: {
            "action": "onlineDevice",
            "params": {
                "deviceId": string,
                "batteryStatus": string,
                "connectionStatus": string
            }
        }
        No Response:

    3. Offline Setting
        Request: {
            "action": "offlineDevice",
            "params": {
                "deviceId": string,
            }
        }
        No Response:
