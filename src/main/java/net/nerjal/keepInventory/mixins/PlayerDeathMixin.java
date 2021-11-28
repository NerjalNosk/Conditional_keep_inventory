package net.nerjal.keepInventory.mixins;

import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.world.GameRules;
import net.minecraft.world.World;
import net.nerjal.keepInventory.ConditionalKeepInventoryMod;
import net.nerjal.keepInventory.Validation;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(PlayerEntity.class)
public abstract class PlayerDeathMixin extends LivingEntity {
    //@Shadow protected abstract void vanishCursedItems();

    @Shadow public abstract Text getName();

    protected PlayerDeathMixin(EntityType<? extends LivingEntity> entityType, World world) {
        super(entityType, world);
    }

    @Redirect(
            method = "dropInventory",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/GameRules;getBoolean(Lnet/minecraft/world/GameRules$Key;)Z"
            )
    )
    public boolean checkValidation(GameRules gameRules, GameRules.Key<GameRules.BooleanRule> rule) {
        //ConditionalKeepInventoryMod.LOGGER.info("Checking validity of last taken damage before applying potential keepInventory");
        Validation damageTest = ConditionalKeepInventoryMod.getPlayerValidation(this.uuid);
        //ConditionalKeepInventoryMod.LOGGER.info(String.format("Validation obtained: %s",damageTest));
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

    @Inject(method = "onDeath", at = @At(value = "HEAD"))
    protected void checkDeath(DamageSource source, CallbackInfo ci) {
        String name = this.getName().asString();
        ConditionalKeepInventoryMod.LOGGER.info(String.format("Death dimension key: %s",this.world.getRegistryKey().getValue().toString()));
        boolean whitelisted = ConditionalKeepInventoryMod.isWhitelisted(source,this.world.getRegistryKey().getValue().toString(),this);
        boolean blacklisted = ConditionalKeepInventoryMod.isBlacklisted(source,this.world.getRegistryKey().getValue().toString(),this);
        ConditionalKeepInventoryMod.LOGGER.info(String.format("Damage! Name: %s ; Whitelisted: %b ; Blacklisted: %b",name,whitelisted,blacklisted));
    }

    /*@Inject(method = "onDeath", at = @At(value = "INVOKE"))
    private void updateDamageData(DamageSource source, CallbackInfo ci) {
        ConditionalKeepInventoryMod.updatePlayerDamage(this.uuid,source);
        ConditionalKeepInventoryMod.LOGGER.info("Updated player damage data");
    }*/
}
