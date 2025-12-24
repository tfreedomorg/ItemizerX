package me.focusvity.itemizerx;

import org.apache.commons.lang3.StringUtils;
import org.bukkit.ChatColor;
import org.bukkit.NamespacedKey;
import org.bukkit.Registry;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.entity.Player;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.EquipmentSlotGroup;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;

public class AttributeManager {

    public static void addAttr(final Player player, final String[] args) {
        if (args.length < 4) {
            player.sendMessage(colorize("&b/itemizer attr add <&fname&b> <&fstrength&b> [&fslot&b] &c- "
                + "&6Add an attribute"));
            return;
        }
        final Attributes a = Attributes.get(args[2]);
        if (a == null) {
            player.sendMessage(colorize("&4\"" + args[2] + "\" is not a valid attribute type."));
            return;
        }
        double amount;
        try {
            amount = Double.parseDouble(args[3]);
        } catch (NumberFormatException ex) {
            player.sendMessage(colorize("&4\"" + args[3] + "\" is not a valid number."));
            return;
        }
        if (Double.isNaN(amount)) {
            player.sendMessage(colorize("&4Please do not use &f'NaN (Not a Number)'"));
            return;
        }
        final ItemStack item = player.getInventory().getItemInMainHand();
        if (item.getType().isAir()) {
            player.sendMessage(colorize("&4Get an ITEM in hand!"));
            return;
        }
        final ItemMeta meta = item.getItemMeta();
        if (meta == null) {
            player.sendMessage(colorize("&4Could not get item meta!"));
            return;
        }
        
        // Check if attribute with same name already exists
        if (meta.getAttributeModifiers() != null) {
            for (Attribute attr : meta.getAttributeModifiers().keySet()) {
                for (AttributeModifier mod : meta.getAttributeModifiers(attr)) {
                    if (mod.getName().equals(args[2])) {
                        player.sendMessage(colorize("&4An attribute with the name \"&f" + args[2] + "&4\" already exists!"));
                        return;
                    }
                }
            }
        }
        
        AttributeModifier.Operation operation = AttributeModifier.Operation.ADD_NUMBER;
        if (a.op == 1) {
            operation = AttributeModifier.Operation.MULTIPLY_SCALAR_1;
        }
        
        NamespacedKey key = NamespacedKey.minecraft(args[2].toLowerCase().replace(" ", "_"));
        AttributeModifier modifier;
        
        if (args.length == 5) {
            final List<String> options = new ArrayList<>();
            options.add("mainhand");
            options.add("offhand");
            options.add("head");
            options.add("chest");
            options.add("legs");
            options.add("feet");
            if (!options.contains(args[4].toLowerCase())) {
                player.sendMessage(colorize("&2Supported options:\n"
                    + "&e" + StringUtils.join(options, ", ")));
                return;
            }
            
            EquipmentSlotGroup slotGroup = null;
            switch (args[4].toLowerCase()) {
                case "mainhand" -> slotGroup = EquipmentSlotGroup.MAINHAND;
                case "offhand" -> slotGroup = EquipmentSlotGroup.OFFHAND;
                case "head" -> slotGroup = EquipmentSlotGroup.HEAD;
                case "chest" -> slotGroup = EquipmentSlotGroup.CHEST;
                case "legs" -> slotGroup = EquipmentSlotGroup.LEGS;
                case "feet" -> slotGroup = EquipmentSlotGroup.FEET;
            }
            
            if (slotGroup != null) {
                modifier = new AttributeModifier(
                    key,
                    amount,
                    operation,
                    slotGroup
                );
            } else {
                modifier = new AttributeModifier(
                    key,
                    amount,
                    operation
                );
            }
        } else {
            modifier = new AttributeModifier(
                key,
                amount,
                operation
            );
        }
        
        meta.addAttributeModifier(a.getAttribute(), modifier);
        item.setItemMeta(meta);
        player.getInventory().setItemInMainHand(item);
        player.sendMessage(colorize("&2Attribute added!"));
    }

    public static void removeAttr(final Player player, final String string) {
        final ItemStack item = player.getInventory().getItemInMainHand();
        if (item.getType().isAir()) {
            player.sendMessage(colorize("&4Get an ITEM in hand!"));
            return;
        }
        final ItemMeta meta = item.getItemMeta();
        if (meta == null) {
            player.sendMessage(colorize("&4Could not get item meta!"));
            return;
        }
        
        if (meta.getAttributeModifiers() == null || meta.getAttributeModifiers().isEmpty()) {
            player.sendMessage(colorize("&4The attribute \"" + string + "\" doesn't exist!"));
            return;
        }
        
        boolean removed = false;
        for (Attribute attr : meta.getAttributeModifiers().keySet()) {
            for (AttributeModifier mod : meta.getAttributeModifiers(attr)) {
                if (mod.getName().equals(string)) {
                    meta.removeAttributeModifier(attr, mod);
                    removed = true;
                    break;
                }
            }
        }
        
        if (!removed) {
            player.sendMessage(colorize("&4The attribute \"" + string + "\" doesn't exist!"));
            return;
        }
        
        item.setItemMeta(meta);
        player.getInventory().setItemInMainHand(item);
        player.sendMessage(colorize("&2Attribute removed!"));
    }

    public static void listAttr(final Player player) {
        final ItemStack item = player.getInventory().getItemInMainHand();
        if (item.getType().isAir()) {
            player.sendMessage(colorize("&eThis item has no attributes."));
            return;
        }
        final ItemMeta meta = item.getItemMeta();
        if (meta == null || meta.getAttributeModifiers() == null || meta.getAttributeModifiers().isEmpty()) {
            player.sendMessage(colorize("&eThis item has no attributes."));
            return;
        }
        player.sendMessage(colorize("&2Item attributes: "));
        for (Attribute attr : meta.getAttributeModifiers().keySet()) {
            for (AttributeModifier mod : meta.getAttributeModifiers(attr)) {
                Attributes attrEnum = Attributes.getByAttribute(attr);
                String attrName = attrEnum != null ? attrEnum.mcName : attr.getKey().getKey();
                player.sendMessage(colorize("&e" + attrName + ", " + mod.getAmount()));
            }
        }
    }

    private static String colorize(String string) {
        return ChatColor.translateAlternateColorCodes('&', string);
    }

    public enum Attributes {

        MAX_HEALTH("generic.max_health", 0),
        FOLLOW_RANGE("generic.follow_range", 1),
        KNOCKBACK_RESISTANCE("generic.knockback_resistance", 1),
        MOVEMENT_SPEED("generic.movement_speed", 1),
        DAMAGE("generic.attack_damage", 0),
        ARMOR("generic.armor", 0),
        ARMOR_TOUGHNESS("generic.armor_toughness", 0),
        FLYING_SPEED("generic.flying_speed", 1),
        ATTACK_SPEED("generic.attack_speed", 1),
        LUCK("generic.luck", 0),
        HORSE_JUMP("horse.jump_strength", 1),
        ZOMBIE_REINFORCEMENTS("zombie.spawn_reinforcements", 1);

        private final String mcName;
        private final int op;
        private Attribute attribute;

        Attributes(String mcName, int op) {
            this.mcName = mcName;
            this.op = op;
        }

        public Attribute getAttribute() {
            if (attribute == null) {
                attribute = Registry.ATTRIBUTE.get(NamespacedKey.minecraft(mcName));
            }
            return attribute;
        }

        public static Attributes get(String name) {
            for (Attributes attr : values()) {
                if (attr.name().equalsIgnoreCase(name) || attr.mcName.equalsIgnoreCase(name)) {
                    return attr;
                }
            }
            return null;
        }

        public static Attributes getByAttribute(Attribute attribute) {
            for (Attributes attr : values()) {
                if (attr.getAttribute() == attribute) {
                    return attr;
                }
            }
            return null;
        }

        public static String getAttributes() {
            return StringUtils.join(values(), ", ");
        }

        public static List<String> getAttributeList() {
            List<String> attributes = new ArrayList<>();
            for (Attributes attr : values()) {
                attributes.add(attr.name());
            }
            return attributes;
        }
    }
}
