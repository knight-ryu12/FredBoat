package fredboat.main;

import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager;
import fredboat.agent.FredBoatAgent;
import fredboat.audio.player.AudioConnectionFacade;
import fredboat.audio.player.PlayerRegistry;
import fredboat.config.property.AppConfig;
import fredboat.config.property.AudioSourcesConfig;
import fredboat.config.property.Credentials;
import fredboat.config.property.PropertyConfigProvider;
import fredboat.db.DatabaseManager;
import fredboat.db.EntityIO;
import fredboat.event.EventListenerBoat;
import fredboat.feature.metrics.BotMetrics;
import fredboat.feature.metrics.Metrics;
import fredboat.metrics.OkHttpEventMetrics;
import fredboat.util.rest.Http;
import io.prometheus.client.hibernate.HibernateStatisticsCollector;
import net.dv8tion.jda.bot.sharding.ShardManager;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import javax.annotation.Nonnull;
import java.util.concurrent.ExecutorService;

/**
 * Class responsible for controlling FredBoat at large
 */
@Component
public class BotController {

    public static final Http HTTP = new Http(Http.DEFAULT_BUILDER.newBuilder()
            .eventListener(new OkHttpEventMetrics("default", Metrics.httpEventCounter))
            .build());

    private final PropertyConfigProvider configProvider;
    private final AudioConnectionFacade audioConnectionFacade;
    private final ShardManager shardManager;
    //central event listener that all events by all shards pass through
    private final EventListenerBoat mainEventListener;
    private final ShutdownHandler shutdownHandler;
    private final DatabaseManager databaseManager;
    private final EntityIO entityIO;
    private final PlayerRegistry playerRegistry;
    private final ShardContext shardContext;
    private final BotMetrics botMetrics;
    private final ExecutorService executor;
    private final AudioPlayerManager audioPlayerManager;


    public BotController(PropertyConfigProvider configProvider, AudioConnectionFacade audioConnectionFacade, ShardManager shardManager,
                         EventListenerBoat eventListenerBoat, ShutdownHandler shutdownHandler, DatabaseManager databaseManager,
                         EntityIO entityIO, ExecutorService executor, HibernateStatisticsCollector hibernateStats,
                         PlayerRegistry playerRegistry, ShardContext shardContext, BotMetrics botMetrics,
                         @Qualifier("loadAudioPlayerManager") AudioPlayerManager audioPlayerManager) {
        this.configProvider = configProvider;
        this.audioConnectionFacade = audioConnectionFacade;
        this.shardManager = shardManager;
        this.mainEventListener = eventListenerBoat;
        this.shutdownHandler = shutdownHandler;
        this.databaseManager = databaseManager;
        this.entityIO = entityIO;
        hibernateStats.register(); //call this exactly once after all db connections have been created
        this.executor = executor;
        this.playerRegistry = playerRegistry;
        this.shardContext = shardContext;
        this.botMetrics = botMetrics;
        this.audioPlayerManager = audioPlayerManager;

        Runtime.getRuntime().addShutdownHook(new Thread(createShutdownHook(), "FredBoat main shutdownhook"));
    }

    public AppConfig getAppConfig() {
        return configProvider.getAppConfig();
    }

    public AudioSourcesConfig getAudioSourcesConfig() {
        return configProvider.getAudioSourcesConfig();
    }

    public Credentials getCredentials() {
        return configProvider.getCredentials();
    }

    public AudioConnectionFacade getAudioConnectionFacade() {
        return audioConnectionFacade;
    }

    public ShutdownHandler getShutdownHandler() {
        return shutdownHandler;
    }

    public DatabaseManager getDatabaseManager() {
        return databaseManager;
    }

    @Nonnull
    public ExecutorService getExecutor() {
        return executor;
    }

    public EventListenerBoat getMainEventListener() {
        return mainEventListener;
    }

    // Can be null during init, but usually not
    public ShardManager getShardManager() {
        return shardManager;
    }

    @Nonnull
    public EntityIO getEntityIO() {
        return entityIO;
    }

    public PlayerRegistry getPlayerRegistry() {
        return playerRegistry;
    }

    public ShardContext getShardContext() {
        return shardContext;
    }

    public BotMetrics getBotMetrics() {
        return botMetrics;
    }

    public AudioPlayerManager getAudioPlayerManager() {
        return audioPlayerManager;
    }

    //Shutdown hook
    private Runnable createShutdownHook() {
        return FredBoatAgent::shutdown;
    }
}
