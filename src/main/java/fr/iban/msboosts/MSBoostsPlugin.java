package fr.iban.msboosts;

import com.tcoded.folialib.FoliaLib;
import com.tcoded.folialib.impl.PlatformScheduler;
import dev.dejvokep.boostedyaml.YamlDocument;
import dev.dejvokep.boostedyaml.settings.dumper.DumperSettings;
import dev.dejvokep.boostedyaml.settings.general.GeneralSettings;
import dev.dejvokep.boostedyaml.settings.loader.LoaderSettings;
import dev.dejvokep.boostedyaml.settings.updater.UpdaterSettings;
import fr.iban.bukkitcore.CoreBukkitPlugin;
import fr.iban.bukkitcore.commands.*;
import fr.iban.bukkitcore.manager.BukkitPlayerManager;
import fr.iban.common.messaging.AbstractMessagingManager;
import fr.iban.msboosts.api.BoostManager;
import fr.iban.msboosts.command.BoostCMD;
import fr.iban.msboosts.command.BoostsCMD;
import fr.iban.msboosts.lang.LangManager;
import fr.iban.msboosts.listener.CoreMessageListener;
import fr.iban.msboosts.listener.JobsListener;
import fr.iban.msboosts.listener.JoinQuitListeners;
import fr.iban.msboosts.manager.BoostManagerImpl;
import fr.iban.msboosts.papi.BoostPlaceholderExpansion;
import org.bukkit.Bukkit;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;
import revxrsal.commands.Lamp;
import revxrsal.commands.bukkit.BukkitLamp;
import revxrsal.commands.bukkit.actor.BukkitCommandActor;

import java.io.File;
import java.io.IOException;
import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public final class MSBoostsPlugin extends JavaPlugin {

    public static final String BOOSTS_SYNC_CHANNEL = "BoostsSyncChannel";

    private ExecutorService singleThreadExecutor;
    private BoostManager boostManager;
    private LangManager langManager;
    private FoliaLib foliaLib;
    private YamlDocument config;

    @Override
    public void onEnable() {
        loadConfig();

        singleThreadExecutor = Executors.newSingleThreadExecutor();

        this.foliaLib = new FoliaLib(this);

        this.langManager = new LangManager(this);
        this.langManager.load();

        this.boostManager = new BoostManagerImpl(this);

        PluginManager pluginManager = getServer().getPluginManager();
        pluginManager.registerEvents(new JoinQuitListeners(this), this);
        pluginManager.registerEvents(new CoreMessageListener(this), this);
        pluginManager.registerEvents(new JobsListener(this), this);

        if(Bukkit.getPluginManager().isPluginEnabled("PlaceholderAPI")){
            new BoostPlaceholderExpansion(this).register();
        }

        registerCommands();
    }

    @Override
    public void onDisable() {
        boostManager.handleBoosts();
        singleThreadExecutor.shutdown();
    }

    public void loadConfig() {
        try {
            config = YamlDocument.create(
                    new File(getDataFolder(), "config.yml"),
                    Objects.requireNonNull(getResource("config.yml")),
                    GeneralSettings.builder().setKeyFormat(GeneralSettings.KeyFormat.OBJECT).build(),
                    LoaderSettings.DEFAULT,
                    DumperSettings.DEFAULT,
                    UpdaterSettings.DEFAULT
            );
            config.update();
            config.save();
        } catch (IOException e) {
            getLogger().severe("Failed to load configuration file, disabling plugin.");
            e.printStackTrace();
            Bukkit.getPluginManager().disablePlugin(this);
        }
    }

    private void registerCommands() {
        Lamp<BukkitCommandActor> lamp =  BukkitLamp.builder(this)
                .accept(new CoreCommandHandlerVisitor(CoreBukkitPlugin.getInstance()).visitor())
                .build();

        lamp.register(new BoostsCMD(this));
        lamp.register(new BoostCMD(this));
    }



    public BoostManager getBoostManager() {
        return boostManager;
    }

    public @NotNull YamlDocument getConfiguration() {
        return config;
    }

    public PlatformScheduler getScheduler() {
        return foliaLib.getScheduler();
    }

    public void executeQueued(Runnable runnable) {
        singleThreadExecutor.execute(runnable);
    }

    public BukkitPlayerManager getPlayerManager() {
        return CoreBukkitPlugin.getInstance().getPlayerManager();
    }

    public AbstractMessagingManager getMessenger() {
        return CoreBukkitPlugin.getInstance().getMessagingManager();
    }

    public LangManager getLangManager() {
        return langManager;
    }
}
