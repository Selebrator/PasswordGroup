package de.selebrator.pwgroup;

import net.milkbowl.vault.permission.Permission;
import org.bukkit.ChatColor;
import org.bukkit.command.*;
import org.bukkit.entity.Player;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.List;

public class Main extends JavaPlugin {

	private static Permission permission = null;
	private String password = this.getConfig().getString("password");
	private List<String> groups = this.getConfig().getStringList("groups");
	private String permission_deny = this.getConfig().getString("permission_deny");
	private String message_user = ChatColor.translateAlternateColorCodes('&', this.getConfig().getString("message_user"));
	private String message_deny = ChatColor.translateAlternateColorCodes('&', this.getConfig().getString("message_deny"));
	private String message_wrong = ChatColor.translateAlternateColorCodes('&', this.getConfig().getString("message_wrong"));
	private String message_wrong_notify = ChatColor.translateAlternateColorCodes('&', this.getConfig().getString("message_wrong_notify"));
	private String message_success = ChatColor.translateAlternateColorCodes('&', this.getConfig().getString("message_success"));
	private String message_success_notify = ChatColor.translateAlternateColorCodes('&', this.getConfig().getString("message_success_notify"));

	@Override
	public void onEnable() {
		this.loadConfig();
		this.setupPermissions();
	}

	private void loadConfig() {
		this.getConfig().options().copyDefaults(true);
		this.saveConfig();
	}

	private boolean setupPermissions() {
		RegisteredServiceProvider<Permission> permissionProvider = getServer().getServicesManager().getRegistration(net.milkbowl.vault.permission.Permission.class);
		if(permissionProvider != null)
			permission = permissionProvider.getProvider();
		return permission != null;
	}

	@Override
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
		Player player;
		if(sender instanceof Player)
			player = (Player) sender;
		else {
			sender.sendMessage(this.message_user);
			return true;
		}

		if(permission.has(player, this.permission_deny)) {
			player.sendMessage(this.message_deny);
			return true;
		}

		if(args.length == 1) {
			if(args[0].equalsIgnoreCase(this.password)) {
				for(String group : permission.getPlayerGroups(player))
					permission.playerRemoveGroup(null, player, group);

				for(String group : this.groups)
					permission.playerAddGroup(null, player, group);
				player.sendMessage(this.message_success);
				String notification = this.message_success_notify
						.replaceAll("%player_uuid%", player.getUniqueId().toString())
						.replaceAll("%player_displayname%", player.getDisplayName())
						.replaceAll("%player_name%", player.getName());

				this.notify(notification, sender, "pwgroup.notify.success");
				return true;
			} else {
				player.sendMessage(this.message_wrong);
				String notification = this.message_wrong_notify
						.replaceAll("%player_uuid%", player.getUniqueId().toString())
						.replaceAll("%player_displayname%", player.getDisplayName())
						.replaceAll("%player_name%", player.getName())
						.replaceAll("%input%", args[0]);

				this.notify(notification, sender, "pwgroup.notify.wrong");
				return true;
			}
		}

		return false;
	}

	private void notify(String message, CommandSender sender, String permission) {
		if(!(sender instanceof ConsoleCommandSender))
			this.getServer().getConsoleSender().sendMessage(message);

		this.getServer().getOnlinePlayers().stream()
				.filter(player -> Main.permission.has(player, permission))
				.filter(player -> player != sender)
				.peek(player -> System.out.println(player.getName()))
				.forEach(player -> player.sendMessage(message));
	}
}
