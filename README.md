# HarmonyOS Distributed Scheduler - How to launch an ability from one device to another?

## Introduction 
HarmonyOS is a future-proof distributed operating system oriented to all-scenario smart lifestyles. For consumers, HarmonyOS integrates their various smart devices into "One Super Device" that delivers the best possible interaction experience through ultra-fast connection, multi-device collaboration, and resource sharing between different devices.

![SuperDevice.](/assets/super_device_2.jpg "SuperDevice")

Users are using two or more devices to experience an all-scenario, multi-device lifestyle. Each type of device has its unique advantages and disadvantages specific to scenarios. For example, a watch provides straightforward access to information. A TV is excellent at providing immersive watching experience, but is terrible at text input. If multiple devices can sense each other and be integrated into "One Super Device" in a distributed operating system, each device can play its strengths and avoid its weaknesses, providing users with natural and frictionless distributed experiences. In HarmonyOS, distributed experience is called distributed hop.

## Distributed Scheduler
In HarmonyOS, the Distributed Scheduler provides unified component management for the "super virtual device" built by multiple devices running HarmonyOS. The Distributed Scheduler defines a unified capability baseline, API format, data structure, and service description language for applications to adapt to different hardware. You can perform various distributed tasks using the Distributed Scheduler, such as remote startup, remote calling, and seamless migration of abilities.

The Distributed Scheduler allows Ability instances (basic components for distributed scheduling) to be started, stopped, connected, disconnected, and migrated across devices, enabling cross-device component management.

**Starting or stopping an ability**
The Distributed Scheduler provides remote ability management. You can start an FA (Feature Ability, that is, an ability using the Page template) and start or stop a PA (Particle Ability, that is, an ability using either the Service or Data template) from a remote device.

**Connecting to or disconnecting from an ability**
The Distributed Scheduler provides cross-device PA control. By connecting to a remote PA, you can obtain the cross-device client for task scheduling. After the cross-device task is completed, you can then disconnect from the remote PA to unregister this client.

**Migrating an ability**
The Distributed Scheduler enables cross-device ability migration. You can call a migration method to seamlessly migrate an FA from the local device to a specified remote device.

### When to Use Distributed Scheduler?
With the help of APIs provided by the Distributed Scheduler, you can integrate the distributed scheduling capabilities into your application to implement cross-device collaboration. Based on different ability templates and intentions, the Distributed Scheduler allows you to start a remote FA or PA, stop a remote PA, connect to a remote PA, disconnect from a remote PA, or migrate an FA to another device. The following uses device A (local device) and device B (remote device) as an example to describe when and how to use these distributed scheduling capabilities:

**Device A starts an FA on device B.**
On device A, the user touches the startup button provided by the local application to start a particular FA on device B. For example, to enable your users to open the photo gallery application installed on device B, all you need is to specify the action of opening the gallery application in the Intent.

# How to Develop
1.- Create a new Java HarmonyOS projec

![NewProject.](/assets/new_project.PNG "NewProject")

2.- Add the cross-device collaboration permission to the reqPermissions attribute in the config.json file for the particular ability to enable distributed scheduling. 
```
"reqPermissions": [
  {
    "name": "ohos.permission.DISTRIBUTED_DEVICE_STATE_CHANGE"
  },
  {
    "name": "ohos.permission.GET_DISTRIBUTED_DEVICE_INFO"
  },
  {
    "name": "ohos.permission.DISTRIBUTED_DATA"
  },
  {
    "name": "ohos.permission.DISTRIBUTED_DATASYNC"
  }
]
```

3.- Add device types where the application runs on **config.json** file
```
"deviceType": [
  "phone",
  "wearable"
]
```

4.- Create the main layout, this application is very simple, it is just an AbilitySlice that contains a button, from where we will launch the Feature Ability (FA) from device A to device B and vice versa.
```
<?xml version="1.0" encoding="utf-8"?>
<DirectionalLayout
    xmlns:ohos="http://schemas.huawei.com/res/ohos"
    ohos:height="match_parent"
    ohos:width="match_parent"
    ohos:alignment="center"
    ohos:orientation="vertical">
 
    <Button
        ohos:id="$+id:select_device_button"
        ohos:height="45vp"
        ohos:width="match_parent"
        ohos:background_element="$graphic:button_background"
        ohos:layout_alignment="horizontal_center"
        ohos:margin="18vp"
        ohos:text="Selecciona un dispositivo"
        ohos:text_size="$float:button_text_size"/>
</DirectionalLayout>
```

Using **multi-device preview**: 

![MultidevicePreview.](/assets/multidevice_preview.PNG "Multi-Device Preview")

5.- Explicitly declare the required permission
In the onStart method of the **MainAbility.java** we explicitly request the permissions to the user.
```
@Override
public void onStart(Intent intent) {
    // Explicitly request user permissions
    requestPermissionsFromUser(new String[]{SystemPermission.DISTRIBUTED_DATASYNC}, 0);
    super.onStart(intent);
    super.setMainRoute(MainAbilitySlice.class.getName());
}
```

6.- Obtain a list of available devices.
Add a listener to the Button **select_device_button** to obtain all the remote devices information:
```
@Override
public void onStart(Intent intent) {
    super.onStart(intent);
    super.setUIContent(ResourceTable.Layout_ability_main);
 
    devicesButton = (Button) findComponentById(ResourceTable.Id_select_device_button);
    devicesButton.setClickedListener(button -> {
        showDevicesList();
    });
}
```

To obtain information about all remote devices on the distributed network, call DeviceManager#getDeviceList(int). To obtain information about a specified remote device, call DeviceManager#getDeviceInfo(String).
Indicates the flag used for querying specified devices. The value DeviceInfo#FLAG_GET_ALL_DEVICE means to query all online and offline devices on the distributed network; DeviceInfo#FLAG_GET_ONLINE_DEVICE means to query all online devices on the distributed network; and DeviceInfo#FLAG_GET_OFFLINE_DEVICE means to query all offline devices on the distributed network.
```
List<DeviceInfo> deviceList = DeviceManager.getDeviceList(DeviceInfo.FLAG_GET_ONLINE_DEVICE);
if (deviceList == null || deviceList.isEmpty()) {
    new ToastDialog(getContext()).setContentText("Device not found").show();
}
```

7.- After obtain all devices list, print it on a ListDialog as follow
```
private void showDevicesList() {
    List<DeviceInfo> deviceList = DeviceManager.getDeviceList(DeviceInfo.FLAG_GET_ONLINE_DEVICE);
    if (deviceList == null || deviceList.isEmpty()) {
        new ToastDialog(getContext()).setContentText("Device not found").show();
    }
 
    String[] deviceNameList = new String[deviceList.size()];
    int pos = 0;
 
    for (DeviceInfo deviceInfo : deviceList) {
        deviceNameList[pos] = deviceInfo.getDeviceName();
        pos++;
    }
 
    ListDialog listDialog = new ListDialog(getContext());
    listDialog.setItems(deviceNameList);
    listDialog.setOnSingleSelectListener((iDialog, i) -> {
        DeviceInfo deviceInfo = deviceList.get(i);
        if (deviceInfo == null || TextTool.isNullOrEmpty(deviceInfo.getDeviceId())) {
            //Dispositivo invalido
            return;
        }
        onRemoteDeviceSelected(deviceInfo);
    });
    listDialog.setTransparent(true);
    listDialog.setAutoClosable(true);
 
    listDialog.setAlignment(LayoutAlignment.CENTER);
    listDialog.show();
}
```

![Preview.](/assets/preview.png "Preview")

8.- Set a function on the click listener for the item selected for starting a remote **FA** and implement the remote **FA** startup capability.
```
private void onRemoteDeviceSelected(DeviceInfo deviceInfo) {
    Intent intent = new Intent();
    Operation operation = new Intent.OperationBuilder()
            .withDeviceId(deviceInfo.getDeviceId())
            .withBundleName(getBundleName())
            .withAbilityName(MainAbility.class)
            .withFlags(Intent.FLAG_ABILITYSLICE_MULTI_DEVICE)
            .build();
    intent.setOperation(operation);
    try {
        List<AbilityInfo> abilityInfoList = getBundleManager().queryAbilityByIntent(intent, 0, 0);
        if (abilityInfoList != null && !abilityInfoList.isEmpty()) {
            startAbility(intent);
        }
    } catch (RemoteException e) {
        // Excepción
    }
}
```

Use the **OperationBuilder** class of **Intent** to construct an **Operation** object and set the **deviceId** (left empty if a local ability is required), bundleName, and abilityName attributes for the object.

**FLAG_ABILITYSLICE_MULTI_DEVICE** Supports multi-device startup in the distributed scheduling system.

![Example.](/assets/example.gif "Example")

## Tips and Tricks 

All the application resource files, such as strings, images, and audio files, are stored in the resources directory, allowing you to easily access, use, and maintain them. The resources directory consists of two types of sub-directories: the base sub-directory and qualifiers sub-directories, and the rawfile sub-directory. 

The name of a qualifiers sub-directory consists of one or more qualifiers that represent the application scenarios or device characteristics, covering the mobile country code (MCC), mobile network code (MNC), language, script, country or region, screen orientation, device type, night mode, and screen density. The qualifiers are separated using underscores (_) or hyphens (-). When creating a qualifiers sub-directory, you need to understand the directory naming conventions and the rules for matching qualifiers sub-directories and the device status.

Device type


Indicates the device type. The value can be:

* **phone**: smartphones

* **tablet**: tablets

* **tcar**: head units

* **ttv**: smart TVs

* **twearable**: wearables

Then, to configure different font sizes to fit the different devices that we have set up in the project, we create the wearable directory within resources directory, where we will have an element directory that contains the float.json file where we will add the different measurements depending on of the device.

![Structure.](/assets/structure.PNG "Structure")


![Capture.](/assets/Capture.PNG "Capture")

## References
**Distributed Scheduler Overview**
(https://developer.harmonyos.com/en/docs/documentation/doc-guides/ability-distributed-overview-0000001050419345)

**DeviceManager**
(https://developer.harmonyos.com/en/docs/documentation/doc-references/devicemanager-0000001054358820)

**ListDialog**
(https://developer.harmonyos.com/en/docs/documentation/doc-references/listdialog-0000001054120087)

**Resource File Categories**
(https://developer.harmonyos.com/en/docs/documentation/doc-guides/basic-resource-file-categories-0000001052066099)

**Intent**
(https://developer.harmonyos.com/en/docs/documentation/doc-references/intent-0000001054120019)

**Intent.OperationBuilder**
(https://developer.harmonyos.com/en/docs/documentation/doc-references/intent_operationbuilder-0000001054119948)