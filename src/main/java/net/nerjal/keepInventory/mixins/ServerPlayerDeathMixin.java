package net.nerjal.keepInventory.mixins;

import com.mojang.authlib.GameProfile;
import net.minecraft.entity.ExperienceOrbEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.GameRules;
import net.minecraft.world.World;
import net.nerjal.keepInventory.ConditionalKeepInventoryMod;
import net.nerjal.keepInventory.Validation;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(ServerPlayerEntity.class)
public abstract class ServerPlayerDeathMixin extends PlayerEntity {

    public ServerPlayerDeathMixin(World world, BlockPos pos, float yaw, GameProfile profile) {
        super(world, pos, yaw, profile);
    }

    @Redirect(
            method = "copyFrom",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/GameRules;getBoolean(Lnet/minecraft/world/GameRules$Key;)Z"
            )
    )
    public boolean conditionalKeepInventoryCheck(GameRules gameRules, GameRules.Key<GameRules.BooleanRule> rule) {
        if (gameRules.getBoolean(ConditionalKeepInventoryMod.conditionalKeepInventoryRule)) {
            if (gameRules.getBoolean(rule)) {
                return !ConditionalKeepInventoryMod.getPlayerValidation(this.uuid).equals(Validation.BLACKLIST);
            }
            return ConditionalKeepInventoryMod.getPlayerValidation(this.uuid).equals(Validation.WHITELIST);
        }
        return gameRules.getBoolean(rule);
    }
}
