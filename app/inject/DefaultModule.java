package inject;

import com.google.inject.AbstractModule;
import com.google.inject.Singleton;

import java.util.List;

import play.Play;
import play.classloading.ApplicationClasses;
import redis.clients.jedis.ShardedJedisPool;

/**
 * Created by zhouxichao on 8/5/15.
 */
public class DefaultModule extends AbstractModule {

    @Override
    protected void configure() {

        List<ApplicationClasses.ApplicationClass> applicationClasses = Play.classes.all();
        for (ApplicationClasses.ApplicationClass interfaceApplicationClass : applicationClasses) {
            if (interfaceApplicationClass.getPackage().startsWith("services.ifaces")) {
                List<ApplicationClasses.ApplicationClass> implApplicationClasses = Play.classes.getAssignableClasses(interfaceApplicationClass.javaClass);
                if (!implApplicationClasses.isEmpty()) {
                    Class<?> interfaceClass = interfaceApplicationClass.javaClass;
                    Class implClass = implApplicationClasses.get(0).javaClass;
                    bind(interfaceClass).to(implClass).in(Singleton.class);
                }
            }
        }
        bind(ShardedJedisPool.class).toProvider(ShardedJedisPoolProvider.class).in(Singleton.class);

    }
}
