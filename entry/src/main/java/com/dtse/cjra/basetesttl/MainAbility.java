package com.dtse.cjra.basetesttl;

import com.dtse.cjra.basetesttl.slice.MainAbilitySlice;
import ohos.aafwk.ability.Ability;
import ohos.aafwk.content.Intent;
import ohos.security.SystemPermission;

public class MainAbility extends Ability {
    @Override
    public void onStart(Intent intent) {
        // Solicitar explicitamente los permisos al usuario
        requestPermissionsFromUser(new String[]{SystemPermission.DISTRIBUTED_DATASYNC}, 0);
        super.onStart(intent);
        super.setMainRoute(MainAbilitySlice.class.getName());
    }
}
