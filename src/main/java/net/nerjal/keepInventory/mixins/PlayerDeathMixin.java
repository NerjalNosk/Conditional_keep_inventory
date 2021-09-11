package net.nerjal.keepInventory.mixins;

import net.minecraft.entity.EntityType;
//import net.minecraft.entity.ExperienceOrbEntity;
import net.minecraft.entity.LivingEntity;
//import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.player.PlayerEntity;
//import net.minecraft.server.world.ServerWorld;
import net.minecraft.world.GameRules;
import net.minecraft.world.World;
import net.nerjal.keepInventory.ConditionalKeepInventoryMod;
import net.nerjal.keepInventory.Validation;
import org.spongepowered.asm.mixin.Mixin;
//import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(PlayerEntity.class)
public abstract class PlayerDeathMixin extends LivingEntity {
    //@Shadow protected abstract void vanishCursedItems();

    protected PlayerDeathMixin(EntityType<? extends LivingEntity> entityType, World world) {
        super(entityType, world);
    }

    /*@Override
    protected void drop(DamageSource source) {
        ConditionalKeepInventoryMod.LOGGER.info("Debugging: Player Death issued");
        if (this.world.getGameRules().getBoolean(ConditionalKeepInventoryMod.conditionalKeepInventoryRule)) {
            if (this.world.getGameRules().getBoolean(GameRules.KEEP_INVENTORY)) {
                if (ConditionalKeepInventoryMod.isBlacklisted(source)) {
                    ConditionalKeepInventoryMod.LOGGER.info("reached blacklisted");
                    super.drop(source);
                } else {
                    if (source.getAttacker() != null && source.getAttacker().isPlayer() && this.world instanceof ServerWorld) ExperienceOrbEntity.spawn((ServerWorld)this.world,this.getPos(),20);
                }
            } else if (ConditionalKeepInventoryMod.isWhitelisted(source)) {
                ConditionalKeepInventoryMod.LOGGER.info("reached whitelisted");
                if (source.getAttacker() != null && source.getAttacker().isPlayer() && this.world instanceof ServerWorld serverWorld) ExperienceOrbEntity.spawn(serverWorld,this.getPos(),20);
                if (this.world.getGameRules().getBoolean(ConditionalKeepInventoryMod.conditionalDoVanishing)) this.vanishCursedItems();
            } else super.drop(source);
        }
    }*/

    @Redirect(
            method = "dropInventory",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/GameRules;getBoolean(Lnet/minecraft/world/GameRules$Key;)Z"
            )
    )
    public boolean checkValidation(GameRules gameRules, GameRules.Key<GameRules.BooleanRule> rule) {
        ConditionalKeepInventoryMod.LOGGER.info("Checking validity of last taken damage before applying potential keepInventory");
        Validation damageTest = ConditionalKeepInventoryMod.getPlayerValidation(this.uuid);
        ConditionalKeepInventoryMod.LOGGER.info(String.format("Validation obtained: %s",damageTest));
        if (gameRules.getBoolean(ConditionalKeepInventoryMod.conditionalKeepInventoryRule)) {
            if (gameRules.getBoolean(rule)) {
                return !damageTest.compare(Validation.BLACKLIST);
            }
            return damageTest.compare(Validation.WHITELIST);
        }
        return gameRules.getBoolean(rule);
    }

    @Redirect(
            method = "getXpToDrop",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/GameRules;getBoolean(Lnet/minecraft/world/GameRules$Key;)Z"
            )
    )
    public boolean checkValid(GameRules gameRules, GameRules.Key<GameRules.BooleanRule> rule) {
        return checkValidation(gameRules,rule);
    }

    /*@Inject(method = "onDeath", at = @At(value = "INVOKE"))
    private void updateDamageData(DamageSource source, CallbackInfo ci) {
        ConditionalKeepInventoryMod.updatePlayerDamage(this.uuid,source);
        ConditionalKeepInventoryMod.LOGGER.info("Updated player damage data");
    }*/
}
