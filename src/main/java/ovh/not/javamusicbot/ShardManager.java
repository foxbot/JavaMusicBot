package ovh.not.javamusicbot;

import net.dv8tion.jda.core.AccountType;
import net.dv8tion.jda.core.JDA;
import net.dv8tion.jda.core.JDABuilder;
import net.dv8tion.jda.core.entities.Game;
import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.exceptions.RateLimitedException;
import ovh.not.javamusicbot.rpc.Rpc;

import javax.security.auth.login.LoginException;

public class ShardManager {
    private final Rpc rpc;
    public final Shard[] shards;
    public UserManager userManager = null;

    ShardManager(Config config, Constants constants) {
        shards = new Shard[1];
        shards[0] = new Shard(this, config, constants);
        if (config.patreon) {
            userManager = new UserManager(config, this);
        }
        this.rpc = null;
    }

    ShardManager(Config config, Constants constants, int shardCount, int minShard, int maxShard) {
        shards = new Shard[(maxShard - minShard) + 1];
        int index = 0;
        for (int shardId = minShard; shardId < maxShard + 1;) {
            System.out.println("Starting shard " + shardId + "...");
            Shard shard = new Shard(this, config, constants, shardId, shardCount);
            shards[index] = shard;
            shardId++;
            index++;
        }
        if (config.patreon) {
            userManager = new UserManager(config, this);
        }
        this.rpc = new Rpc(config, this);
    }

    public Guild getGuild(String id) {
        for (Shard shard : shards) {
            Guild guild = shard.jda.getGuildById(id);
            if (guild != null) {
                return guild;
            }
        }
        return null;
    }

    public class Shard {
        public final ShardManager manager;
        public final Config config;
        private final Constants constants;
        private final boolean sharding;
        public int id = 0;
        public int shardCount = 0;
        public JDA jda = null;

        private Shard(ShardManager manager, Config config, Constants constants) {
            this.manager = manager;
            this.config = config;
            this.constants = constants;
            this.sharding = false;
            create();
        }

        private Shard(ShardManager manager, Config config, Constants constants, int shard, int shardCount) {
            this.manager = manager;
            this.config = config;
            this.constants = constants;
            this.sharding = true;
            this.id = shard;
            this.shardCount = shardCount;
            create();
        }

        private void create() {
            CommandManager commandManager = new CommandManager(config, constants, this);
            JDABuilder builder = new JDABuilder(AccountType.BOT)
                    .setToken(config.token)
                    .addEventListener(new Listener(config, commandManager, this));
            if (sharding) {
                builder.useSharding(id, shardCount);
            }
            try {
                jda = builder.buildBlocking();
                jda.getPresence().setGame(Game.of(config.game));
            } catch (LoginException | InterruptedException | RateLimitedException e) {
                e.printStackTrace();
            }
        }

        public void restart() {
            System.out.println("Shutting down shard " + id + "...");
            jda.shutdown(false);
            System.out.println("Restarting shard " + id + "...");
            create();
            System.out.println("Shard " + id + " restarted!");
        }
    }
}
