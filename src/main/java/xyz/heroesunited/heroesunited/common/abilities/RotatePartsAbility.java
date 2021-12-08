package xyz.heroesunited.heroesunited.common.abilities;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.minecraft.util.GsonHelper;
import xyz.heroesunited.heroesunited.client.events.SetupAnimEvent;
import xyz.heroesunited.heroesunited.util.HUJsonUtils;
import xyz.heroesunited.heroesunited.util.PlayerPart;

import java.util.Map;
import java.util.function.Consumer;

public class RotatePartsAbility extends JSONAbility {

    public RotatePartsAbility(AbilityType type) {
        super(type);
    }

    @Override
    public void initializeClient(Consumer<IAbilityClientProperties> consumer) {
        super.initializeClient(consumer);
        consumer.accept(new IAbilityClientProperties() {
            @Override
            public void setupAnim(SetupAnimEvent event) {
                if (RotatePartsAbility.this.getJsonObject().has("parts") && getEnabled()) {
                    JsonObject overrides = GsonHelper.getAsJsonObject(getJsonObject(), "parts");

                    for (Map.Entry<String, JsonElement> entry : overrides.entrySet()) {
                        PlayerPart part = PlayerPart.byName(entry.getKey());
                        if (part != null && entry.getValue() instanceof JsonObject) {
                            JsonObject jsonObject = (JsonObject) entry.getValue();
                            if (jsonObject.has("value")) {
                                HUJsonUtils.translatePivotOfModel(part.modelPart(event.getPlayerModel()), GsonHelper.getAsString(jsonObject, "xyz"), GsonHelper.getAsFloat(jsonObject, "value"), GsonHelper.getAsBoolean(jsonObject, "player", false));
                            } else {
                                HUJsonUtils.rotatePartOfModel(part.modelPart(event.getPlayerModel()), GsonHelper.getAsString(jsonObject, "xyz"), GsonHelper.getAsFloat(jsonObject, "angle"), GsonHelper.getAsBoolean(jsonObject, "player", false));
                            }
                        }
                    }
                }
            }
        });
    }
}
