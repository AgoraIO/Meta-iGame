package io.agora.scene.rtegame.util;

import androidx.annotation.IntDef;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@IntDef(value = {GameEnvType.ENV_AGORA_OFFICIAL, GameEnvType.ENV_AGORA_TEST, GameEnvType.ENV_HURAN_OFFICIAL, GameEnvType.ENV_HURAN_TEST})
@Retention(RetentionPolicy.SOURCE)
public @interface GameEnvType {
    int ENV_AGORA_OFFICIAL = 100;
    int ENV_AGORA_TEST = 101;
    int ENV_HURAN_OFFICIAL = 200;
    int ENV_HURAN_TEST = 201;
}
