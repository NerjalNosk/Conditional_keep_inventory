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

    @Inject(method = "drop",at = @At(value = "INVOKE"))
    private void dropCall(DamageSource source, CallbackInfo ci) {
        ConditionalKeepInventoryMod.LOGGER.info("called LivingEntity#drop");
    }

    @Inject(method="damage", at = @At("INVOKE"))
    protected void updatePlayerDamageData(DamageSource source, float amount, CallbackInfoReturnable<Boolean> cir) {
        for (PlayerEntity player : this.world.getPlayers()) {
            if (player.getUuid().equals(this.uuid)) {
                ConditionalKeepInventoryMod.updatePlayerDamage(this.uuid,source);
                break;
            }
        }
    }
}
