package adhdmc.simpleplayerutils.commands.inventories;

import adhdmc.simpleplayerutils.SimplePlayerUtils;
import adhdmc.simpleplayerutils.util.SPUMessage;
import adhdmc.simpleplayerutils.util.SPUPerm;
import adhdmc.simpleplayerutils.util.SPUSound;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.List;
import java.util.UUID;

public class TrashCommand implements CommandExecutor, TabCompleter {
    MiniMessage miniMessage = SimplePlayerUtils.getMiniMessage();
    private static final HashMap<UUID, Inventory> invMap = new HashMap<>();

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        //If console runs this without supplying a player, error and return
        if (args.length == 0 && !(sender instanceof Player)) {
            sender.sendRichMessage(SPUMessage.ERROR_ONLY_PLAYER.getMessage());
            return false;
        }
        //If the sender does not have permission to either open an TRASH menu for themselves, or others, error and return
        if (!(sender.hasPermission(SPUPerm.TRASH.getPerm()) || sender.hasPermission(SPUPerm.TRASH_OTHER.getPerm()))) {
            sender.sendMessage(miniMessage.deserialize(SPUMessage.ERROR_NO_PERMISSION.getMessage(),
                    Placeholder.parsed("plugin_prefix", SPUMessage.PLUGIN_PREFIX.getMessage())));
            return false;
        }
        //If the sender doesn't have perms to open an TRASH menu for themselves, and supplies no other player, error and return
        if (args.length == 0 && !sender.hasPermission(SPUPerm.TRASH.getPerm())) {
            sender.sendMessage(miniMessage.deserialize(SPUMessage.ERROR_NO_PERMISSION.getMessage(),
                    Placeholder.parsed("plugin_prefix", SPUMessage.PLUGIN_PREFIX.getMessage())));
            return false;
        }
        //If sender has permission and no other player is provided, open menu and return
        if (args.length == 0) {
            Player player = (Player) sender;
            openTrash(player, null);
            return true;
        }
        //If sender has permission and supplies an argument, check for player
        if (args.length == 1 && sender.hasPermission(SPUPerm.TRASH_OTHER.getPerm())) {
            Player player = SimplePlayerUtils.getInstance().getServer().getPlayer(args[0]);
            //If player doesn't exist, error and return
            if (player == null) {
                sender.sendMessage(miniMessage.deserialize(SPUMessage.ERROR_NO_VALID_PLAYER_SUPPLIED.getMessage(),
                        Placeholder.parsed("plugin_prefix", SPUMessage.PLUGIN_PREFIX.getMessage()),
                        Placeholder.parsed("name", args[0])));
                return false;
            }
            //if Player exists, open their TRASH menu and return
            openTrash(player, sender);
            return true;
        }
        //If someone has made it this far, there's an error, send them an error and return.
        sender.sendMessage(miniMessage.deserialize(SPUMessage.ERROR_GENERAL.getMessage(),
                Placeholder.parsed("plugin_prefix", SPUMessage.PLUGIN_PREFIX.getMessage())));
        return false;
    }

    private void openTrash(Player player, CommandSender sender){
        Location playerLocation = player.getLocation();
        Inventory trashInventory = Bukkit.createInventory(null, 36, miniMessage.deserialize(SPUMessage.TRASH_INVENTORY_NAME.getMessage()));
        player.openInventory(trashInventory);
        UUID playerUUID = player.getUniqueId();
        invMap.put(playerUUID, trashInventory);
        player.playSound(playerLocation, SPUSound.TRASH_SOUND.getSound(), 1, 1);
        if (sender != null) {
            sender.sendMessage(miniMessage.deserialize(SPUMessage.TRASH_COMMAND_OTHER.getMessage(),
                    Placeholder.parsed("plugin_prefix", SPUMessage.PLUGIN_PREFIX.getMessage()),
                    Placeholder.component("user", player.displayName())));
        }
    }

    public static HashMap<UUID, Inventory> getInvMap() {
        return invMap;
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        return null;
    }
}