package me.byteful.plugin.leveltools.api;

import com.cryptomorin.xseries.XEnchantment;
import java.util.Arrays;
import java.util.Locale;
import java.util.Objects;
import java.util.Optional;

import me.byteful.plugin.leveltools.LevelToolsPlugin;
import me.byteful.plugin.leveltools.api.item.LevelToolsItem;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.math.NumberUtils;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public enum RewardType {
  COMMAND("command", false) {
    @Override
    public void apply(
        @NotNull LevelToolsItem tool, @NotNull String[] split, @NotNull Player player) {
      LevelToolsPlugin.getInstance().getScheduler().sync(() -> Bukkit.dispatchCommand(
          Bukkit.getConsoleSender(),
          String.join(" ", Arrays.copyOfRange(split, 1, split.length))
              .replace("{player}", player.getName())
              .replace("%player%", player.getName())));
    }
  },
  PLAYER_COMMAND("player-command", false) {
    @Override
    public void apply(
        @NotNull LevelToolsItem tool, @NotNull String[] split, @NotNull Player player) {
      player.chat(
          "/"
              + String.join(" ", Arrays.copyOfRange(split, 1, split.length))
                  .replace("{player}", player.getName())
                  .replace("%player%", player.getName()));
    }
  },
  PLAYER_OPCOMMAND("player-opcommand", false) {
    @Override
    public void apply(
        @NotNull LevelToolsItem tool, @NotNull String[] split, @NotNull Player player) {
      final boolean isOP = player.isOp();
      if (!isOP) {
        player.setOp(true);
      }
      PLAYER_COMMAND.apply(tool, split, player);
      player.setOp(isOP);
    }
  },
  ENCHANT("enchant") {
    @Override
    public void apply(
        @NotNull LevelToolsItem tool, @NotNull String[] split, @NotNull Player player) {
      if (split.length < 3) {
        return;
      }

      final Optional<XEnchantment> enchant = XEnchantment.matchXEnchantment(split[1]);

      if (enchant.isPresent() && NumberUtils.isNumber(split[2])) {
        tool.enchant(enchant.get().getEnchant(), Integer.parseInt(split[2]));
      }
    }
  },
  ENCHANT_2("enchant2") {
    @Override
    public void apply(
        @NotNull LevelToolsItem tool, @NotNull String[] split, @NotNull Player player) {
      if (split.length < 3) {
        return;
      }

      final Optional<XEnchantment> enchant = XEnchantment.matchXEnchantment(split[1]);

      if (NumberUtils.isNumber(split[2])) {
        final int level = Integer.parseInt(split[2]);

        if (enchant.isPresent()
            && tool.getItemStack()
                    .getEnchantmentLevel(Objects.requireNonNull(enchant.get().getEnchant()))
                < level) {
          tool.enchant(enchant.get().getEnchant(), level);
        }
      }
    }
  },
  ENCHANT_3("enchant3") {
    @Override
    public void apply(
        @NotNull LevelToolsItem tool, @NotNull String[] split, @NotNull Player player) {
      if (split.length < 3) {
        return;
      }

      final Optional<XEnchantment> enchant = XEnchantment.matchXEnchantment(split[1]);

      if (NumberUtils.isNumber(split[2])) {
        final int level = Integer.parseInt(split[2]);

        if (enchant.isPresent()) {
          final int currentLvl =
              tool.getItemStack()
                  .getEnchantmentLevel(Objects.requireNonNull(enchant.get().getEnchant()));
          tool.enchant(enchant.get().getEnchant(), currentLvl + level);
        }
      }
    }
  },
  ATTRIBUTE("attribute") {
    @Override
    public void apply(
        @NotNull LevelToolsItem tool, @NotNull String[] split, @NotNull Player player) {
      if (split.length < 3) {
        return;
      }

      String attribute = split[1];

      if (NumberUtils.isNumber(split[2])) {
        final double modifier = Double.parseDouble(split[2]);

        if (StringUtils.countMatches(attribute, "_") >= 2) {
          attribute = attribute.toLowerCase(Locale.ROOT).replaceFirst("_+", ".").trim();
        }

        tool.modifyAttribute(attribute, modifier);
      }
    }
  };

  @NotNull private final String configKey;
  private final boolean shouldUpdate;

  RewardType(@NotNull String configKey) {
    this.configKey = configKey;
    this.shouldUpdate = true;
  }

  RewardType(@NotNull String configKey, boolean shouldUpdate) {
    this.configKey = configKey;
    this.shouldUpdate = shouldUpdate;
  }

  @NotNull
  public static Optional<RewardType> fromConfigKey(@NotNull String configKey) {
    for (RewardType value : values()) {
      if (value.configKey.equals(configKey)) {
        return Optional.of(value);
      }
    }

    return Optional.empty();
  }

  public abstract void apply(
      @NotNull LevelToolsItem tool, @NotNull String[] split, @NotNull Player player);

  @NotNull
  public String getConfigKey() {
    return configKey;
  }

  public boolean isShouldUpdate() {
    return shouldUpdate;
  }
}
