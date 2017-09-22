package de.selebrator.pwgroup;

import net.milkbowl.vault.permission.Permission;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
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
	private String message_success = ChatColor.translateAlternateColorCodes('&', this.getConfig().getString("message_success"));

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
				for(String group : this.groups)
					permission.playerAddGroup(null, player, group);
				player.sendMessage(this.message_success);
				return true;
			} else {
				player.sendMessage(this.message_wrong);
				return true;
			}
		}

		return false;
	}
}
