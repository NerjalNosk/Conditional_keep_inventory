package net.nerjal.keepInventory.mixins;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.world.World;
import net.nerjal.keepInventory.ConditionalKeepInventoryMod;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(LivingEntity.class)
public abstract class LivingEntityMixin extends Entity {
    public LivingEntityMixin(EntityType<?> type, World world) {
        super(type, world);
    }

    @Inject(method="damage", at = @At(value = "HEAD"))
    protected void updatePlayerDamageData(DamageSource source, float amount, CallbackInfoReturnable<Boolean> cir) {
        for (PlayerEntity player : this.world.getPlayers()) {
            if (player.getUuid().equals(this.uuid)) {
                ConditionalKeepInventoryMod.LOGGER.info(String.format("Death dimension key: %s",this.world.getRegistryKey().getValue().toString()));
                ConditionalKeepInventoryMod.updatePlayerDamage(this.uuid,source,this.world.getRegistryKey().getValue().toString());
                break;
            }
        }
    }
}
