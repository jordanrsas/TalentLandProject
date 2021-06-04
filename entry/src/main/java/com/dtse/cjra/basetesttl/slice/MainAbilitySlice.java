package com.dtse.cjra.basetesttl.slice;

import com.dtse.cjra.basetesttl.MainAbility;
import com.dtse.cjra.basetesttl.ResourceTable;
import ohos.aafwk.ability.AbilitySlice;
import ohos.aafwk.content.Intent;
import ohos.aafwk.content.Operation;
import ohos.agp.components.Button;
import ohos.agp.utils.LayoutAlignment;
import ohos.agp.utils.TextTool;
import ohos.agp.window.dialog.ListDialog;
import ohos.agp.window.dialog.ToastDialog;
import ohos.bundle.AbilityInfo;
import ohos.distributedschedule.interwork.DeviceInfo;
import ohos.distributedschedule.interwork.DeviceManager;
import ohos.rpc.RemoteException;
import ohos.security.SystemPermission;

import java.util.List;

public class MainAbilitySlice extends AbilitySlice {

    private Button devicesButton;
    private List<DeviceInfo> deviceList;

    @Override
    public void onStart(Intent intent) {
        super.onStart(intent);
        super.setUIContent(ResourceTable.Layout_ability_main);

        devicesButton = (Button) findComponentById(ResourceTable.Id_select_device_button);
        devicesButton.setClickedListener(button -> {
            showDevicesList();
        });
    }

    private void showDevicesList() {
        deviceList = DeviceManager.getDeviceList(DeviceInfo.FLAG_GET_ONLINE_DEVICE);
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

    @Override
    public void onActive() {
        super.onActive();
    }

    @Override
    public void onForeground(Intent intent) {
        super.onForeground(intent);
    }
}
