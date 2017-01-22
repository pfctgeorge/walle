package services;

import com.google.inject.Inject;

import models.R;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.ShardedJedis;
import redis.clients.jedis.ShardedJedisPool;
import services.ifaces.AsyncService;
import services.ifaces.WechatService;

/**
 * Created by pfctgeorge on 17/1/22.
 */
public abstract class BasicService {

    @Inject
    protected ShardedJedisPool shardedJedisPool;

    @Inject
    protected WechatService wechatService;

    @Inject
    protected AsyncService asyncService;

    public ShardedJedis getShardJedisFromPool() {
        ShardedJedis shardedJedis = R.shardedJedisTL.get();
        if (shardedJedis == null) {
            shardedJedis = shardedJedisPool.getResource();
            R.shardedJedisTL.set(shardedJedis);
        }
        return shardedJedis;
    }

    public Jedis getJedisFromPool(String key) {
        ShardedJedis shardedJedis = R.shardedJedisTL.get();
        if (shardedJedis == null) {
            shardedJedis = shardedJedisPool.getResource();
            R.shardedJedisTL.set(shardedJedis);
        }
        return shardedJedis.getShard(key);
    }

    public Jedis getJedisFromPool(byte[] key) {
        ShardedJedis shardedJedis = R.shardedJedisTL.get();
        if (shardedJedis == null) {
            shardedJedis = shardedJedisPool.getResource();
            R.shardedJedisTL.set(shardedJedis);
        }
        return shardedJedis.getShard(key);
    }

}
