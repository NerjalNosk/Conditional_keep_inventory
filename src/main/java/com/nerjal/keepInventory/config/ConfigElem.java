package com.nerjal.keepInventory.config;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.nerjal.keepInventory.ConditionalKeepInventoryMod;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.damage.ProjectileDamageSource;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;

import java.util.*;

public class ConfigElem {
    private int id;
    private boolean toggle;
    private Set<String> attackerOptions;
    private Set<String> sourceOptions;
    private Set<String> projectileOptions;
    private Set<String> weaponOptions;
    private Set<String> dimensionOptions;
    private Map<String,Set<String>> stuffOptions;

    public ConfigElem(int id, boolean toggle, Collection<String> a, Collection<String> s, Collection<String> p, Collection<String> w, Collection<String> d, Map<String,Set<String>> st) {
        if (id <= 0) {
            return;
        }
        this.id = id;
        this.toggle = toggle;
        if (a == null) this.attackerOptions = null;
        else this.attackerOptions = Set.copyOf(a);
        if (s == null) this.sourceOptions = null;
        else this.sourceOptions = Set.copyOf(s);
        if (p == null) this.projectileOptions = null;
        else this.projectileOptions = Set.copyOf(p);
        if (w == null) this.weaponOptions = null;
        else this.weaponOptions = Set.copyOf(w);
        if (d == null) this.dimensionOptions = null;
        else this.dimensionOptions = Set.copyOf(d);
        if (st == null) this.stuffOptions = null;
        else this.stuffOptions = Map.copyOf(st);
    }

    public static ConfigElem fromJson(JsonObject json) {
        int id;
        boolean toggle;
        Set<String> a;
        Set<String> s;
        Set<String> p;
        Set<String> w;
        Set<String> d;
        Map<String,Set<String>> st;
        try {
            id = json.get("id").getAsInt();
        } catch (JsonParseException|NullPointerException e) {
            return null;
        }
        if (id <= 0) return null;
        try {
            toggle = json.get("toggle").getAsBoolean();
        } catch (JsonParseException|NullPointerException e) {
            toggle = true;
        }
        try {
            a = new HashSet<>();
            try {
                a.add(json.get("attacker").getAsString());
            } catch (JsonParseException|UnsupportedOperationException|IllegalStateException e) {
                JsonArray array = json.getAsJsonArray("attacker");
                for (JsonElement elem : array) {
                    a.add(elem.getAsString());
                }
            }
        } catch (JsonParseException|NullPointerException e) {
            a = null;
        }
        try {
            s = new HashSet<>();
            try {
                s.add(json.get("source").getAsString().toLowerCase());
            } catch (JsonParseException|UnsupportedOperationException|IllegalStateException e) {
                JsonArray array = json.getAsJsonArray("source");
                for (JsonElement elem : array) {
                    s.add(elem.getAsString());
                }
            }
        } catch (JsonParseException|NullPointerException e) {
            s = null;
        }
        try {
            p = new HashSet<>();
            try {
                p.add(json.get("projectile").getAsString());
            } catch (JsonParseException|UnsupportedOperationException|IllegalStateException e) {
                JsonArray array = json.getAsJsonArray("projectile");
                for (JsonElement elem : array) {
                    p.add(elem.getAsString());
                }
            }
        } catch (JsonParseException|NullPointerException e) {
            p = null;
        }
        try {
            w = new HashSet<>();
            try {
                w.add(json.get("weapon").getAsString());
            } catch (JsonParseException|UnsupportedOperationException|IllegalStateException e) {
                JsonArray array = json.getAsJsonArray("weapon");
                for (JsonElement elem : array) {
                    w.add(elem.getAsString());
                }
            }
        } catch (JsonParseException|NullPointerException e) {
            w = null;
        }
        try {
            d = new HashSet<>();
            try {
                d.add(json.get("dimension").getAsString());
            } catch (JsonParseException|UnsupportedOperationException|IllegalStateException e) {
                JsonArray array = json.getAsJsonArray("dimension");
                for (JsonElement elem : array) {
                    d.add(elem.getAsString());
                }
            }
        } catch (JsonParseException|NullPointerException e) {
            d = null;
        }
        st = stuffFromJson(json);
        if (a == null && s == null && p == null && w == null && d == null && st == null) {
            ConditionalKeepInventoryMod.LOGGER.info(String.format("Return null on condition JSON %s",json));
            return null;
        }
        return new ConfigElem(id,toggle,a,s,p,w,d,st);
    }

    public static ConfigElem parseJson(JsonObject json,ListComparator l) {
        int id;
        if (l.check==0) id = ConditionalKeepInventoryMod.firstAvailableBlacklistId();
        else id = ConditionalKeepInventoryMod.firstAvailableWhitelistId();
        if (!json.has("id")) json.addProperty("id",id);
        ConfigElem elem = ConfigElem.fromJson(json);
        if (elem == null) return null;
        elem.setId(id);
        return elem;
    }

    public JsonObject toJson() {
        JsonObject object = new JsonObject();
        object.addProperty("id",this.id);
        object.addProperty("toggle",this.toggle);
        if (this.attackerOptions != null && this.attackerOptions.size() != 0) {
            if (this.attackerOptions.size() == 1) object.addProperty("attacker",this.attackerOptions.iterator().next());
            else {
                JsonArray array = new JsonArray();
                for (String attackerOption : this.attackerOptions) {
                    array.add(attackerOption);
                }
                object.add("attacker",array);
            }
        }
        if (this.sourceOptions != null && this.sourceOptions.size() != 0) {
            if (this.sourceOptions.size() == 1) object.addProperty("source",this.sourceOptions.iterator().next());
            else {
                JsonArray array = new JsonArray();
                for (String sourceOption : this.sourceOptions) {
                    array.add(sourceOption);
                }
                object.add("source",array);
            }
        }
        if (this.projectileOptions != null && this.projectileOptions.size() != 0) {
            if (this.projectileOptions.size() == 1) object.addProperty("projectile",this.projectileOptions.iterator().next());
            else {
                JsonArray array = new JsonArray();
                for (String sourceOption : this.projectileOptions) {
                    array.add(sourceOption);
                }
                object.add("projectile",array);
            }
        }
        if (this.weaponOptions != null && this.weaponOptions.size() != 0) {
            if (this.weaponOptions.size() == 1) object.addProperty("weapon",this.weaponOptions.iterator().next());
            else {
                JsonArray array = new JsonArray();
                for (String sourceOption : this.weaponOptions) {
                    array.add(sourceOption);
                }
                object.add("weapon",array);
            }
        }
        if (this.dimensionOptions != null && this.dimensionOptions.size() != 0) {
            if (this.dimensionOptions.size() == 1) object.addProperty("dimension",this.dimensionOptions.iterator().next());
            else {
                JsonArray array = new JsonArray();
                for (String sourceOption : this.dimensionOptions) {
                    array.add(sourceOption);
                }
                object.add("dimension",array);
            }
        }
        if (this.stuffOptions != null && !(this.stuffOptions.keySet().isEmpty())) {
            if (this.stuffOptions.containsKey("head") &! this.stuffOptions.get("head").isEmpty()) {
                if (this.stuffOptions.get("head").size() == 1) object.addProperty("head",this.weaponOptions.iterator().next());
                else {
                    JsonArray array = new JsonArray();
                    for (String s : this.stuffOptions.get("head")) array.add(s);
                    object.add("head",array);
                }
            }
            if (this.stuffOptions.containsKey("chest") &! this.stuffOptions.get("chest").isEmpty()) {
                if (this.stuffOptions.get("chest").size() == 1) object.addProperty("chest",this.weaponOptions.iterator().next());
                else {
                    JsonArray array = new JsonArray();
                    for (String s : this.stuffOptions.get("chest")) array.add(s);
                    object.add("chest",array);
                }
            }
            if (this.stuffOptions.containsKey("legs") &! this.stuffOptions.get("legs").isEmpty()) {
                if (this.stuffOptions.get("legs").size() == 1) object.addProperty("legs",this.weaponOptions.iterator().next());
                else {
                    JsonArray array = new JsonArray();
                    for (String s : this.stuffOptions.get("legs")) array.add(s);
                    object.add("legs",array);
                }
            }
            if (this.stuffOptions.containsKey("feet") &! this.stuffOptions.get("feet").isEmpty()) {
                if (this.stuffOptions.get("feet").size() == 1) object.addProperty("feet",this.weaponOptions.iterator().next());
                else {
                    JsonArray array = new JsonArray();
                    for (String s : this.stuffOptions.get("feet")) array.add(s);
                    object.add("feet",array);
                }
            }
            if (this.stuffOptions.containsKey("hand_1") &! this.stuffOptions.get("hand_1").isEmpty()) {
                if (this.stuffOptions.get("hand_1").size() == 1) object.addProperty("hand_1",this.weaponOptions.iterator().next());
                else {
                    JsonArray array = new JsonArray();
                    for (String s : this.stuffOptions.get("hand_1")) array.add(s);
                    object.add("hand_1",array);
                }
            }
            if (this.stuffOptions.containsKey("hand_2") &! this.stuffOptions.get("hand_2").isEmpty()) {
                if (this.stuffOptions.get("hand_2").size() == 1) object.addProperty("hand_2",this.weaponOptions.iterator().next());
                else {
                    JsonArray array = new JsonArray();
                    for (String s : this.stuffOptions.get("hand_2")) array.add(s);
                    object.add("hand_2",array);
                }
            }
        }
        return object;
    }

    public int getId() {
        return this.id;
    }
    public boolean getToggle() {
        return this.toggle;
    }
    public boolean toggle() {
        this.toggle = !this.toggle;
        return this.toggle;
    }
    public Set<String> getKillerEntity() {
        return this.attackerOptions;
    }
    public Set<String> getSource() {
        return this.sourceOptions;
    }
    public Set<String> getProjectile() {
        return this.projectileOptions;
    }
    public Set<String> getWeapon() {
        return this.weaponOptions;
    }
    public Set<String> getDimension() {
        return this.dimensionOptions;
    }
    public Map<String,Set<String>> getStuff() {
        return this.stuffOptions;
    }
    protected void setId(int id) {
        this.id = id;
    }

    public boolean meetsCondition(DamageSource damage, String worldKey, LivingEntity victim) {
        if (!this.toggle) return false;
        if (!(victim instanceof PlayerEntity player)) return false;
        // --- do only uncomment the following line for debug purposes ---
        //ConditionalKeepInventoryMod.LOGGER.info(String.format("id: %d, source: %s, killerEntity: %s, targetName: %s",this.id,this.source,this.killerEntity,damage.getName()));
        if (this.sourceOptions != null && this.sourceOptions.size() != 0) {
            boolean b = false;
            for (String s : this.sourceOptions) {
                if (damage.getName().toLowerCase().equals(s)) b = true;
                if (b) break;
            }
            if (!b) return false;
        }
        if (this.projectileOptions != null && this.projectileOptions.size() != 0) {
            boolean b = false;
            for (String s : this.projectileOptions) {
                if (s.equals("") &! (damage instanceof ProjectileDamageSource)) b = true;
                if (b) break;
                List<String> parse = Arrays.asList(s.split(":"));
                EntityType<?> target;
                if (parse.size()>1) target = Registry.ENTITY_TYPE.get(new Identifier(parse.get(0),parse.get(1)));
                else target = Registry.ENTITY_TYPE.get(new Identifier(s));
                if (damage.getSource() != null && damage.getSource().getType().equals(target)) b = true;
            }
            if (!b) return false;
        }
        if (this.attackerOptions != null && this.attackerOptions.size() != 0) {
            boolean b = false;
            for (String s : this.attackerOptions) {
                if (s.equals("") && damage.getAttacker() == null) b = true;
                if (b) break;
                List<String> parse = Arrays.asList(s.split(":"));
                EntityType<?> target;
                if (parse.size()>1) target = Registry.ENTITY_TYPE.get(new Identifier(parse.get(0),parse.get(1)));
                else target = Registry.ENTITY_TYPE.get(new Identifier(s));
                if (damage.getAttacker() != null && damage.getAttacker().getType().equals(target)) b = true;
            }
            if (!b) return false;
        }
        if (this.weaponOptions != null && this.weaponOptions.size() != 0) {
            boolean b = false;
            for (String s : this.weaponOptions) {
                if (s.equals("") && (damage.getAttacker() == null || damage.getAttacker().getItemsHand() != null &! damage.getAttacker().getItemsHand().iterator().hasNext())) b = true;
                if (b) break;
                List<String> parse = Arrays.asList(s.split(":"));
                Item target;
                if (parse.size()>1) target = Registry.ITEM.get(new Identifier(parse.get(0),parse.get(1)));
                else target = Registry.ITEM.get(new Identifier(s));
                if (damage.getAttacker() != null && damage.getAttacker().getItemsHand().iterator().next() != null && damage.getAttacker().getItemsHand().iterator().next().getItem().equals(target)) b = true;
            }
            if (!b) return false;
        }
        if (this.dimensionOptions != null && this.dimensionOptions.size() != 0) {
            boolean b = false;
            for (String s : this.dimensionOptions) {
                String dim;
                if (s.indexOf(':') < 0) dim = "minecraft:"+s;
                else {
                    String[] t = s.split(":");
                    dim = t[0]+":"+t[1];
                }
                if (worldKey.equals(dim)) {
                    b = true;
                    break;
                }
            }
            if (!b) return false;
        }
        if (this.stuffOptions != null && (!this.stuffOptions.keySet().isEmpty())) {
            return this.stuffMeetsCondition(player);
        }
        return true;
    }


    private static Map<String,Set<String>> stuffFromJson(JsonObject json) {
        if (json == null) return null;
        if (!json.has("head") &! json.has("chest") &! json.has("legs") &! json.has("feet") &! json.has("hand_1") &! json.has("hand_2")) return null;
        Map<String,Set<String>> out = new HashMap<>();
        if (json.has("head")) {
            Set<String> s = new HashSet<>();
            try {
                try {
                    s.add(json.get("head").getAsString());
                } catch (JsonParseException|UnsupportedOperationException|IllegalStateException e) {
                    JsonArray array = json.getAsJsonArray("head");
                    for (JsonElement elem : array) s.add(elem.getAsString());
                }
            } catch (JsonParseException|NullPointerException e) {
                s = null;
            }
            if (s != null) out.put("head",s);
        }
        if (json.has("chest")) {
            Set<String> s = new HashSet<>();
            try {
                try {
                    s.add(json.get("chest").getAsString());
                } catch (JsonParseException|UnsupportedOperationException|IllegalStateException e) {
                    JsonArray array = json.getAsJsonArray("chest");
                    for (JsonElement elem : array) s.add(elem.getAsString());
                }
            } catch (JsonParseException|NullPointerException e) {
                s = null;
            }
            if (s != null) out.put("chest",s);
        }
        if (json.has("legs")) {
            Set<String> s = new HashSet<>();
            try {
                try {
                    s.add(json.get("legs").getAsString());
                } catch (JsonParseException|UnsupportedOperationException|IllegalStateException e) {
                    JsonArray array = json.getAsJsonArray("legs");
                    for (JsonElement elem : array) s.add(elem.getAsString());
                }
            } catch (JsonParseException|NullPointerException e) {
                s = null;
            }
            if (s != null) out.put("legs",s);
        }
        if (json.has("feet")) {
            Set<String> s = new HashSet<>();
            try {
                try {
                    s.add(json.get("feet").getAsString());
                } catch (JsonParseException|UnsupportedOperationException|IllegalStateException e) {
                    JsonArray array = json.getAsJsonArray("feet");
                    for (JsonElement elem : array) s.add(elem.getAsString());
                }
            } catch (JsonParseException|NullPointerException e) {
                s = null;
            }
            if (s != null) out.put("feet",s);
        }
        if (json.has("hand_1")) {
            Set<String> s = new HashSet<>();
            try {
                try {
                    s.add(json.get("hand_1").getAsString());
                } catch (JsonParseException|UnsupportedOperationException|IllegalStateException e) {
                    JsonArray array = json.getAsJsonArray("hand_1");
                    for (JsonElement elem : array) s.add(elem.getAsString());
                }
            } catch (JsonParseException|NullPointerException e) {
                s = null;
            }
            if (s != null) out.put("hand_1",s);
        }
        if (json.has("hand_2")) {
            Set<String> s = new HashSet<>();
            try {
                try {
                    s.add(json.get("hand_2").getAsString());
                } catch (JsonParseException|UnsupportedOperationException|IllegalStateException e) {
                    JsonArray array = json.getAsJsonArray("hand_2");
                    for (JsonElement elem : array) s.add(elem.getAsString());
                }
            } catch (JsonParseException|NullPointerException e) {
                s = null;
            }
            if (s != null) out.put("hand_2",s);
        }
        if (out.keySet().isEmpty()) return null;
        return out;
    }

    private boolean stuffMeetsCondition(PlayerEntity player) {
        if (this.stuffOptions.containsKey("head")) {
            if (player.getInventory().getArmorStack(3).isEmpty() &! (this.stuffOptions.get("head").isEmpty() || this.stuffOptions.get("head").contains(""))) return false;
            boolean test = false;
            Item item = player.getInventory().getArmorStack(3).getItem();
            for (String s : this.stuffOptions.get("head")) {
                Item target;
                if (s.indexOf(':') < 0) target = Registry.ITEM.get(new Identifier(s));
                else {
                    String[] t = s.split(":");
                    target = Registry.ITEM.get(new Identifier(t[0],t[1]));
                }
                if (item.equals(target)) test = true;
                if (test) break;
            }
            if (!test) return false;
        }
        if (this.stuffOptions.containsKey("chest")) {
            if (player.getInventory().getArmorStack(2).isEmpty() &! (this.stuffOptions.get("chest") .isEmpty() || this.stuffOptions.get("chest").contains(""))) return false;
            boolean test = false;
            Item item = player.getInventory().getArmorStack(2).getItem();
            for (String s : this.stuffOptions.get("chest")) {
                Item target;
                if (s.indexOf(':') < 0) target = Registry.ITEM.get(new Identifier(s));
                else {
                    String[] t = s.split(":");
                    target = Registry.ITEM.get(new Identifier(t[0],t[1]));
                }
                if (item.equals(target)) test = true;
                if (test) break;
            }
            if (!test) return false;
        }
        if (this.stuffOptions.containsKey("legs")) {
            if (player.getInventory().getArmorStack(1).isEmpty() &! (this.stuffOptions.get("legs") .isEmpty() || this.stuffOptions.get("legs").contains(""))) return false;
            boolean test = false;
            Item item = player.getInventory().getArmorStack(1).getItem();
            for (String s : this.stuffOptions.get("legs")) {
                Item target;
                if (s.indexOf(':') < 0) target = Registry.ITEM.get(new Identifier(s));
                else {
                    String[] t = s.split(":");
                    target = Registry.ITEM.get(new Identifier(t[0],t[1]));
                }
                if (item.equals(target)) test = true;
                if (test) break;
            }
            if (!test) return false;
        }
        if (this.stuffOptions.containsKey("feet")) {
            if (player.getInventory().getArmorStack(0).isEmpty() &! (this.stuffOptions.get("feet") .isEmpty() || this.stuffOptions.get("feet").contains(""))) return false;
            boolean test = false;
            Item item = player.getInventory().getArmorStack(0).getItem();
            for (String s : this.stuffOptions.get("feet")) {
                Item target;
                if (s.indexOf(':') < 0) target = Registry.ITEM.get(new Identifier(s));
                else {
                    String[] t = s.split(":");
                    target = Registry.ITEM.get(new Identifier(t[0],t[1]));
                }
                if (item.equals(target)) test = true;
                if (test) break;
            }
            if (!test) return false;
        }
        List<ItemStack> hands = new ArrayList<>();
        player.getItemsHand().forEach(hands::add);
        if (this.stuffOptions.containsKey("hand_1")) {
            if (hands.isEmpty() &! (this.stuffOptions.get("hand_1") .isEmpty() || this.stuffOptions.get("hand_1").contains(""))) return false;
            boolean test = false;
            Item item = hands.get(0).getItem();
            for (String s : this.stuffOptions.get("hand_1")) {
                Item target;
                if (s.indexOf(':') < 0) target = Registry.ITEM.get(new Identifier(s));
                else {
                    String[] t = s.split(":");
                    target = Registry.ITEM.get(new Identifier(t[0],t[1]));
                }
                if (item.equals(target)) test = true;
                if (test) break;
            }
            if (!test) return false;
        }
        if (this.stuffOptions.containsKey("hand_2")) {
            if (hands.size()<2 &! (this.stuffOptions.get("hand_2") .isEmpty() || this.stuffOptions.get("hand_2").contains(""))) return false;
            boolean test = false;
            Item item = hands.get(1).getItem();
            for (String s : this.stuffOptions.get("chest")) {
                Item target;
                if (s.indexOf(':') < 0) target = Registry.ITEM.get(new Identifier(s));
                else {
                    String[] t = s.split(":");
                    target = Registry.ITEM.get(new Identifier(t[0],t[1]));
                }
                if (item.equals(target)) test = true;
                if (test) break;
            }
            return test;
        }
        return true;
    }

    private String stuffToString() {
        StringBuilder out = new StringBuilder();
        if (this.stuffOptions.containsKey("head") && this.stuffOptions.get("head") != null && this.stuffOptions.get("head").size() != 0) {
            String head;
            Set<String> t = this.stuffOptions.get("head");
            if (t.size() == 1) head = t.iterator().next();
            else head = String.format("[ %s ]",String.join(", ",t));
            out.append(String.format("; Head: %s",head));
        }
        if (this.stuffOptions.containsKey("chest") && this.stuffOptions.get("chest") != null && this.stuffOptions.get("chest").size() != 0) {
            String chest;
            Set<String> t = this.stuffOptions.get("chest");
            if (t.size() == 1) chest = t.iterator().next();
            else chest = String.format("[ %s ]",String.join(", ",t));
            out.append(String.format("; Chest: %s",chest));
        }
        if (this.stuffOptions.containsKey("legs") && this.stuffOptions.get("legs") != null && this.stuffOptions.get("legs").size() != 0) {
            String legs;
            Set<String> t = this.stuffOptions.get("legs");
            if (t.size() == 1) legs = t.iterator().next();
            else legs = String.format("[ %s ]",String.join(", ",t));
            out.append(String.format("; Legs: %s",legs));
        }
        if (this.stuffOptions.containsKey("feet") && this.stuffOptions.get("feet") != null && this.stuffOptions.get("feet").size() != 0) {
            String feet;
            Set<String> t = this.stuffOptions.get("feet");
            if (t.size() == 1) feet = t.iterator().next();
            else feet = String.format("[ %s ]",String.join(", ",t));
            out.append(String.format("; Feet: %s",feet));
        }
        if (this.stuffOptions.containsKey("hand_1") && this.stuffOptions.get("hand_1") != null && this.stuffOptions.get("hand_1").size() != 0) {
            String hand;
            Set<String> t = this.stuffOptions.get("hand_1");
            if (t.size() == 1) hand = t.iterator().next();
            else hand = String.format("[ %s ]",String.join(", ",t));
            out.append(String.format("; Hand_1: %s",hand));
        }
        if (this.stuffOptions.containsKey("hand_1") && this.stuffOptions.get("hand_1") != null && this.stuffOptions.get("hand_1").size() != 0) {
            String hand;
            Set<String> t = this.stuffOptions.get("hand_1");
            if (t.size() == 1) hand = t.iterator().next();
            else hand = String.format("[ %s ]",String.join(", ",t));
            out.append(String.format("; Hand_1: %s",hand));
        }
        if (this.stuffOptions.containsKey("hand_2") && this.stuffOptions.get("hand_2") != null && this.stuffOptions.get("hand_2").size() != 0) {
            String hand;
            Set<String> t = this.stuffOptions.get("hand_2");
            if (t.size() == 1) hand = t.iterator().next();
            else hand = String.format("[ %s ]",String.join(", ",t));
            out.append(String.format("; Hand_2: %s",hand));
        }
        return out.toString();
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

    @Override
    public String toString() {
        StringBuilder out = new StringBuilder("Conditions: ");
        out.append(String.format("Id: %d ",this.id));
        out.append(String.format("; Toggle: %b ",this.toggle));

        if (this.attackerOptions != null && this.attackerOptions.size() != 0) {
            String attacker;
            if (this.attackerOptions.size() == 1) attacker = this.attackerOptions.iterator().next();
            else attacker = String.format("[ %s ]",String.join(", ",this.attackerOptions));
            out.append(String.format("; Attacker: %s",attacker));
        }
        if (this.sourceOptions != null && this.sourceOptions.size() != 0) {
            String source;
            if (this.sourceOptions.size() == 1) source = this.sourceOptions.iterator().next();
            else source = String.format("[ %s ]",String.join(", ",this.sourceOptions));
            out.append(String.format("; Source: %s",source));
        }
        if (this.projectileOptions != null && this.projectileOptions.size() != 0) {
            String projectile;
            if (this.projectileOptions.size() == 1) projectile = this.projectileOptions.iterator().next();
            else projectile = String.format("[ %s ]",String.join(", ",this.projectileOptions));
            out.append(String.format("; Projectile: %s",projectile));
        }
        if (this.weaponOptions != null && this.weaponOptions.size() != 0) {
            String weapon;
            if (this.weaponOptions.size() == 1) weapon = this.weaponOptions.iterator().next();
            else weapon = String.format("[ %s ]",String.join(", ",this.weaponOptions));
            out.append(String.format("; Weapon: %s",weapon));
        }
        if (this.dimensionOptions != null && this.dimensionOptions.size() != 0) {
            String dimension;
            if (this.dimensionOptions.size() == 1) dimension = this.dimensionOptions.iterator().next();
            else dimension = String.format("[ %s ]",String.join(", ",this.dimensionOptions));
            out.append(String.format("; Dimension: %s",dimension));
        }
        if (this.stuffOptions != null && !(this.stuffOptions.keySet().isEmpty())) {
            out.append(this.stuffToString());
        }
        return out.toString();
    }
}
