package services.ifaces;

import services.strategies.async.AsyncExecuteStrategy;

/**
 * Created by pfctgeorge on 15/10/23.
 */
public interface AsyncService {

    void execute(AsyncExecuteStrategy strategy);

}
