package services;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import play.Logger;
import services.ifaces.AsyncService;
import services.strategies.async.AsyncExecuteStrategy;

/**
 * Created by pfctgeorge on 15/10/23.
 */
public class AsyncServiceImpl extends BasicService implements AsyncService {

    private final ExecutorService executorService = Executors.newFixedThreadPool(10);

    @Override
    public void execute(AsyncExecuteStrategy strategy) {
        Logger.info("Start to submit task: " + strategy.toString() + " to thread pool.");
        executorService.execute(new AsyncTaskSubmitter(strategy));
        Logger.info("Submitted task: " + strategy.toString() + " successfully.");

    }

    private final class AsyncTaskSubmitter implements Runnable {

        private AsyncExecuteStrategy strategy;

        private AsyncTaskSubmitter(AsyncExecuteStrategy strategy) {
            this.strategy = strategy;
        }

        @Override
        public void run() {
            strategy.execute();
        }
    }
}
