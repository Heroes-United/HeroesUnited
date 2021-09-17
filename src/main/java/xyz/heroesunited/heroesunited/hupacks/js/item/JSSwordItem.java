package xyz.heroesunited.heroesunited.hupacks.js.item;

import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.SwordItem;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.world.World;

import javax.script.Invocable;
import javax.script.ScriptEngine;
import javax.script.ScriptException;
import java.util.Map;

public class JSSwordItem extends SwordItem implements IJSItem {

    private final ScriptEngine engine;

    public JSSwordItem(Map.Entry<JSItemProperties, ScriptEngine> entry) {
        super(entry.getKey().tier, entry.getKey().attackDamage, entry.getKey().attackSpeed, entry.getKey());
        this.engine = entry.getValue();
    }

    @Override
    public void inventoryTick(ItemStack stack, World world, Entity entity, int itemSlot, boolean selected) {
        super.inventoryTick(stack, world, entity, itemSlot, selected);
        try {
            ((Invocable) engine).invokeFunction("inventoryTick", stack, world, entity, itemSlot, selected);
        } catch (ScriptException | NoSuchMethodException ignored) {
        }
    }

    @Override
    public ActionResult<ItemStack> use(World world, PlayerEntity player, Hand hand) {
        try {
            return (ActionResult<ItemStack>) ((Invocable) engine).invokeFunction("use", world, player, hand);
        } catch (ScriptException | NoSuchMethodException e) {
            return super.use(world, player, hand);
        }
    }

    @Override
    public ScriptEngine getEngine() {
        return engine;
    }
}