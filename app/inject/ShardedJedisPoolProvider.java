package inject;

import com.google.inject.Provider;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

import play.Play;
import play.exceptions.ConfigurationException;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;
import redis.clients.jedis.JedisShardInfo;
import redis.clients.jedis.Protocol;
import redis.clients.jedis.ShardedJedis;
import redis.clients.jedis.ShardedJedisPool;

/**
 * Created by zhouxichao on 8/5/15.
 */
public class ShardedJedisPoolProvider implements Provider<ShardedJedisPool> {

    @Override
    public ShardedJedisPool get() {
        if (Play.configuration.containsKey("redis.1.url")) {
            int timeout = Integer.parseInt(Play.configuration.getProperty("redis.timeout", "5000"));
            int maxActive = Integer.parseInt(Play.configuration.getProperty("redis.maxactive", "500"));
            int maxIdle = Integer.parseInt(Play.configuration.getProperty("redis.maxidle", "50"));

            List<JedisShardInfo> shards = new ArrayList<>();

            int nb = 1;
            while (Play.configuration.containsKey("redis." + nb + ".url")) {
                RedisConnectionInfo redisConnInfo = new RedisConnectionInfo(Play.configuration.getProperty("redis." + nb + ".url"), timeout);
                shards.add(redisConnInfo.getShardInfo());
                nb++;
            }

            JedisPoolConfig poolConfig = new JedisPoolConfig();
            poolConfig.setMaxTotal(maxActive);
            poolConfig.setMaxIdle(maxIdle);
            return new ShardedJedisPool(poolConfig, shards, ShardedJedis.DEFAULT_KEY_TAG_PATTERN);
        }
        return null;
    }

    private static class RedisConnectionInfo {

        private final String host;
        private final int port;
        private final String password;
        private final int timeout;

        RedisConnectionInfo(String redisUrl, int timeout) {
            URI redisUri;
            try {
                redisUri = new URI(redisUrl);
            } catch (URISyntaxException e) {
                throw new ConfigurationException("Bad configuration for redis: unable to parse redis url (" + redisUrl + ")");
            }

            host = redisUri.getHost();

            if (redisUri.getPort() > 0) {
                port = redisUri.getPort();
            } else {
                port = Protocol.DEFAULT_PORT;
            }

            String userInfo = redisUri.getUserInfo();
            if (userInfo != null) {
                String[] parsedUserInfo = userInfo.split(":");
                password = parsedUserInfo[parsedUserInfo.length - 1];
            } else {
                password = null;
            }

            this.timeout = timeout;
        }

        JedisPool getConnectionPool() {
            if (password == null) {
                return new JedisPool(new JedisPoolConfig(), host, port, timeout);
            }

            return new JedisPool(new JedisPoolConfig(), host, port, timeout, password);
        }

        JedisShardInfo getShardInfo() {
            JedisShardInfo si = new JedisShardInfo(host, port, timeout);
            si.setPassword(password);
            return si;
        }
    }
}
