package net.nerjal.keepInventory.config;

import net.minecraft.entity.EntityType;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.damage.ProjectileDamageSource;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;
import net.nerjal.keepInventory.ConditionalKeepInventoryMod;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;

public class ConfigElem {
    private int id;
    private boolean toggle;
    private String killerEntity;
    private String source;
    private String projectile;
    private String weapon;

    public ConfigElem(int id, boolean toggle, String entity, String source, String projectile, String heldItem) {
        if (id == 0) {
            return;
        }
        this.id = id;
        this.toggle = toggle;
        this.killerEntity = entity;
        this.source = source;
        this.projectile = projectile;
        this.weapon = heldItem;
    }

    public int getId() {
        return this.id;
    }
    public boolean getToggle() {
        return this.toggle;
    }
    public String getKillerEntity() {
        return this.killerEntity;
    }
    public String getSource() {
        return this.source;
    }
    public String getProjectile() {
        return this.projectile;
    }
    public String getWeapon() {
        return this.weapon;
    }

    public boolean meetsCondition(DamageSource damage) {
        if (!this.toggle) return false;
        //ConditionalKeepInventoryMod.LOGGER.info(String.format("id: %d, source: %s, killerEntity: %s, targetName: %s",this.id,this.source,this.killerEntity,damage.getName()));
        if (this.source != null && !this.source.equals(damage.getName().toLowerCase())) return false;
        if (this.projectile != null) {
            if (!(damage instanceof ProjectileDamageSource projectileDamage)) return false;
            EntityType targetEntity;
            List<String> parseEntity = Arrays.asList(this.projectile.split(":"));
            if (parseEntity.size()>1) targetEntity = Registry.ENTITY_TYPE.get(new Identifier(parseEntity.get(0),parseEntity.get(1)));
            else targetEntity = Registry.ENTITY_TYPE.get(new Identifier(this.projectile));
            if (Objects.requireNonNull(projectileDamage.getSource()).getType() == null || !projectileDamage.getSource().getType().equals(targetEntity)) return false;
        }
        if (this.killerEntity != null) {
            List<String> parseEntity = Arrays.asList(this.killerEntity.split(":"));
            EntityType targetEntity;
            if (parseEntity.size()>1) {
                targetEntity = Registry.ENTITY_TYPE.get(new Identifier(parseEntity.get(0), parseEntity.get(1)));
            } else {
                targetEntity = Registry.ENTITY_TYPE.get(new Identifier(this.killerEntity));
            }
            if (this.killerEntity == "" || !Objects.requireNonNull(damage.getAttacker()).getType().equals(targetEntity)) return false;
        }
        if (this.weapon != null) {
            if (Objects.requireNonNull(damage.getAttacker()).getItemsHand() != null && this.weapon.equals("")) return false;
            if (!damage.getAttacker().getItemsHand().iterator().next().getName().asString().equals(this.weapon)) return false;
        }
        return true;
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof ConfigElem e)) return false;
        return e.getId() == id;
    }

    @Override
    public int hashCode() {
        return this.id;
    }
}
