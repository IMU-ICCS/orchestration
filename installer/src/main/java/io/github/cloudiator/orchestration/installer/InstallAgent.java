package io.github.cloudiator.orchestration.installer;

import com.google.inject.Guice;
import com.google.inject.Injector;
import de.uniulm.omi.cloudiator.util.configuration.Configuration;
import de.uniulm.omi.cloudiator.util.execution.ExecutionService;
import de.uniulm.omi.cloudiator.util.execution.LoggingScheduledThreadPoolExecutor;
import de.uniulm.omi.cloudiator.util.execution.ScheduledThreadPoolExecutorExecutionService;
import org.cloudiator.messaging.kafka.KafkaContext;
import org.cloudiator.messaging.kafka.KafkaMessagingModule;
import org.cloudiator.messaging.services.MessageServiceModule;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by Daniel Seybold on 28.06.2017.
 */
public class InstallAgent {

  private static final Logger LOGGER =
      LoggerFactory.getLogger(InstallAgent.class);
  private static Injector injector =
      Guice.createInjector(
          new KafkaMessagingModule(new KafkaContext(Configuration.conf())),
          new MessageServiceModule(),
          new InstallAgentModule());

  /**
   * starts the virtual machine agent.
   *
   * @param args args
   */
  public static void main(String[] args) {

    LOGGER.debug("Starting InstallAgent...");

    //TODO: change to use queue and worker instead of single instance
    final InstallEventSubscriber installEventSubscriber =
        injector.getInstance(InstallEventSubscriber.class);
    installEventSubscriber.run();

    LOGGER.debug("shutting down InstallAgent...");

  }

}
